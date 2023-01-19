package org.opensearch.simpleschema.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.domain.DomainResource
import org.opensearch.simpleschema.index.SimpleSearchIndex
import org.opensearch.simpleschema.model.SchemaCompilationType
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import org.opensearch.simpleschema.security.UserAccessManager
import org.opensearch.simpleschema.util.logger
import java.lang.IllegalArgumentException
import java.time.Instant

internal object SimpleSchemaDomainActions {
    private val log by logger(SimpleSchemaDomainActions::class.java)

    fun create(request: CreateSimpleSchemaDomainRequest, user: User?): CreateSimpleSchemaDomainResponse {
        log.info("${SimpleSchemaPlugin.LOG_PREFIX}:SimpleSchemaDomain-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val requestObjectData = request.toObjectData()
        val objectDoc = SimpleSchemaObjectDoc(
            request.objectId,
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            SimpleSchemaObjectType.SCHEMA_DOMAIN,
            requestObjectData
        )
        if (SimpleSearchIndex.getSimpleSchemaObject(request.objectId) != null) {
            log.info("attempted to recreate existing schema: ${request.objectId}")
            throw OpenSearchStatusException(
                "Creation failed: Schema ID already exists",
                RestStatus.BAD_REQUEST
            )
        }
        // TODO unaddressed edge case where compilation is successful but storage fails
        compile(objectDoc, user)
        val docId = SimpleSearchIndex.createSimpleSchemaObject(objectDoc, request.objectId)
        docId ?: throw OpenSearchStatusException(
            "Object creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        return CreateSimpleSchemaDomainResponse(docId, requestObjectData.entities)
    }

    fun get(request: GetSimpleSchemaDomainRequest, user: User?): GetSimpleSchemaDomainResponse {
        log.info("${SimpleSchemaPlugin.LOG_PREFIX}:SimpleSchemaDomain-get")
        UserAccessManager.validateUser(user)
        val result = SimpleSearchIndex.getSimpleSchemaObject(request.objectId)
        result ?: throw OpenSearchStatusException(
            "Schema not found",
            RestStatus.NOT_FOUND
        )
        return GetSimpleSchemaDomainResponse(result.simpleSchemaObjectDoc.objectId)
    }

    private fun compile(objectDoc: SimpleSchemaObjectDoc, user: User?) {
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
