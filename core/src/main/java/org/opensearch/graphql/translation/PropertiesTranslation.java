package org.opensearch.graphql.translation;

import graphql.schema.*;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.PrimitiveType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * translate the GQL entity's & relation's properties into a flat list of ontology properties
 */
public class PropertiesTranslation implements TranslationStrategy{

    public void translate(GraphQLSchema graphQLSchema, TranslationContext context) {
        context.getBuilder().withProperties(new HashSet<>(context.getProperties()));
    }
}
