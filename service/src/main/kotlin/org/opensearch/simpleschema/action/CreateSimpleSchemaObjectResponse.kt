/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.model.BaseResponse
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import java.io.IOException

/**
 * Action Response for creating new configuration.
 */
internal class CreateSimpleSchemaObjectResponse : BaseResponse {
    val objectId: String

    companion object {
        private val log by logger(CreateSimpleSchemaObjectResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateSimpleSchemaObjectResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): CreateSimpleSchemaObjectResponse {
            var objectId: String? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    OBJECT_ID_FIELD -> objectId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing CreateObjectResponse")
                    }
                }
            }
            objectId ?: throw IllegalArgumentException("$OBJECT_ID_FIELD field absent")
            return CreateSimpleSchemaObjectResponse(objectId)
        }
    }

    /**
     * constructor for creating the class
     * @param id the id of the created Object
     */
    constructor(id: String) {
        this.objectId = id
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(objectId)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(OBJECT_ID_FIELD, objectId)
            .endObject()
    }
}
