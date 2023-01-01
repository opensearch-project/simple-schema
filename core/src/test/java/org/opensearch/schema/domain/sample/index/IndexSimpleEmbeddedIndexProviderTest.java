package org.opensearch.schema.domain.sample.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleEmbeddedOntologyTranslatorTest;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.DirectiveEnumTypes;
import org.opensearch.schema.ontology.DirectiveType;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.opensearch.schema.index.schema.IndexMappingUtils.MAPPING_TYPE;
import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;
import static org.opensearch.schema.ontology.DirectiveType.Argument.of;

/**
 * This test is verifying that the process of generating an index provider from the ontology is working as expected
 */
public class IndexSimpleEmbeddedIndexProviderTest {
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
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        ontology = GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology;
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> true);

        Assert.assertNotNull(new ObjectMapper().writeValueAsString(indexProvider));
    }

    @Test
    /**
     * verify the author entity index-provider contains basic structure and properties
     */
    public void testAuthorEntityIndexProviderTest() {
        Assert.assertTrue(indexProvider.getEntity("Author").isPresent());
        Entity author = indexProvider.getEntity("Author").get();

        Assert.assertEquals("Author", author.getType().getName());

        Assert.assertEquals(NestingType.NONE, author.getNesting());
        Assert.assertEquals(MappingIndexType.STATIC, author.getMapping());

        Assert.assertEquals(1, author.getNested().size());
        Assert.assertTrue(author.getNested().containsKey("books"));

        Assert.assertEquals(0, author.getAdditionalProperties().size());
        Assert.assertEquals(List.of("Author"), author.getProps().getValues());


        Assert.assertEquals(1, author.getDirectives().size());
        Assert.assertEquals(new DirectiveType("model", DirectiveType.DirectiveClasses.DATATYPE), author.getDirectives().get(0));
    }

    @Test
    /**
     * verify the book entity index-provider contains basic structure and properties
     */
    public void testBooksNestedEntityIndexProviderTest() {
        Assert.assertFalse(indexProvider.getEntity("Book").isPresent());
        Assert.assertFalse(indexProvider.getEntity("Author").get().getNested().isEmpty());
        Assert.assertTrue(indexProvider.getEntity("Author").get().getNested().containsKey("books"));

        Entity book = indexProvider.getEntity("Author").get().getNested().get("books");
        Assert.assertEquals("Book", book.getType().getName());

        Assert.assertEquals(MappingIndexType.NESTED, book.getMapping());
        Assert.assertEquals(NestingType.EMBEDDING, book.getNesting());

        Assert.assertEquals(0, book.getNested().size());

        Assert.assertEquals(0, book.getAdditionalProperties().size());
        Assert.assertEquals(List.of("Book"), book.getProps().getValues());


        Assert.assertEquals(0, book.getDirectives().size());
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
        Assert.assertEquals(RELATION.getName(), has_book.getDirectives().get(0).getName());
        Assert.assertTrue( has_book.getDirectives().get(0).getArgument(MAPPING_TYPE).isPresent());
        Assert.assertEquals( "embedded",has_book.getDirectives().get(0).getArgument(MAPPING_TYPE).get().value);

        Assert.assertEquals(MappingIndexType.NONE, has_book.getMapping());
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
        Assert.assertEquals(RELATION.getName(), has_author.getDirectives().get(0).getName());
        Assert.assertTrue( has_author.getDirectives().get(0).getArgument(MAPPING_TYPE).isPresent());
        Assert.assertEquals( "reverse",has_author.getDirectives().get(0).getArgument(MAPPING_TYPE).get().value);

        Assert.assertEquals(MappingIndexType.NONE, has_author.getMapping());
        Assert.assertEquals(NestingType.NONE, has_author.getNesting());
    }
}