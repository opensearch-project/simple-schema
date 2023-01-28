package org.opensearch.simpleschema.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.domain.DomainCompiler
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
            "ignore",
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            SimpleSchemaObjectType.SCHEMA_DOMAIN,
            requestObjectData
        )
        // TODO unaddressed edge case where compilation is successful but storage fails
        DomainCompiler.compile(objectDoc, user)
        val docId = SimpleSearchIndex.createSimpleSchemaObject(objectDoc)
        docId ?: throw OpenSearchStatusException(
            "Object creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        return CreateSimpleSchemaDomainResponse(docId, requestObjectData.name, requestObjectData.entities)
    }

    fun get(request: GetSimpleSchemaDomainRequest, user: User?): GetSimpleSchemaDomainResponse {
        log.info("${SimpleSchemaPlugin.LOG_PREFIX}:SimpleSchemaDomain-get")
        UserAccessManager.validateUser(user)
        val result = SimpleSearchIndex.getSimpleSchemaObject(request.objectId)
        result ?: throw OpenSearchStatusException(
            "Schema not found",
            RestStatus.NOT_FOUND
        )
        return GetSimpleSchemaDomainResponse(result.simpleSchemaObjectDoc.objectId, result.simpleSchemaObjectDoc.objectData as SchemaCompilationType)
    }


}
