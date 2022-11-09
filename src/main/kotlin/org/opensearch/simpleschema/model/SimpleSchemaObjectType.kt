/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.simpleschema.model

import org.opensearch.commons.utils.EnumParser
import org.opensearch.simpleschema.model.RestTag.INDEX_PROVIDER_FIELD
import org.opensearch.simpleschema.model.RestTag.SCHEMA_ENTITY_FIELD
import java.util.*

/**
 * Enum for SimpleSchemaObject type
 */
enum class SimpleSchemaObjectType(val tag: String) {
    NONE("none") {
        override fun toString(): String {
            return tag
        }
    },
    SCHEMA_ENTITY(SCHEMA_ENTITY_FIELD) {
        override fun toString(): String {
            return tag
        }
    },
    INDEX_PROVIDER(INDEX_PROVIDER_FIELD) {
        override fun toString(): String {
            return tag
        }
    };

    companion object {
        private val tagMap = values().associateBy { it.tag }

        val enumParser = EnumParser { fromTagOrDefault(it) }

        /**
         * Get ConfigType from tag or NONE if not found
         * @param tag the tag
         * @return ConfigType corresponding to tag. NONE if invalid tag.
         */
        fun fromTagOrDefault(tag: String): SimpleSchemaObjectType {
            return tagMap[tag] ?: NONE
        }

        fun getAll(): EnumSet<SimpleSchemaObjectType> {
            val allTypes = EnumSet.allOf(SimpleSchemaObjectType::class.java)
            allTypes.remove(NONE)
            return allTypes
        }
    }
}
