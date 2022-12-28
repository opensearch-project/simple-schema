package org.opensearch.schema.index.schema.domain.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IndexProviderSimpleChildTest {
    static Ontology ontology;
    static Accessor accessor;

    @BeforeAll
    public static void setup() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaChildBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        accessor = new Accessor(ontology);

    }

    @Test
    public void createHasAuthorRelationTest() {
        RelationshipType has_author = accessor.relation$("has_Author");
        List<Relation> relation = new RelationMappingTranslator().translate(has_author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse( relation.isEmpty());
        assertEquals(has_author.getrType(), relation.get(0).getType().getName());
        assertEquals(has_author.getDirectives(), relation.get(0).getDirectives());
        Assertions.assertEquals(new Props(List.of("has_Author")), relation.get(0).getProps());
    }

    @Test
    public void createHasBooksRelationTest() {
        RelationshipType has_book = accessor.relation$("has_Book");
        List<Relation> relation = new RelationMappingTranslator().translate(has_book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse( relation.isEmpty());
        assertEquals(has_book.getrType(), relation.get(0).getType().getName());
        assertEquals(has_book.getDirectives(), relation.get(0).getDirectives());
        assertEquals(new Props(List.of("has_Book")), relation.get(0).getProps());
    }

    @Test
    public void createAuthorEntityTest() {
        EntityType author = accessor.entity$("Author");
        List<Entity> entity = new EntityMappingTranslator().translate(author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse( entity.isEmpty());
        assertEquals(author.geteType(), entity.get(0).getType().getName());
        assertEquals(author.getDirectives(), entity.get(0).getDirectives());
        assertEquals(new Props(List.of("Author")), entity.get(0).getProps());
    }

    @Test
    public void createBookEntityTest() {
        EntityType book = accessor.entity$("Book");
        List<Entity> entity = new EntityMappingTranslator().translate(book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertFalse( entity.isEmpty());
        assertEquals(book.geteType(), entity.get(0).getType().getName());
        assertEquals(book.getDirectives(), entity.get(0).getDirectives());
        assertEquals(new Props(List.of("Book")), entity.get(0).getProps());
    }

}