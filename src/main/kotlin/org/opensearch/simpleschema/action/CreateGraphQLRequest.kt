package org.opensearch.simpleschema.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
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
import java.io.IOException

internal class CreateGraphQLRequest : ActionRequest, ToXContentObject {
    val objectId: String?
    val type: SimpleSchemaObjectType
    val objectData: BaseObjectData?

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
        fun parse(parser: XContentParser, id: String? = null): CreateGraphQLRequest {
            TODO("not implemented")
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        TODO("not implemented")
    }

    /**
     * constructor for creating the class
     * @param objectId optional id to use for Object
     * @param type type of Object
     * @param objectData the Object
     */
    constructor(objectId: String? = null, type: SimpleSchemaObjectType, objectData: BaseObjectData) {
        this.objectId = objectId
        this.type = type
        this.objectData = objectData
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readOptionalString()
        type = input.readEnum(SimpleSchemaObjectType::class.java)
        objectData = input.readOptionalWriteable(
            SimpleSchemaObjectDataProperties.getReaderForObjectType(
                input.readEnum(
                    SimpleSchemaObjectType::class.java
                )
            )
        )
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
