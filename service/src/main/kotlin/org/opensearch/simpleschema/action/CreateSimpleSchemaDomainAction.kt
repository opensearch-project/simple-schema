package org.opensearch.simpleschema.action

import org.opensearch.action.ActionType
import org.opensearch.action.support.ActionFilters
import org.opensearch.client.Client
import org.opensearch.common.inject.Inject
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.commons.authuser.User
import org.opensearch.transport.TransportService

internal class CreateSimpleSchemaDomainAction @Inject constructor(
    transportService: TransportService,
    client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : PluginBaseAction<CreateSimpleSchemaDomainRequest, CreateSimpleSchemaDomainResponse>(
    NAME,
    transportService,
    client,
    actionFilters,
    ::CreateSimpleSchemaDomainRequest
) {
    companion object {
        private const val NAME = "cluster:admin/opensearch/simpleschema/domain/create"
        internal val ACTION_TYPE = ActionType(NAME, ::CreateSimpleSchemaDomainResponse)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(
        request: CreateSimpleSchemaDomainRequest,
        user: User?
    ): CreateSimpleSchemaDomainResponse {
        return SimpleSchemaDomainActions.create(request, user)
    }
}
