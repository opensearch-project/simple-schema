/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.simpleschema.createObjectFromJsonString
import org.opensearch.simpleschema.getJsonString
import org.opensearch.simpleschema.model.Ontology
import org.opensearch.simpleschema.model.SimpleSchemaObjectType

internal class CreateSimpleSchemaObjectRequestTests {
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

    private val objectRequest =
        CreateSimpleSchemaObjectRequest("test-id", SimpleSchemaObjectType.ONTOLOGY, sample )

    @Test
    fun `Create object serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(objectRequest) { CreateSimpleSchemaObjectRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(objectRequest.objectData, recreatedObject.objectData)
    }

    @Test
    fun `Create object serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(objectRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(objectRequest.objectData, recreatedObject.objectData)
    }

    @Test
    fun `Create object should deserialize json object using parser`() {
        val jsonString =
            "{  \"ontology\": {\n" +
                "    \"type\": \"test\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"namespace\": [\n" +
                "      \"a\",\n" +
                "      \"b\"\n" +
                "    ],\n" +
                "    \"content\": \"{\\n  \\\"ont\\\": \\\"user\\\",\\n  \\\"directives\\\": [],\\n  \\\"entityTypes\\\": [\\n    {\\n      \\\"eType\\\": \\\"Geo\\\",\\n      \\\"name\\\": \\\"Geo\\\",\\n      \\\"properties\\\": [\\n        \\\"name\\\",\\n        \\\"location\\\",\\n        \\\"timezone\\\"\\n      ],\\n      \\\"abstract\\\": false\\n    },\\n  ],\\n  \\\"relationshipTypes\\\": [],\\n  \\\"properties\\\": [\\n    {\\n      \\\"pType\\\": \\\"name\\\",\\n      \\\"name\\\": \\\"name\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"timezone\\\",\\n      \\\"name\\\": \\\"timezone\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"location\\\",\\n      \\\"name\\\": \\\"location\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"GEOPOINT\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n  ]\\n}\"\n" +
                "  }\n}"
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(sample, recreatedObject.objectData)
    }

    @Test
    fun `Create object should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { CreateSimpleSchemaObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Create object should safely ignore extra field in json object`() {
        val jsonString =
            " { \"ontology\": {\n" +
                "    \"foo\": \"bar\",\n" +
                "    \"type\": \"test\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"namespace\": [\n" +
                "      \"a\",\n" +
                "      \"b\"\n" +
                "    ],\n" +
                "    \"content\": \"{\\n  \\\"ont\\\": \\\"user\\\",\\n  \\\"directives\\\": [],\\n  \\\"entityTypes\\\": [\\n    {\\n      \\\"eType\\\": \\\"Geo\\\",\\n      \\\"name\\\": \\\"Geo\\\",\\n      \\\"properties\\\": [\\n        \\\"name\\\",\\n        \\\"location\\\",\\n        \\\"timezone\\\"\\n      ],\\n      \\\"abstract\\\": false\\n    },\\n  ],\\n  \\\"relationshipTypes\\\": [],\\n  \\\"properties\\\": [\\n    {\\n      \\\"pType\\\": \\\"name\\\",\\n      \\\"name\\\": \\\"name\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"timezone\\\",\\n      \\\"name\\\": \\\"timezone\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"location\\\",\\n      \\\"name\\\": \\\"location\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"GEOPOINT\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n  ]\\n}\"\n" +
                "  }\n}"
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(sample, recreatedObject.objectData)
    }
}
