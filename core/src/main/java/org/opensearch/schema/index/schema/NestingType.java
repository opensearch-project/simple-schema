package org.opensearch.schema.index.schema;

import org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType;

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
    EMBEDDED,

    //internal document which will be a nested document inside the same lucene segment
    NESTED,

    //internal skeleton document (containing only FK and optional redundant fields) which will be a nested document inside the same lucene segment
    // additional remote index for the actual entity will be generated as well
    REFERENCE,

    //same as REFERENCE but nested to allow one to many type of relationships
    NESTED_REFERENCE,

    //stored as parent-child model = this is the child
    CHILD;

    static NestingType translate(PhysicalEntityRelationsDirectiveType directive) {
        switch (directive) {
            case NESTED:
                return NESTED;

            //verify if the internal entity is singular -> use REFERENCE, if is list -> use NESTED_REFERENCE
            // currently supporting only a REFERENCE type for a many-to-many join relationship table
            case FOREIGN:
//                return NESTED_REFERENCE; //todo - for future support
                return REFERENCE;

            case REVERSE:
                return NONE;
            case CHILD:
                return CHILD;
            default:
                return EMBEDDED;
        }
    }
}
