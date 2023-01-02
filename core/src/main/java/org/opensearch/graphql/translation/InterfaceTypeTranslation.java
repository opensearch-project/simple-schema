package org.opensearch.graphql.translation;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLSchema;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Property;

import java.util.List;
import java.util.stream.Collectors;

import static org.opensearch.graphql.translation.TranslationUtils.populateProperties;

/**
 * translate the GQL interfaces into an ontology list of types
 */
public class InterfaceTypeTranslation implements TranslationStrategy {

    public void translate(GraphQLSchema graphQLSchema, TranslationContext context) {
        List<EntityType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLInterfaceType.class.isAssignableFrom(p.getClass()))
                .map(ifc -> createInterface(((GraphQLInterfaceType) ifc), context))
                .collect(Collectors.toList());
        context.getBuilder().addEntityTypes(collect);
    }

    private EntityType createInterface(GraphQLInterfaceType ifc, TranslationStrategy.TranslationContext context) {
        List<Property> properties = populateProperties(ifc.getFieldDefinitions(), context);
        EntityType.Builder builder = EntityType.Builder.get();
        builder.withName(ifc.getName()).withEType(ifc.getName());
        builder.isAbstract(true);
        builder.withProperties(properties.stream().map(Property::getName).collect(Collectors.toList()));
        builder.withMandatory(properties.stream()
                .filter(p -> p instanceof Property.MandatoryProperty).map(Property::getName).collect(Collectors.toList()));

        return builder.build();

    }
}
