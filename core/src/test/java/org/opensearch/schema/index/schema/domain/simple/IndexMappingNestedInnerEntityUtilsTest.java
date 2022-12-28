package org.opensearch.schema.index.schema.domain.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.index.schema.IndexMappingUtils;
import org.opensearch.schema.index.schema.MappingIndexType;
import org.opensearch.schema.index.schema.NestingType;
import org.opensearch.schema.index.schema.Props;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EPair;
import org.opensearch.schema.ontology.Ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IndexMappingNestedInnerEntityUtilsTest {

    static Ontology ontology;
    static Accessor accessor;

    @Test
    void testCreateSimpleProperties() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaNestedBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

        Props test = IndexMappingUtils.createProperties("test", accessor);
        Assert.assertEquals(new Props(List.of("test")), test);
    }

    @Test
    void testCalculateChildNestedEntityMappingType() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaNestedBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

        Assertions.assertEquals(MappingIndexType.STATIC, IndexMappingUtils.calculateMappingType(accessor.entity$("Author"), accessor));
        assertEquals(MappingIndexType.NESTED, IndexMappingUtils.calculateMappingType(accessor.entity$("Book"), accessor));
    }

    @Test
    void testCalculateChildNestedRelationMappingType() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaNestedBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

        assertFalse(accessor.relation$("has_Author").getePairs().isEmpty());
        EPair has_author = accessor.relation$("has_Author").getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Author"),has_author, accessor));

        assertFalse(accessor.relation$("has_Book").getePairs().isEmpty());
        EPair has_book = accessor.relation$("has_Book").getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Book"),has_book, accessor));
    }

    @Test
    void calculateChildNestedEntityNestingType() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaNestedBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

        Assertions.assertEquals(NestingType.NONE, IndexMappingUtils.calculateNestingType(accessor.entity$("Author"), accessor));
        assertEquals(NestingType.NESTING, IndexMappingUtils.calculateNestingType(accessor.entity$("Book"), accessor));
    }

    @Test
    void calculateChildNestedRelationNestingType() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaNestedBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

        assertFalse(accessor.relation$("has_Author").getePairs().isEmpty());
        EPair has_author = accessor.relation$("has_Author").getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Author"),has_author, accessor));

        assertFalse(accessor.relation$("has_Book").getePairs().isEmpty());
        EPair has_book = accessor.relation$("has_Book").getePairs().get(0);
        assertEquals(MappingIndexType.NONE, IndexMappingUtils.calculateMappingType(accessor.relation$("has_Book"),has_book, accessor));
    }
}