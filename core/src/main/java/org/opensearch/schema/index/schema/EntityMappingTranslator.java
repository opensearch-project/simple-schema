package org.opensearch.schema.index.schema;

import javaslang.Tuple2;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EPair;
import org.opensearch.schema.ontology.EntityType;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.schema.IndexMappingUtils.*;


public class EntityMappingTranslator implements MappingTranslator<EntityType, Entity> {

    @Override
    public List<Entity> translate(EntityType entity, MappingTranslatorContext context) {
        //inferring MappingIndexType & NestingType - according to the directives and the nesting composition of the elements

        // switch for the different type of nesting
        // 1->  REFERENCE would generate adding a FK field
        // 2->  NESTED_REFERENCE would generate adding a FK nested field (for many-to-one)
        // 3->  NESTED would generate adding a nested document entity
        // 4->  CHILD would generate adding a Child document entity (as part of parent-child mapping)
        // 5->  EMBEDDED would generate adding an embedded document entity
        // 5->  NONE would not generate anything

        MappingIndexType mappingIndexType = calculateMappingType(entity, context.getAccessor());
        NestingType nestingType = calculateNestingType(Optional.empty(),entity, context.getAccessor());
        return List.of(Objects.requireNonNull(createEntity(entity, mappingIndexType, nestingType, context.getAccessor())));
    }


    public static Entity createEntity(EntityType e, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
        switch (nestingType) {
            case REFERENCE:
            case NESTED_REFERENCE:
                // create minimal representation  - TODO add redundant here
                // indices need to be lower cased
                // nested entities are not created for a reference typed entity
                return new Entity(BaseTypeElement.Type.of(e.getName()), nestingType, mappingIndexType,
                        createProperties(e.getName(), accessor));
            default:
                return new Entity(BaseTypeElement.Type.of(e.getName()), nestingType, mappingIndexType,
                        e.getDirectives(),
                        // indices need to be lower cased
                        createProperties(e.getName(), accessor),
                        createNestedElements(e, accessor),
                        Collections.emptyMap());
        }
    }

    public static Map<String, Entity> createNestedElements(EntityType e, Accessor accessor) {
        //first filter out the logical nested entities which have physical foreign directives
        return accessor.relationsPairsBySourceEntity(e, r -> true)
                .stream()
                //switch for the different type of nesting
                // REFERENCE would generate adding a FK field
                // NESTED_REFERENCE would generate adding a FK nested field (for many-to-one)
                // NESTED would generate adding a nested document entity
                // CHILD would generate adding a Child document entity (as part of parent-child mapping)
                // EMBEDDED would generate adding an embedded document entity

                .map(p -> {
                            MappingIndexType mappingIndexType = calculateMappingType(accessor.entity$(p.geteTypeB()), accessor);
                            NestingType nestingType = calculateNestingType(Optional.of(e),accessor.entity$(p.geteTypeB()), accessor);
                            // when an entity is self referencing (they must reside in the same index)
                            if (accessor.isSelfReference(p)) {
                                nestingType = p.getReferenceType().equals(EPair.RelationReferenceType.ONE_TO_ONE) ? NestingType.REFERENCE : NestingType.NESTED_REFERENCE;
                            }
                            //in case of mutual reference - using the NONE type mapping since all keys are located in the join index
                            if (accessor.isJoinIndexForeignRelation(p)) {
                                //no physical representation of nested elements here - all fields reside in the join table
                                return null;
                            }
                            //in case of foreign reference - using the REFERENCE type mapping
                            if (accessor.isForeignRelation(p)) {
                                nestingType = p.getReferenceType().equals(EPair.RelationReferenceType.ONE_TO_ONE) ? NestingType.REFERENCE : NestingType.NESTED_REFERENCE;
                            }
                            //in case of a reverse reference - there is no physical representation to this logical relationship
                            if (accessor.isReverseRelation(p)) {
                                //no physical representation of nested elements here - all fields reside in the parent document
                                return null;
                            }
                            //other types of inner entities that are contained inside the wrapping element
                            return new Tuple2<>(p.getSideAFieldName(),
                                    createEntity(
                                            accessor.entity$(p.geteTypeB()),// relation destination entity type
                                            mappingIndexType,
//                                            accessor.property$(p.getSideAFieldName()).getType().isArray() ? NestingType.NESTED : NestingType.EMBEDDED,
                                            nestingType,
                                            accessor));
                        }
                ).filter(Objects::nonNull)
                .collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }


}
