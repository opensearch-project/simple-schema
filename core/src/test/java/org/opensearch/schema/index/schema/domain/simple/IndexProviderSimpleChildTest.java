package org.opensearch.schema.index.schema.domain.simple;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.index.schema.*;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.RelationshipType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Relation relation = new RelationMappingTranslator().translate(has_author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertEquals(has_author.getrType(), relation.getType().getName());
        assertEquals(has_author.getDirectives(), relation.getDirectives());
        Assertions.assertEquals(new Props(List.of("has_Author")), relation.getProps());
    }

    @Test
    public void createHasBooksRelationTest() {
        RelationshipType has_book = accessor.relation$("has_Book");
        Relation relation = new RelationMappingTranslator().translate(has_book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertEquals(has_book.getrType(), relation.getType().getName());
        assertEquals(has_book.getDirectives(), relation.getDirectives());
        assertEquals(new Props(List.of("has_Book")), relation.getProps());
    }

    @Test
    public void createAuthorEntityTest() {
        EntityType author = accessor.entity$("Author");
        Entity entity = new EntityMappingTranslator().translate(author, new MappingTranslator.MappingTranslatorContext(accessor));
        assertEquals(author.geteType(), entity.getType().getName());
        assertEquals(author.getDirectives(), entity.getDirectives());
        assertEquals(new Props(List.of("Author")), entity.getProps());
    }

    @Test
    public void createBookEntityTest() {
        EntityType book = accessor.entity$("Book");
        Entity entity = new EntityMappingTranslator().translate(book, new MappingTranslator.MappingTranslatorContext(accessor));
        assertEquals(book.geteType(), entity.getType().getName());
        assertEquals(book.getDirectives(), entity.getDirectives());
        assertEquals(new Props(List.of("Book")), entity.getProps());
    }

}