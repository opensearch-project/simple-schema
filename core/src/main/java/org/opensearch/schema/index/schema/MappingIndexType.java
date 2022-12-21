package org.opensearch.schema.index.schema;

/**
 * the physical mapping strategies of representing entities and their relations
 */
public enum MappingIndexType {
    //static index - a standard index
    STATIC,
    //common general index - unifies all entities under the same physical index
    UNIFIED,
    //time partitioned index
    TIME,
    //none represents this (entity) mapping has no Independent mapping of it own
    NONE,
}
