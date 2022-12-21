package org.opensearch.schema.domain.observability.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.BaseTypeElement.Type;
import org.opensearch.schema.index.schema.Entity;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.*;

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
    public static Accessor ontologyAccessor;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
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
        ontologyAccessor = new Accessor(ontology);
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
        Assertions.assertTrue(PhysicalEntityRelationsDirectiveType.FOREIGN.isSame(ontologyAccessor.relation$("has_User").getDirectives().get(0).getArguments().get(0).value.toString()));

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
        Assertions.assertEquals(ontologyAccessor.entity$("User").idFieldName(), "id");
        Assertions.assertEquals(ontologyAccessor.entity$("User").getProperties().size(), 8);
        Assertions.assertEquals(ontologyAccessor.entity$("User").getMandatory().size(), 1);


    }

     /**
     * test creation of an index provider using the predicate conditions for top level entity will be created an index
     */
    @Test
    public void testIndexProviderBuilder() throws Exception {
        IndexProvider provider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream().anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.RELATION.isSame(d.getName())));

        String valueAsString = new ObjectMapper().writeValueAsString(provider);
        Assert.assertNotNull(valueAsString);

        List<Entity> rootEntities = new ArrayList<>(provider.getEntities());

        Optional<Entity> client = rootEntities.stream().filter(p -> p.getType().equals(Type.of("Client"))).findFirst();
        Assertions.assertTrue(client.isPresent());

        Optional<Entity> server = rootEntities.stream().filter(p -> p.getType().equals(Type.of("Server"))).findFirst();
        Assertions.assertTrue(server.isPresent());

        Optional<Entity> user = rootEntities.stream().filter(p -> p.getType().equals(Type.of("User"))).findFirst();
        Assertions.assertTrue(user.isPresent());

        Map<String, Entity> clientNested = client.get().getNested();

//      Client.user : is defined as >> user:User @relation(mappingType: "foreign")
//      Assertions.assertTrue(clientNested.containsKey("user"));
//      Assertions.assertEquals("User",clientNested.get("user").getType().getName());

        Assertions.assertTrue(clientNested.containsKey("as"));
        Assertions.assertEquals("AutonomousSystem",clientNested.get("as").getType().getName());
        Assertions.assertTrue(clientNested.containsKey("geo"));
        Assertions.assertEquals("Geo",clientNested.get("geo").getType().getName());

        Assertions.assertTrue(provider.getRelations().stream().anyMatch(r->r.getType().getName().equals("has_User")));
        Assertions.assertTrue(provider.getRelations().stream().anyMatch(r->r.getType().getName().equals("has_AutonomousSystem")));

        Assertions.assertTrue(provider.getRelations().stream().flatMap(r->r.getDirectives().stream()).anyMatch(d->d.containsArgVal(PhysicalEntityRelationsDirectiveType.FOREIGN.getName())));
        Assertions.assertTrue(provider.getRelations().stream().flatMap(r->r.getDirectives().stream()).anyMatch(d->d.containsArgVal(PhysicalEntityRelationsDirectiveType.EMBEDDED.getName())));
    }

}
