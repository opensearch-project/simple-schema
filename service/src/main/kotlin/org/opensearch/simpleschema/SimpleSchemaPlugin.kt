/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.simpleschema

import org.opensearch.simpleschema.util.logger
import org.opensearch.action.ActionRequest
import org.opensearch.action.ActionResponse
import org.opensearch.client.Client
import org.opensearch.cluster.metadata.IndexNameExpressionResolver
import org.opensearch.cluster.node.DiscoveryNodes
import org.opensearch.cluster.service.ClusterService
import org.opensearch.common.io.stream.NamedWriteableRegistry
import org.opensearch.common.settings.ClusterSettings
import org.opensearch.common.settings.IndexScopedSettings
import org.opensearch.common.settings.Settings
import org.opensearch.common.settings.SettingsFilter
import org.opensearch.common.settings.Setting
import org.opensearch.jobscheduler.spi.JobSchedulerExtension
import org.opensearch.common.xcontent.NamedXContentRegistry
import org.opensearch.env.Environment
import org.opensearch.env.NodeEnvironment
import org.opensearch.plugins.ActionPlugin
import org.opensearch.plugins.Plugin
import org.opensearch.repositories.RepositoriesService
import org.opensearch.rest.RestController
import org.opensearch.rest.RestHandler
import org.opensearch.script.ScriptService
import org.opensearch.simpleschema.index.SimpleSearchIndex
import org.opensearch.simpleschema.resthandler.SimpleSchemaRestHandler
import org.opensearch.simpleschema.settings.PluginSettings
import org.opensearch.threadpool.ThreadPool
import org.opensearch.watcher.ResourceWatcherService
import java.util.function.Supplier
import org.opensearch.jobscheduler.spi.ScheduledJobParser
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.plugins.ClusterPlugin
import org.opensearch.simpleschema.action.CreateSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.DeleteSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.GetSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.UpdateSimpleSchemaObjectAction
import org.opensearch.simpleschema.action.CreateSimpleSchemaDomainAction
import org.opensearch.simpleschema.action.GetSimpleSchemaDomainAction
import org.opensearch.simpleschema.scheduler.SimpleSearchJobParser
import org.opensearch.simpleschema.scheduler.SimpleSearchJobRunner
import org.opensearch.simpleschema.resthandler.SchedulerRestHandler
import org.opensearch.simpleschema.resthandler.SimpleSchemaDomainRestHandler


/**
 * Entry point of the OpenSearch simple schema plugin.
 * This class initializes the rest handlers.
 */
class SimpleSchemaPlugin : Plugin(), ActionPlugin, ClusterPlugin, JobSchedulerExtension {

    companion object {
        private val log by logger(SimpleSchemaPlugin::class.java)
        const val PLUGIN_NAME = "opensearch-simple-schema"
        const val LOG_PREFIX = "simpleschema"
        const val BASE_SIMPLESCHEMA_URI = "/_plugins/_simpleschema"
    }

    /**
     * {@inheritDoc}
     */
    override fun getSettings(): List<Setting<*>> {
        return PluginSettings.getAllSettings()
    }

    /**
     * {@inheritDoc}
     */
    override fun createComponents(
        client: Client,
        clusterService: ClusterService,
        threadPool: ThreadPool,
        resourceWatcherService: ResourceWatcherService,
        scriptService: ScriptService,
        xContentRegistry: NamedXContentRegistry,
        environment: Environment,
        nodeEnvironment: NodeEnvironment,
        namedWriteableRegistry: NamedWriteableRegistry,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        repositoriesServiceSupplier: Supplier<RepositoriesService>
    ): Collection<Any> {
        PluginSettings.addSettingsUpdateConsumer(clusterService)
        SimpleSearchIndex.initialize(client, clusterService)
        return emptyList()
    }

    override fun onNodeStarted() {
        super.onNodeStarted()
        SimpleSearchIndex.afterStart()
    }

    /**
     * {@inheritDoc}
     */
    override fun getRestHandlers(
        settings: Settings,
        restController: RestController,
        clusterSettings: ClusterSettings,
        indexScopedSettings: IndexScopedSettings,
        settingsFilter: SettingsFilter,
        indexNameExpressionResolver: IndexNameExpressionResolver,
        nodesInCluster: Supplier<DiscoveryNodes>
    ): List<RestHandler> {
        return listOf(
            SimpleSchemaRestHandler(),
            SimpleSchemaDomainRestHandler(),
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun getActions(): List<ActionPlugin.ActionHandler<out ActionRequest, out ActionResponse>> {
        return listOf(
            ActionPlugin.ActionHandler(
                CreateSimpleSchemaObjectAction.ACTION_TYPE,
                CreateSimpleSchemaObjectAction::class.java
            ),
            ActionPlugin.ActionHandler(
                DeleteSimpleSchemaObjectAction.ACTION_TYPE,
                DeleteSimpleSchemaObjectAction::class.java
            ),
            ActionPlugin.ActionHandler(
                GetSimpleSchemaObjectAction.ACTION_TYPE,
                GetSimpleSchemaObjectAction::class.java
            ),
            ActionPlugin.ActionHandler(
                UpdateSimpleSchemaObjectAction.ACTION_TYPE,
                UpdateSimpleSchemaObjectAction::class.java
            ),
            ActionPlugin.ActionHandler(
                CreateSimpleSchemaDomainAction.ACTION_TYPE,
                CreateSimpleSchemaDomainAction::class.java
            ),
            ActionPlugin.ActionHandler(
                GetSimpleSchemaDomainAction.ACTION_TYPE,
                GetSimpleSchemaDomainAction::class.java
            )
        )
    }

    override fun getJobType(): String {
        return "simpleschema"
    }

    override fun getJobIndex(): String {
        return SchedulerRestHandler.SCHEDULED_JOB_INDEX
    }

    override fun getJobRunner(): ScheduledJobRunner {
        return SimpleSearchJobRunner
    }

    override fun getJobParser(): ScheduledJobParser {
        return SimpleSearchJobParser
    }
}
