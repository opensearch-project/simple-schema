/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.simpleschema.model.SimpleSchemaObjectDataProperties.getReaderForObjectType
import org.opensearch.simpleschema.model.RestTag.ACCESS_LIST_FIELD
import org.opensearch.simpleschema.model.RestTag.CREATED_TIME_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.simpleschema.model.RestTag.TENANT_FIELD
import org.opensearch.simpleschema.model.RestTag.UPDATED_TIME_FIELD
import org.opensearch.simpleschema.security.UserAccessManager
import java.io.IOException
import java.time.Instant

/**
 * Data class representing Object.
 */
data class SimpleSchemaObjectDoc(
    val objectId: String,
    val updatedTime: Instant,
    val createdTime: Instant,
    val tenant: String,
    val access: List<String>, // "User:user", "Role:sample_role", "BERole:sample_backend_role"
    val type: SimpleSchemaObjectType,
    val objectData: BaseObjectData?
) : BaseModel {

    companion object {
        private val log by logger(SimpleSchemaObjectDoc::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SimpleSchemaObjectDoc(it) }

        /**
         * Parse the data from parser and create object
         * @param parser data referenced at parser
         * @return created object
         */
        @JvmStatic
        @Throws(IOException::class)
        @Suppress("ComplexMethod")
        fun parse(parser: XContentParser, useId: String? = null): SimpleSchemaObjectDoc {
            var objectId: String? = useId
            var updatedTime: Instant? = null
            var createdTime: Instant? = null
            var tenant: String? = null
            var access: List<String> = listOf()
            var type: SimpleSchemaObjectType? = null
            var objectData: BaseObjectData? = null

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
                    UPDATED_TIME_FIELD -> updatedTime = Instant.ofEpochMilli(parser.longValue())
                    CREATED_TIME_FIELD -> createdTime = Instant.ofEpochMilli(parser.longValue())
                    TENANT_FIELD -> tenant = parser.text()
                    ACCESS_LIST_FIELD -> access = parser.stringList()
                    else -> {
                        val objectTypeForTag = SimpleSchemaObjectType.fromTagOrDefault(fieldName)
                        if (objectTypeForTag != SimpleSchemaObjectType.NONE && objectData == null) {
                            objectData =
                                SimpleSchemaObjectDataProperties.createObjectData(objectTypeForTag, parser)
                            type = objectTypeForTag
                        } else {
                            parser.skipChildren()
                            log.info("Unexpected field: $fieldName, while parsing ObjectDoc")
                        }
                    }
                }
            }
            objectId ?: throw IllegalArgumentException("$OBJECT_ID_FIELD field absent")
            updatedTime ?: throw IllegalArgumentException("$UPDATED_TIME_FIELD field absent")
            createdTime ?: throw IllegalArgumentException("$CREATED_TIME_FIELD field absent")
            tenant = tenant ?: UserAccessManager.DEFAULT_TENANT
            type ?: throw IllegalArgumentException("Object type field absent")
            objectData ?: throw IllegalArgumentException("Object data field absent")
            return SimpleSchemaObjectDoc(objectId, updatedTime, createdTime, tenant, access, type, objectData)
        }
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        objectId = input.readString(),
        updatedTime = input.readInstant(),
        createdTime = input.readInstant(),
        tenant = input.readString(),
        access = input.readStringList(),
        type = input.readEnum(SimpleSchemaObjectType::class.java),
        objectData = input.readOptionalWriteable(getReaderForObjectType(input.readEnum(SimpleSchemaObjectType::class.java)))
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(objectId)
        output.writeInstant(updatedTime)
        output.writeInstant(createdTime)
        output.writeString(tenant)
        output.writeStringCollection(access)
        output.writeEnum(type)
        output.writeEnum(type) // type is read twice in constructor
        output.writeOptionalWriteable(objectData)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
        if (params?.paramAsBoolean(OBJECT_ID_FIELD, false) == true) {
            builder.field(OBJECT_ID_FIELD, objectId)
        }
        builder.field(UPDATED_TIME_FIELD, updatedTime.toEpochMilli())
            .field(CREATED_TIME_FIELD, createdTime.toEpochMilli())
            .field(TENANT_FIELD, tenant)
        if (params?.paramAsBoolean(ACCESS_LIST_FIELD, true) == true && access.isNotEmpty()) {
            builder.field(ACCESS_LIST_FIELD, access)
        }
        builder.field(type.tag, objectData)
            .endObject()
        return builder
    }
}
