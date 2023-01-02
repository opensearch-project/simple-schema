package org.opensearch.graphql.translation;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLSchema;
import org.opensearch.schema.ontology.EnumeratedType;
import org.opensearch.schema.ontology.Value;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * translate the GQL enumerations into a names list of enums
 */
public class EnumTypeTranslation implements TranslationStrategy{

    public void translate(GraphQLSchema graphQLSchema, TranslationContext context) {
       List<EnumeratedType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLEnumType.class.isAssignableFrom(p.getClass()))
                .filter(p -> !context.getLanguageTypes().contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createEnum((GraphQLEnumType) ifc))
                .collect(Collectors.toList());

        context.getBuilder().withEnumeratedTypes(collect);
    }

    private EnumeratedType createEnum(GraphQLEnumType ifc) {
        AtomicInteger counter = new AtomicInteger(0);
        EnumeratedType.EnumeratedTypeBuilder builder = EnumeratedType.EnumeratedTypeBuilder.anEnumeratedType();
        builder.withEType(ifc.getName());
        builder.withValues(ifc.getValues().stream()
                .map(v -> new Value(counter.getAndIncrement(), v.getName()))
                .collect(Collectors.toList()));
        return builder.build();
    }

}
