/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.simpleschema.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.rest.BaseRestHandler
import org.opensearch.rest.BaseRestHandler.RestChannelConsumer
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestHandler.Route
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestRequest.Method.DELETE
import org.opensearch.rest.RestRequest.Method.GET
import org.opensearch.rest.RestRequest.Method.POST
import org.opensearch.rest.RestRequest.Method.PUT
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.BASE_SIMPLESCHEMA_URI
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.DeleteSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.DeleteSimpleSchemaObjectResponse
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.UpdateSimpleSchemaObjectRequest
import org.opensearch.simpleschema.model.RestTag.FROM_INDEX_FIELD
import org.opensearch.simpleschema.model.RestTag.MAX_ITEMS_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_ID_LIST_FIELD
import org.opensearch.simpleschema.model.RestTag.OBJECT_TYPE_FIELD
import org.opensearch.simpleschema.model.RestTag.SORT_FIELD_FIELD
import org.opensearch.simpleschema.model.RestTag.SORT_ORDER_FIELD

/**
 * Rest handler for SimpleSchema object lifecycle management.
 * This handler uses [SimpleSchemaActions].
 */
internal class SimpleSchemaRestHandler : BaseRestHandler() {
    companion object {
        private const val SIMPLESCHEMA_ACTION = "simpleschema_actions"
        private const val SIMPLESCHEMA_OBJECT_URL = "$BASE_SIMPLESCHEMA_URI/object"
        private const val SIMPLESCHEMA_GRAPHQL_URL = "$BASE_SIMPLESCHEMA_URI/_igraphql"
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
            Route(POST, SIMPLESCHEMA_OBJECT_URL),
            /**
             * Update object
             * Request URL: PUT SIMPLESCHEMA_URL/{objectId}
             * Request body: Ref [org.opensearch.simpleschema.model.UpdateSimpleSchemaObjectRequest]
             * Response body: Ref [org.opensearch.simpleschema.model.UpdateSimpleSchemaObjectResponse]
             */
            Route(PUT, "$SIMPLESCHEMA_OBJECT_URL/{$OBJECT_ID_FIELD}"),
            /**
             * Get a object
             * Request URL: GET SIMPLESCHEMA_URL/{objectId}
             * Request body: Ref [org.opensearch.simpleschema.model.GetSimpleSchemaObjectRequest]
             * Response body: Ref [org.opensearch.simpleschema.model.GetSimpleSchemaObjectResponse]
             */
            Route(GET, "$SIMPLESCHEMA_OBJECT_URL/{$OBJECT_ID_FIELD}"),
            Route(GET, SIMPLESCHEMA_OBJECT_URL),
            /**
             * Delete object
             * Request URL: DELETE SIMPLESCHEMA_URL/{objectId}
             * Request body: Ref [org.opensearch.simpleschema.model.DeleteSimpleSchemaObjectRequest]
             * Response body: Ref [org.opensearch.simpleschema.model.DeleteSimpleSchemaObjectResponse]
             */
            Route(DELETE, "$SIMPLESCHEMA_OBJECT_URL/{$OBJECT_ID_FIELD}"),
            Route(DELETE, SIMPLESCHEMA_OBJECT_URL)
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

    private fun handleSimpleSchemaObjectRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> RestChannelConsumer {
                ObjectRequestExecutor().executePostRequest(request, client, it)
            }

            PUT -> RestChannelConsumer {
                ObjectRequestExecutor().executePutRequest(request, client, it)
            }

            GET -> RestChannelConsumer {
                ObjectRequestExecutor().executeGetRequest(request, client, it)
            }

            DELETE -> RestChannelConsumer {
                ObjectRequestExecutor().executeDeleteRequest(request, client, it)
            }

            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed here"))
            }
        }
    }

    private fun handleGraphQLRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when (request.method()) {
            POST -> RestChannelConsumer {
                GraphQLRequestExecutor().executePostRequest(request, client, it)
            }

            GET -> RestChannelConsumer {
                GraphQLRequestExecutor().executeGetRequest(request, client, it)
            }

            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed here"))
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun prepareRequest(request: RestRequest, client: NodeClient): RestChannelConsumer {
        return when {
            request.path().startsWith(SIMPLESCHEMA_OBJECT_URL) -> handleSimpleSchemaObjectRequest(request, client)
            request.path().startsWith(SIMPLESCHEMA_GRAPHQL_URL) -> handleGraphQLRequest(request, client)
            else -> RestChannelConsumer {
                it.sendResponse(BytesRestResponse(RestStatus.NOT_FOUND, "${request.path()} handler was not found"))
            }
        }
    }
}
