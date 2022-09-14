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
 * This element represents an ontology entity which is stored as a document in the DB
 * The ontology has a type, name, description and the actual ontology json SDL as content
 * The namespace represent the general belonging of this ontology - this ontology may belong to multiple namespaces
 */
internal data class Ontology(
    val type: String,
    val name: String,
    val description: String?,
    val namespace: List<String>?,
    val content: String,
) : BaseObjectData {

    internal companion object {
        private val log by logger(Ontology::class.java)
        private const val TYPE_TAG = "type"
        private const val NAME_TAG = "name"
        private const val DESCRIPTION_TAG = "description"
        private const val NAMESPACE_TAG = "namespace"
        private const val CONTENT_TAG = "content"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Ontology(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create Ontology object
         * @param parser data referenced at parser
         * @return created Ontology object
         */
        fun parse(parser: XContentParser): Ontology {
            var type = "Undefined"
            var name = "Undefined"
            var content = "{}"
            var description: String? = null
            var namespace: List<String>? = null

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
                    NAMESPACE_TAG -> namespace = parser.stringList()
                    CONTENT_TAG -> content = parser.text()
                    else -> {
                        parser.skipChildren()
                        log.info("$LOG_PREFIX:Ontology Skipping Unknown field $fieldName")
                    }
                }
            }
            return Ontology(type, name, description,namespace,content)
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
        namespace = input.readStringList(),
        content = input.readString(),
    )

    /**
     * {@inheritDoc}
     */
    override fun writeTo(output: StreamOutput) {
        output.writeString(type)
        output.writeString(name)
        output.writeString(description)
        output.writeStringCollection(namespace)
        output.writeString(content)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(TYPE_TAG, type)
            .field(NAME_TAG, name)
            .fieldIfNotNull(DESCRIPTION_TAG, description)
            .fieldIfNotNull(NAMESPACE_TAG, content)
            .fieldIfNotNull(CONTENT_TAG, content)
        return builder.endObject()
    }
}
