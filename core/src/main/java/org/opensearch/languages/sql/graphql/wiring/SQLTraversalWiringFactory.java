package org.opensearch.languages.sql.graphql.wiring;


import graphql.Internal;
import graphql.schema.*;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.InterfaceWiringEnvironment;
import graphql.schema.idl.UnionWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.sql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.Accessor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * GraphQL callback factory for generating a query based on the GraphQL visitor - this factory specifically generates an SQL query
 */
@Internal
public class SQLTraversalWiringFactory implements WiringFactory {
    private GraphQLSchema schema;

    private QueryTranslationStrategy.QueryTranslatorContext<Query.Builder> context;
    private List<QueryTranslationStrategy<Query.Builder>> strategies;

    public SQLTraversalWiringFactory(List<QueryTranslationStrategy<Query.Builder>> strategies, Accessor accessor, Query.Builder builder) {
        this.strategies = strategies;
        this.schema = GraphQLEngineFactory.schema()
                .orElseThrow(() -> new SchemaError.SchemaErrorException("GraphQL schema not present", "Expecting the GraphQL schema to be created during this stage"));
        this.context = new QueryTranslationStrategy.QueryTranslatorContext<>(accessor,builder,schema);
    }


    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return env -> schema.getImplementations((GraphQLInterfaceType) env.getFieldType()).get(0);
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return env -> env.getSchema().getQueryType();
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        return env -> {
            GraphQLType fieldType = env.getFieldType();
            //populate environment
            context.setEnv(env);
            if (fieldType instanceof GraphQLList) {
                return Arrays.asList(getObject(context, ((GraphQLList) fieldType).getWrappedType()));
            } else {
                return getObject(context, fieldType);
            }
        };
    }

    private Object getObject(QueryTranslationStrategy.QueryTranslatorContext<Query.Builder> context, GraphQLType fieldType)  {
        return strategies.stream()
                .map(strategy->strategy.translate(context,fieldType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}


