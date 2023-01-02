package org.opensearch.schema.index.schema.domain.simple.mapping;

import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleForeignJoinIndexOntologyTranslatorTest;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleForeignOntologyTranslatorTest;
import org.opensearch.schema.index.schema.IndexMappingUtils;
import org.opensearch.schema.index.schema.MappingIndexType;
import org.opensearch.schema.index.schema.NestingType;
import org.opensearch.schema.index.schema.Props;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EPair;
import org.opensearch.schema.ontology.Ontology;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IndexMappingForeignJoinEntitiesUtilsTest {

    static Ontology ontology;
    static Accessor accessor;
    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @Test
    void testCreateSimpleProperties() throws Exception {
        GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.ontology);

        Props test = IndexMappingUtils.createProperties("test", accessor);
        Assert.assertEquals(new Props(List.of("test")), test);
    }

    @Test
    void testCalculateChildReferenceEntityMappingType() throws Exception {
        GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.ontology);

        Assertions.assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.entity$("Author"), accessor));
        assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.entity$("Book"), accessor));
    }

    @Test
    void testCalculateChildReferenceRelationMappingType() throws Exception {
        GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.ontology);

        assertFalse(accessor.relation$("has_Author").getePairs().isEmpty());
        EPair has_author = accessor.relation$("has_Author").getePairs().get(0);
        assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Author"),has_author, accessor));

        assertFalse(accessor.relation$("has_Book").getePairs().isEmpty());
        EPair has_book = accessor.relation$("has_Book").getePairs().get(0);
        assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Book"),has_book, accessor));
    }

    @Test
    void calculateChildReferenceEntityNestingType() throws Exception {
        GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.ontology);

        Assertions.assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(Optional.empty(),accessor.entity$("Author"), accessor));
        assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(Optional.empty(),accessor.entity$("Book"), accessor));
    }

    @Test
    /**
     * verify nesting type correctly assembled
     */
    void calculateChildReferenceRelationNestingType() throws Exception {
        GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignJoinIndexOntologyTranslatorTest.ontology);

        assertFalse(accessor.relation$("has_Author").getePairs().isEmpty());
        EPair has_author = accessor.relation$("has_Author").getePairs().get(0);
        assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(accessor.relation$("has_Author"),has_author, accessor));


        assertFalse(accessor.relation$("has_Book").getePairs().isEmpty());
        EPair has_book = accessor.relation$("has_Book").getePairs().get(0);
        assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(accessor.relation$("has_Book"),has_book, accessor));
    }
}