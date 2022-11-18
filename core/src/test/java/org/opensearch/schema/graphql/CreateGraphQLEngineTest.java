package org.opensearch.schema.graphql;

import graphql.Assert;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class CreateGraphQLEngineTest {


    @BeforeAll
    /**
     * load sample graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");

        InputStream simpleSchemaInput = new FileInputStream("../schema/sample/simple.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

    }

    private static Map<String, Object> introspect(GraphQLSchema schema) {
        GraphQL gql = GraphQL.newGraphQL(schema).build();
        ExecutionResult result = gql.execute(IntrospectionQuery.INTROSPECTION_QUERY);
        Assert.assertTrue(result.getErrors().size() == 0, () -> "The schema has errors during Introspection");
        return result.getData();
    }

}
