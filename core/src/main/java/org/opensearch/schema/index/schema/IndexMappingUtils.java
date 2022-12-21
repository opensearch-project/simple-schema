package org.opensearch.schema.index.schema;

import com.google.common.collect.ImmutableList;
import org.opensearch.schema.ontology.*;

import java.util.Collection;
import java.util.List;

public class IndexMappingUtils {

    public static final String MAPPING_TYPE = "mappingType";

    public static Props createProperties(String name, Accessor accessor) {
        return new Props(ImmutableList.of(name));
    }

    /**
     * derive the entity physical index mapping type
     * @param entity
     * @param accessor
     * @return
     */
    public static MappingIndexType calculateMappingType(EntityType entity, Accessor accessor) {
        //first: if entity has directive stating a @model - it should have a MappingIndexType.STATIC mapping
        if(accessor.getDirective(entity, DirectiveEnumTypes.MODEL.getName()).isPresent()) {
            return MappingIndexType.STATIC;
        }
        //Second: if entity is nested within another entity -> its considered as nested
        if(accessor.isNestedEntity(entity.geteType())){
            return MappingIndexType.NESTED;
        }

        return MappingIndexType.NONE;
    }

    /**
     * derive the relation physical index mapping type
     * @param relation
     * @param accessor
     * @return
     */
    public static MappingIndexType calculateMappingType(RelationshipType relation, Accessor accessor) {
        //first: if relation has directive stating a @model - it should have a MappingIndexType.STATIC mapping
        if(accessor.getDirective(relation, DirectiveEnumTypes.MODEL.getName()).isPresent()) {
            return MappingIndexType.STATIC;
        }
        //Second: if relation is foreign to another relation -> its considered as static
        if(accessor.isForeignRelation(relation)){
            return MappingIndexType.STATIC;
        }

        return MappingIndexType.NONE;
    }

    /**
     * derive the entity physical index mapping type
     * @param entity
     * @param accessor
     * @return
     */
    public static NestingType calculateNestingType(EntityType entity, Accessor accessor) {
        //entity is considered nested if its mapping type was determined nesting due to its being internal in another entity
        if(!calculateMappingType(entity,accessor).equals(MappingIndexType.NESTED))
            return NestingType.NONE;

        //get the relationship where the given entity is the target (sideB)
        Collection<EPair> pairs = accessor.relationsPairsByTargetEntity(entity, r -> !accessor.isForeignRelation(r));
        if(pairs.isEmpty()) {
            //without any other indication the default nesting strategy would become 'nesting documents'
            return NestingType.NESTED;
        }

        //relation pair where sideA is the container and sideB is the nested
        List<RelationshipType> relationshipTypes = accessor.relationByTargetEntity(entity);
        //todo - here we need to select the appropriate one according to the context of the container entity - for now selecting the first one

        RelationshipType relationshipType = relationshipTypes.get(0);

        // if relation has directive stating a @relation -
        if(accessor.getDirective(relationshipType, DirectiveEnumTypes.RELATION.getName()).isPresent()) {
            DirectiveType relationDirective = accessor.getDirective(relationshipType, DirectiveEnumTypes.RELATION.getName()).get();
            DirectiveType.Argument mappingType = relationDirective.getArgument(MAPPING_TYPE).get();
            //first: if relation has directive with mappingType = EMBEDDED
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.EMBEDDED.getName()))
                return NestingType.EMBEDDED;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.NESTED.getName()))
                return NestingType.NESTED;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))
                return NestingType.REFERENCE;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.CHILD.getName()))
                return NestingType.CHILD;
        }
        //without any other indication the default nesting strategy would become 'nesting documents'
        return NestingType.NESTED;
    }
    /**
     * derive the relation physical index mapping type
     * @param relation
     * @param accessor
     * @return
     */
    public static NestingType calculateNestingType(RelationshipType relation, Accessor accessor) {
        //relation is considered nested if its mapping type was determined nesting due to its being internal in another relation
        if(!calculateMappingType(relation,accessor).equals(MappingIndexType.NESTED))
            return NestingType.NONE;

        // if relation has directive stating a @relation -
        if(accessor.getDirective(relation, DirectiveEnumTypes.RELATION.getName()).isPresent()) {
            DirectiveType relationDirective = accessor.getDirective(relation, DirectiveEnumTypes.RELATION.getName()).get();
            DirectiveType.Argument mappingType = relationDirective.getArgument(MAPPING_TYPE).get();
            //first: if relation has directive with mappingType = EMBEDDED
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.EMBEDDED.getName()))
                return NestingType.EMBEDDED;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.NESTED.getName()))
                return NestingType.NESTED;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.FOREIGN.getName()))
                return NestingType.REFERENCE;
            if( mappingType.equalsValue(PhysicalEntityRelationsDirectiveType.CHILD.getName()))
                return NestingType.CHILD;
        }
        //without any other indication the default nesting strategy would become 'nesting documents'
        return NestingType.NESTED;

    }
}
