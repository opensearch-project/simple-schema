package org.opensearch.languages.oql.graphql.wiring;

import graphql.schema.GraphQLSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.oql.graphql.GraphQLToOQLTransformer;
import org.opensearch.languages.oql.graphql.wiring.strategies.EntityWithPredicateTranslation;
import org.opensearch.languages.oql.graphql.wiring.strategies.ValuesTranslation;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.languages.oql.query.descriptor.QueryDescriptor;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityWithPredicateTranslationTest {
    private static List<InputStream> streams;
    private static GraphQLSchema graphQLSchema;
    private static Accessor accessor;
    private static GraphQLSchema qlSchema;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

    /**
     * load sample graphQL SDL files, transform them into the ontology & index-provider components
     */
    @BeforeAll
    public static void setUp() throws Exception {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");

        InputStream simpleSchemaInput = new FileInputStream("../schema/sample/simple.graphql");
        streams = Arrays.asList(filterSchemaInput, aggregationSchemaInput, utilsSchemaInput, simpleSchemaInput);
        //expect engine not yet created
        assertTrue(GraphQLEngineFactory.engine().isEmpty());

        // first create an ontology from the GQL SDL
        GraphQLToOntologyTransformer graphQLToOntologyTransformer = new GraphQLToOntologyTransformer();
        qlSchema = GraphQLEngineFactory.generateSchema(streams);
        assertTrue(GraphQLEngineFactory.schema().isPresent());

        //next generate the ontology from the GQL schema
        Ontology ontology = graphQLToOntologyTransformer.transform("Simple", GraphQLEngineFactory.schema().get());
        assertNotNull(ontology);
        accessor = new Accessor(ontology);

        //next generate the actual GQL engine
        GraphQLEngineFactory.generateEngine(GraphQLEngineFactory.schema().get());
        //expect engine created correctly
        assertTrue(GraphQLEngineFactory.engine().isPresent());
    }

    @Test
    void translate() {
        String q = " {\n" +
                "    book {" +
                "      title \n" +
                "    }\n" +
                "}";

        List<QueryTranslationStrategy<Query.Builder>> translationStrategies = List.of(
                new EntityWithPredicateTranslation(),
                new ValuesTranslation()
        );
        GraphQLToOQLTransformer transformer = new GraphQLToOQLTransformer();
        Query query = transformer.transform(translationStrategies, accessor, q);

        String expected = "[└── Start, \n" +
                "    ──Typ[Book:1]──Q[2]:{3}, \n" +
                "                       └─?[3]:[title<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }
}