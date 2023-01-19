package org.opensearch.simpleschema.domain

import org.opensearch.simpleschema.model.SchemaCompilationType
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import java.lang.IllegalArgumentException

class SchemaCompiler {
    // TODO fix
    fun compile(objectDoc: SimpleSchemaObjectDoc) {
        if (objectDoc.type != SimpleSchemaObjectType.SCHEMA_DOMAIN) {
            throw IllegalArgumentException("Attempted to domain-compile a non-domain object doc: " +
                "expected type ${SimpleSchemaObjectType.SCHEMA_DOMAIN} but got ${objectDoc.type}")
        }
        val compilationData = objectDoc.objectData as SchemaCompilationType
        val domain = DomainResource(compilationData.name!!, compilationData.entities)
        DomainRepository.createDomain(domain)
    }
}
