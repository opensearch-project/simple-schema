package org.opensearch.schema.index.schema;

/**
 * The physical mapping strategies of representing entities and their relations
 * <br>
 * STATIC - this is the regular standard index where an entity is directly mapped into the index
 * <br>
 * UNIFIED - this is the unified global index where all available entities are mapped into the same index
 * <br>
 * PARTITIONED - this is a static based mapping with a specific key functioning as a partitioning filter to separate the data into different indices (mostly used for time based partitioning)
 * <br>
 * NESTED - this indicated that the entity mapping is nested inside another entity's mapping - the following options for nesting are available see {@link org.opensearch.schema.index.schema.NestingType}
 *       - embedded
 *       - nested
 *       - child
 *       - reference
 * <br>
 * NONE - this indicates that this entity has no independent mapping of its own, nor it is nested
 *
 * <br>
 *
 * Entity Vs Relation:
 *
 *   - Entity document may be mapped in any of the listed above mapping strategies regardless of how the entities are logically composed (nested or not)
 *   this allows separating the logical structure from the physical storage strategies.
 *
 *   - Relation documents represents the connection between the logical entities (one-to-one, one-to-many, many-to-many)
 *   this states that the physical relationship manifestation is coupled to how the physical entities are actually stored, for example -
 *   <br>
 *    - For Author->Books relation in a case where the books are nested inside the author mapping - the relation (Author)->[has_Books]-(Book) can only be
 *    represented in the actual structure of the Author entity index and the relationship will have no independent physical (index) representation of its own
 *    - NONE - no physical index representing the relationship
 *
 *   <br>
 *    - For Author->Books relation in a case where the books are stored separately from the author mapping - the relation (Author)->[has_Books]-(Book) can be
 *    represented in the next manner (mapping type):
 *      - STATIC - meaning that the has_Books relation would reside in its own index
 *      - PARTITIONED - meaning that the has_Books relation would reside in its own index with respect to the partition key (preferably time based key)
 *
 *   <br>
 *    - For Author->Books relation in a case where the books are stored separately from the author mapping and there may be multiple types of relationships between the
 *    Author & Book :
 *      (Author)->[written]-(Books)
 *      (Author)->[recommended]-(Books)
 *
 *      the relationships written & recommended can be represented in the next manner (mapping type):
 *      - STATIC - meaning that the written / recommended relation would reside in their own index
 *      - PARTITIONED - meaning that written / recommended relation  reside in their own index with respect to the partition key (preferably time based key)
 *      - UNIFIED - meaning that all the relations (written,recommended) between Author & books would be stored in a generic index representing both type of relations
 *
 *  <br>
 *      NESTED - currently has no concrete use for relationship physical representation
 *
 *
 */
public enum MappingIndexType {
    //static index - a standard index
    STATIC,

    //common general index - unifies all entities under the same physical index
    UNIFIED,

    //a key-based key partitioned index - each index would have a defined interval
    PARTITIONED,

    //nested represents this (entity) mapping has a nesting specific mapping - see {@link org.opensearch.schema.index.schema.NestingType}
    NESTED,

    //none represents this (entity) mapping has no Independent mapping of it own
    NONE,
}
