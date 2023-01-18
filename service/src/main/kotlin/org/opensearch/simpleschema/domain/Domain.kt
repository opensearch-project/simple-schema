package org.opensearch.simpleschema.domain

import org.opensearch.common.io.stream.StreamOutput
import org.opensearch.common.xcontent.ToXContent
import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.simpleschema.model.BaseObjectData
import java.net.URLEncoder
import java.time.Instant

class Domain(val name: String, val entities: List<String>) : ToXContentObject, BaseObjectData {
    val created: Instant = Instant.now()
    private val encodedName: String = URLEncoder.encode(name, "utf-8")

    override fun writeTo(out: StreamOutput) {
        out.writeString(name)
        out.writeInstant(created)
        out.writeStringArray(entities.toTypedArray())
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
