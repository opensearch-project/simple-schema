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
 * This element represents a physical indices' entity which is stored as a document in the DB
 * The entity has a type, name, description, ontology and the actual content field which contains the Index-Provider SDL Json
 * The indices represent list of indices that are described in this Physical index-provider
 * physical
 */
internal data class IndexProvider(
    val type: String,
    val name: String?,//nullable
    val description: String?,//nullable
    val indices: List<String>?,//nullable
    val ontology: String,
    val content: String,
) : BaseObjectData {

    internal companion object {
        private val log by logger(IndexProvider::class.java)
        private const val TYPE_TAG = "type"
        private const val NAME_TAG = "name"
        private const val DESCRIPTION_TAG = "description"
        private const val INDICES_TAG = "indices"
        private const val ONTOLOGY_TAG = "ontology"
        private const val CONTENT_TAG = "content"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { IndexProvider(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create SchemaEntity
         * @param parser data referenced at parser
         * @return created SchemaEntity object
         */
        fun parse(parser: XContentParser): IndexProvider {
            var type = "Undefined"
            var name: String? = null //nullable
            var description: String? = null //nullable
            var indices: List<String>? = null //nullable
            var ontology = "Undefined"
            var content = "{}"
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
                    INDICES_TAG -> indices = parser.stringList()
                    ONTOLOGY_TAG -> ontology = parser.text()
                    CONTENT_TAG -> content = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:SchemaType Skipping Unknown field $fieldName")
                    }
                }
            }
            return IndexProvider(type, name, description, indices, ontology, content)
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
        name = input.readOptionalString(),//nullable
        description = input.readOptionalString(),//nullable
        indices = input.readOptionalStringList(),//nullable
        ontology = input.readString(),
        content = input.readString(),
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeOptionalString(name) //nullable
        output.writeOptionalString(description) //nullable
        output.writeOptionalStringCollection(indices) //nullable
        output.writeString(ontology)
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
            .fieldIfNotNull(INDICES_TAG, indices) //nullable
            .field(ONTOLOGY_TAG, ontology)
            .field(CONTENT_TAG, content)
        return builder.endObject()
    }
}
