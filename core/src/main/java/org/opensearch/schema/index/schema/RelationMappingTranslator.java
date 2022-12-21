package org.opensearch.schema.index.schema;

import javaslang.Tuple2;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.DirectiveType;
import org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType;
import org.opensearch.schema.ontology.RelationshipType;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.schema.IndexMappingUtils.createProperties;
import static org.opensearch.schema.index.schema.NestingType.NONE;
import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;

public class RelationMappingTranslator implements MappingTranslator<RelationshipType,Relation>{

    @Override
    public Relation translate(RelationshipType relation, MappingTranslatorContext context) {
        return createRelation(relation,context.getAccessor());
    }

    public static Relation createRelation(RelationshipType r, Accessor accessor) {
        Optional<DirectiveType> relationDirective = r.getDirectives().stream().filter(d -> RELATION.isSame(d.getName())).findAny();
        if (relationDirective.isEmpty()) {
            //simplified assumption of top level entities are static and without nesting
            return createRelation(r, MappingIndexType.STATIC, NONE, accessor);
        }
        if (relationDirective.get().getArgument(RELATION.getArgument(0)).isEmpty()) {
            //simplified assumption of default embedded relation hierarchy between parent & child object
            return createRelation(r, MappingIndexType.NONE, NestingType.EMBEDDED, accessor);
        }

        // get the directive instruction of how this relationship is to be physically stored
        DirectiveType.Argument argument = relationDirective.get().getArgument(RELATION.getArgument(0)).get();
        PhysicalEntityRelationsDirectiveType relationsDirective = PhysicalEntityRelationsDirectiveType.from(argument.value.toString());

        return createRelation(r, MappingIndexType.NONE, NestingType.translate(relationsDirective), accessor);
    }

    public static Relation createRelation(RelationshipType r, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
        return new Relation(BaseTypeElement.Type.of(r.getName()), nestingType, mappingIndexType,
                false,
                r.getDirectives(),
                createNestedElements(r, accessor),
                // indices need to be lower cased
                createProperties(r.getName(), accessor),
                Collections.emptySet(), Collections.emptyMap());
    }

    public static Map<String, Relation> createNestedElements(RelationshipType r, Accessor accessor) {
        return r.getProperties().stream()
                .filter(p -> accessor.getNestedRelationByPropertyName(p).isPresent())
                .map(p ->
                        new Tuple2<>(p, createRelation(
                                accessor.getNestedRelationByPropertyName(p).get(),//relation type
                                MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                accessor.property$(p).getType().isArray() ? NestingType.NESTED : NestingType.EMBEDDED,
                                accessor))
                ).collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }


}
