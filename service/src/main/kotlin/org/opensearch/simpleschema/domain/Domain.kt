package org.opensearch.simpleschema.domain

import java.time.LocalDateTime
import java.util.UUID
import java.net.URLEncoder

class Domain(val name: String) {
    val created: LocalDateTime = LocalDateTime.now()
    private val encodedName: String = URLEncoder.encode(name, "utf-8")

    fun toJson(): String {
        return """
            {
                "schema": "${name.replace("\n", "\\n")}",
                "creation": "$created",
                "links": {
                    "graphql": "/domain/$encodedName/schema/graphql",
                    "index": "/domain/$encodedName/schema/index",
                    "ontology": "/domain/$encodedName/schema/ontology"
                }
            }
        """.trimIndent()
    }
}