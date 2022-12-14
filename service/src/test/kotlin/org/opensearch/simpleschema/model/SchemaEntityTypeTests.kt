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

internal class SchemaEntityTypeTests {
    val entity = "type Author {\n" +
        "    name: String!\n" +
        "    born: DateTime!\n" +
        "    died: DateTime\n" +
        "    nationality: String!\n" +
        "    books: [Book]\n" +
        "}"

    private val sample = SchemaEntityType("test", "test", null, listOf("a", "b"), entity)

    @Test
    fun `SchemaEntityType serialize and deserialize transport object should be equal`() {
        val recreatedObject = recreateObject(sample) { SchemaEntityType(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `SchemaEntityType serialize and deserialize using json object should be equal`() {
        val jsonString = getJsonString(sample)
        val recreatedObject = createObjectFromJsonString(jsonString) { SchemaEntityType.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `SchemaEntityType should deserialize json object using parser`() {
        val jsonString =
            "{\"type\":\"test\",\"name\":\"test\",\"catalog\":[\"a\",\"b\"],\"content\":\"type Author {\\n    name: String!\\n    born: DateTime!\\n    died: DateTime\\n    nationality: String!\\n    books: [Book]\\n}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SchemaEntityType.parse(it) }
        assertEquals(sample, recreatedObject)
    }

    @Test
    fun `SchemaEntityType should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SchemaEntityType.parse(it) }
        }
    }

    @Test
    fun `SchemaEntityType should safely ignore extra field in json object`() {
        val jsonString =
            "{\"type\":\"test\",\"foo\":\"bar\",\"name\":\"test\",\"catalog\":[\"a\",\"b\"],\"content\":\"type Author {\\n    name: String!\\n    born: DateTime!\\n    died: DateTime\\n    nationality: String!\\n    books: [Book]\\n}\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { SchemaEntityType.parse(it) }
        assertEquals(sample, recreatedObject)
    }
}
