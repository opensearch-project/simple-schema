package org.opensearch.simpleschema.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.transport.TransportService

internal class GetSimpleSchemaDomainAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<GetSimpleSchemaDomainRequest, GetSimpleSchemaDomainResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::GetSimpleSchemaDomainRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/domain/get"
        internal val ACTION_TYPE = ActionType(NAME, ::GetSimpleSchemaDomainResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: GetSimpleSchemaDomainRequest,
        user: User?
    ): GetSimpleSchemaDomainResponse {
        return SimpleSchemaDomainActions.get(request, user)
    }
}