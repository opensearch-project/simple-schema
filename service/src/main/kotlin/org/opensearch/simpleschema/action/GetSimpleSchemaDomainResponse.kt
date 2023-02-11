package org.opensearch.simpleschema.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.common.xcontent.XContentParserUtils
import org.opensearch.commons.utils.fieldIfNotNull
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.model.BaseResponse
import org.opensearch.simpleschema.model.RestTag
import org.opensearch.simpleschema.model.SchemaCompilationType
import java.io.IOException

internal class GetSimpleSchemaDomainResponse : BaseResponse {


    private var objectId: String
    private var data: SchemaCompilationType

    /**
     * constructor for creating the class
     * @param id the id of the created Object
     */
    constructor(id: String, data: SchemaCompilationType) {
        this.objectId = id
        this.data = data
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readString()
        data = SchemaCompilationType(
            input.readString(),
            input.readStringList(),
            input.readOptionalString(),
            input.readOptionalStringList()
        )
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        output.writeString(objectId)
        output.writeString(data.name)
        output.writeStringArray(data.entities.toTypedArray())
        output.writeOptionalString(data.description)
        output.writeOptionalStringArray(data.catalog?.toTypedArray())
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        builder!!
        builder.startObject()
            .field(RestTag.OBJECT_ID_FIELD, objectId)
            .field(RestTag.NAME_FIELD, data.name)
            .field(RestTag.ENTITY_LIST_FIELD, data.entities)
            .fieldIfNotNull("description", data.description)
            .fieldIfNotNull("catalog", data.catalog)
        val domain = DomainRepository.getDomain(objectId)
        if (domain != null) {
            builder.field("domain")
            domain.toXContent(builder, params)
        }
        return builder.endObject()
    }
}
