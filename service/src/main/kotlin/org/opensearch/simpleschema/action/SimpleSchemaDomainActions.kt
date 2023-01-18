package org.opensearch.simpleschema.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.domain.SchemaCompiler
import org.opensearch.simpleschema.index.SimpleSearchIndex
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import org.opensearch.simpleschema.security.UserAccessManager
import org.opensearch.simpleschema.util.logger
import java.time.Instant

internal object SimpleSchemaDomainActions {
    private val log by logger(SimpleSchemaDomainActions::class.java)

    fun create(request: CreateSimpleSchemaDomainRequest, user: User?): CreateSimpleSchemaDomainResponse {
        log.info("${SimpleSchemaPlugin.LOG_PREFIX}:SimpleSchemaDomain-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val objectDoc = SimpleSchemaObjectDoc(
            request.objectId,
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            SimpleSchemaObjectType.SCHEMA_DOMAIN,
            request.entitiesAsObjectData()
        )
        if (SimpleSearchIndex.getSimpleSchemaObject(request.objectId) != null) {
            log.info("attempted to recreate existing schema: ${request.objectId}")
            throw OpenSearchStatusException(
                "Creation failed: Schema ID already exists",
                RestStatus.BAD_REQUEST
            )
        }
        val docId = SimpleSearchIndex.createSimpleSchemaObject(objectDoc, request.objectId)
        docId ?: throw OpenSearchStatusException(
            "Object creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        SchemaCompiler().compile(objectDoc)
        return CreateSimpleSchemaDomainResponse(docId)
    }
}
