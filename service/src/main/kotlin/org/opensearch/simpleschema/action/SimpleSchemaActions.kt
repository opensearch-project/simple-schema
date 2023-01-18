/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.opensearch.OpenSearchStatusException
import org.opensearch.commons.authuser.User
import org.opensearch.simpleschema.index.SimpleSearchIndex
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectSearchResult
import org.opensearch.simpleschema.security.UserAccessManager
import org.opensearch.simpleschema.util.logger
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.domain.DomainRepository
import org.opensearch.simpleschema.domain.SchemaCompiler
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import java.time.Instant

/**
 * Object index operation actions.
 */
internal object SimpleSchemaActions {
    private val log by logger(SimpleSchemaActions::class.java)

    /**
     * Create new Object
     * @param request [CreateSimpleSchemaObjectRequest] object
     * @return [CreateSimpleSchemaObjectResponse]
     */
    fun create(request: CreateSimpleSchemaObjectRequest, user: User?): CreateSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-create")
        UserAccessManager.validateUser(user)
        val currentTime = Instant.now()
        val objectDoc = SimpleSchemaObjectDoc(
            "ignore",
            currentTime,
            currentTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            request.type,
            request.objectData
        )
        val docId = SimpleSearchIndex.createSimpleSchemaObject(objectDoc, request.objectId)
        docId ?: throw OpenSearchStatusException(
            "Object Creation failed",
            RestStatus.INTERNAL_SERVER_ERROR
        )
        // TODO move
//        if (objectDoc.type == SimpleSchemaObjectType.SCHEMA_COMPILATION) {
//            SchemaCompiler().compile(objectDoc)
//        }
        return CreateSimpleSchemaObjectResponse(docId)
    }

    /**
     * Update Object
     * @param request [UpdateSimpleSchemaObjectRequest] object
     * @return [UpdateSimpleSchemaObjectResponse]
     */
    fun update(request: UpdateSimpleSchemaObjectRequest, user: User?): UpdateSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-update ${request.objectId}")
        UserAccessManager.validateUser(user)
        val objectDocInfo = SimpleSearchIndex.getSimpleSchemaObject(request.objectId)
        objectDocInfo
            ?: throw OpenSearchStatusException(
                "SimpleSchemaObject ${request.objectId} not found",
                RestStatus.NOT_FOUND
            )
        val currentDoc = objectDocInfo.simpleSchemaObjectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException(
                "Permission denied for Object ${request.objectId}",
                RestStatus.FORBIDDEN
            )
        }
        if (currentDoc.type != request.type) {
            throw OpenSearchStatusException("Object type cannot be changed after creation", RestStatus.CONFLICT)
        }
        val currentTime = Instant.now()
        val objectDoc = SimpleSchemaObjectDoc(
            request.objectId,
            currentTime,
            currentDoc.createdTime,
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getAllAccessInfo(user),
            request.type,
            request.objectData
        )
        if (!SimpleSearchIndex.updateSimpleSchemaObject(request.objectId, objectDoc)) {
            throw OpenSearchStatusException("Object Update failed", RestStatus.INTERNAL_SERVER_ERROR)
        }
        return UpdateSimpleSchemaObjectResponse(request.objectId)
    }

    /**
     * Get Object info
     * @param request [GetSimpleSchemaObjectRequest] object
     * @return [GetSimpleSchemaObjectResponse]
     */
    fun get(request: GetSimpleSchemaObjectRequest, user: User?): GetSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-get ${request.objectIds}")
        UserAccessManager.validateUser(user)
        return when (request.objectIds.size) {
            0 -> getAll(request, user)
            1 -> info(request.objectIds.first(), user)
            else -> info(request.objectIds, user)
        }
    }

    /**
     * Get Object info
     * @param objectId object id
     * @param user the user info object
     * @return [GetSimpleSchemaObjectResponse]
     */
    private fun info(objectId: String, user: User?): GetSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-info $objectId")
        val objectDocInfo = SimpleSearchIndex.getSimpleSchemaObject(objectId)
        objectDocInfo
            ?: run {
                throw OpenSearchStatusException("Object $objectId not found", RestStatus.NOT_FOUND)
            }
        val currentDoc = objectDocInfo.simpleSchemaObjectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException("Permission denied for Object $objectId", RestStatus.FORBIDDEN)
        }
        val docInfo = SimpleSchemaObjectDoc(
            objectId,
            currentDoc.updatedTime,
            currentDoc.createdTime,
            currentDoc.tenant,
            currentDoc.access,
            currentDoc.type,
            currentDoc.objectData
        )
        return GetSimpleSchemaObjectResponse(
            SimpleSchemaObjectSearchResult(docInfo),
            UserAccessManager.hasAllInfoAccess(user)
        )
    }

    /**
     * Get Object info
     * @param objectIds object id set
     * @param user the user info object
     * @return [GetSimpleSchemaObjectResponse]
     */
    private fun info(objectIds: Set<String>, user: User?): GetSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-info $objectIds")
        val objectDocs = SimpleSearchIndex.getSimpleSchemaObjects(objectIds)
        if (objectDocs.size != objectIds.size) {
            val mutableSet = objectIds.toMutableSet()
            objectDocs.forEach { mutableSet.remove(it.id) }
            throw OpenSearchStatusException(
                "Object $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        objectDocs.forEach {
            val currentDoc = it.simpleSchemaObjectDoc
            if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
                throw OpenSearchStatusException(
                    "Permission denied for Object ${it.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val configSearchResult = objectDocs.map {
            SimpleSchemaObjectDoc(
                it.id!!,
                it.simpleSchemaObjectDoc.updatedTime,
                it.simpleSchemaObjectDoc.createdTime,
                it.simpleSchemaObjectDoc.tenant,
                it.simpleSchemaObjectDoc.access,
                it.simpleSchemaObjectDoc.type,
                it.simpleSchemaObjectDoc.objectData
            )
        }
        return GetSimpleSchemaObjectResponse(
            SimpleSchemaObjectSearchResult(configSearchResult),
            UserAccessManager.hasAllInfoAccess(user)
        )
    }

    /**
     * Get all Object matching the criteria
     * @param request [GetSimpleSchemaObjectRequest] object
     * @param user the user info object
     * @return [GetSimpleSchemaObjectResponse]
     */
    private fun getAll(request: GetSimpleSchemaObjectRequest, user: User?): GetSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-getAll")
        val searchResult = SimpleSearchIndex.getAllSimpleSchemaObjects(
            UserAccessManager.getUserTenant(user),
            UserAccessManager.getSearchAccessInfo(user),
            request
        )
        return GetSimpleSchemaObjectResponse(searchResult, UserAccessManager.hasAllInfoAccess(user))
    }

    /**
     * Delete Object
     * @param request [SimpleSchemaObjectRequest] object
     * @param user the user info object
     * @return [DeleteSimpleSchemaObjectResponse]
     */
    fun delete(request: SimpleSchemaObjectRequest, user: User?): DeleteSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-delete ${request.objectIds}")
        return if (request.objectIds.size == 1) {
            delete(request.objectIds.first(), user)
        } else {
            delete(request.objectIds, user)
        }
    }

    /**
     * Delete by object id
     *
     * @param objectId
     * @param user
     * @return [DeleteSimpleSchemaObjectResponse]
     */
    private fun delete(objectId: String, user: User?): DeleteSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-delete $objectId")
        UserAccessManager.validateUser(user)
        val objectDocInfo = SimpleSearchIndex.getSimpleSchemaObject(objectId)
        objectDocInfo
            ?: run {
                throw OpenSearchStatusException(
                    "SimpleSchemaObject $objectId not found",
                    RestStatus.NOT_FOUND
                )
            }

        val currentDoc = objectDocInfo.simpleSchemaObjectDoc
        if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
            throw OpenSearchStatusException(
                "Permission denied for Object $objectId",
                RestStatus.FORBIDDEN
            )
        }
        if (!SimpleSearchIndex.deleteSimpleSchemaObject(objectId)) {
            throw OpenSearchStatusException(
                "Object $objectId delete failed",
                RestStatus.REQUEST_TIMEOUT
            )
        }
        return DeleteSimpleSchemaObjectResponse(mapOf(Pair(objectId, RestStatus.OK)))
    }

    /**
     * Delete Object
     * @param objectIds Object object ids
     * @param user the user info object
     * @return [DeleteSimpleSchemaObjectResponse]
     */
    fun delete(objectIds: Set<String>, user: User?): DeleteSimpleSchemaObjectResponse {
        log.info("$LOG_PREFIX:SimpleSchemaObject-delete $objectIds")
        UserAccessManager.validateUser(user)
        val configDocs = SimpleSearchIndex.getSimpleSchemaObjects(objectIds)
        if (configDocs.size != objectIds.size) {
            val mutableSet = objectIds.toMutableSet()
            configDocs.forEach { mutableSet.remove(it.id) }
            throw OpenSearchStatusException(
                "Object $mutableSet not found",
                RestStatus.NOT_FOUND
            )
        }
        configDocs.forEach {
            val currentDoc = it.simpleSchemaObjectDoc
            if (!UserAccessManager.doesUserHasAccess(user, currentDoc.tenant, currentDoc.access)) {
                throw OpenSearchStatusException(
                    "Permission denied for Object ${it.id}",
                    RestStatus.FORBIDDEN
                )
            }
        }
        val deleteStatus = SimpleSearchIndex.deleteSimpleSchemaObjects(objectIds)
        return DeleteSimpleSchemaObjectResponse(deleteStatus)
    }
}
