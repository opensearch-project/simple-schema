package org.opensearch.languages.oql.domain.sample;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.FileInputStream;
import java.io.InputStream;


/**
 * This test is verifying that the (example) simple SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLSimpleOQLTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
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

        ontology = transformer.transform("simple", utilsSchemaInput, filterSchemaInput, aggregationSchemaInput, simpleSchemaInput);
        Assertions.assertNotNull(ontology);
        ontologyAccessor = new Accessor(ontology);
    }


    @Test
    public void testBooksQueryTranslator() {
        //todo implement
    }

    @Test
    public void testAuthorsQueryTranslator() {
        //todo implement
    }

}
