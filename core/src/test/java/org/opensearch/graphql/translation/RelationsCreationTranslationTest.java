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

import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;
import static org.opensearch.schema.ontology.DirectiveType.Argument.of;
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

        EPair has_authorPair = accessor.relation("has_Author").get().getePairs().get(0);
        Assert.assertEquals("Book->Author", has_authorPair.getName());
        Assert.assertEquals("Author", has_authorPair.geteTypeB());
        Assert.assertEquals("@id", has_authorPair.getSideBIdField());
        Assert.assertEquals("Book", has_authorPair.geteTypeA());
        Assert.assertEquals("ISBN", has_authorPair.getSideAIdField());
        Assert.assertEquals("author", has_authorPair.getSideAFieldName());

        Assert.assertEquals(new DirectiveType(RELATION.getName(), DirectiveType.DirectiveClasses.DATATYPE,
                        List.of(of(RELATION.getArgument(0),FOREIGN.getName()))),
                accessor.relation("has_Author").get().getDirectives().get(0));
    }


    @Test
    void translateHasBook() {
        Accessor accessor = init();
        //verify relation created
        Assert.assertTrue(accessor.relation("has_Book").isPresent());

        Assert.assertEquals("ISBN",accessor.relation("has_Book").get().idFieldName());

        EPair has_authorPair = accessor.relation("has_Book").get().getePairs().get(0);
        Assert.assertEquals("Author->Book", has_authorPair.getName());
        Assert.assertEquals("Author", has_authorPair.geteTypeA());
        Assert.assertEquals("@id", has_authorPair.getSideAIdField());
        Assert.assertEquals("Book", has_authorPair.geteTypeB());
        Assert.assertEquals("ISBN", has_authorPair.getSideBIdField());
        Assert.assertEquals("books", has_authorPair.getSideAFieldName());

        Assert.assertEquals(new DirectiveType(RELATION.getName(), DirectiveType.DirectiveClasses.DATATYPE,
                        List.of(of(RELATION.getArgument(0),FOREIGN.getName()))),
                accessor.relation("has_Book").get().getDirectives().get(0));
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