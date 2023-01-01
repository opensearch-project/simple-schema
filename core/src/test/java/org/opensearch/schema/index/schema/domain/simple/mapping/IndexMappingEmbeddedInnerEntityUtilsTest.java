package org.opensearch.schema.index.schema.domain.simple.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleEmbeddedOntologyTranslatorTest;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleNestedOntologyTranslatorTest;
import org.opensearch.schema.index.schema.IndexMappingUtils;
import org.opensearch.schema.index.schema.MappingIndexType;
import org.opensearch.schema.index.schema.NestingType;
import org.opensearch.schema.index.schema.Props;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EPair;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.RelationshipType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexMappingEmbeddedInnerEntityUtilsTest {

    static Ontology ontology;
    static Accessor accessor;
    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @Test
    void testCreateSimpleProperties() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);

        Props test = IndexMappingUtils.createProperties("test", accessor);
        Assert.assertEquals(new Props(List.of("test")), test);
    }

    @Test
    void testCalculateChildEmbeddedEntityMappingType() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);

        Assertions.assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.entity$("Author"), accessor));
        assertEquals(MappingIndexType.NESTED, IndexMappingUtils.calculateMappingType(accessor.entity$("Book"), accessor));
    }

    @Test
    void testCalculateChildEmbeddedRelationMappingType() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);


        RelationshipType has_author = accessor.relation$("has_Author");
        EPair authorPair = has_author.getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(has_author, authorPair, accessor));

        RelationshipType has_book = accessor.relation$("has_Book");
        EPair bookPair = has_book.getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(has_book, bookPair, accessor));
    }

    @Test
    void calculateChildEmbeddedEntityNestingType() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);

        Assertions.assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(Optional.empty(),accessor.entity$("Author"), accessor));
        assertEquals(NestingType.EMBEDDING, IndexMappingUtils.calculateNestingType(Optional.of(accessor.entity$("Author")),accessor.entity$("Book"), accessor));
    }

    @Test
    void calculateChildEmbeddedRelationNestingType() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);


        RelationshipType has_author = accessor.relation$("has_Author");
        EPair authorPair = has_author.getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(has_author, authorPair, accessor));

        RelationshipType has_book = accessor.relation$("has_Book");
        EPair bookPair = has_book.getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(has_book, bookPair, accessor));
    }
}