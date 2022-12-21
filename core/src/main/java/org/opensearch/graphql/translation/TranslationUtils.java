package org.opensearch.graphql.translation;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import org.opensearch.schema.ontology.ObjectType;
import org.opensearch.schema.ontology.PrimitiveType;
import org.opensearch.schema.ontology.Property;
import org.opensearch.schema.ontology.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.opensearch.schema.ontology.PrimitiveType.Types.STRING;
import static org.opensearch.schema.ontology.PrimitiveType.Types.find;

/**
 * helper method for graphQL to Ontology translation
 */
public class TranslationUtils {

    /**
     * populate primitive property type according to entities
     *
     * @param type
     * @param fieldName
     * @return
     */
    public static Optional<Property> createProperty(GraphQLFieldDefinition type, String fieldName, TranslationStrategy.TranslationContext context) {
        //in case of nested object field
        if (type.getType() instanceof GraphQLObjectType) {
            String objTypeName = ((GraphQLObjectType) type.getType()).getName();
            return Optional.of(new Property(fieldName, objTypeName, new ObjectType(objTypeName)));
        }

        //scalar type property
        Type definitionType = type.getDefinition().getType();
        if ((definitionType instanceof TypeName) && (!context.getObjectTypes().contains(((TypeName) definitionType).getName()))) {
            return Optional.of(new Property(fieldName, fieldName, resolvePrimitiveType(definitionType)));
        }

        //list type
        if (definitionType instanceof ListType) {
            //in case of list with nested object field
            if (((GraphQLList) type.getType()).getWrappedType() instanceof GraphQLObjectType) {
                GraphQLObjectType wrappedType = (GraphQLObjectType) ((GraphQLList) type.getType()).getWrappedType();
                return Optional.of(new Property(fieldName, wrappedType.getName(), new ObjectType.ArrayOfObjects(wrappedType.getName())));
            }

            //case for list of primitives
            return Optional.of(new Property(fieldName, fieldName, resolvePrimitiveType(definitionType)));
        }
        //non-null type - may contain all sub-types (wrapper)
        if (definitionType instanceof NonNullType) {
            Type rawType = ((NonNullType) definitionType).getType();

            //in case of non-null with nested object field
            if (((GraphQLNonNull) type.getType()).getWrappedType() instanceof GraphQLObjectType) {
                GraphQLObjectType wrappedType = (GraphQLObjectType) ((GraphQLNonNull) type.getType()).getWrappedType();
                return Property.MandatoryProperty.of(Optional.of(new Property(fieldName, wrappedType.getName(), new ObjectType(wrappedType.getName()))));
            }

            //validate only scalars are registered as properties
            if ((rawType instanceof TypeName) && (!context.getObjectTypes().contains(((TypeName) rawType).getName()))) {
                return Property.MandatoryProperty.of(Optional.of(new Property(fieldName, fieldName, resolvePrimitiveType(rawType))));
            }

            if (rawType instanceof ListType) {
                return Property.MandatoryProperty.of(Optional.of(new Property(fieldName, fieldName, resolvePrimitiveType(rawType))));
            }
        }

        return Optional.empty();
    }

    private static PropertyType resolvePrimitiveType(Type type) {
        if (TypeName.class.equals(type.getClass())) {
            return find(((TypeName) type).getName())
                    //string is default
                    .orElse(PrimitiveType.Types.STRING).asType();
        } else if (ListType.class.equals(type.getClass())) {
            PropertyType propertyType = find(((TypeName) ((ListType) type).getType()).getName())
                    //string is default
                    .orElse(STRING).asType();
            return new PrimitiveType.ArrayOfPrimitives(propertyType);

        }
        return STRING.asType();
    }

    public static List<Property> populateProperties(List<GraphQLFieldDefinition> fieldDefinitions, TranslationStrategy.TranslationContext context) {
        Set<Property> collect = fieldDefinitions.stream()
                .filter(p -> Type.class.isAssignableFrom(p.getDefinition().getType().getClass()))
                .map(p -> createProperty(p, p.getName(), context))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        //add properties to context properties set
        context.addProperties(collect);
        return new ArrayList<>(collect);
    }

}
