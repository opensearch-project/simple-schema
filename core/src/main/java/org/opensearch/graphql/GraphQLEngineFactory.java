package org.opensearch.graphql;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import graphql.schema.idl.errors.SchemaProblem;
import org.opensearch.query.Query;
import org.opensearch.query.QueryBuilder;
import org.opensearch.schema.SchemaError;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import static graphql.Scalars.GraphQLString;

/**
 * This is the GraphQL schema & engine factory
 */
public class GraphQLEngineFactory {
    private static final SchemaParser schemaParser = new SchemaParser();
    private static final SchemaGenerator schemaGenerator = new SchemaGenerator();
    private static TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
    private static GraphQLSchema graphQLSchema;
    private static GraphQL gql;

    /**
     * add GQL type using inputStream
     *
     * @return validation if the type successfully merged
     */
    public static boolean addType(InputStream type) {
        return addType(parse(type));
    }

    /**
     * add GQL type
     *
     * @return validation if the type successfully merged
     */
    public static boolean addType(TypeDefinitionRegistry type) {
        try {
            typeRegistry.merge(type);
        } catch (SchemaProblem err) {
            return false;
        }
        return true;
    }

    /**
     * generate GQL schema - will create a new schema even if schema was already created
     *
     * @param streams - list of GQL schema files
     * @return
     */
    public static GraphQLSchema generateSchema(WiringFactory factory,List<InputStream> streams) {
        // each registry is merged into the main registry
        streams.forEach(GraphQLEngineFactory::addType);
        return generateSchema(factory);
    }


    /**
     * generate GQL schema - will create a new schema even if schema was already created
     *
     * @return
     */
    public static GraphQLSchema generateSchema(List<InputStream> streams) {
        // each registry is merged into the main registry
        streams.forEach(GraphQLEngineFactory::addType);
        return generateSchema(new EchoingWiringFactory());
    }

    /**
     * generate GQL schema - will create a new schema even if schema was already created
     * @param factory
     * @return
     */
    public static GraphQLSchema generateSchema(WiringFactory factory) {
        //create schema
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring()
                .wiringFactory(factory)
                .scalar(ExtendedScalars.newAliasedScalar("Text")
                        .aliasedScalar(GraphQLString)
                        .build())
                .scalar(ExtendedScalars.newAliasedScalar("IP")
                        .aliasedScalar(GraphQLString)
                        .build())
                .scalar(ExtendedScalars.newAliasedScalar("GeoPoint")
                        .aliasedScalar(GraphQLString)
                        .build())
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.Json)
                .scalar(ExtendedScalars.Object)
                .scalar(ExtendedScalars.Url)
                .scalar(ExtendedScalars.DateTime)
                .scalar(ExtendedScalars.Time);
        graphQLSchema = schemaGenerator.makeExecutableSchema(
                SchemaGenerator.Options.defaultOptions(),
                typeRegistry,
                builder.build());
        return schema().get();
    }

    private static TypeDefinitionRegistry parse(InputStream s) {
        try {
            return schemaParser.parse(new InputStreamReader(s));
        } catch (Throwable err) {
            //log parse errors
            throw new SchemaError.SchemaErrorException("Couldn't parse the input schema file", err);
        }
    }


    /**
     * generate GQL engine - will create a new engine even if engine was already created
     *
     * @param schema -  the schema from which the query will be validated and build upon
     * @return
     */
    public static GraphQL generateEngine(GraphQLSchema schema) {
        gql = GraphQL.newGraphQL(schema).build();
        return engine().get();
    }

    /**
     * get GQL engine
     *
     * @return
     */
    public static Optional<GraphQL> engine() {
        if (gql != null)
            return Optional.of(gql);

        return Optional.empty();
    }

    /**
     * get GQL type definition registry
     *
     * @return
     */
    public static TypeDefinitionRegistry registry() {
        return typeRegistry;
    }

    /**
     * get GQL schema
     *
     * @return
     */
    public static Optional<GraphQLSchema> schema() {
        if (graphQLSchema != null)
            return Optional.of(graphQLSchema);

        return Optional.empty();
    }

    /**
     * reset all state from GQL schema & types
     * @return
     */
    public static boolean reset() {
        typeRegistry = new TypeDefinitionRegistry();
        graphQLSchema = null;
        gql = null;
        return true;
    }

}
