package org.opensearch.simpleschema.domain

import org.opensearch.OpenSearchStatusException
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType

class SchemaCompiler {
    fun compile(objectDoc: SimpleSchemaObjectDoc) {
        if (objectDoc.type != SimpleSchemaObjectType.SCHEMA_COMPILATION) {
            throw OpenSearchStatusException(
                "Attempted to compile a schema document of the wrong type: " +
                    "expected `${SimpleSchemaObjectType.SCHEMA_COMPILATION}` but got `${objectDoc.type}`",
                RestStatus.INTERNAL_SERVER_ERROR
            )
        }
        val domain = Domain(objectDoc.objectId)
        DomainRepository.createDomain(domain)
    }
}
