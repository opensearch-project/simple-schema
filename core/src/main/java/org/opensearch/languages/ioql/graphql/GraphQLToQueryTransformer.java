package org.opensearch.languages.ioql.graphql;


import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.Transformer;
import org.opensearch.languages.ioql.graphql.wiring.TraversalWiringFactory;
import org.opensearch.languages.ioql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;

/**
 * This component is the transformation element which takes a GQL text query and using the existing GQL engine (expected to be generated)
 * will transform this GQL query into a valid IOQL - Intermediate Ontological Query Language
 */
public class GraphQLToQueryTransformer implements Transformer<Query> {

    /**
     * translates graphQL textual query into a IOQL (Intermediate Ontological Query Language) query
     * @param accessor
     * @param query
     * @return
     */
    public synchronized Query transform(Accessor accessor, String query) {
        Query.Builder instance = Query.Builder.instance();
        TraversalWiringFactory factory = new TraversalWiringFactory(accessor, instance);
        ExecutionResult execute = GraphQLEngineFactory
                .generateEngine(
                        GraphQLEngineFactory.generateSchema(factory)
                ).execute(query);
        if (execute.getErrors().isEmpty())
            return instance.build();
        // throw error over failed query parsing
        throw new SchemaError.SchemaErrorException("Error Transforming the GQL text query into a IOQL query",
                execute.getErrors().stream().map(GraphQLError::getMessage).collect(Collectors.toList()));
    }

}
