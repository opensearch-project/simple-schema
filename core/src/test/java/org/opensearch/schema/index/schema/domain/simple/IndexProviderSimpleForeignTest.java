package org.opensearch.schema.index.schema.domain.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleForeignOntologyTranslatorTest;
import org.opensearch.schema.domain.sample.graphql.GraphQLSimpleNestedOntologyTranslatorTest;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.RelationshipType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexProviderSimpleForeignTest {
    static Ontology ontology;
    static Accessor accessor;
    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    public static void setup() throws Exception {
        GraphQLSimpleForeignOntologyTranslatorTest.setUp();
        accessor = new Accessor(GraphQLSimpleForeignOntologyTranslatorTest.ontology);

    }

    @Test
    public void createHasAuthorRelationTest() {
        RelationshipType has_author = accessor.relation$("has_Author");
        List<Relation> relations = new RelationMappingTranslator().translate(has_author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(relations.isEmpty());
        assertEquals(1, relations.size());

        Relation relation = relations.get(0);
        assertEquals(has_author.getrType(), relation.getType().getName());
        assertFalse( has_author.getePairs().isEmpty());

        assertEquals(has_author.getePairs().get(0).getDirectives(), relation.getDirectives());
        Assertions.assertEquals(new Props(List.of("has_Author")), relation.getProps());
    }

    @Test
    public void createHasBooksRelationTest() {
        RelationshipType has_book = accessor.relation$("has_Book");
        List<Relation> relations = new RelationMappingTranslator().translate(has_book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse(relations.isEmpty());
        assertEquals(1, relations.size());

        Relation relation = relations.get(0);
        assertEquals(has_book.getrType(), relation.getType().getName());

        assertFalse( has_book.getePairs().isEmpty());
        assertEquals(has_book.getePairs().get(0).getDirectives(), relation.getDirectives());
        assertEquals(new Props(List.of("has_Book")), relation.getProps());
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