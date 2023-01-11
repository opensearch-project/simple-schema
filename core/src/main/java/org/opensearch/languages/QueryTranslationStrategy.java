package org.opensearch.languages;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
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
public interface QueryTranslationStrategy<T extends QueryBuilder > {

    Optional<Object> translate(QueryTranslatorContext<T> context, GraphQLType fieldType) ;

    class QueryTranslatorContext<T extends QueryBuilder> {
        private GraphQLSchema schema;
        private T builder;
        private DataFetchingEnvironment env;
        private Accessor accessor;
        private Map<String, Integer> pathContext;


        public QueryTranslatorContext(Accessor accessor, T builder,GraphQLSchema schema, DataFetchingEnvironment env) {
            this.accessor = accessor;
            this.builder = builder;
            this.schema = schema;
            this.env = env;
            this.pathContext = new HashMap<>();
        }

        public QueryTranslatorContext(Accessor accessor, T builder, GraphQLSchema schema) {
            this(accessor,builder,schema,null);
        }

        public GraphQLSchema getSchema() {
            return schema;
        }

        public T getBuilder() {
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
