package org.opensearch.graphql.translation;

import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import org.opensearch.schema.ontology.PrimitiveType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * translate the GQL primitives into a names list of ontology primitives
 */
public class PrimitivesTranslation implements TranslationStrategy{

    public void translate(GraphQLSchema graphQLSchema, TranslationContext context) {
        Set<PrimitiveType> types = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLScalarType.class.isAssignableFrom(p.getClass()))
                .filter(p -> !PrimitiveType.Types.contains(p.getName().toUpperCase()))
                .map(p -> createPrimitive((GraphQLScalarType) p))
                .collect(Collectors.toSet());
        context.getBuilder().withPrimitives(types);
    }

    private PrimitiveType createPrimitive(GraphQLScalarType scalar) {
        return new PrimitiveType(scalar.getName().toLowerCase(), resolvePrimitive(scalar));
    }

    private Class resolvePrimitive(GraphQLScalarType scalar) {
        try {
            return scalar.getCoercing().getClass().getDeclaredMethod("parseValue", Object.class).getReturnType();
        } catch (NoSuchMethodException e) {
            return Object.class;
        }
    }


}
