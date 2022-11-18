package org.opensearch.schema.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GraphQLEngineFactoryTest {
    private static List<InputStream> streams;
    private static GraphQLSchema schema;

    @BeforeAll
    /**
     * load sample graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");

        InputStream simpleSchemaInput = new FileInputStream("../schema/sample/simple.graphql");
        streams = Arrays.asList(filterSchemaInput, aggregationSchemaInput, utilsSchemaInput, simpleSchemaInput);
        //expect engine created correctly
        assertTrue(GraphQLEngineFactory.engine().isEmpty());

        schema = GraphQLEngineFactory.generateSchema(streams);
        assertNotNull(schema);

    }

    @Test
    void testGenerateSchema() {
        assertTrue(schema.containsType("Author"));
        assertTrue(schema.containsType("Book"));
        assertTrue(schema.containsType("Query"));
        //this part is auto generated according to the search & aggregation generation strategy
        assertTrue(schema.containsType("AuthorFilter"));
        assertTrue(schema.containsType("BookFilter"));
        assertTrue(schema.containsType("AuthorAggregationFilter"));
        assertTrue(schema.containsType("BookAggregationFilter"));
        assertTrue(schema.containsType("AuthorResults"));
        assertTrue(schema.containsType("BookResults"));
        assertTrue(schema.containsType("AuthorAggregationResults"));
        assertTrue(schema.containsType("BookAggregationResults"));
    }

    @Test
    void testGenerateEngine() {
        GraphQL engine = GraphQLEngineFactory.generateEngine(schema);
        ExecutionResult result = engine.execute(IntrospectionQuery.INTROSPECTION_QUERY);
        assertNotNull(result);
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.isDataPresent());

        Map schema = (Map) ((Map) result.getData()).get("__schema");
        assertNotNull(schema);
        assertNotNull(schema.get("types"));

        List types = (List) schema.get("types");
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("Author")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("Book")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("AuthorFilter")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("BookFilter")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("AuthorAggregationFilter")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("BookAggregationFilter")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("AuthorResults")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("BookResults")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("AuthorAggregationResults")));
        assertTrue(types.stream().anyMatch(p->
                ((Map)p).get("name").equals("BookAggregationResults")));
    }
}