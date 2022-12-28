package org.opensearch.schema.index.schema;

/**
 * Representing the nesting documents physical strategy, this nesting strategy can be implemented in one of the following:
 * <br>
 *  NONE - no nesting
 * <br>
 *  EMBEDDED - internal document which will be flattened to a dot separated key path
 * <br>
 *  NESTED - internal document which will be a nested document inside the same lucene segment
 * <br>
 *  CHILD - internal document which will be stored as parent-child model ( this is the child )
 * <br>
 * REFERENCE - internal skeleton document which containing only FK and optional redundant fields - the document which will be a nested document inside the same lucene segment
 *            an additional remote index for the actual remote (referenced) entity must be generated as well
 * <br>
 *
 * NESTED_REFERENCE - same as REFERENCE but indicates that the nested element allow one-to-many relationships ( list of nested entities)
 *
 */
public enum NestingType {
    //no nesting
    NONE,

    //internal document which will be flattened to a dot separated key path
    EMBEDDING,

    //internal document which will be a nested document inside the same lucene segment
    NESTING,

    //internal skeleton document (containing only FK and optional redundant fields) which will be a nested document inside the same lucene segment
    // additional remote index for the actual entity will be generated as well
    REFERENCE,

    //same as REFERENCE but nested to allow one to many type of relationships
    NESTED_REFERENCE,

    //stored as parent-child model = this is the child
    CHILD;
}
