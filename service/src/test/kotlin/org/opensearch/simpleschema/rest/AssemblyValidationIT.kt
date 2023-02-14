/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.rest

import org.opensearch.rest.RestRequest
import org.opensearch.rest.RestStatus
import org.opensearch.simpleschema.PluginRestTestCase

class AssemblyValidationIT : PluginRestTestCase() {
    companion object {
        private const val INDEX_NAME = ".opensearch-simpleschema"
    }

    fun `test simple schema index was created`() {
        // verify metrics mapping template was created successfully as part of the plugin initialization
        Thread.sleep(200)
        val response = executeRequest(
            RestRequest.Method.GET.name,
            "/$INDEX_NAME",
            "",
            RestStatus.OK.status
        )
        assertNotNull(response.get(INDEX_NAME))
        Thread.sleep(200)
    }

}
