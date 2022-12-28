package org.opensearch.simpleschema.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.rest.RestChannel
import org.opensearch.rest.RestRequest
import org.opensearch.simpleschema.action.CreateGraphQLAction
import org.opensearch.simpleschema.action.CreateGraphQLRequest
import org.opensearch.simpleschema.util.contentParserNextToken

class GraphQLRequestExecutor {
    fun executePostRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        client.execute(
            CreateGraphQLAction.ACTION_TYPE,
            CreateGraphQLRequest.parse(request.contentParserNextToken()),
            RestResponseToXContentListener(channel)
        )
    }

    fun executeGetRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        TODO("not implemented")
    }
}
