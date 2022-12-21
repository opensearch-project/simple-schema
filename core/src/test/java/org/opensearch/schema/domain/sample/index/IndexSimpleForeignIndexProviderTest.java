package org.opensearch.schema.domain.sample.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.DirectiveEnumTypes;
import org.opensearch.schema.ontology.DirectiveType;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;
import static org.opensearch.schema.ontology.DirectiveType.Argument.of;

/**
 * This test is verifying that the process of generating an index provider from the ontology is working as expected
 */
public class IndexSimpleForeignIndexProviderTest {
    static Ontology ontology;
    static IndexProvider indexProvider;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    /**
     * load process (including all it's dependencies) graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaReferenceBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.RELATION.isSame(d.getName())));
    }

    @Test
    /**
     * verify the author entity index-provider contains basic structure and properties
     */
    public void testAuthorEntityIndexProviderTest()  {
        Assert.assertTrue(indexProvider.getEntity("Author").isPresent());
        Entity author = indexProvider.getEntity("Author").get();

        Assert.assertEquals("Author",author.getType().getName());

        Assert.assertEquals(NestingType.NONE,author.getNesting());
        Assert.assertEquals(MappingIndexType.STATIC,author.getMapping());

        Assert.assertEquals(0,author.getNested().size());

        Assert.assertEquals(0,author.getAdditionalProperties().size());
        Assert.assertEquals(List.of("Author"),author.getProps().getValues());


        Assert.assertEquals(1,author.getDirectives().size());
        Assert.assertEquals(new DirectiveType("model", DirectiveType.DirectiveClasses.DATATYPE),author.getDirectives().get(0));
    }

    @Test
    /**
     * verify the book entity index-provider contains basic structure and properties
     */
    public void testBooksEntityIndexProviderTest()  {
        Assert.assertTrue(indexProvider.getEntity("Book").isPresent());

        Entity book = indexProvider.getEntity("Book").get();

        Assert.assertEquals("Book",book.getType().getName());

        Assert.assertEquals(NestingType.NONE,book.getNesting());
        Assert.assertEquals(MappingIndexType.STATIC,book.getMapping());

        Assert.assertEquals(0,book.getNested().size());

        Assert.assertEquals(0,book.getAdditionalProperties().size());
        Assert.assertEquals(List.of("Book"),book.getProps().getValues());


        Assert.assertEquals(1,book.getDirectives().size());
        Assert.assertEquals(new DirectiveType("model", DirectiveType.DirectiveClasses.DATATYPE),book.getDirectives().get(0));
    }

    @Test
    /**
     * verify the relationships exist according to the expected original entities hierarchy
     */
    public void testHasBookRelationshipsIndexProviderTest() {
        Assert.assertEquals(2, indexProvider.getRelations().size());
        Assert.assertTrue(indexProvider.getRelation("has_Book").isPresent());
        Relation has_book = indexProvider.getRelation("has_Book").get();
        Assert.assertEquals(1, has_book.getDirectives().size());
        Assert.assertEquals(new DirectiveType(RELATION.name().toLowerCase(), DirectiveType.DirectiveClasses.DATATYPE,
                        Collections.singletonList(of(RELATION.getArgument(0), PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))),
                has_book.getDirectives().get(0));

        //TODO - Fix According to specific tests
        Assert.assertEquals(MappingIndexType.STATIC, has_book.getMapping());
        Assert.assertEquals(NestingType.NONE, has_book.getNesting());

    }

    @Test
    /**
     * verify the relationships exist according to the expected original entities hierarchy
     */
    public void testHasAuthorRelationshipsIndexProviderTest() {
        Assert.assertTrue(indexProvider.getRelation("has_Author").isPresent());
        Relation has_author = indexProvider.getRelation("has_Author").get();
        Assert.assertEquals(1, has_author.getDirectives().size());
        Assert.assertEquals(new DirectiveType(RELATION.name().toLowerCase(), DirectiveType.DirectiveClasses.DATATYPE,
                        Collections.singletonList(of(RELATION.getArgument(0), PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))),
                has_author.getDirectives().get(0));

        //TODO - Fix According to specific tests
/*
        Assert.assertEquals(NestingType.NONE, has_author.getNesting());
        Assert.assertEquals(MappingIndexType.STATIC, has_author.getMapping());
*/
    }
}
