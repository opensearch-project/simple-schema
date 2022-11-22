package org.opensearch.simpleschema.action

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opensearch.common.io.stream.InputStreamStreamInput
import org.opensearch.common.io.stream.OutputStreamStreamOutput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.test.assertContentEquals

class CreateGraphQLRequestTests {
    private val xContentType = XContentType.JSON
    private val createSchemaGraphQL = """
        type Author { name: String! \n born: DateTime! \n died: DateTime \n nationality: String! \n books: [Book] \n }
    """.trim()
    private val createSchemaRequest = "{\"content\": \"$createSchemaGraphQL\"}".trimIndent()

    @Test
    fun `Parsing a valid schema request object is successful`() {
        val parser: XContentParser = xContentType
            .xContent()
            .createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, createSchemaRequest)
        parser.nextToken()
        val request = CreateGraphQLRequest.parse(parser)
        assertEquals(request.document.definitions.size, 1)
    }

    @Test
    fun `Parsing an input stream and parsing XContent produces same results`() {
        val parser: XContentParser = xContentType
            .xContent()
            .createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, createSchemaRequest)
        parser.nextToken()
        val request1 = CreateGraphQLRequest.parse(parser)

        val outputStream = ByteArrayOutputStream()
        val graphQLStreamOutput = OutputStreamStreamOutput(outputStream)
        // Set an empty ID for superclass constructor to read
        graphQLStreamOutput.writeString("")
        graphQLStreamOutput.writeString(createSchemaGraphQL.replace("\\n", "\n"))
        graphQLStreamOutput.flush()
        println(outputStream.toString())
        val graphQLStreamInput = InputStreamStreamInput(outputStream.toString().byteInputStream())
        val request2 = CreateGraphQLRequest(graphQLStreamInput)
        // Definitions are unequal due to invisible fields, but the strings show they're parsed equivalently
        assertEquals(request1.document.definitions.toString(), request2.document.definitions.toString())
    }
}