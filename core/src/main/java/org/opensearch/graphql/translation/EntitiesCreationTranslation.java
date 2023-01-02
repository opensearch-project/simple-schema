package org.opensearch.graphql.translation;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Property;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.graphql.GraphQLSchemaUtils.*;
import static org.opensearch.graphql.translation.TranslationUtils.populateProperties;

/**
 * translate the GQL entities into ontological entities
 */
public class EntitiesCreationTranslation implements TranslationStrategy {

    public void translate(GraphQLSchema graphQLSchema, TranslationContext context) {
        List<EntityType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                .filter(p -> getDirective((GraphQLObjectType) p, "autoGen").isEmpty())
                .filter(p -> !context.getLanguageTypes().contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createEntity((GraphQLObjectType) ifc, context))
                .collect(Collectors.toList());
        context.getBuilder().addEntityTypes(collect);
    }

    /**
     * generate entity (interface) type
     *
     * @return
     */
    private EntityType createEntity(GraphQLObjectType object, TranslationStrategy.TranslationContext context) {
        List<Property> properties = populateProperties(object.getFieldDefinitions(), context);
        EntityType.Builder builder = EntityType.Builder.get();
        //populate id field if present
        getIDFieldName(object).ifPresent(builder::withIdField);
        builder.withName(object.getName()).withEType(object.getName());
        builder.withParentTypes(object.getInterfaces().stream()
                .filter(p -> context.getBuilder().getEntityType(p.getName()).isPresent())
                .map(p -> context.getBuilder().getEntityType(p.getName()).get().geteType()).collect(Collectors.toList()));
        builder.withProperties(properties.stream().map(Property::getName).collect(Collectors.toList()));
        builder.withMandatory(properties.stream()
                .filter(p -> p instanceof Property.MandatoryProperty).map(Property::getName).collect(Collectors.toList()));
        //populate directives
        builder.withDirectives(formatDirective(Optional.of(object)));
        return builder.build();
    }
}
