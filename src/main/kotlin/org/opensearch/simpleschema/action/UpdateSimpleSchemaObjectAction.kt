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
 * Update Object transport action
 */
internal class UpdateSimpleSchemaObjectAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<UpdateSimpleSchemaObjectRequest, UpdateSimpleSchemaObjectResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::UpdateSimpleSchemaObjectRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/update"
        internal val ACTION_TYPE = ActionType(NAME, ::UpdateSimpleSchemaObjectResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: UpdateSimpleSchemaObjectRequest, user: User?): UpdateSimpleSchemaObjectResponse {
        return SimpleSchemaActions.update(request, user)
    }
}
