/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParser.Token
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.model.BaseResponse
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.simpleschema.util.logger
import java.io.IOException

/**
 * Object-update response.
 * <pre> JSON format
 * {@code
 * {
 *   "objectId":"objectId"
 * }
 * }</pre>
 */
internal class UpdateSimpleSchemaObjectResponse(
    val objectId: String?
) : BaseResponse() {

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(
        objectId = input.readString()
    )

    companion object {
        private val log by logger(UpdateSimpleSchemaObjectResponse::class.java)

        /**
         * Parse the data from parser and create [UpdateSimpleSchemaObjectResponse] object
         * @param parser data referenced at parser
         * @return created [UpdateSimpleSchemaObjectResponse] object
         */
        fun parse(parser: XContentParser): UpdateSimpleSchemaObjectResponse {
            var objectId: String? = null
            XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
            while (Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    OBJECT_ID_FIELD -> objectId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                    }
                }
            }
            objectId ?: throw IllegalArgumentException("$OBJECT_ID_FIELD field absent")
            return UpdateSimpleSchemaObjectResponse(objectId)
        }
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
        return builder!!.startObject()
            .field(OBJECT_ID_FIELD, objectId)
            .endObject()
    }
}
