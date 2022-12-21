package org.opensearch.schema.domain.observability.graphql;

import graphql.schema.GraphQLSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.ontology.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.opensearch.schema.ontology.PrimitiveType.Types.*;
import static org.opensearch.schema.ontology.Property.equal;

/**
 * This test is verifying that the base (core root log entity) SDL is correctly transformed into ontology components
 */
public class GraphQLOntologyBaseTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;
    public static GraphQLSchema graphQLSchema;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    /**
     * load base SDL files, transform it into the ontology components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream baseSchemaInput = new FileInputStream("../schema/observability/logs/base.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("base",utilsSchemaInput,filterSchemaInput,aggregationSchemaInput,baseSchemaInput);
        ontologyAccessor = new Accessor(ontology);
        Assertions.assertNotNull(ontology);
    }

    @Test
    /**
     * test enumerations are correctly created
     */
    public void testEnumTranslation() {
        Assertions.assertEquals(ontologyAccessor.enumeratedType$("StreamType"),
                new EnumeratedType("StreamType",
                        Arrays.asList(new Value(0, "logs"),
                                new Value(1, "metrics"),
                                new Value(2, "traces"),
                                new Value(3, "synthetics"))));
    }

    /**
     * test properties are correctly translated (sample properties are selected for comparison)
     */
    @Test
    public void testSamplePropertiesTranslation() {
        Assertions.assertTrue(equal(ontologyAccessor.property$("id"), new Property.MandatoryProperty(new Property("id", "id", ID.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("name"), new Property.MandatoryProperty(new Property("name", "name", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("timestamp"), new Property("timestamp", "timestamp", TIME.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("labels"), new Property("labels", "labels", JSON.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("location"), new Property("location", "location", GEOPOINT.asType())));
    }

    /**
     * test the base abstract root entity is correctly translated into ontology structure
     */
    @Test
    public void testEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("BaseRecord").isAbstract(), true);
        Assertions.assertEquals(ontologyAccessor.entity$("BaseRecord").geteType(), "BaseRecord");
        Assertions.assertEquals(ontologyAccessor.entity$("BaseRecord").getProperties().size(), 5);
        Assertions.assertEquals(ontologyAccessor.entity$("BaseRecord").getMandatory().size(), 1);

        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").isAbstract(), false);
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").geteType(), "AutonomousSystem");
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getIdField().size(), 0);//todo - fix according to the @Key directive
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getProperties().size(), 2);
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getMandatory().size(), 1);


    }
}
