package org.opensearch.simpleschema.domain

import org.opensearch.common.xcontent.XContentBuilder
import java.time.LocalDateTime
import java.net.URLEncoder

class Domain(val name: String) {
    val created: LocalDateTime = LocalDateTime.now()
    private val encodedName: String = URLEncoder.encode(name, "utf-8")

    fun toXContent(builder: XContentBuilder?): XContentBuilder {
        builder!!
        builder.startObject()
        builder.field("schema", name)
        builder.field("creation", created.toString())
        builder.startObject("links")
        builder.field("graphql", "/domain/$encodedName/schema/graphql")
        builder.field("index", "/domain/$encodedName/schema/index")
        builder.field("ontology", "/domain/$encodedName/schema/ontology")
        builder.endObject()
        return builder.endObject()
    }
}
