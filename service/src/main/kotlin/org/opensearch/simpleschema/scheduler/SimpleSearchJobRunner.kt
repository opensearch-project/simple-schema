/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opensearch.jobscheduler.spi.JobExecutionContext
import org.opensearch.jobscheduler.spi.ScheduledJobParameter
import org.opensearch.jobscheduler.spi.ScheduledJobRunner
import org.opensearch.simpleschema.SimpleSchemaPlugin.Companion.LOG_PREFIX
import org.opensearch.simpleschema.model.ScheduledJobDoc
import org.opensearch.simpleschema.util.logger

internal object SimpleSearchJobRunner : ScheduledJobRunner {
    private val log by logger(SimpleSearchJobRunner::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    override fun runJob(job: ScheduledJobParameter, context: JobExecutionContext) {
        if (job !is ScheduledJobDoc) {
            log.warn("$LOG_PREFIX:job is not of type ScheduledJobDoc:${job.javaClass.name}")
            throw IllegalArgumentException("job is not of type ScheduledJobDoc:${job.javaClass.name}")
        }

        scope.launch {
            val scheduledJob: ScheduledJobDoc = job
            val jobType = scheduledJob.jobType
            // TODO: Add logic to retrieve metric and update metric index. E,g. run PPL/SQL query via transport API
            //   and write to metric index after some processing.

            log.info("POC: Running job type: ${jobType.name}")
        }
    }
}
