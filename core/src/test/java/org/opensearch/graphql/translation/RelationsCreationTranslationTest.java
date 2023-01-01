package org.opensearch.graphql.translation;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.EchoingWiringFactory;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.DirectiveType;
import org.opensearch.schema.ontology.EPair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.opensearch.schema.index.schema.IndexMappingUtils.MAPPING_TYPE;
import static org.opensearch.schema.index.schema.IndexMappingUtils.NAME;
import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;
import static org.opensearch.schema.ontology.DirectiveType.Argument.of;
import static org.opensearch.schema.ontology.EPair.RelationReferenceType.ONE_TO_MANY;
import static org.opensearch.schema.ontology.EPair.RelationReferenceType.ONE_TO_ONE;
import static org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType.FOREIGN;

class RelationsCreationTranslationTest {
    static GraphQLSchema schema;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

    @BeforeAll
    static void setup() throws FileNotFoundException {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/sample/simpleGQLForeignBooks.graphql");
        schema = GraphQLEngineFactory.generateSchema(new EchoingWiringFactory(), Arrays.asList(utilsSchemaInput,stream));
    }

    @Test
    void translateHasAuthor() {
        Accessor accessor = init();
        //verify relation created
        Assert.assertTrue(accessor.relation("has_Author").isPresent());
        Assert.assertEquals("has_Author",accessor.relation("has_Author").get().getrType());
        Assert.assertEquals("@id",accessor.relation("has_Author").get().idFieldName());
        Assert.assertFalse(accessor.relation("has_Author").get().getePairs().isEmpty());

        EPair has_authorPair = accessor.relation("has_Author").get().getePairs().get(0);
        Assert.assertEquals("Book->Author", has_authorPair.getName());
        Assert.assertEquals(ONE_TO_ONE, has_authorPair.getReferenceType());
        Assert.assertEquals("Author", has_authorPair.geteTypeB());
        Assert.assertEquals("@id", has_authorPair.getSideBIdField());
        Assert.assertEquals("Book", has_authorPair.geteTypeA());
        Assert.assertEquals("ISBN", has_authorPair.getSideAIdField());
        Assert.assertEquals("author", has_authorPair.getSideAFieldName());

        Assert.assertEquals(RELATION.getName(), has_authorPair.getDirectives().get(0).getName());
        Assert.assertTrue(has_authorPair.getDirectives().get(0).getArgument(MAPPING_TYPE).isPresent());
        Assert.assertEquals(of(MAPPING_TYPE,FOREIGN.getName()), has_authorPair.getDirectives().get(0).getArgument(MAPPING_TYPE).get());
        Assert.assertTrue(has_authorPair.getDirectives().get(0).getArgument(NAME).isPresent());
        Assert.assertNull(has_authorPair.getDirectives().get(0).getArgument(NAME).get().value);
    }


    @Test
    void translateHasBook() {
        Accessor accessor = init();
        //verify relation created
        Assert.assertTrue(accessor.relation("has_Book").isPresent());
        Assert.assertEquals("ISBN",accessor.relation("has_Book").get().idFieldName());
        Assert.assertFalse(accessor.relation("has_Author").get().getePairs().isEmpty());

        EPair has_bookPair = accessor.relation("has_Book").get().getePairs().get(0);
        Assert.assertEquals(ONE_TO_MANY, has_bookPair.getReferenceType());
        Assert.assertEquals("written", has_bookPair.getName());
        Assert.assertEquals("Author", has_bookPair.geteTypeA());
        Assert.assertEquals("@id", has_bookPair.getSideAIdField());
        Assert.assertEquals("Book", has_bookPair.geteTypeB());
        Assert.assertEquals("ISBN", has_bookPair.getSideBIdField());
        Assert.assertEquals("books", has_bookPair.getSideAFieldName());

        Assert.assertEquals(RELATION.getName(), has_bookPair.getDirectives().get(0).getName());
        Assert.assertTrue(has_bookPair.getDirectives().get(0).getArgument(MAPPING_TYPE).isPresent());
        Assert.assertEquals(of(MAPPING_TYPE,FOREIGN.getName()), has_bookPair.getDirectives().get(0).getArgument(MAPPING_TYPE).get());
        Assert.assertTrue(has_bookPair.getDirectives().get(0).getArgument(NAME).isPresent());
        Assert.assertEquals("written",has_bookPair.getDirectives().get(0).getArgument(NAME).get().value.toString());
    }

    private Accessor init() {
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        ObjectTypeTranslation objectTypeTranslation = new ObjectTypeTranslation();
        objectTypeTranslation.translate(schema, context);

        EntitiesCreationTranslation entitiesCreationTranslation = new EntitiesCreationTranslation();
        entitiesCreationTranslation.translate(schema, context);

        RelationsCreationTranslation transformer = new RelationsCreationTranslation();
        transformer.translate(schema, context);
        return new Accessor(context.getBuilder().build());
    }

}