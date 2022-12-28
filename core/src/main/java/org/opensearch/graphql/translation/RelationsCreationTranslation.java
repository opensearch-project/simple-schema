package org.opensearch.graphql.translation;

import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import javaslang.Tuple2;
import org.opensearch.schema.ontology.EPair;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.RelationshipType;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.opensearch.graphql.GraphQLSchemaUtils.*;

/**
 * translate the GQL relations into ontological entities
 */
public class RelationsCreationTranslation implements TranslationStrategy {
    public static final String HAS = "has_";

    public void translate(GraphQLSchema graphQLSchema, TranslationStrategy.TranslationContext context) {
        Map<String, List<RelationshipType>> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                .filter(p -> getDirective((GraphQLObjectType) p, "autoGen").isEmpty())
                .filter(p -> !context.getLanguageTypes().contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createRelation(ifc.getName(), ((GraphQLObjectType) ifc).getFieldDefinitions(), context))
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(RelationshipType::getrType));

        //merge e-pairs
        collect.forEach((key, value) -> {
            List<EPair> pairs = value.stream()
                    .flatMap(ep -> ep.getePairs().stream())
                    .collect(Collectors.toList());
            //replace multi relationships with one containing all epairs
            RelationshipType relationshipType = value.get(0);
            relationshipType.setePairs(pairs);
            context.getBuilder().addRelationshipType(relationshipType);
        });
    }

    /**
     * @param name
     * @param fieldDefinitions
     * @return
     */
    private List<RelationshipType> createRelation(String name, List<GraphQLFieldDefinition> fieldDefinitions, TranslationStrategy.TranslationContext context) {
        Set<Tuple2<String, TypeName>> typeNames = fieldDefinitions.stream()
                .filter(p -> Type.class.isAssignableFrom(p.getDefinition().getType().getClass()))
                .map(p -> filter(p.getDefinition().getType(), p.getName(), type -> context.getObjectTypes().contains(type.getName())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        //relationships for each entity
        List<RelationshipType> collect = typeNames.stream()
                //t is a tuple<fieldName,fieldType>
                .map(t -> new Tuple2<>(t._1, context.getBuilder().getEntityType(t._2.getName())))
                .filter(t -> t._2.isPresent())
                .map(t -> new Tuple2<>(t._1, t._2.get()))
                .map(t -> RelationshipType.Builder.get()
                        .withIdField(t._2().getIdField())
                        //nested objects are directional by nature (nesting dictates the direction)
                        .withDirectional(true)
                        .withName(getRelationName(t._2.geteType()))//field name
                        .withRType(getRelationName(t._2.geteType()))//field name is the relation type
                        .withEPairs(singletonList(createEPair(name, fieldDefinitions, t, context)))
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    private String getRelationName(String name) {
        return name.startsWith(HAS) ? name : HAS + name;
    }

    private EPair createEPair(String name, List<GraphQLFieldDefinition> fieldDefinitions, Tuple2<String, EntityType> t, TranslationContext context) {
        EntityType sideA = context.getBuilder().getEntityType(name).get();
        String sideAFieldName = t._1;
        EntityType sideB = t._2;
        EPair.RelationReferenceType relationReferenceType = calculateReferenceType(name, t, context);
        //get the directives for the relationship pair
        return new EPair(formatDirective(getFieldByName(fieldDefinitions, t._1)), sideA.geteType(), relationReferenceType, sideAFieldName, sideA.idFieldName(), sideB.geteType(), sideB.idFieldName());
    }

    /**
     * calculate the RelationReferenceType according to the next rules:
     * <br>
     * - one-to-one : in case the sideB entity is not a list
     * <br>
     * - one-to-many : in case the sideB entity is a list
     * <br>
     * - many-to-many : in case the sideB entity is a list and the relation's opposite side is also a list
     * this will be calculated in a post build pass only after all the relationships are creted
     *
     * @param name
     * @param tuple2
     * @param context
     * @return
     */
    private EPair.RelationReferenceType calculateReferenceType(String name, Tuple2<String, EntityType> tuple2, TranslationContext context) {
        //todo implement
        return null;
    }

}
