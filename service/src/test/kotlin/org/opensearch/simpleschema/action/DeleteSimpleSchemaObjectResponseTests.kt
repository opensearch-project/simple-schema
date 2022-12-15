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
import org.opensearch.rest.RestStatus

internal class DeleteSimpleSchemaObjectResponseTests {
    @Test
    fun `Delete response serialize and deserialize transport object should be equal`() {
        val objectResponse = DeleteSimpleSchemaObjectResponse(mapOf(Pair("test-id", RestStatus.OK)))
        val recreatedObject = recreateObject(objectResponse) { DeleteSimpleSchemaObjectResponse(it) }
        assertEquals(objectResponse.objectIdToStatus, recreatedObject.objectIdToStatus)
    }

    @Test
    fun `Delete response serialize and deserialize using json object should be equal`() {
        val objectResponse = DeleteSimpleSchemaObjectResponse(mapOf(Pair("test-id", RestStatus.OK)))
        val jsonString = getJsonString(objectResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectResponse.objectIdToStatus, recreatedObject.objectIdToStatus)
    }

    @Test
    fun `Delete response should deserialize json object using parser`() {
        val objectId = "test-id"
        val objectResponse = DeleteSimpleSchemaObjectResponse(mapOf(Pair(objectId, RestStatus.OK)))
        val jsonString = """
        {
            "deleteResponseList":{
                "$objectId":"OK"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectResponse.objectIdToStatus, recreatedObject.objectIdToStatus)
    }

    @Test
    fun `Delete response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { DeleteSimpleSchemaObjectResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should throw exception when objectId is replace with objectId2 in json object`() {
        val jsonString = "{\"objectId2\":\"test-id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { DeleteSimpleSchemaObjectResponse.parse(it) }
        }
    }

    @Test
    fun `Delete response should safely ignore extra field in json object`() {
        val objectId = "test-id"
        val objectResponse = DeleteSimpleSchemaObjectResponse(mapOf(Pair(objectId, RestStatus.OK)))
        val jsonString = """
        {
            "deleteResponseList":{
                "$objectId":"OK"
            },
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { DeleteSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectResponse.objectIdToStatus, recreatedObject.objectIdToStatus)
    }
}
