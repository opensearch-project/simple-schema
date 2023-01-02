package org.opensearch.schema.index.schema.domain.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleEmbeddedOntologyTranslatorTest;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleNestedOntologyTranslatorTest;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexProviderSimpleEmbeddedTest {
    static Ontology ontology;
    static Accessor accessor;
    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    public static void setup() throws Exception {
        GraphQLSimpleEmbeddedOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleEmbeddedOntologyTranslatorTest.ontology);
    }

    @Test
    public void createHasAuthorRelationTest() {
        RelationshipType has_author = accessor.relation$("has_Author");
        List<Relation> relations = new RelationMappingTranslator().translate(has_author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(relations.isEmpty());
        assertEquals(1, relations.size());

        Relation relation = relations.get(0);
        assertEquals(has_author.getrType(), relation.getType().getName());
        assertFalse(relation.getDirectives().isEmpty());

        assertFalse( has_author.getePairs().isEmpty());
        assertEquals(has_author.getePairs().get(0).getDirectives(), relation.getDirectives());

        assertTrue(relation.getProps().getValues().stream().anyMatch(v -> v.equals("has_Author")));
        assertEquals(NestingType.NONE, relation.getNesting());
        assertEquals(MappingIndexType.NONE, relation.getMapping());
        assertTrue(relation.getRedundant().isEmpty());
        assertFalse(relation.isSymmetric());
    }

    @Test
    public void createHasBooksRelationTest() {
        RelationshipType has_book = accessor.relation$("has_Book");
        List<Relation> relations = new RelationMappingTranslator().translate(has_book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(relations.isEmpty());
        assertEquals(1, relations.size());

        Relation relation = relations.get(0);
        assertEquals(has_book.getrType(), relation.getType().getName());
        assertFalse(relation.getDirectives().isEmpty());

        assertFalse( has_book.getePairs().isEmpty());
        assertEquals(has_book.getePairs().get(0).getDirectives(), relation.getDirectives());

        assertTrue(relation.getProps().getValues().stream().anyMatch(v -> v.equals("has_Book")));

        assertEquals(NestingType.NONE, relation.getNesting());
        assertEquals(MappingIndexType.NONE, relation.getMapping());
        assertTrue(relation.getRedundant().isEmpty());
        assertFalse(relation.isSymmetric());
    }

    @Test
    public void createAuthorEntityTest() {
        EntityType author = accessor.entity$("Author");
        List<Entity> entities = new EntityMappingTranslator().translate(author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(author.geteType(), entity.getType().getName());
        assertEquals(author.getDirectives(), entity.getDirectives());
        assertEquals(new Props(List.of("Author")), entity.getProps());
    }

    @Test
    public void createBookEntityTest() {
        EntityType book = accessor.entity$("Book");
        List<Entity> entities = new EntityMappingTranslator().translate(book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(book.geteType(), entity.getType().getName());
        assertEquals(book.getDirectives(), entity.getDirectives());
        assertEquals(new Props(List.of("Book")), entity.getProps());
    }
}
