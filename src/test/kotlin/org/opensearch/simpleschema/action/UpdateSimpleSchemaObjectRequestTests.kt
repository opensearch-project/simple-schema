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
import org.opensearch.simpleschema.constructSampleSchemaObjectDoc
import org.opensearch.simpleschema.createObjectFromJsonString
import org.opensearch.simpleschema.getJsonString

internal class UpdateSimpleSchemaObjectRequestTests {
    @Test
    fun `Update object serialize and deserialize transport object should be equal`() {
        val sampleObject = constructSampleSchemaObjectDoc()
        val objectRequest = UpdateSimpleSchemaObjectRequest(sampleObject.objectData!!, sampleObject.type, "test-id")
        val recreatedObject =
            recreateObject(objectRequest) { UpdateSimpleSchemaObjectRequest(it) }
        assertNull(recreatedObject.validate())
        assertEquals(objectRequest.objectData, recreatedObject.objectData)
        assertEquals("test-id", recreatedObject.objectId)
    }

    @Test
    fun `Update object serialize and deserialize using json object should be equal webhook`() {
        val sampleObject = constructSampleSchemaObjectDoc()
        val objectRequest = UpdateSimpleSchemaObjectRequest(sampleObject.objectData!!, sampleObject.type, "test-id")
        val jsonString = getJsonString(objectRequest)
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(objectRequest.objectData, recreatedObject.objectData)
        assertEquals("test-id", recreatedObject.objectId)
    }

    @Test
    fun `Update object should deserialize json object using parser`() {
        val sampleObject = constructSampleSchemaObjectDoc()
        val jsonString = """
        {
            "objectId":"test-id",
            "timestamp":{
                "name":"test object",
                "index":"opensearch_dashboards_sample_data_logs",
                "type":"timestamp",
                "dsl_type":"date"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(sampleObject.objectData, recreatedObject.objectData)
        assertEquals("test-id", recreatedObject.objectId)
    }

    @Test
    fun `Update object should throw exception when invalid json object is passed`() {
        val jsonString = "sample message"
        assertThrows<JsonParseException> {
            createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectRequest.parse(it) }
        }
    }

    @Test
    fun `Update object should safely ignore extra field in json object`() {
        val sampleObject = constructSampleSchemaObjectDoc()
        val jsonString = """
        {
            "objectId":"test-id",
            "timestamp":{
                "name":"test object",
                "index":"opensearch_dashboards_sample_data_logs",
                "type":"timestamp",
                "dsl_type":"date",
                "extra_field_1":["extra", "value"],
                "extra_field_2":{"extra":"value"},
                "extra_field_3":"extra value 3"
            }
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { UpdateSimpleSchemaObjectRequest.parse(it) }
        assertEquals(sampleObject.objectData, recreatedObject.objectData)
        assertEquals("test-id", recreatedObject.objectId)
    }
}
