/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.simpleschema.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.utils.logger
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.BASE_SIMPLESCHEMA_URI
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.index.SimpleSearchQueryHelper
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import org.opensearch.simpleschema.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.simpleschema.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_LIST_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_TYPE_FIELD
import org.opensearch.simpleschema.model.RestTag.SORT_FIELD_FIELD
import org.opensearch.simpleschema.model.RestTag.SORT_ORDER_FIELD
import org.opensearch.simpleschema.settings.PluginSettings
import org.opensearch.simpleschema.util.contentParserNextToken
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestStatus
import org.opensearch.search.sort.SortOrder
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectAction
import java.util.EnumSet

/**
 * Rest handler for SimpleSchema object lifecycle management.
 * This handler uses [SimpleSchemaActions].
 */
internal class SimpleSchemaDomainRestHandler : BaseRestHandler() {
    companion object {
        private const val SIMPLESCHEMA_ACTION = "simpleschema_domain_actions"
        private const val SIMPLESCHEMA_URL = "$BASE_SIMPLESCHEMA_URI/domain"
        private val log by logger(SimpleSchemaDomainRestHandler::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return SIMPLESCHEMA_ACTION
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Create a new object
             * Request URL: POST SIMPLESCHEMA_URL
             * Request body: Ref [org.opensearch.simpleschema.model.CreateSimpleSchemaObjectRequest]
             * Response body: Ref [org.opensearch.simpleschema.model.CreateSimpleSchemaObjectResponse]
             */
            Route(POST, SIMPLESCHEMA_URL),
            /**
             * Get an object
             * Request URL: GET SIMPLESCHEMA_URL/{objectId}
             * Request body: Ref [org.opensearch.simpleschema.model.GetSimpleSchemaObjectRequest]
             * Response body: Ref [org.opensearch.simpleschema.model.GetSimpleSchemaObjectResponse]
             */
            Route(GET, "$SIMPLESCHEMA_URL/{$OBJECT_ID_FIELD}"),
            Route(GET, SIMPLESCHEMA_URL)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(
            OBJECT_ID_FIELD,
            OBJECT_ID_LIST_FIELD,
            OBJECT_TYPE_FIELD,
            SORT_FIELD_FIELD,
            SORT_ORDER_FIELD,
            FROM_INDEX_FIELD,
            MAX_ITEMS_FIELD
        )
    }

    private fun executePostRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return RestChannelConsumer {
            client.execute(
                CreateSimpleSchemaObjectAction.ACTION_TYPE,
                CreateSimpleSchemaObjectRequest.parse(request.contentParserNextToken()),
                RestResponseToXContentListener(it)
            )
        }
    }

    private fun executeGetRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        val objectId: String? = request.param(OBJECT_ID_FIELD)
        val objectIdListString: String? = request.param(OBJECT_ID_LIST_FIELD)
        val objectIdList = getObjectIdSet(objectId, objectIdListString)
        val types: EnumSet<SimpleSchemaObjectType> = getTypesSet(request.param(OBJECT_TYPE_FIELD))
        val sortField: String? = request.param(SORT_FIELD_FIELD)
        val sortOrderString: String? = request.param(SORT_ORDER_FIELD)
        val sortOrder: SortOrder? = if (sortOrderString == null) {
            null
        } else {
            SortOrder.fromString(sortOrderString)
        }
        val fromIndex = request.param(FROM_INDEX_FIELD)?.toIntOrNull() ?: 0
        val maxItems = request.param(MAX_ITEMS_FIELD)?.toIntOrNull() ?: PluginSettings.defaultItemsQueryCount
        val filterParams = request.params()
            .filter { SimpleSearchQueryHelper.FILTER_PARAMS.contains(it.key) }
            .map { Pair(it.key, request.param(it.key)) }
            .toMap()
        log.info(
            "$LOG_PREFIX:executeGetRequest idList:$objectIdList types:$types, from:$fromIndex, maxItems:$maxItems," +
                " sortField:$sortField, sortOrder=$sortOrder, filters=$filterParams"
        )
        return RestChannelConsumer {
            client.execute(
                GetSimpleSchemaObjectAction.ACTION_TYPE,
                GetSimpleSchemaObjectRequest(
                    objectIdList,
                    types,
                    fromIndex,
                    maxItems,
                    sortField,
                    sortOrder,
                    filterParams
                ),
                RestResponseToXContentListener(it)
            )
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> executePostRequest(request, client)
            GET -> executeGetRequest(request, client)
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed"))
            }
        }
    }

    private fun getObjectIdSet(objectId: String?, objectIdList: String?): Set<String> {
        var retIds: Set<String> = setOf()
        if (objectId != null) {
            retIds = setOf(objectId)
        }
        if (objectIdList != null) {
            retIds = objectIdList.split(",").union(retIds)
        }
        return retIds
    }

    private fun getTypesSet(typesString: String?): EnumSet<SimpleSchemaObjectType> {
        var types: EnumSet<SimpleSchemaObjectType> = EnumSet.noneOf(SimpleSchemaObjectType::class.java)
        typesString?.split(",")?.forEach { types.add(SimpleSchemaObjectType.fromTagOrDefault(it)) }
        return types
    }
}
