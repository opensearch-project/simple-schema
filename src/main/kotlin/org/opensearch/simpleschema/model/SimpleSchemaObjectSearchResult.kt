/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.model

import org.apache.lucene.search.TotalHits
import org.opensearch.action.search.SearchResponse
import org.opensearch.common.io.stream.StreamInput
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.simpleschema.model.RestTag.OBJECT_LIST_FIELD

/**
 * SimpleSchemaObject search results
 */
internal class SimpleSchemaObjectSearchResult : SearchResults<SimpleSchemaObjectDoc> {

    /**
     * single item result constructor
     */
    constructor(objectItem: SimpleSchemaObjectDoc) : super(OBJECT_LIST_FIELD, objectItem)

    /**
     * multiple items result constructor
     */
    constructor(objectList: List<SimpleSchemaObjectDoc>) : this(
        0,
        objectList.size.toLong(),
        TotalHits.Relation.EQUAL_TO,
        objectList
    )

    /**
     * all param constructor
     */
    constructor(
        startIndex: Long,
        totalHits: Long,
        totalHitRelation: TotalHits.Relation,
        objectList: List<SimpleSchemaObjectDoc>
    ) : super(startIndex, totalHits, totalHitRelation, OBJECT_LIST_FIELD, objectList)

    /**
     * Constructor used in transport action communication.
     * @param input StreamInput stream to deserialize data from.
     */
    constructor(input: StreamInput) : super(input, SimpleSchemaObjectDoc.reader)

    /**
     * Construct object from XContentParser
     */
    constructor(parser: XContentParser) : super(parser, OBJECT_LIST_FIELD)

    /**
     * Construct object from SearchResponse
     */
    constructor(from: Long, response: SearchResponse, searchHitParser: SearchHitParser<SimpleSchemaObjectDoc>) : super(
        from,
        response,
        searchHitParser,
        OBJECT_LIST_FIELD
    )

    /**
     * {@inheritDoc}
     */
    override fun parseItem(parser: XContentParser, useId: String?): SimpleSchemaObjectDoc {
        return SimpleSchemaObjectDoc.parse(parser, useId)
    }
}
