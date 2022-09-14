/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.action.ValidateActions
import org.opensearch.common.Strings
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
import org.opensearch.simpleschema.model.SimpleSchemaObjectDataProperties
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import java.io.IOException

/**
 * Action request for creating new configuration.
 */
internal class UpdateSimpleSchemaObjectRequest : ActionRequest, ToXContentObject {
    val objectId: String
    val type: SimpleSchemaObjectType
    val objectData: BaseObjectData?

    companion object {
        private val log by logger(UpdateSimpleSchemaObjectRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { UpdateSimpleSchemaObjectRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         * @param id optional id to use if missed in XContent
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser, id: String? = null): UpdateSimpleSchemaObjectRequest {
            var objectId: String? = id
            var type: SimpleSchemaObjectType? = null
            var baseObjectData: BaseObjectData? = null

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
                        val objectTypeForTag = SimpleSchemaObjectType.fromTagOrDefault(fieldName)
                        if (objectTypeForTag != SimpleSchemaObjectType.NONE && baseObjectData == null) {
                            baseObjectData =
                                SimpleSchemaObjectDataProperties.createObjectData(objectTypeForTag, parser)
                            type = objectTypeForTag
                        } else {
                            parser.skipChildren()
                            log.info("Unexpected field: $fieldName, while parsing CreateObservabilityObjectRequest")
                        }
                    }
                }
            }
            objectId ?: throw IllegalArgumentException("$OBJECT_ID_FIELD field absent")
            type ?: throw IllegalArgumentException("Object type field absent")
            baseObjectData ?: throw IllegalArgumentException("Object data field absent")
            return UpdateSimpleSchemaObjectRequest(baseObjectData, type, objectId)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .fieldIfNotNull(OBJECT_ID_FIELD, objectId)
            .field(type.tag, objectData)
            .endObject()
    }

    /**
     * constructor for creating the class
     * @param objectData the Object
     * @param objectId optional id to use for Object
     */
    constructor(objectData: BaseObjectData, type: SimpleSchemaObjectType, objectId: String) {
        this.objectData = objectData
        this.type = type
        this.objectId = objectId
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readString()
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
        super.writeTo(output)
        output.writeString(objectId)
        output.writeEnum(type)
        output.writeEnum(type)
        output.writeOptionalWriteable(objectData)
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        var validationException: ActionRequestValidationException? = null
        if (Strings.isNullOrEmpty(objectId)) {
            validationException = ValidateActions.addValidationError("objectId is null or empty", validationException)
        }
        return validationException
    }
}
