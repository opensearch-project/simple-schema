package org.opensearch.simpleschema.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.transport.TransportService

internal class CreateGraphQLAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateGraphQLRequest, CreateGraphQLResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::CreateGraphQLRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/create"
        internal val ACTION_TYPE = ActionType(NAME, ::CreateGraphQLResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: CreateGraphQLRequest,
        user: User?
    ): CreateGraphQLResponse {
        TODO("not implemented")
    }
}
