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
            " {\n" +
                "    \"type\": \"test\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"namespace\": [\n" +
                "      \"a\",\n" +
                "      \"b\"\n" +
                "    ],\n" +
                "    \"content\": \"{\\n  \\\"ont\\\": \\\"user\\\",\\n  \\\"directives\\\": [],\\n  \\\"entityTypes\\\": [\\n    {\\n      \\\"eType\\\": \\\"Geo\\\",\\n      \\\"name\\\": \\\"Geo\\\",\\n      \\\"properties\\\": [\\n        \\\"name\\\",\\n        \\\"location\\\",\\n        \\\"timezone\\\"\\n      ],\\n      \\\"abstract\\\": false\\n    },\\n  ],\\n  \\\"relationshipTypes\\\": [],\\n  \\\"properties\\\": [\\n    {\\n      \\\"pType\\\": \\\"name\\\",\\n      \\\"name\\\": \\\"name\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"timezone\\\",\\n      \\\"name\\\": \\\"timezone\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"location\\\",\\n      \\\"name\\\": \\\"location\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"GEOPOINT\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n  ]\\n}\"\n" +
            "  }"
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
            " {\n" +
                "    \"foo\": \"bar\",\n" +
                "    \"type\": \"test\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"namespace\": [\n" +
                "      \"a\",\n" +
                "      \"b\"\n" +
                "    ],\n" +
                "    \"content\": \"{\\n  \\\"ont\\\": \\\"user\\\",\\n  \\\"directives\\\": [],\\n  \\\"entityTypes\\\": [\\n    {\\n      \\\"eType\\\": \\\"Geo\\\",\\n      \\\"name\\\": \\\"Geo\\\",\\n      \\\"properties\\\": [\\n        \\\"name\\\",\\n        \\\"location\\\",\\n        \\\"timezone\\\"\\n      ],\\n      \\\"abstract\\\": false\\n    },\\n  ],\\n  \\\"relationshipTypes\\\": [],\\n  \\\"properties\\\": [\\n    {\\n      \\\"pType\\\": \\\"name\\\",\\n      \\\"name\\\": \\\"name\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"timezone\\\",\\n      \\\"name\\\": \\\"timezone\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"STRING\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n    {\\n      \\\"pType\\\": \\\"location\\\",\\n      \\\"name\\\": \\\"location\\\",\\n      \\\"type\\\": {\\n        \\\"pType\\\": \\\"Primitive\\\",\\n        \\\"type\\\": \\\"GEOPOINT\\\",\\n        \\\"array\\\": false\\n      }\\n    },\\n  ]\\n}\"\n" +
                " }"
        val recreatedObject = createObjectFromJsonString(jsonString) { Ontology.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
