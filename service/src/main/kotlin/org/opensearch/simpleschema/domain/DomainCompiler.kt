package org.opensearch.simpleschema.domain

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.SimpleSchemaActions
import org.opensearch.simpleschema.model.SchemaCompilationType
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import java.lang.IllegalArgumentException

object DomainCompiler {
    /**
     * Provided a SimpleSchemaObjectDoc, this method generates the compiled Domain object.
     * If compilation is accessible, the Domain is accessible through the DomainRepository.
     * Otherwise, an exception will be thrown.
     *
     * The provided User object should have access to the entities being referenced by the SimpleSchemaObjectDoc.
     */
    fun compile(objectDoc: SimpleSchemaObjectDoc, user: User?) {
        if (objectDoc.type != SimpleSchemaObjectType.SCHEMA_DOMAIN) {
            throw IllegalArgumentException("Attempted to domain-compile a non-domain object doc: " +
                "expected type ${SimpleSchemaObjectType.SCHEMA_DOMAIN} but got ${objectDoc.type}")
        }
        val compilationData = objectDoc.objectData as SchemaCompilationType
        val entityData = getEntityData(compilationData.entities, user)
        if (entityData.keys != compilationData.entities.toSet()) {
            throw OpenSearchStatusException(
                "Compilation failed: One or more required entities could not be found.",
                RestStatus.NOT_FOUND
            )
        }
        // TODO: generating domain resource,
        //  but skipping compilation until more details about the expected artifact are known
        val domain = DomainResource(compilationData.name, compilationData.entities)
        DomainRepository.createDomain(domain)
    }

    private fun getEntityData(entities: List<String>, user: User?): Map<String, SimpleSchemaObjectDoc> {
        return SimpleSchemaActions
            .get(GetSimpleSchemaObjectRequest(entities.toSet()), user)
            .searchResult
            .objectList
            .associateBy { it.objectId }
    }
}
