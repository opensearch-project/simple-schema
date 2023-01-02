package org.opensearch.schema.index.schema;

import com.google.common.collect.ImmutableList;
import org.opensearch.schema.ontology.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class IndexMappingUtils {

    public static final String FIELDS = "fields";
    public static final String MAPPING_TYPE = "mappingType";
    public static final String NAME = "name";

    public static Props createProperties(String name, Accessor accessor) {
        return new Props(ImmutableList.of(name));
    }

    /**
     * derive the entity physical index mapping type
     *
     * @param entity
     * @param accessor
     * @return
     */
    public static MappingIndexType calculateMappingType(EntityType entity, Accessor accessor) {
        //first: if entity has directive stating a @model - it should have a MappingIndexType.STATIC mapping
        if (accessor.getDirective(entity, DirectiveEnumTypes.MODEL.getName()).isPresent()) {
            return MappingIndexType.STATIC;
        }
        //Second: if entity is nested within another entity -> its considered as nested
        if (accessor.isNestedEntity(entity.geteType())) {
            return MappingIndexType.NESTED;
        }

        return MappingIndexType.NONE;
    }

    /**
     * infer the relation physical index nesting type
     *
     * @param relation
     * @param accessor
     * @return
     */
    public static MappingIndexType calculateMappingType(RelationshipType relation, EPair pair, Accessor accessor) {
        //first: if relation has directive stating a @model - it should have a MappingIndexType.STATIC mapping
        if (accessor.getDirective(relation, DirectiveEnumTypes.MODEL.getName()).isPresent()) {
            return MappingIndexType.STATIC;
        }

        //second: if relation is foreign to another relation -> its considered as static
        if (accessor.isJoinIndexForeignRelation(pair)) {
            return MappingIndexType.STATIC;
        }

        return MappingIndexType.NONE;
    }

    /**
     * derive the physical index nesting type
     *
     * @param parent
     * @param entity
     * @param accessor
     * @return
     */
    public static NestingType calculateNestingType(Optional<EntityType> parent, EntityType entity, Accessor accessor) {
        //entity is considered nested if its mapping type was determined nesting due to its being internal in another entity
        if (!calculateMappingType(entity, accessor).equals(MappingIndexType.NESTED))
            return NestingType.NONE;

        //expecting to have a parent entity for NESTED MappingIndexType entities
        if(parent.isEmpty())
            return NestingType.NONE;

        //get the relationship where the given entity is the target (sideB) and the parent is the source
        List<EPair> pairs = accessor.relationsPairs(parent.get(), entity );
        if (pairs.isEmpty()) {
            //without any other indication the default nesting strategy would become 'nesting documents'
            return NestingType.NESTING;
        }

        //todo - here we need to select the appropriate one according to the context of the container entity - for now selecting the first one
        EPair pair = pairs.get(0);

        // if relation has directive stating a @relation -
        if (accessor.getDirective(pair, DirectiveEnumTypes.RELATION.getName()).isPresent()) {
            DirectiveType relationDirective = accessor.getDirective(pair, DirectiveEnumTypes.RELATION.getName()).get();
            DirectiveType.Argument mappingType = relationDirective.getArgument(MAPPING_TYPE).get();
            //first: if relation has directive with mappingType = EMBEDDED
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.EMBEDDED.getName()))
                return NestingType.EMBEDDING;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.NESTED.getName()))
                return NestingType.NESTING;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))
                return NestingType.REFERENCE;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.CHILD.getName()))
                return NestingType.CHILD;
        }
        //without any other indication the default nesting strategy would become 'nesting documents'
        return NestingType.NESTING;
    }

    /**
     * derive the physical index nesting type
     *
     * @param relation
     * @param accessor
     * @return
     */
    public static NestingType calculateNestingType(RelationshipType relation, EPair pair, Accessor accessor) {
        //relation is considered nested if its mapping type was determined nesting due to its being internal in another relation
        if (!calculateMappingType(relation, pair, accessor).equals(MappingIndexType.NESTED))
            return NestingType.NONE;

        // if relation has directive stating a @relation -
        if (accessor.getDirective(pair, DirectiveEnumTypes.RELATION.getName()).isPresent()) {
            DirectiveType relationDirective = accessor.getDirective(relation, DirectiveEnumTypes.RELATION.getName()).get();
            DirectiveType.Argument mappingType = relationDirective.getArgument(MAPPING_TYPE).get();
            //first: if relation has directive with mappingType = EMBEDDED
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.EMBEDDED.getName()))
                return NestingType.EMBEDDING;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.NESTED.getName()))
                return NestingType.NESTING;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))
                return NestingType.REFERENCE;
            if (mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.CHILD.getName()))
                return NestingType.CHILD;
        }
        //without any other indication the default nesting strategy would become 'nesting documents'
        return NestingType.NESTING;

    }
}
