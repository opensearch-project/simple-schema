package org.opensearch.simpleschema.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.*
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.model.RestTag
import java.io.IOException

internal class GetSimpleSchemaDomainRequest : ActionRequest, ToXContentObject {
    val objectId: String

    companion object {
        private val log by logger(GetSimpleSchemaDomainRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { GetSimpleSchemaDomainRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): GetSimpleSchemaDomainRequest {
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
                    RestTag.OBJECT_ID_FIELD -> objectId = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing CreateDomainRequest")
                    }
                }
            }
            objectId ?: throw IllegalArgumentException("${RestTag.OBJECT_ID_FIELD} field absent")
            return GetSimpleSchemaDomainRequest(objectId)
        }
    }

    constructor(objectId: String) {
        this.objectId = objectId
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
    override fun validate(): ActionRequestValidationException? {
        return null
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(RestTag.OBJECT_ID_FIELD, objectId)
            .endObject()
    }
}