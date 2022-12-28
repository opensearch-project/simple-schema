package org.opensearch.schema.domain.observability.graphql;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.opensearch.schema.ontology.PrimitiveType.Types.*;
import static org.opensearch.schema.ontology.Property.equal;


/**
 * This test is verifying that the agent SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLOntologyAgentTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;
    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

    @BeforeAll
    /**
     * load base & agent graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");

        InputStream baseSchemaInput = new FileInputStream("../schema/observability/logs/base.graphql");
        InputStream agentSchemaInput = new FileInputStream("../schema/observability/logs/agent.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("agents",utilsSchemaInput,filterSchemaInput,aggregationSchemaInput, baseSchemaInput, agentSchemaInput);
        Assertions.assertNotNull(ontology);
        ontologyAccessor = new Accessor(ontology);
    }

    /**
     * test creation of an index provider using the predicate conditions for top level entity will be created an index
     */
    @Test
    public void testIndexProviderBuilder() {
        IndexProvider provider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream().anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> true);

        Assertions.assertEquals(provider.getEntities().size(), 1);
        Assertions.assertEquals(provider.getRelations().size(), 0);
    }


    /**
     * test enumerations are correctly created
     */
    @Test
    public void testEnumTranslation() {
        Assertions.assertEquals(ontologyAccessor.enumeratedType$("AgentIdStatus"),
                new EnumeratedType("AgentIdStatus",
                        Arrays.asList(new Value(0, "verified"),
                                new Value(1, "mismatch"),
                                new Value(2, "missing"),
                                new Value(3, "auth_metadata_missing"))));
    }

    /**
     * test properties are correctly translated (sample properties are selected for comparison)
     */
    @Test
    public void testSamplePropertiesTranslation() {
        Assertions.assertTrue(equal(ontologyAccessor.property$("id"),
                new Property.MandatoryProperty(new Property("id", "id", ID.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("name"),
                new Property.MandatoryProperty(new Property("name", "name", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("labels"),
                new Property.MandatoryProperty(new Property("labels", "labels", JSON.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("tags"),
                new Property.MandatoryProperty(new Property("tags", "tags", STRING.asListType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("type"),
                new Property("type", "type", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("version"),
                new Property("version", "version", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("number"),
                new Property("number", "number", LONG.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("timestamp"),
                new Property("timestamp", "timestamp", TIME.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("location"),
                new Property("location", "location", GEOPOINT.asType())));
    }

    /**
     * test the agent is correctly translated into ontology structure
     */
    @Test
    public void testAgentEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("Agent").geteType(), "Agent");
        Assertions.assertEquals(ontologyAccessor.entity$("Agent").getProperties().size(), 12);
        Assertions.assertEquals(ontologyAccessor.entity$("Agent").getMandatory().size(), 2);
    }


}
