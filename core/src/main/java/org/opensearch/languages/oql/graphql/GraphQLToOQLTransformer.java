package org.opensearch.languages.oql.graphql;


import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.Transformer;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.oql.graphql.wiring.*;
import org.opensearch.languages.oql.graphql.wiring.strategies.EntityWithPredicateTranslation;
import org.opensearch.languages.oql.graphql.wiring.strategies.InterfaceTranslation;
import org.opensearch.languages.oql.graphql.wiring.strategies.ValuesTranslation;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.Accessor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This component is the transformation element which takes a GQL text query and using the existing GQL engine (expected to be generated)
 * will transform this GQL query into a valid OQL -  Ontological Query Language
 */
public class GraphQLToOQLTransformer implements Transformer<Query> {

    /**
     * translates graphQL textual query into a OQL ( Ontological Query Language) query
     *
     * @param accessor
     * @param query
     * @return
     */
    public synchronized Query transform(List<QueryTranslationStrategy<Query.Builder>> translationStrategies, Accessor accessor, String query) {
        Query.Builder instance = Query.Builder.instance();
        OQLTraversalWiringFactory factory = new OQLTraversalWiringFactory(translationStrategies, accessor, instance);
        ExecutionResult execute = GraphQLEngineFactory
                .generateEngine(
                        GraphQLEngineFactory.generateSchema(factory)
                ).execute(query);
        if (execute.getErrors().isEmpty())
            return instance.build();
        // throw error over failed query parsing
        throw new SchemaError.SchemaErrorException("Error Transforming the GQL text query into a OQL query",
                execute.getErrors().stream().map(GraphQLError::getMessage).collect(Collectors.toList()));
    }

    public synchronized Query transform(Accessor accessor, String query) {
        List<QueryTranslationStrategy<Query.Builder>> translationStrategies = List.of(
                new EntityWithPredicateTranslation(),
                new InterfaceTranslation(),
                new ValuesTranslation());
        return transform(translationStrategies, accessor, query);
    }

}
