package org.opensearch.graphql.translation;

import graphql.schema.GraphQLSchema;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.Property;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opensearch.graphql.GraphQLSchemaUtils.QUERY;

/**
 * Contains:
 * <br>
 * An interface for the translation of a specific section in the schema
 * <br>
 * A context for graphQL to Ontology translation session
 */
public interface TranslationStrategy {
    /**
     * translates the specific section of the GraphQLSchema into the same relevant section in the ontology
     * @param graphQLSchema
     * @param context
     */
    void translate(GraphQLSchema graphQLSchema, TranslationStrategy.TranslationContext context);

    /**
     * A context for graphQL to Ontology translation session
     */
    class TranslationContext {
        private Ontology.OntologyBuilder builder;
        private Set<String> objectTypes;
        private Set<Property> properties;

        private Set<String> languageTypes = new HashSet<>();

        public TranslationContext(String ontologyName) {
            objectTypes = new HashSet<>();
            properties = new HashSet<>();
            languageTypes.addAll(Arrays.asList(QUERY));
            builder = Ontology.OntologyBuilder.anOntology(ontologyName);

        }

        public void addObjectTypes(List<String> types) {
            objectTypes.addAll(types);
        }

        public Ontology.OntologyBuilder getBuilder() {
            return builder;
        }

        public void addProperties(Set<Property> properties) {
            this.properties.addAll(properties);
        }

        public Set<Property> getProperties() {
            return properties;
        }

        public Set<String> getObjectTypes() {
            return objectTypes;
        }

        public Set<String> getLanguageTypes() {
            return languageTypes;
        }

        public Ontology build() {
            return builder.build();
        }
    }
}
