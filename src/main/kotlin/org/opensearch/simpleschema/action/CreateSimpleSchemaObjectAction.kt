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
 * Create SimpleSchemaObject transport action
 */
internal class CreateSimpleSchemaObjectAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateSimpleSchemaObjectRequest, CreateSimpleSchemaObjectResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::CreateSimpleSchemaObjectRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/create"
        internal val ACTION_TYPE = ActionType(NAME, ::CreateSimpleSchemaObjectResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: CreateSimpleSchemaObjectRequest,
        user: User?
    ): CreateSimpleSchemaObjectResponse {
        return SimpleSchemaActions.create(request, user)
    }
}
