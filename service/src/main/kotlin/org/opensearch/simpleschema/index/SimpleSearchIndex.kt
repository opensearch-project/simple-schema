/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.index

import org.opensearch.ResourceAlreadyExistsException
import org.opensearch.action.ActionListener
import org.opensearch.action.DocWriteResponse
import org.opensearch.action.admin.indices.create.CreateIndexAction
import org.opensearch.action.admin.indices.create.CreateIndexRequest
import org.opensearch.action.admin.indices.create.CreateIndexResponse
import org.opensearch.action.admin.indices.mapping.put.PutMappingRequest
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.delete.DeleteRequest
import org.opensearch.action.get.GetRequest
import org.opensearch.action.get.GetResponse
import org.opensearch.action.get.MultiGetRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.action.search.SearchRequest
import org.opensearch.action.update.UpdateRequest
import org.opensearch.client.Client
import org.opensearch.cluster.ClusterChangedEvent
import org.opensearch.cluster.ClusterState
import org.opensearch.cluster.ClusterStateListener
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.component.LifecycleListener
import org.opensearch.common.unit.TimeValue
import org.opensearch.common.xcontent.LoggingDeprecationHandler
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.common.xcontent.XContentType
import org.opensearch.index.IndexNotFoundException
import org.opensearch.index.query.QueryBuilders
import org.opensearch.rest.RestStatus
import org.opensearch.search.SearchHit
import org.opensearch.search.builder.SearchSourceBuilder
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectRequest
import org.opensearch.simpleschema.model.RestTag.ACCESS_LIST_FIELD
import org.opensearch.simpleschema.model.RestTag.TENANT_FIELD
import org.opensearch.simpleschema.model.SearchResults
import org.opensearch.simpleschema.model.SimpleSchemaObjectDoc
import org.opensearch.simpleschema.model.SimpleSchemaObjectDocInfo
import org.opensearch.simpleschema.model.SimpleSchemaObjectSearchResult
import org.opensearch.simpleschema.settings.PluginSettings
import org.opensearch.simpleschema.util.SecureIndexClient
import org.opensearch.simpleschema.util.logger
import java.util.concurrent.TimeUnit

/**
 * Class for doing OpenSearch index operation to maintain SimpleSchema objects in cluster.
 */
@Suppress("TooManyFunctions")
internal object SimpleSearchIndex : LifecycleListener() {
    private val log by logger(SimpleSearchIndex::class.java)
    private const val INDEX_NAME = ".opensearch-simpleschema"
    private const val SIMPLESCHEMA_MAPPING_FILE_NAME = "simpleschema-mapping.yml"
    private const val SIMPLESCHEMA_SETTINGS_FILE_NAME = "simpleschema-settings.yml"

    private var mappingsUpdated: Boolean = false
    private lateinit var client: Client
    private lateinit var clusterService: ClusterService

