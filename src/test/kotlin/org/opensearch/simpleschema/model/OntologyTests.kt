/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.model

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.simpleschema.createObjectFromJsonString
import org.opensearch.simpleschema.getJsonString

internal class OntologyTests {
    var ontology = "{\n" +
        "  \"ont\": \"user\",\n" +
        "  \"directives\": [],\n" +
        "  \"entityTypes\": [\n" +
        "    {\n" +
        "      \"eType\": \"Geo\",\n" +
        "      \"name\": \"Geo\",\n" +
        "      \"properties\": [\n" +
        "        \"name\",\n" +
        "        \"location\",\n" +
        "        \"timezone\"\n" +
        "      ],\n" +
        "      \"abstract\": false\n" +
        "    },\n" +
        "  ],\n" +
        "  \"relationshipTypes\": [],\n" +
        "  \"properties\": [\n" +
        "    {\n" +
        "      \"pType\": \"name\",\n" +
        "      \"name\": \"name\",\n" +
        "      \"type\": {\n" +
        "        \"pType\": \"Primitive\",\n" +
        "        \"type\": \"STRING\",\n" +
        "        \"array\": false\n" +
        "      }\n" +
        "    },\n" +
        "    {\n" +
        "      \"pType\": \"timezone\",\n" +
        "      \"name\": \"timezone\",\n" +
        "      \"type\": {\n" +
        "        \"pType\": \"Primitive\",\n" +
        "        \"type\": \"STRING\",\n" +
        "        \"array\": false\n" +
        "      }\n" +
        "    },\n" +
        "    {\n" +
        "      \"pType\": \"location\",\n" +
        "      \"name\": \"location\",\n" +
        "      \"type\": {\n" +
        "        \"pType\": \"Primitive\",\n" +
        "        \"type\": \"GEOPOINT\",\n" +
        "        \"array\": false\n" +
        "      }\n" +
        "    },\n" +
        "  ]\n" +
        "}"

    private val sample = Ontology("test","test",null,listOf("a","b"),ontology)

    @Test
    fun `Ontology serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(sample) { Ontology(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `Ontology serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(sample)
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `Ontology should deserialize json object using parser`() {
        val jsonString =
            "{\"name\":\"test-notebook\",\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"backend\":\"Default\",\"paragraphs\":[{\"output\":[{\"result\":\"sample paragraph\",\"outputType\":\"MARKDOWN\",\"execution_time\":\"0 ms\"}],\"input\":{\"inputText\":\"%md sample paragraph\",\"inputType\":\"MARKDOWN\"},\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"id\":\"paragraph_bcd3c65c-91db-489d-b667-496fd378714e\"}]}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `Ontology should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        }
    }

    @Test
    fun `Ontology should safely ignore extra field in json object`() {
        val jsonString =
            "{\"name\":\"test-notebook\",\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"backend\":\"Default\",\"paragraphs\":[{\"output\":[{\"result\":\"sample paragraph\",\"outputType\":\"MARKDOWN\",\"execution_time\":\"0 ms\"}],\"input\":{\"inputText\":\"%md sample paragraph\",\"inputType\":\"MARKDOWN\"},\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"id\":\"paragraph_bcd3c65c-91db-489d-b667-496fd378714e\"}],\"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
