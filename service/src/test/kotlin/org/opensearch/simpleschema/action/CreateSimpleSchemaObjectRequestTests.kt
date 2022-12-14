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
import org.opensearch.simpleschema.model.SchemaEntityType
import org.opensearch.simpleschema.model.SimpleSchemaObjectType

internal class CreateSimpleSchemaObjectRequestTests {
    val entity = "type Author {\n" +
        "    name: String!\n" +
        "    born: DateTime!\n" +
        "    died: DateTime\n" +
        "    nationality: String!\n" +
        "    books: [Book]\n" +
        "}"

    private val sample = SchemaEntityType("test", "test", null, listOf("a", "b"), entity)

    private val objectRequest =
        CreateSimpleSchemaObjectRequest("test-id", SimpleSchemaObjectType.SCHEMA_ENTITY, sample )

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
            "{\"objectId\":\"test-id\",\"schemaEntityType\":{\"type\":\"test\",\"name\":\"test\",\"catalog\":[\"a\",\"b\"],\"content\":\"type Author {\\n    name: String!\\n    born: DateTime!\\n    died: DateTime\\n    nationality: String!\\n    books: [Book]\\n}\"}}"
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
            "{\"objectId\":\"test-id\",\"schemaEntityType\":{\"type\":\"test\",\"name\":\"test\",\"catalog\":[\"a\",\"b\"],\"content\":\"type Author {\\n    name: String!\\n    born: DateTime!\\n    died: DateTime\\n    nationality: String!\\n    books: [Book]\\n}\"}}"
        val recreatedObject = createObjectFromJsonString(jsonString) { CreateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(sample, recreatedObject.objectData)
    }
}
