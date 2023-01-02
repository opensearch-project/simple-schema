package org.opensearch.languages.oql.graphql.wiring;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.schema.ontology.Accessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Contains:
 * <br>
 * An interface for the translation of a specific section in the query
 * <br>
 * A context for graphQL to Ontology Query translation session
 */
public interface QueryTranslationStrategy {

    Optional<Object> translate(QueryTranslatorContext context, GraphQLType fieldType) ;

    class QueryTranslatorContext {
        private GraphQLSchema schema;
        private Query.Builder builder;
        private DataFetchingEnvironment env;
        private Accessor accessor;
        private Map<String, Integer> pathContext;


        public QueryTranslatorContext(Accessor accessor, Query.Builder builder,GraphQLSchema schema, DataFetchingEnvironment env) {
            this.accessor = accessor;
            this.builder = builder;
            this.schema = schema;
            this.env = env;
            this.pathContext = new HashMap<>();
        }

        public QueryTranslatorContext(Accessor accessor, Query.Builder builder, GraphQLSchema schema) {
            this(accessor,builder,schema,null);
        }

        public GraphQLSchema getSchema() {
            return schema;
        }

        public Query.Builder getBuilder() {
            return builder;
        }

        public DataFetchingEnvironment getEnv() {
            return env;
        }

        public Accessor getAccessor() {
            return accessor;
        }

        public Map<String, Integer> getPathContext() {
            return pathContext;
        }

        public void setEnv(DataFetchingEnvironment env) {
            this.env = env;
        }
    }
}
