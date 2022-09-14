package org.opensearch.simpleschema.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.*
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.util.fieldIfNotNull
import org.opensearch.simpleschema.util.logger
import org.opensearch.simpleschema.util.stringList

/**
 * This element represents a schema entity which is stored as a document in the DB
 * The entity has a type, name, catalog, description and the actual GraphQL SDL as content
 * The catalog represent the general belonging of this specific entity - this entity may belong to multiple catalogs
 */
internal data class SchemaEntityType(
    val type: String,
    val name: String?,
    val description: String?,
    val catalog: List<String>?,
    val content: String,
) : BaseObjectData {

    internal companion object {
        private val log by logger(SchemaEntityType::class.java)
        private const val TYPE_TAG = "type"
        private const val NAME_TAG = "name"
        private const val DESCRIPTION_TAG = "description"
        private const val CATALOG_TAG = "catalog"
        private const val CONTENT_TAG = "content"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SchemaEntityType(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create SchemaEntity
         * @param parser data referenced at parser
         * @return created SchemaEntity object
         */
        fun parse(parser: XContentParser): SchemaEntityType {
            var type = "Undefined"
            var content = "{}"
            var description: String? = null
            var catalog: List<String>? = null
            var name: String? = null
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    TYPE_TAG -> type = parser.text()
                    NAME_TAG -> name = parser.text()
                    DESCRIPTION_TAG -> description = parser.text()
                    CATALOG_TAG -> catalog = parser.stringList()
                    CONTENT_TAG -> content = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:SchemaType Skipping Unknown field $fieldName")
                    }
                }
            }
            return SchemaEntityType(type, name, description,catalog,content)
        }
    }


    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        type = input.readString(),
        name = input.readString(),
        description = input.readString(),
        catalog = input.readStringList(),
        content = input.readString(),
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeString(name)
        output.writeString(description)
        output.writeStringCollection(catalog)
        output.writeString(content)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .fieldIfNotNull(TYPE_TAG, type)
            .fieldIfNotNull(NAME_TAG, name)
            .fieldIfNotNull(DESCRIPTION_TAG, description)
            .fieldIfNotNull(CATALOG_TAG, catalog)
            .fieldIfNotNull(CONTENT_TAG, content)
        return builder.endObject()
    }
}
