package org.opensearch.schema;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.validation.ValidationResult;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaValidatorTest {
    public static Ontology ontology;
    public static IndexProvider indexProvider;
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
        indexProvider = IndexProvider.Builder.generate(ontology);
    }

    @Test
    void validate() {
        SchemaValidator validator = new SchemaValidator();
        ValidationResult.ValidationResults results = validator.validate(indexProvider, new Accessor(ontology));
        assertTrue(results.isValid());
    }

    @Test
    void validateFailsOnIDFieldVerification() {
        //todo implement
    }

    @Test
    void validateFailsOnFieldTypeVerification() {
        //todo implement
    }

    @Test
    void validateFailsOnRelationsFieldsVerification() {
        //todo implement
    }
    @Test
    void validateFailsOnRelationsRedundantFieldsVerification() {
        //todo implement
    }
}