package org.opensearch.simpleschema.resthandler

import org.opensearch.client.node.NodeClient
import org.opensearch.commons.utils.logger
import org.opensearch.rest.BytesRestResponse
import org.opensearch.rest.RestChannel
import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.search.sort.SortOrder
import org.opensearch.simpleschema.SimpleSchemaPlugin
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.DeleteSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.DeleteSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectRequest
import org.opensearch.simpleschema.action.UpdateSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.UpdateSimpleSchemaObjectRequest
import org.opensearch.simpleschema.index.SimpleSearchQueryHelper
import org.opensearch.simpleschema.model.RestTag
import org.opensearch.simpleschema.model.SimpleSchemaObjectType
import org.opensearch.simpleschema.settings.PluginSettings
import org.opensearch.simpleschema.util.contentParserNextToken
import java.util.*

class ObjectRequestExecutor {
    companion object {
        private val log by logger(ObjectRequestExecutor::class.java)
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
        val types: EnumSet<SimpleSchemaObjectType> = EnumSet.noneOf(SimpleSchemaObjectType::class.java)
        typesString?.split(",")?.forEach { types.add(SimpleSchemaObjectType.fromTagOrDefault(it)) }
        return types
    }

    fun executePostRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        return client.execute(
            CreateSimpleSchemaObjectAction.ACTION_TYPE,
            CreateSimpleSchemaObjectRequest.parse(request.contentParserNextToken()),
            RestResponseToXContentListener(channel)
        )
    }

    fun executePutRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        return client.execute(
            UpdateSimpleSchemaObjectAction.ACTION_TYPE,
            UpdateSimpleSchemaObjectRequest.parse(request.contentParserNextToken(), request.param(RestTag.OBJECT_ID_FIELD)),
            RestResponseToXContentListener(channel)
        )
    }

    fun executeGetRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        val objectId: String? = request.param(RestTag.OBJECT_ID_FIELD)
        val objectIdListString: String? = request.param(RestTag.OBJECT_ID_LIST_FIELD)
        val objectIdList = getObjectIdSet(objectId, objectIdListString)
        val types: EnumSet<SimpleSchemaObjectType> = getTypesSet(request.param(RestTag.OBJECT_TYPE_FIELD))
        val sortField: String? = request.param(RestTag.SORT_FIELD_FIELD)
        val sortOrderString: String? = request.param(RestTag.SORT_ORDER_FIELD)
        val sortOrder: SortOrder? = if (sortOrderString == null) {
            null
        } else {
            SortOrder.fromString(sortOrderString)
        }
        val fromIndex = request.param(RestTag.FROM_INDEX_FIELD)?.toIntOrNull() ?: 0
        val maxItems = request.param(RestTag.MAX_ITEMS_FIELD)?.toIntOrNull() ?: PluginSettings.defaultItemsQueryCount
        val filterParams = request.params()
            .filter { SimpleSearchQueryHelper.FILTER_PARAMS.contains(it.key) }
            .map { Pair(it.key, request.param(it.key)) }
            .toMap()
        log.info(
            "${SimpleSchemaPlugin.LOG_PREFIX}:executeGetRequest idList:$objectIdList types:$types, from:$fromIndex, maxItems:$maxItems," +
                " sortField:$sortField, sortOrder=$sortOrder, filters=$filterParams"
        )
        return client.execute(
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
            RestResponseToXContentListener(channel)
        )
    }

    fun executeDeleteRequest(request: RestRequest, client: NodeClient, channel: RestChannel) {
        val objectId: String? = request.param(RestTag.OBJECT_ID_FIELD)
        val objectIdSet: Set<String> =
            request.paramAsStringArray(RestTag.OBJECT_ID_LIST_FIELD, arrayOf(objectId))
                .filter { s -> !s.isNullOrBlank() }
                .toSet()
        return if (objectIdSet.isEmpty()) {
            channel.sendResponse(
                BytesRestResponse(
                    RestStatus.BAD_REQUEST,
                    "either ${RestTag.OBJECT_ID_FIELD} or ${RestTag.OBJECT_ID_LIST_FIELD} is required"
                )
            )
        } else {
            client.execute(
                DeleteSimpleSchemaObjectAction.ACTION_TYPE,
                DeleteSimpleSchemaObjectRequest(objectIdSet),
                RestResponseToXContentListener(channel)
            )
        }
    }
}
