/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import com.fasterxml.jackson.core.JsonParseException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opensearch.commons.utils.recreateObject
import org.opensearch.simpleschema.createObjectFromJsonString
import org.opensearch.simpleschema.getJsonString

internal class SimpleSchemaObjectRequestTests {
    @Test
    fun `Delete request serialize and deserialize transport object should be equal`() {
        val deleteRequest = SimpleSchemaObjectRequest(setOf("test-id"))
        val recreatedObject = recreateObject(deleteRequest) { SimpleSchemaObjectRequest(it) }
        assertEquals(deleteRequest.objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request serialize and deserialize using json object should be equal`() {
        val deleteRequest = SimpleSchemaObjectRequest(setOf("sample_config_id"))
        val jsonString = getJsonString(deleteRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { SimpleSchemaObjectRequest.parse(it) }
        assertEquals(deleteRequest.objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request should deserialize json object using parser`() {
        val objectId = "test-id"
        val objectIds = setOf(objectId)
        val jsonString = """
        {
            "objectIdList":["$objectId"]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SimpleSchemaObjectRequest.parse(it) }
        assertEquals(objectIds, recreatedObject.objectIds)
    }

    @Test
    fun `Delete request should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { SimpleSchemaObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Delete request should throw exception when objectIdLists is replace with objectIdLists2 in json object`() {
        val jsonString = """
        {
            "objectIdLists":["test-id"]
        }
        """.trimIndent()
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { SimpleSchemaObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Delete request should safely ignore extra field in json object`() {
        val objectId = "test-id"
        val objectIds = setOf(objectId)
        val jsonString = """
        {
            "objectIdList":["$objectId"],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { SimpleSchemaObjectRequest.parse(it) }
        assertEquals(objectIds, recreatedObject.objectIds)
    }
}
