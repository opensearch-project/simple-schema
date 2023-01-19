/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.opensearch.common.xcontent.*
import org.opensearch.simpleschema.model.SchemaEntityType
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import java.io.ByteArrayOutputStream
import java.time.Instant

private const val DEFAULT_TIME_ACCURACY_SEC = 5L

var ontology = "{}"

var schemaEntityType = "type Author {\n" +
    "    id: ID!\n" +
    "    name: String!\n" +
    "    born: DateTime!\n" +
    "    died: DateTime\n" +
    "    nationality: String!\n" +
    "    books: [Book]\n" +
    "}"

val indexProvider = "{}"

fun constructSampleSchemaObjectDoc(
    name: String = "test schema entity type",
    id: String = "test-id"
): SimpleSchemaObjectDoc {
    return SimpleSchemaObjectDoc(
        id,
        Instant.ofEpochMilli(1638482208790),
        Instant.ofEpochMilli(1638482208790),
        "test-tenant",
        listOf("test-access"),
        SimpleSchemaObjectType.SCHEMA_ENTITY,
        SchemaEntityType(
            name,
            name,
            null,
            listOf("a", "b"),
            "type Author {\n" +
                "    id: ID!\n" +
                "    name: String!\n" +
                "    born: DateTime!\n" +
                "    died: DateTime\n" +
                "    nationality: String!\n" +
                "    books: [Book]\n" +
                "}"
        )
    )
}

fun constructSchemaEntityTypeRequest(name: String = "test schema entity"): String {
    return """
        {
            "schemaEntityType":{
                "name" : "$name",
                "type" : "${SimpleSchemaObjectType.SCHEMA_ENTITY}",
                "catalog" : ["test"],
                "content" : "type Author { id: ID! \nname: String! \n born: DateTime! \n died: DateTime \n nationality: String! \n books: [Book] \n }"
                }
            }
    """.trimIndent()
}

fun constructSchemaDomainRequest(name: String = "test schema compilation"): String {
    return """
     {
         "objectId" : "$name",
         "catalog" : ["test"],
         "entityList" : ["Book", "Author"]
     }
    """.trimIndent()
}


fun constructIndexProviderRequest(name: String = "test Index Provider", content: String = indexProvider): String {
    return """
        {
            "indexProvider":{
                "name" : "$name",
                "type" : "$name",
                "description" : "$name",
                "ontology" : "test",
                "indices" : ["test"],
                "content" : "$content"
            }
        }
    """.trimIndent()
}

fun jsonify(text: String): JsonObject {
    return JsonParser.parseString(text).asJsonObject
}

fun validateErrorResponse(response: JsonObject, statusCode: Int, errorType: String = "status_exception") {
    Assert.assertNotNull("Error response content should be generated", response)
    val status = response.get("status").asInt
    val error = response.get("error").asJsonObject
    val rootCause = error.get("root_cause").asJsonArray
    val type = error.get("type").asString
    val reason = error.get("reason").asString
    Assert.assertEquals(statusCode, status)
    Assert.assertEquals(errorType, type)
    Assert.assertNotNull(reason)
    Assert.assertNotNull(rootCause)
    Assert.assertTrue(rootCause.size() > 0)
}

fun getJsonString(xContent: ToXContent, params: ToXContent.Params? = ToXContent.EMPTY_PARAMS): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        val builder = XContentFactory.jsonBuilder(byteArrayOutputStream)
        xContent.toXContent(builder, params)
        builder.close()
        return byteArrayOutputStream.toString("UTF8")
    }
}

inline fun <reified CreateType> createObjectFromJsonString(
    jsonString: String,
    block: (XContentParser) -> CreateType
): CreateType {
    val parser = XContentType.JSON.xContent()
        .createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.IGNORE_DEPRECATIONS, jsonString)
    parser.nextToken()
    return block(parser)
}
