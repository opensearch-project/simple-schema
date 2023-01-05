/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.resthandler

import org.opensearch.rest.RestChannel
import org.opensearch.rest.RestStatus
import org.opensearch.rest.action.RestToXContentListener
import org.opensearch.simpleschema.model.BaseResponse

/**
 * Overrides RestToXContentListener REST based action listener that assumes the response is of type
 * {@link ToXContent} and automatically builds an XContent based response
 * (wrapping the toXContent in startObject/endObject).
 */
internal class RestResponseToXContentListener<Response : BaseResponse>(channel: RestChannel) : RestToXContentListener<Response>(channel) {
    /**
     * {@inheritDoc}
     */
    override fun getStatus(response: Response): RestStatus {
        return response.getStatus()
    }
}
