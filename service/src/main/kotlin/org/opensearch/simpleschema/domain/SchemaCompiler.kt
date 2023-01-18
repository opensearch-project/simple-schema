package org.opensearch.simpleschema.domain

import org.opensearch.OpenSearchStatusException
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType

class SchemaCompiler {
    // TODO fix
    fun compile(objectDoc: SimpleSchemaObjectDoc) {
        val domain = Domain(objectDoc.objectId)
        DomainRepository.createDomain(domain)
    }
}
