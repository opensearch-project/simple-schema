/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.apache.lucene.search.TotalHits
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.opensearch.commons.utils.recreateObject
import org.opensearch.simpleschema.constructSampleSchemaObjectDoc
import org.opensearch.simpleschema.createObjectFromJsonString
import org.opensearch.simpleschema.getJsonString
import org.opensearch.simpleschema.model.SimpleSchemaObjectSearchResult
import java.time.Instant

internal class GetSimpleSchemaObjectResponseTests {

    private fun assertSearchResultEquals(
        expected: SimpleSchemaObjectSearchResult,
        actual: SimpleSchemaObjectSearchResult
    ) {
        assertEquals(expected.startIndex, actual.startIndex)
        assertEquals(expected.totalHits, actual.totalHits)
        assertEquals(expected.totalHitRelation, actual.totalHitRelation)
        assertEquals(expected.objectListFieldName, actual.objectListFieldName)
        assertEquals(expected.objectList, actual.objectList)
    }

    @Test
    fun `Search result serialize and deserialize with config object should be equal`() {
        val sampleSampleSchemaObjectDoc = constructSampleSchemaObjectDoc()
        val searchResult = SimpleSchemaObjectSearchResult(sampleSampleSchemaObjectDoc)
        val searchResponse = GetSimpleSchemaObjectResponse(searchResult, false)
        val recreatedObject = recreateObject(searchResponse) { GetSimpleSchemaObjectResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize with multiple config object should be equal`() {
        val objectInfo1 = constructSampleSchemaObjectDoc("test 1", "test-id-1")
        val objectInfo2 = constructSampleSchemaObjectDoc("test 2", "test-id-2")
        val searchResult = SimpleSchemaObjectSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(objectInfo1, objectInfo2)
        )
        val searchResponse = GetSimpleSchemaObjectResponse(searchResult, false)
        val recreatedObject = recreateObject(searchResponse) { GetSimpleSchemaObjectResponse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json object object should be equal`() {
        val objectInfo = constructSampleSchemaObjectDoc()
        val searchResult = SimpleSchemaObjectSearchResult(objectInfo)
        val searchResponse = GetSimpleSchemaObjectResponse(searchResult, false)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetSimpleSchemaObjectResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result serialize and deserialize using json with multiple object object should be equal`() {
        val objectInfo1 = constructSampleSchemaObjectDoc("test 1", "test-id-1")
        val objectInfo2 = constructSampleSchemaObjectDoc("test 2", "test-id-2")
        val searchResult = SimpleSchemaObjectSearchResult(
            100,
            1000,
            TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO,
            listOf(objectInfo1, objectInfo2)
        )
        val searchResponse = GetSimpleSchemaObjectResponse(searchResult, false)
        val jsonString = getJsonString(searchResponse)
        val recreatedObject = createObjectFromJsonString(jsonString) { GetSimpleSchemaObjectResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should safely ignore extra field in json object`() {
        val objectInfo = constructSampleSchemaObjectDoc()
        val searchResult = SimpleSchemaObjectSearchResult(objectInfo)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHist":"1",
            "totalHitRelation":"eq",
            "simpleSchemaObjectList":[
                {
                    "objectId":"test-id",
                    "lastUpdatedTimeMs":1638482208790,
                    "createdTimeMs":1638482208790,
                    "tenant":"test-tenant",
                    "access":["test-access"],
                    "type":"schemaEntityType",
                    "schemaEntityType":   {
                        "type":"test schema entity type",
                        "name":"test schema entity type",
                        "catalog":["a","b"],
                        "content":"type Author {\n    name: String!\n    born: DateTime!\n    died: DateTime\n    nationality: String!\n    books: [Book]\n}"
                    }
                }
            ],
            "extra_field_1":["extra", "value"],
            "extra_field_2":{"extra":"value"},
            "extra_field_3":"extra value 3"
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetSimpleSchemaObjectResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should safely fallback to default if startIndex, totalHits or totalHitRelation field absent in json object`() {
        val objectInfo = constructSampleSchemaObjectDoc()
        val searchResult = SimpleSchemaObjectSearchResult(objectInfo)
        val jsonString = """
        {
            "simpleSchemaObjectList":[
                 {
                   "objectId":"test-id",
                    "lastUpdatedTimeMs":1638482208790,
                    "createdTimeMs":1638482208790,
                    "tenant":"test-tenant",
                    "access":["test-access"],
                    "type":"schemaEntityType",
                    "schemaEntityType":   {
                        "type":"test schema entity type",
                        "name":"test schema entity type",
                        "catalog":["a","b"],
                        "content":"type Author {\n    name: String!\n    born: DateTime!\n    died: DateTime\n    nationality: String!\n    books: [Book]\n}"
                    }
                }
            ]
        }
        """.trimIndent()
        val recreatedObject = createObjectFromJsonString(jsonString) { GetSimpleSchemaObjectResponse.parse(it) }
        assertSearchResultEquals(searchResult, recreatedObject.searchResult)
    }

    @Test
    fun `Search result should throw exception if notificationConfigs is absent in json`() {
        val lastUpdatedTimeMs = Instant.ofEpochMilli(Instant.now().toEpochMilli())
        val createdTimeMs = lastUpdatedTimeMs.minusSeconds(1000)
        val jsonString = """
        {
            "startIndex":"0",
            "totalHist":"1",
            "totalHitRelation":"eq",
            "simpleSchemaObjectList":[
                {
                    "objectId":"object-Id",
                    "lastUpdatedTimeMs":"${lastUpdatedTimeMs.toEpochMilli()}",
                    "createdTimeMs":"${createdTimeMs.toEpochMilli()}"
                }
            ]
        }
        """.trimIndent()
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            createObjectFromJsonString(jsonString) { GetSimpleSchemaObjectResponse.parse(it) }
        }
    }
}
