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

internal class UpdateSimpleSchemaObjectResponseTests {

    @Test
    fun `Update response serialize and deserialize transport object should be equal`() {
        val configResponse = UpdateSimpleSchemaObjectResponse("sample_config_id")
        val recreatedObject = recreateObject(configResponse) { UpdateSimpleSchemaObjectResponse(it) }
        assertEquals(configResponse.objectId, recreatedObject.objectId)
    }

    @Test
    fun `Update response serialize and deserialize using json object should be equal`() {
        val objectResponse = UpdateSimpleSchemaObjectResponse("test-id")
        val jsonString = getJsonString(objectResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectResponse.objectId, recreatedObject.objectId)
    }

    @Test
    fun `Update response should deserialize json object using parser`() {
        val objectId = "test-id"
        val jsonString = "{\"objectId\":\"$objectId\"}"
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectId, recreatedObject.objectId)
    }

    @Test
    fun `Update response should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should throw exception when objectId is replace with objectId2 in json object`() {
        val jsonString = "{\"objectId2\":\"test-id\"}"
        assertThrows<IllegalArgumentException> {
            createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectResponse.parse(it) }
        }
    }

    @Test
    fun `Update response should safely ignore extra field in json object`() {
        val objectId = "test-id"
        val jsonString = """
        {
            "objectId":"$objectId",
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectResponse.parse(it) }
        assertEquals(objectId, recreatedObject.objectId)
    }
}
