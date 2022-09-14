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

internal class IndexProviderTests {
    var indexProvider = "{\n" +
        "  \"entities\": [\n" +
        "    {\n" +
        "      \"type\": \"User\",\n" +
        "      \"partition\": \"NESTED\",\n" +
        "      \"props\": {\n" +
        "        \"values\": [\n" +
        "          \"User\"\n" +
        "        ]\n" +
        "      },\n" +
        "      \"nested\": [\n" +
        "        {\n" +
        "          \"type\": \"Group\",\n" +
        "          \"partition\": \"NESTED\",\n" +
        "          \"props\": {\n" +
        "            \"values\": [\n" +
        "              \"Group\"\n" +
        "            ]\n" +
        "          },\n" +
        "          \"nested\": [],\n" +
        "          \"mapping\": \"INDEX\"\n" +
        "        }\n" +
        "      ],\n" +
        "      \"mapping\": \"INDEX\"\n" +
        "    },\n" +
        "    {\n" +
        "      \"type\": \"Group\",\n" +
        "      \"partition\": \"NESTED\",\n" +
        "      \"props\": {\n" +
        "        \"values\": [\n" +
        "          \"Group\"\n" +
        "        ]\n" +
        "      },\n" +
        "      \"nested\": [],\n" +
        "      \"mapping\": \"INDEX\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"relations\": [],\n" +
        "  \"ontology\": \"client\",\n" +
        "  \"rootEntities\": [\n" +
        "    {\n" +
        "      \"type\": \"User\",\n" +
        "      \"partition\": \"STATIC\",\n" +
        "      \"props\": {\n" +
        "        \"values\": [\n" +
        "          \"User\"\n" +
        "        ]\n" +
        "      },\n" +
        "      \"nested\": [\n" +
        "        {\n" +
        "          \"type\": \"Group\",\n" +
        "          \"partition\": \"NESTED\",\n" +
        "          \"props\": {\n" +
        "            \"values\": [\n" +
        "              \"Group\"\n" +
        "            ]\n" +
        "          },\n" +
        "          \"nested\": [],\n" +
        "          \"mapping\": \"INDEX\"\n" +
        "        }\n" +
        "      ],\n" +
        "      \"mapping\": \"INDEX\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"rootRelations\": []\n" +
        "}"

    private val sample = IndexProvider("test","test",null,listOf("a","b"),"ont",indexProvider)

    @Test
    fun `IndexProvider serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(sample) { Ontology(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(sample)
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should deserialize json object using parser`() {
        val jsonString =
            "{\"name\":\"test-notebook\",\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"backend\":\"Default\",\"paragraphs\":[{\"output\":[{\"result\":\"sample paragraph\",\"outputType\":\"MARKDOWN\",\"execution_time\":\"0 ms\"}],\"input\":{\"inputText\":\"%md sample paragraph\",\"inputType\":\"MARKDOWN\"},\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"id\":\"paragraph_bcd3c65c-91db-489d-b667-496fd378714e\"}]}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        }
    }

    @Test
    fun `IndexProvider should safely ignore extra field in json object`() {
        val jsonString =
            "{\"name\":\"test-notebook\",\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"backend\":\"Default\",\"paragraphs\":[{\"output\":[{\"result\":\"sample paragraph\",\"outputType\":\"MARKDOWN\",\"execution_time\":\"0 ms\"}],\"input\":{\"inputText\":\"%md sample paragraph\",\"inputType\":\"MARKDOWN\"},\"dateCreated\":\"2021-12-01T18:33:40.017Z\",\"dateModified\":\"2021-12-01T18:33:40.017Z\",\"id\":\"paragraph_bcd3c65c-91db-489d-b667-496fd378714e\"}],\"another\":\"field\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
