/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.transport.TransportService

/**
 * Delete Object transport action
 */
internal class DeleteSimpleSchemaObjectAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<SimpleSchemaObjectRequest, DeleteSimpleSchemaObjectResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::SimpleSchemaObjectRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/delete"
        internal val ACTION_TYPE = ActionType(NAME, ::DeleteSimpleSchemaObjectResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: SimpleSchemaObjectRequest, user: User?): DeleteSimpleSchemaObjectResponse {
        return SimpleSchemaActions.delete(request, user)
    }
}
