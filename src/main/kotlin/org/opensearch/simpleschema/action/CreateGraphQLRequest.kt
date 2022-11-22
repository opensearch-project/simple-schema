package org.opensearch.simpleschema.action

import graphql.language.Document
import graphql.parser.Parser
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.InputStreamStreamInput
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.model.BaseObjectData
import org.opensearch.simpleschema.model.RestTag
import org.opensearch.simpleschema.model.SimpleSchemaObjectDataProperties
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.IllegalArgumentException

internal class CreateGraphQLRequest : ActionRequest, ToXContentObject {
    val document: Document

    companion object {
        private val log by logger(CreateGraphQLRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateGraphQLRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         * @param id optional id to use if missed in XContent
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): CreateGraphQLRequest {
            var rawContent: String? = null
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    "content" -> rawContent = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: '$fieldName' while parsing CreateGraphQLRequest, ignoring")
                    }
                }
            }
            rawContent ?: throw IllegalArgumentException("Required field 'content' absent")
            return CreateGraphQLRequest(Parser.parse(rawContent))
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        TODO("not implemented")
    }

    /**
     * Constructor for creating the class
     * @param document A GraphQL Document describing a new object or schema addition
     */
    constructor(document: Document) {
        this.document = document
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        this.document = Parser.parse(input.readString())
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        TODO("not implemented")
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        TODO("not implemented")
    }
}
