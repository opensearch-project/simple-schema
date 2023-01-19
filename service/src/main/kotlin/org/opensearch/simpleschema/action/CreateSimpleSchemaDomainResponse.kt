package org.opensearch.simpleschema.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.model.BaseResponse
import org.opensearch.simpleschema.model.RestTag
import java.io.IOException

internal class CreateSimpleSchemaDomainResponse : BaseResponse {
    private var objectId: String
    private var entities: List<String>

    /**
     * constructor for creating the class
     * @param id the id of the created Object
     */
    constructor(id: String, entities: List<String>) {
        this.objectId = id
        this.entities = entities
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readString()
        entities = input.readStringList()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(objectId)
        output.writeStringArray(entities.toTypedArray())
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        return builder.startObject()
            .field(RestTag.OBJECT_ID_FIELD, objectId)
            .field(RestTag.ENTITY_LIST_FIELD, entities)
            .endObject()
    }
}