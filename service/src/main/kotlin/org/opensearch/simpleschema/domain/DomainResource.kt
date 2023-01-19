package org.opensearch.simpleschema.domain

import org.opensearch.common.xcontent.ToXContentObject
import org.opensearch.common.xcontent.XContentBuilder
import org.opensearch.common.xcontent.ToXContent
import java.net.URLEncoder
import java.time.Instant

class DomainResource(val name: String, val entities: List<String>?) : ToXContentObject {
    val created: Instant = Instant.now()
    private val encodedName: String = URLEncoder.encode(name, "utf-8")

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
