package org.opensearch.simpleschema.action

import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionRequestValidationException
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.logger
import org.opensearch.commons.utils.stringList
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.domain.DomainResource
import org.opensearch.simpleschema.model.RestTag
import org.opensearch.simpleschema.model.SchemaCompilationType
import java.io.IOException

internal class CreateSimpleSchemaDomainRequest : ActionRequest, ToXContentObject {
    val name: String
    val entities: List<String>

    companion object {
        private val log by logger(CreateSimpleSchemaDomainRequest::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateSimpleSchemaDomainRequest(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): CreateSimpleSchemaDomainRequest {
            var name: String? = null
            var entities: List<String>? = null

            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (parser.nextToken() != XContentParser.Token.END_OBJECT) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    RestTag.NAME_FIELD -> name = parser.text()
                    RestTag.ENTITY_LIST_FIELD -> entities = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("Unexpected field: $fieldName, while parsing CreateDomainRequest")
                    }
                }
            }
            name ?: throw IllegalArgumentException("Required field '${RestTag.NAME_FIELD}' is absent")
            entities ?: throw IllegalArgumentException("Required field '${RestTag.ENTITY_LIST_FIELD}' is absent")
            return CreateSimpleSchemaDomainRequest(name, entities)
        }
    }

    constructor(name: String, entities: List<String>) {
        this.name = name
        this.entities = entities
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        name = input.readString()
        entities = input.readStringList()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        // TODO currently no validation
        return null
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(RestTag.NAME_FIELD, name)
            .field(RestTag.ENTITY_LIST_FIELD, entities)
        val domain = DomainRepository.getDomain(name)
        if (domain != null) {
            builder.field("domain")
            domain.toXContent(builder, params)
        }
        return builder.endObject()
    }

    fun toObjectData(): SchemaCompilationType {
        return SchemaCompilationType(name, entities, null, null)
    }
}
