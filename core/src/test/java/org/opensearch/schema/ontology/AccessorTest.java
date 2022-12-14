package org.opensearch.schema.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class AccessorTest {
    static Accessor accessor;
    static ObjectMapper mapper = new ObjectMapper();

    @Before
    public static void setup() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/observability/user.json");
        Ontology ontology = mapper.readValue(stream,Ontology.class);
        accessor = new Accessor(ontology);

    }

    @Test
    void testEntity() {
        //todo implement
    }

    @Test
    void entity$() {
        //todo implement
    }

    @Test
    void eType() {
        //todo implement
    }

    @Test
    void eType$() {
        //todo implement
    }

    @Test
    void $relation() {
        //todo implement
    }

    @Test
    void relation() {
        //todo implement
    }

    @Test
    void relationsPairsBySourceEntity() {
        //todo implement
    }

    @Test
    void relation$() {
        //todo implement
    }

    @Test
    void rType() {
        //todo implement
    }

    @Test
    void rType$() {
        //todo implement
    }

    @Test
    void $property() {
        //todo implement
    }

    @Test
    void property() {
        //todo implement
    }

    @Test
    void properties() {
        //todo implement
    }

    @Test
    void property$() {
        //todo implement
    }

    @Test
    void pType() {
        //todo implement
    }

    @Test
    void entities() {
        //todo implement
    }

    @Test
    void getNestedRelationByPropertyName() {
        //todo implement
    }

    @Test
    void relations() {
        //todo implement
    }

    @Test
    void relationBySideA() {
        //todo implement
    }

    @Test
    void primitiveType() {
        //todo implement
    }

    @Test
    void getEnumeratedTypes() {
        //todo implement
    }

    @Test
    void enumeratedType() {
        //todo implement
    }

    @Test
    void enumeratedType$() {
        //todo implement
    }

    @Test
    void matchNameToType() {
        //todo implement
    }
}