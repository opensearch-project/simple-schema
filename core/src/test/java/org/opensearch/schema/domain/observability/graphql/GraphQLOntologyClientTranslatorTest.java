package org.opensearch.schema.domain.observability.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.graphql.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.Entity;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.ObjectType;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.Property;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.opensearch.schema.ontology.PrimitiveType.Types.*;
import static org.opensearch.schema.ontology.Property.equal;

/**
 * This test is verifying that the user SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLOntologyClientTranslatorTest {
    public static Ontology ontology;
    public static Ontology.Accessor ontologyAccessor;

    @BeforeAll
    /**
     * load base, communication & user graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream baseSchemaInput = new FileInputStream("../schema/observability/logs/base.graphql");
        InputStream userSchemaInput = new FileInputStream("../schema/observability/logs/user.graphql");
        InputStream clientSchemaInput = new FileInputStream("../schema/observability/logs/communication.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("client",utilsSchemaInput,filterSchemaInput,aggregationSchemaInput,baseSchemaInput,userSchemaInput,clientSchemaInput);
        ontologyAccessor = new Ontology.Accessor(ontology);
        Assertions.assertNotNull(ontology);
        String valueAsString = new ObjectMapper().writeValueAsString(ontology);
        Assertions.assertNotNull(valueAsString);
    }

    @Test
    /**
     * test properties are correctly translated (sample properties are selected for comparison)
     */
    public void testSamplePropertiesTranslation() {
        Assertions.assertTrue(equal(ontologyAccessor.property$("id"), new Property.MandatoryProperty(new Property("id", "id", ID.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("name"), new Property.MandatoryProperty(new Property("name", "name", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("group"), new Property.MandatoryProperty(new Property("group", "Group", ObjectType.of("Group")))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("email"), new Property("email", "email", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("fullName"), new Property("fullName", "fullName", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("roles"), new Property("roles", "roles", STRING.asListType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("location"), new Property("location", "location", GEOPOINT.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("geo"), new Property("geo", "Geo", ObjectType.of("Geo"))));
        //implemented as nested object
        Assertions.assertTrue(equal(ontologyAccessor.property$("user"), new Property("user", "User", ObjectType.of("User"))));
    }

    @Test
    /**
     * test the client is correctly translated into ontology structure
     */
    public void testEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("Client").isAbstract(), false);
        Assertions.assertEquals(ontologyAccessor.entity$("Client").geteType(), "Client");
        Assertions.assertEquals(ontologyAccessor.entity$("Client").getIdField().size(), 0);//todo - fix according to @Key directive
        Assertions.assertEquals(ontologyAccessor.entity$("Client").getProperties().size(), 21);
        Assertions.assertEquals(ontologyAccessor.entity$("Client").getMandatory().size(), 1);
    }

    @Test
    /**
     * test the client's relations  is correctly translated into ontology structure
     */
    public void testRelationTranslation() {
        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getrType(), "has_User");
        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getDirectives().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getDirectives().get(0).getName(), "relation");
        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getDirectives().get(0).getArguments().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getDirectives().get(0).getArguments().get(0).value, "foreign");

        Assertions.assertEquals(ontologyAccessor.relation$("has_User").getePairs().get(0).getSideAFieldName(), "user");

        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getrType(), "has_AutonomousSystem");
        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getePairs().get(0).getSideAFieldName(), "as");
        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getDirectives().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getDirectives().get(0).getName(), "relation");
        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getDirectives().get(0).getArguments().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relation$("has_AutonomousSystem").getDirectives().get(0).getArguments().get(0).value, "embedded");

        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").isAbstract(), false);
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").geteType(), "AutonomousSystem");
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getIdField().size(), 0);//todo - fix according to the @Key directive
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getProperties().size(), 2);
        Assertions.assertEquals(ontologyAccessor.entity$("AutonomousSystem").getMandatory().size(), 1);

        Assertions.assertEquals(ontologyAccessor.entity$("User").geteType(), "User");
        Assertions.assertEquals(ontologyAccessor.entity$("User").getIdField().size(), 1);
        Assertions.assertEquals(ontologyAccessor.entity$("User").getIdField().get(0), "id");
        Assertions.assertEquals(ontologyAccessor.entity$("User").getProperties().size(), 8);
        Assertions.assertEquals(ontologyAccessor.entity$("User").getMandatory().size(), 1);


    }

     /**
     * test creation of an index provider using the predicate conditions for top level entity will be created an index
     */
    @Test
    public void testIndexProviderBuilder() throws Exception {
        IndexProvider provider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream().anyMatch(d -> d.getName().equals("model"))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("relation") && d.containsArgVal("foreign")));

        String valueAsString = new ObjectMapper().writeValueAsString(provider);
        Assert.assertNotNull(valueAsString);

        List<Entity> rootEntities = new ArrayList<>(provider.getEntities());
        Assertions.assertEquals(rootEntities.size(),3);
        Optional<Entity> client = rootEntities.stream().filter(p -> p.getType().equals("Client")).findFirst();
        Assertions.assertTrue(client.isPresent());

        Map<String, Entity> nested = client.get().getNested();
        Assertions.assertEquals(nested.size(),3);

        Assertions.assertTrue(nested.containsKey("user"));
        Assertions.assertEquals(nested.get("user").getType(),"User");
        Assertions.assertTrue(nested.containsKey("as"));
        Assertions.assertEquals(nested.get("as").getType(),"AutonomousSystem");
        Assertions.assertTrue(nested.containsKey("geo"));
        Assertions.assertEquals(nested.get("geo").getType(),"Geo");

        Assertions.assertEquals(provider.getRelations().size(),1);
        Assertions.assertEquals(provider.getRelations().get(0).getType(),"has_User");
    }

}
