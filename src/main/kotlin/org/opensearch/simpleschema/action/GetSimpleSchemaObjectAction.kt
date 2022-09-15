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
 * Get Object transport action
 */
internal class GetSimpleSchemaObjectAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetSimpleSchemaObjectRequest, GetSimpleSchemaObjectResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::GetSimpleSchemaObjectRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/get"
        internal val ACTION_TYPE = ActionType(NAME, ::GetSimpleSchemaObjectResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: GetSimpleSchemaObjectRequest, user: User?): GetSimpleSchemaObjectResponse {
        return SimpleSchemaActions.get(request, user)
    }
}
