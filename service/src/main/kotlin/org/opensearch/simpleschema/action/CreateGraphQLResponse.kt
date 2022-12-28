package org.opensearch.simpleschema.action

import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.model.BaseResponse
import java.io.IOException

internal class CreateGraphQLResponse : BaseResponse {
    val objectId: String

    companion object {
        private val log by logger(CreateGraphQLResponse::class.java)

        /**
         * reader to create instance of class from writable.
         */
        val reader = Writeable.Reader { CreateGraphQLResponse(it) }

        /**
         * Creator used in REST communication.
         * @param parser XContentParser to deserialize data from.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun parse(parser: XContentParser): CreateGraphQLResponse {
            TODO("not implemented")
        }
    }

    /**
     * constructor for creating the class
     * @param id the id of the created Object
     */
    constructor(id: String) {
        this.objectId = id
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    constructor(input: StreamInput) : super(input) {
        objectId = input.readString()
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        TODO("not implemented")
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        TODO("not implemented")
    }
}
