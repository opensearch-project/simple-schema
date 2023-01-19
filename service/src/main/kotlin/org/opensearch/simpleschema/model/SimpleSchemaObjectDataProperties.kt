/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.model

import org.opensearch.common.io.stream.Writeable
import org.opensearch.common.xcontent.XContentParser
import org.opensearch.simpleschema.domain.Domain

internal object SimpleSchemaObjectDataProperties {
    /**
     * Properties for ConfigTypes.
     * This data class is used to provide contract across configTypes without reading into config data classes.
     */
    private data class ObjectProperty(
        val objectDataReader: Writeable.Reader<out BaseObjectData>?,
        val objectDataParser: XParser<out BaseObjectData>
    )

    private val OBJECT_PROPERTIES_MAP = mapOf(
        Pair(SimpleSchemaObjectType.SCHEMA_ENTITY, ObjectProperty(SchemaEntityType.reader, SchemaEntityType.xParser)),
        Pair(SimpleSchemaObjectType.INDEX_PROVIDER, ObjectProperty(IndexProvider.reader, IndexProvider.xParser)),
        Pair(SimpleSchemaObjectType.SCHEMA_DOMAIN, ObjectProperty(Domain.reader, Domain.xParser))
    )

    /**
     * Get Reader for provided config type
     * @param @ConfigType
     * @return Reader
     */
    fun getReaderForObjectType(objectType: SimpleSchemaObjectType): Writeable.Reader<out BaseObjectData> {
        return OBJECT_PROPERTIES_MAP[objectType]?.objectDataReader
            ?: throw IllegalArgumentException("Transport action used with unknown ConfigType:$objectType")
    }

    /**
     * Validate config data is of ConfigType
     */
    fun validateObjectData(objectType: SimpleSchemaObjectType, objectData: BaseObjectData?): Boolean {
        return when (objectType) {
            SimpleSchemaObjectType.SCHEMA_ENTITY -> objectData is SchemaEntityType
            SimpleSchemaObjectType.INDEX_PROVIDER -> objectData is IndexProvider
            SimpleSchemaObjectType.SCHEMA_DOMAIN -> objectData is Domain
            SimpleSchemaObjectType.NONE -> true
        }
    }

    /**
     * Creates config data from parser for given configType
     * @param objectType the ConfigType
     * @param parser parser for ConfigType
     * @return created BaseObjectData on success. null if configType is not recognized
     *
     */
    fun createObjectData(objectType: SimpleSchemaObjectType, parser: XContentParser): BaseObjectData? {
        return OBJECT_PROPERTIES_MAP[objectType]?.objectDataParser?.parse(parser)
    }
}
