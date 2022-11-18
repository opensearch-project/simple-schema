package org.opensearch.schema.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AccessorTest {
    static Accessor accessor;
    static ObjectMapper mapper = new ObjectMapper();

    @Before
    public static void setup() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/user.json");
        Ontology ontology = mapper.readValue(stream,Ontology.class);
        accessor = new Accessor(ontology);

    }

    @Test
    void testEntity() {
    }

    @Test
    void entity$() {
    }

    @Test
    void eType() {
    }

    @Test
    void eType$() {
    }

    @Test
    void $relation() {
    }

    @Test
    void relation() {
    }

    @Test
    void relationsPairsBySourceEntity() {
    }

    @Test
    void relation$() {
    }

    @Test
    void rType() {
    }

    @Test
    void rType$() {
    }

    @Test
    void $property() {
    }

    @Test
    void property() {
    }

    @Test
    void properties() {
    }

    @Test
    void property$() {
    }

    @Test
    void pType() {
    }

    @Test
    void entities() {
    }

    @Test
    void getNestedRelationByPropertyName() {
    }

    @Test
    void relations() {
    }

    @Test
    void relationBySideA() {
    }

    @Test
    void primitiveType() {
    }

    @Test
    void getEnumeratedTypes() {
    }

    @Test
    void enumeratedType() {
    }

    @Test
    void enumeratedType$() {
    }

    @Test
    void matchNameToType() {
    }
}