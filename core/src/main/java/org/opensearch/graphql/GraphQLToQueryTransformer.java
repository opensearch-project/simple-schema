package org.opensearch.graphql;


import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.opensearch.query.Query;
import org.opensearch.schema.SchemaError;

import java.util.stream.Collectors;

/**
 * This component is the transformation element which takes a GQL text query and using the existing GQL engine (expected to be generated)
 * will transform this GQL query into a valid IOQL - Intermediate Ontological Query Language
 */
public class GraphQLToQueryTransformer {

    public static Query transform(String query) {
        Query.Builder instance = Query.Builder.instance();
        ExecutionResult execute = GraphQLEngineFactory.engine().get().execute(query);
        if (execute.getErrors().isEmpty())
            return instance.build();
        // throw error over failed query parsing
        throw new SchemaError.SchemaErrorException("Error Transforming the GQL text query into a IOQL query",
                execute.getErrors().stream().map(GraphQLError::getMessage).collect(Collectors.toList()));
    }

}
