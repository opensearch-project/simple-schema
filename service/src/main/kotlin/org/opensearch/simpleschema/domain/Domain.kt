package org.opensearch.simpleschema.domain

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.*
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.model.BaseObjectData
import org.opensearch.simpleschema.model.RestTag
import org.opensearch.simpleschema.model.SchemaEntityType
import org.opensearch.simpleschema.model.XParser
import org.opensearch.simpleschema.util.logger
import org.opensearch.simpleschema.util.stringList
import java.net.URLEncoder
import java.time.Instant

class Domain(val name: String, val entities: List<String>?) : ToXContentObject, BaseObjectData {
    val created: Instant = Instant.now()
    private val encodedName: String = URLEncoder.encode(name, "utf-8")

    internal companion object {
        private val log by logger(Domain::class.java)
        private const val NAME_TAG = "objectId"
        private const val ENTITIES_TAG = "entities"

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { Domain(it) }

        /**
         * Parser to parse xContent
         */
        val xParser = XParser { parse(it) }

        /**
         * Parse the data from parser and create SchemaEntity
         * @param parser data referenced at parser
         * @return created SchemaEntity object
         */
        fun parse(parser: XContentParser): Domain {
            /* TODO: this is called as part of handling a get request, but it's not clear why.
             * The data is always empty.
             * Attempts to parse a Domain anyways for now, until there's enough time to avoid the parse call entirely.
             */
            var name: String? = "" //nullable
            var entities: List<String>? = null // nullable
            XContentParserUtils.ensureExpectedToken(
                XContentParser.Token.START_OBJECT,
                parser.currentToken(),
                parser
            )
            while (XContentParser.Token.END_OBJECT != parser.nextToken()) {
                val fieldName = parser.currentName()
                parser.nextToken()
                when (fieldName) {
                    NAME_TAG -> name = parser.text()
                    ENTITIES_TAG -> entities = parser.stringList()
                    else -> {
                        parser.skipChildren()
                        log.info("${SimpleSchemaPlugin.LOG_PREFIX}:SchemaType Skipping Unknown field $fieldName")
                    }
                }
            }
            name ?:  throw IllegalArgumentException("$NAME_TAG field absent")
            return Domain(name, entities)
        }
    }

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : this(
        name = input.readString(),
        entities = input.readOptionalStringList()
    )

    override fun writeTo(out: StreamOutput) {
        out.writeString(name)
        out.writeInstant(created)
        out.writeStringArray(entities?.toTypedArray())
    }

    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params): XContentBuilder {
        builder!!
        builder.startObject()
        builder.field("schema", name)
        builder.field("creation", created.toString())
        builder.field("entities", entities)
        builder.startObject("links")
        builder.field("graphql", "/domain/$encodedName/schema/graphql")
        builder.field("index", "/domain/$encodedName/schema/index")
        builder.field("ontology", "/domain/$encodedName/schema/ontology")
        builder.endObject()
        return builder.endObject()
    }
}
