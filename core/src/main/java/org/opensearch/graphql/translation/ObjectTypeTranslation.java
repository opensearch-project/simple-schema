package org.opensearch.graphql.translation;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import java.util.stream.Stream;

/**
 * translate the GQL entities into a names list of types
 */
public class ObjectTypeTranslation implements TranslationStrategy{

    public void translate(GraphQLSchema graphQLSchema,TranslationStrategy.TranslationContext context) {
        context.addObjectTypes(Stream.concat(graphQLSchema.getAllTypesAsList().stream()
                                .filter(p -> GraphQLInterfaceType.class.isAssignableFrom(p.getClass()))
                                .map(GraphQLNamedSchemaElement::getName),
                        graphQLSchema.getAllTypesAsList().stream()
                                .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                                .map(GraphQLNamedSchemaElement::getName)
                )
                .filter(p -> !p.startsWith("__"))
                .filter(p -> !context.getLanguageTypes().contains(p)).toList());
    }

}