    private val searchHitParser = object : SearchResults.SearchHitParser<SimpleSchemaObjectDoc> {
        override fun parse(searchHit: SearchHit): SimpleSchemaObjectDoc {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                searchHit.sourceAsString
            )
            parser.nextToken()
            return SimpleSchemaObjectDoc.parse(parser, searchHit.id)
        }
    }

    /**
     * Initialize the class
     * @param client The OpenSearch client
     * @param clusterService The OpenSearch cluster service
     */
    fun initialize(client: Client, clusterService: ClusterService): SimpleSearchIndex {
        SimpleSearchIndex.client = SecureIndexClient(client)
        SimpleSearchIndex.clusterService = clusterService
        mappingsUpdated = false
        return this
    }

    /**
     * once lifecycle indicate start has occurred - instantiating system index creation
     */
    override fun afterStart() {
        // create default index
        createIndex()
    }

    /**
     * Create index using the mapping and settings defined in resource
     */
    @Suppress("TooGenericExceptionCaught")
    private fun createIndex() {
        if (!isIndexExists(INDEX_NAME)) {
            val request = createIndexRequest()
            try {
                val actionFuture = client.admin().indices().create(request)
                val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
                if (response.isAcknowledged) {
                    log.info("$LOG_PREFIX:Index $INDEX_NAME creation Acknowledged")
                } else {
                    throw IllegalStateException("$LOG_PREFIX:Index $INDEX_NAME creation not Acknowledged")
                }
            } catch (exception: Exception) {
                if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                    throw exception
                }
            }
            mappingsUpdated = true
        } else if (!mappingsUpdated) {
            updateMappings()
        }
    }

    private fun createIndexRequest(): CreateIndexRequest? {
        val classLoader = SimpleSearchIndex::class.java.classLoader
        val indexMappingSource = classLoader.getResource(SIMPLESCHEMA_MAPPING_FILE_NAME)?.readText()!!
        val indexSettingsSource = classLoader.getResource(SIMPLESCHEMA_SETTINGS_FILE_NAME)?.readText()!!
        val request = CreateIndexRequest(INDEX_NAME)
            .mapping(indexMappingSource, XContentType.YAML)
            .settings(indexSettingsSource, XContentType.YAML)
        return request
    }

    @Suppress("TooGenericExceptionCaught")
    private fun indexCreateCall() {
        val request = createIndexRequest()
        try {
            client.execute(CreateIndexAction.INSTANCE, request, object : ActionListener<CreateIndexResponse> {
                override fun onResponse(response: CreateIndexResponse) {
                    if (response.isAcknowledged) {
                        log.info("$LOG_PREFIX:Index $INDEX_NAME creation Acknowledged")
                    } else {
                        log.warn("$LOG_PREFIX:Index $INDEX_NAME creation not Acknowledged")
                    }
                }

                override fun onFailure(err: java.lang.Exception?) {
                    log.error("$LOG_PREFIX:Index $INDEX_NAME creation not Acknowledged", err)
                }
            })
        } catch (exception: Exception) {
            if (exception !is ResourceAlreadyExistsException && exception.cause !is ResourceAlreadyExistsException) {
                throw exception
            }
        }
    }

    /**
     * Check if the index mappings have changed and if they have, update them
     */
    private fun updateMappings() {
        val classLoader = SimpleSearchIndex::class.java.classLoader
        val indexMappingSource = classLoader.getResource(SIMPLESCHEMA_MAPPING_FILE_NAME)?.readText()!!
        val request = PutMappingRequest(INDEX_NAME)
            .source(indexMappingSource, XContentType.YAML)
        try {
            val actionFuture = client.admin().indices().putMapping(request)
            val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
            if (response.isAcknowledged) {
                log.info("$LOG_PREFIX:Index $INDEX_NAME update mapping Acknowledged")
            } else {
                throw IllegalStateException("$LOG_PREFIX:Index $INDEX_NAME update mapping not Acknowledged")
            }
            mappingsUpdated = true
        } catch (exception: IndexNotFoundException) {
            log.error("$LOG_PREFIX:IndexNotFoundException:", exception)
        }
    }

    /**
     * Check if the index is created and available.
     * @param index
     * @return true if index is available, false otherwise
     */
    private fun isIndexExists(index: String): Boolean {
        val clusterState = clusterService.state()
        return clusterState.routingTable.hasIndex(index)
    }

    /**
     * Create  object
     *
     * @param simpleSchemaObjectDoc
     * @param id
     * @return object id if successful, otherwise null
     */
    fun createSimpleSchemaObject(simpleSchemaObjectDoc: SimpleSchemaObjectDoc, id: String? = null): String? {
        createIndex()
        val xContent = simpleSchemaObjectDoc.toXContent()
        val indexRequest = IndexRequest(INDEX_NAME)
            .source(xContent)
            .create(true)
        if (id != null) {
            indexRequest.id(id)
        }
        val actionFuture = client.index(indexRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return if (response.result != DocWriteResponse.Result.CREATED) {
            log.warn("$LOG_PREFIX:createSimpleSchemaObject - response:$response")
            null
        } else {
            response.id
        }
    }

    /**
     * Get  object
     *
     * @param id
     * @return [SimpleSchemaObjectDocInfo]
     */
    fun getSimpleSchemaObject(id: String): SimpleSchemaObjectDocInfo? {
        createIndex()
        val getRequest = GetRequest(INDEX_NAME).id(id)
        val actionFuture = client.get(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return parseSimpleSchemaObjectDoc(id, response)
    }

    /**
     * Get multiple  objects
     *
     * @param ids
     * @return list of [SimpleSchemaObjectDocInfo]
     */
    fun getSimpleSchemaObjects(ids: Set<String>): List<SimpleSchemaObjectDocInfo> {
        createIndex()
        val getRequest = MultiGetRequest()
        ids.forEach { getRequest.add(INDEX_NAME, it) }
        val actionFuture = client.multiGet(getRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        return response.responses.mapNotNull { parseSimpleSchemaObjectDoc(it.id, it.response) }
    }

    /**
     * Parse object doc
     *
     * @param id
     * @param response
     * @return [SimpleSchemaObjectDocInfo]
     */
    private fun parseSimpleSchemaObjectDoc(id: String, response: GetResponse): SimpleSchemaObjectDocInfo? {
        return if (response.sourceAsString == null) {
            log.warn("$LOG_PREFIX:getSimpleSchemaObject - $id not found; response:$response")
            null
        } else {
            val parser = XContentType.JSON.xContent().createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response.sourceAsString
            )
            parser.nextToken()
            val doc = SimpleSchemaObjectDoc.parse(parser, id)
            SimpleSchemaObjectDocInfo(id, response.version, response.seqNo, response.primaryTerm, doc)
        }
    }

    /**
     * Get all  objects
     *
     * @param tenant
     * @param access
     * @param request
     * @return [SimpleSchemaObjectSearchResult]
     */
    fun getAllSimpleSchemaObjects(
        tenant: String,
        access: List<String>,
        request: GetSimpleSchemaObjectRequest
    ): SimpleSchemaObjectSearchResult {
        createIndex()
        val queryHelper = SimpleSearchQueryHelper(request.types)
        val sourceBuilder = SearchSourceBuilder()
            .timeout(TimeValue(PluginSettings.operationTimeoutMs, TimeUnit.MILLISECONDS))
            .size(request.maxItems)
            .from(request.fromIndex)
        queryHelper.addSortField(sourceBuilder, request.sortField, request.sortOrder)

        val query = QueryBuilders.boolQuery()
        query.filter(QueryBuilders.termsQuery(TENANT_FIELD, tenant))
        if (access.isNotEmpty()) {
            query.filter(QueryBuilders.termsQuery(ACCESS_LIST_FIELD, access))
        }
        queryHelper.addTypeFilters(query)
        queryHelper.addQueryFilters(query, request.filterParams)
        sourceBuilder.query(query)
        val searchRequest = SearchRequest()
            .indices(INDEX_NAME)
            .source(sourceBuilder)
        val actionFuture = client.search(searchRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val result = SimpleSchemaObjectSearchResult(request.fromIndex.toLong(), response, searchHitParser)
        log.info(
            "$LOG_PREFIX:getAllSimpleSchemaObjects types:${request.types} from:${request.fromIndex}, maxItems:${request.maxItems}," +
                " sortField:${request.sortField}, sortOrder=${request.sortOrder}, filters=${request.filterParams}" +
                " retCount:${result.objectList.size}, totalCount:${result.totalHits}"
        )
        return result
    }

    /**
     * Update  object
     *
     * @param id
     * @param simpleSchemaObjectDoc
     * @return true if successful, otherwise false
     */
    fun updateSimpleSchemaObject(id: String, simpleSchemaObjectDoc: SimpleSchemaObjectDoc): Boolean {
        createIndex()
        val updateRequest = UpdateRequest()
            .index(INDEX_NAME)
            .id(id)
            .doc(simpleSchemaObjectDoc.toXContent())
            .fetchSource(true)
        val actionFuture = client.update(updateRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.UPDATED) {
            log.warn("$LOG_PREFIX:updateSimpleSchemaObject failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.UPDATED
    }

    /**
     * Delete  object
     *
     * @param id
     * @return true if successful, otherwise false
     */
    fun deleteSimpleSchemaObject(id: String): Boolean {
        createIndex()
        val deleteRequest = DeleteRequest()
            .index(INDEX_NAME)
            .id(id)
        val actionFuture = client.delete(deleteRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        if (response.result != DocWriteResponse.Result.DELETED) {
            log.warn("$LOG_PREFIX:deleteSimpleSchemaObject failed for $id; response:$response")
        }
        return response.result == DocWriteResponse.Result.DELETED
    }

    /**
     * Delete multiple  objects
     *
     * @param ids
     * @return map of id to delete status
     */
    fun deleteSimpleSchemaObjects(ids: Set<String>): Map<String, RestStatus> {
        createIndex()
        val bulkRequest = BulkRequest()
        ids.forEach {
            val deleteRequest = DeleteRequest()
                .index(INDEX_NAME)
                .id(it)
            bulkRequest.add(deleteRequest)
        }
        val actionFuture = client.bulk(bulkRequest)
        val response = actionFuture.actionGet(PluginSettings.operationTimeoutMs)
        val mutableMap = mutableMapOf<String, RestStatus>()
        response.forEach {
            mutableMap[it.id] = it.status()
            if (it.isFailed) {
                log.warn("$LOG_PREFIX:deleteSimpleSchemaObjects failed for ${it.id}; response:${it.failureMessage}")
            }
        }
        return mutableMap
    }
}
