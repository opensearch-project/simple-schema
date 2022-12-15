package org.opensearch.schema.index.schema;

/**
 * representing the mapping partitioning physical strategy
 */
public enum NestingType {
    //no nesting
    NONE,
    //internal document which will be flattened to a dot separated key path
    EMBEDDED,
    //internal document which will be a nested document inside the same lucene segment
    NESTED,
    //internal skeleton document (containing only FK and redundant fields) which will be a nested document inside the same lucene segment
    // additional remote index for the actual entity will be generated as well
    REFERENCE,
    //same as REFERENCE but nested to allow one to many type of relationships
    NESTED_REFERENCE
}
