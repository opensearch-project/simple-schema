package org.opensearch.schema.index.schema;

import javaslang.Tuple2;
import org.opensearch.schema.ontology.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.schema.IndexMappingUtils.*;
import static org.opensearch.schema.index.schema.NestingType.NONE;
import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;

public class RelationMappingTranslator implements MappingTranslator<RelationshipType, Relation> {

    @Override
    public List<Relation> translate(RelationshipType relation, MappingTranslatorContext<Relation> context) {
        return relation.getePairs().stream()
                .map(pair -> context.putElement(createRelation(relation, pair, context.getAccessor())))
                .collect(Collectors.toList());
    }

    public static Relation createRelation(RelationshipType r, EPair pair, Accessor accessor) {

        Optional<DirectiveType> relationDirective = accessor.getDirective(pair, RELATION.getName());
        MappingIndexType mappingIndexType = calculateMappingType(r, pair, accessor);
        NestingType nestingType = calculateNestingType(r, pair, accessor);

        if (relationDirective.isEmpty()) {
            //simplified assumption of top level entities are static and without nesting
            return createRelation(r, pair, MappingIndexType.STATIC, NONE, accessor);
        }
        if (relationDirective.get().getArgument(RELATION.getArgument(0)).isEmpty()) {
            //simplified assumption of default embedded relation hierarchy between parent & child object
            return createRelation(r, pair, MappingIndexType.NONE, NONE, accessor);
        }

        // get the directive instruction of how this relationship is to be physically stored
        return createRelation(r, pair, mappingIndexType, nestingType, accessor);
    }

    public static Map<String, Relation> createNestedElements(RelationshipType r, EPair pair, Accessor accessor) {
        MappingIndexType mappingIndexType = calculateMappingType(r, pair, accessor);
        NestingType nestingType = calculateNestingType(r, pair, accessor);
        return r.getProperties().stream()
                .filter(p -> accessor.getNestedRelationByPropertyName(p).isPresent())
                .map(p ->
                        new Tuple2<>(p, createRelation(
                                accessor.getNestedRelationByPropertyName(p).get(),//relation type
                                pair,
                                mappingIndexType,
                                nestingType,
                                accessor))
                ).collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }

    public static Relation createRelation(RelationshipType r, EPair pair, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
        return new Relation(BaseTypeElement.Type.of(r.getName()), nestingType, mappingIndexType,
                false,
                pair.getDirectives(),
                createNestedElements(r, pair, accessor),
                // indices need to be lower cased
                createProperties(r.getName(), accessor),
                Collections.emptySet(), Collections.emptyMap());
    }
}
