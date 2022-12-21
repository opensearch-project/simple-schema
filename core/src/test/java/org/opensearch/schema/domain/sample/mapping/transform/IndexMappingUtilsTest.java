package org.opensearch.schema.domain.sample.mapping.transform;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.index.transform.IndexMappingUtils;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class IndexMappingUtilsTest {

    private static Accessor accessor;

    @BeforeAll
    public static void setup() throws IOException {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaEmbeddedBooks.json");
        accessor = new Accessor(new ObjectMapper().readValue(stream, Ontology.class));
    }

    @Test
    public void testParseDateTimeType() {
        Map<String, Object> map = IndexMappingUtils.parseType(accessor, accessor.pName$("born").getType());
        Assertions.assertEquals("epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS",map.get("format"));
        Assertions.assertEquals("date",map.get("type"));
    }

    @Test
    public void testParseStringType() {
        Map<String, Object> map = IndexMappingUtils.parseType(accessor, accessor.pName$("name").getType());
        Assertions.assertEquals("keyword",map.get("type"));
    }

    @Test
    public void testParseIntType() {
        Map<String, Object> map = IndexMappingUtils.parseType(accessor, accessor.pName$("age").getType());
        Assertions.assertEquals("integer",map.get("type"));
    }

    @Test
    public void testParseTextType() {
        Map<String, Object> map = IndexMappingUtils.parseType(accessor, accessor.pName$("description").getType());
        Assertions.assertEquals("text",map.get("type"));
        Assertions.assertNotNull(map.get("fields"));
        Assertions.assertTrue(map.get("fields") instanceof Map);
        Assertions.assertNotNull(((Map)map.get("fields")).get("keyword"));
        Assertions.assertTrue(((Map)map.get("fields")).get("keyword") instanceof Map);
        Assertions.assertEquals("keyword",((Map)((Map)map.get("fields")).get("keyword")).get("type"));
    }


    @Test
    void testPopulateProperty() {
        //todo add functional tests here
    }

    @Test
    void testGenerateNestedEntityMapping() {
        //todo add functional tests here
    }
}