package org.opensearch.simpleschema.model

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentFactory
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.util.fieldIfNotNull
import org.opensearch.simpleschema.util.logger
import org.opensearch.simpleschema.util.stringList

/**
 * This element represents a schema entity which is stored as a document in the DB
 * The entity has a type, name, catalog, description and the actual GraphQL SDL as content
 * The catalog represent the general belonging of this specific entity - this entity may belong to multiple catalogs
 */
internal data class SchemaCompilationType(
    val type: String,
    val name: String?,//nullable
    val description: String?,//nullable
    val catalog: List<String>?,//nullable
    val content: String,
) : BaseObjectData {

    internal companion object {
        private val log by logger(SchemaCompilationType::class.java)
        private const val TYPE_TAG = "type"
        private const val NAME_TAG = "name"
        private const val DESCRIPTION_TAG = "description"
        private const val CATALOG_TAG = "catalog"
        private const val CONTENT_TAG = "content"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { SchemaCompilationType(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create SchemaCompilation
         * @param parser data referenced at parser
         * @return created SchemaCompilation object
         */
        fun parse(parser: XContentParser): SchemaCompilationType {
            var type = "Undefined"
            var content = "{}"
            var description: String? = null //nullable
            var catalog: List<String>? = null //nullable
            var name: String? = null //nullable
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
            return SchemaCompilationType(type, name, description, catalog, content)
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
        name = input.readOptionalString(), //nullable
        description = input.readOptionalString(), //nullable
        catalog = input.readOptionalStringList(), //nullable
        content = input.readString(),
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeOptionalString(name) //nullable
        output.writeOptionalString(description) //nullable
        output.writeOptionalStringCollection(catalog) //nullable
        output.writeString(content)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(TYPE_TAG, type)
            .fieldIfNotNull(NAME_TAG, name) //nullable
            .fieldIfNotNull(DESCRIPTION_TAG, description) //nullable
            .fieldIfNotNull(CATALOG_TAG, catalog) //nullable
            .field(CONTENT_TAG, content)
        return builder.endObject()
    }
}
