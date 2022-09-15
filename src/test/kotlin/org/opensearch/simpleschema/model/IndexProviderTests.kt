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

    private val sample = IndexProvider("test", "test", null, listOf("a", "b"), "ont", indexProvider)

    @Test
    fun `IndexProvider serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(sample) { IndexProvider(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(sample)
        val recreatedObject = createObjectFromJsonString(jsonString) { IndexProvider.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should deserialize json object using parser`() {
        val jsonString =
            " {\n" +
            "    \"type\": \"test\",\n" +
            "    \"name\": \"test\",\n" +
            "    \"indices\": [\n" +
            "      \"a\",\n" +
            "      \"b\"\n" +
            "    ],\n" +
            "    \"ontology\": \"ont\",\n" +
            "    \"content\": \"{\\n  \\\"entities\\\": [\\n    {\\n      \\\"type\\\": \\\"User\\\",\\n      \\\"partition\\\": \\\"NESTED\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"User\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [\\n        {\\n          \\\"type\\\": \\\"Group\\\",\\n          \\\"partition\\\": \\\"NESTED\\\",\\n          \\\"props\\\": {\\n            \\\"values\\\": [\\n              \\\"Group\\\"\\n            ]\\n          },\\n          \\\"nested\\\": [],\\n          \\\"mapping\\\": \\\"INDEX\\\"\\n        }\\n      ],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    },\\n    {\\n      \\\"type\\\": \\\"Group\\\",\\n      \\\"partition\\\": \\\"NESTED\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"Group\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    }\\n  ],\\n  \\\"relations\\\": [],\\n  \\\"ontology\\\": \\\"client\\\",\\n  \\\"rootEntities\\\": [\\n    {\\n      \\\"type\\\": \\\"User\\\",\\n      \\\"partition\\\": \\\"STATIC\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"User\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [\\n        {\\n          \\\"type\\\": \\\"Group\\\",\\n          \\\"partition\\\": \\\"NESTED\\\",\\n          \\\"props\\\": {\\n            \\\"values\\\": [\\n              \\\"Group\\\"\\n            ]\\n          },\\n          \\\"nested\\\": [],\\n          \\\"mapping\\\": \\\"INDEX\\\"\\n        }\\n      ],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    }\\n  ],\\n  \\\"rootRelations\\\": []\\n}\"\n" +
                "}"
        val recreatedObject = createObjectFromJsonString(jsonString) { IndexProvider.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `IndexProvider should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { IndexProvider.parse(it) }
        }
    }

    @Test
    fun `IndexProvider should safely ignore extra field in json object`() {
        val jsonString =
            " {\n" +
                "    \"type\": \"test\",\n" +
                "    \"foo\": \"bar\",\n" +
                "    \"name\": \"test\",\n" +
                "    \"indices\": [\n" +
                "      \"a\",\n" +
                "      \"b\"\n" +
                "    ],\n" +
                "    \"ontology\": \"ont\",\n" +
                "    \"content\": \"{\\n  \\\"entities\\\": [\\n    {\\n      \\\"type\\\": \\\"User\\\",\\n      \\\"partition\\\": \\\"NESTED\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"User\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [\\n        {\\n          \\\"type\\\": \\\"Group\\\",\\n          \\\"partition\\\": \\\"NESTED\\\",\\n          \\\"props\\\": {\\n            \\\"values\\\": [\\n              \\\"Group\\\"\\n            ]\\n          },\\n          \\\"nested\\\": [],\\n          \\\"mapping\\\": \\\"INDEX\\\"\\n        }\\n      ],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    },\\n    {\\n      \\\"type\\\": \\\"Group\\\",\\n      \\\"partition\\\": \\\"NESTED\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"Group\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    }\\n  ],\\n  \\\"relations\\\": [],\\n  \\\"ontology\\\": \\\"client\\\",\\n  \\\"rootEntities\\\": [\\n    {\\n      \\\"type\\\": \\\"User\\\",\\n      \\\"partition\\\": \\\"STATIC\\\",\\n      \\\"props\\\": {\\n        \\\"values\\\": [\\n          \\\"User\\\"\\n        ]\\n      },\\n      \\\"nested\\\": [\\n        {\\n          \\\"type\\\": \\\"Group\\\",\\n          \\\"partition\\\": \\\"NESTED\\\",\\n          \\\"props\\\": {\\n            \\\"values\\\": [\\n              \\\"Group\\\"\\n            ]\\n          },\\n          \\\"nested\\\": [],\\n          \\\"mapping\\\": \\\"INDEX\\\"\\n        }\\n      ],\\n      \\\"mapping\\\": \\\"INDEX\\\"\\n    }\\n  ],\\n  \\\"rootRelations\\\": []\\n}\"\n" +
                "}"
        val recreatedObject = createObjectFromJsonString(jsonString) { IndexProvider.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
