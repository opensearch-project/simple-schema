package org.opensearch.schema.index.schema;

import javaslang.Tuple2;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EntityType;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.schema.IndexMappingUtils.*;


public class EntityMappingTranslator  implements MappingTranslator<EntityType,Entity>{

    @Override
    public Entity translate(EntityType entity, MappingTranslatorContext context) {
        //inferring MappingIndexType & NestingType - according to the directives and the nesting composition of the elements

        // switch for the different type of nesting
        // 1->  REFERENCE would generate adding a FK field
        // 2->  NESTED_REFERENCE would generate adding a FK nested field (for many-to-one)
        // 3->  NESTED would generate adding a nested document entity
        // 4->  CHILD would generate adding a Child document entity (as part of parent-child mapping)
        // 5->  EMBEDDED would generate adding an embedded document entity

        MappingIndexType mappingIndexType = calculateMappingType(entity, context.getAccessor());
        NestingType nestingType = calculateNestingType(entity, context.getAccessor());
        return createEntity(entity, mappingIndexType, nestingType,context.getAccessor());
    }


    public static Entity createEntity(EntityType e, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
        switch (nestingType) {
            case NESTED:
                REFERENCE:
                NESTED_REFERENCE:
                // create minimal representation  - TODO add redundant here
                // indices need to be lower cased
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
        return accessor.relationsPairsBySourceEntity(e, r -> !accessor.isForeignRelation(r) )
                .stream()
                //TODO switch for the different type of nesting
                // REFERENCE would generate adding a FK field
                // NESTED_REFERENCE would generate adding a FK nested field (for many-to-one)
                // NESTED would generate adding a nested document entity
                // CHILD would generate adding a Child document entity (as part of parent-child mapping)
                // EMBEDDED would generate adding an embedded document entity

                .map(p -> {
                            //in case of mutual reference - using the REFERENCE type mapping
                            if (accessor.isSelfReference(p)) {
                                return new Tuple2<>(p.getSideAFieldName(),
                                        createEntity(
                                                accessor.entity$(p.geteTypeB()),// relation destination entity type
                                                MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                                accessor.property$(p.getSideAFieldName()).getType().isArray() ? NestingType.NESTED_REFERENCE : NestingType.REFERENCE,
                                                accessor));
                            }
                            //other types of inner entities that are contained inside the wrapping element
                            return new Tuple2<>(p.getSideAFieldName(),
                                    createEntity(
                                            accessor.entity$(p.geteTypeB()),// relation destination entity type
                                            MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                            accessor.property$(p.getSideAFieldName()).getType().isArray() ? NestingType.NESTED : NestingType.EMBEDDED,
                                            accessor));
                        }
                ).collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }


}
