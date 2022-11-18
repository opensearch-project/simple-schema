package org.opensearch.schema.domain.observability.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.graphql.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.BaseTypeElement;
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
 * This test is verifying that the process SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLOntologyProcessTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;

    @BeforeAll
    /**
     * load process (including all it's dependencies) graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");

        InputStream baseSchemaInput = new FileInputStream("../schema/observability/logs/base.graphql");
        InputStream userSchemaInput = new FileInputStream("../schema/observability/logs/user.graphql");
        InputStream hashSchemaInput = new FileInputStream("../schema/observability/logs/hash.graphql");
        InputStream executableFormatSchemaInput = new FileInputStream("../schema/observability/logs/executableFormat.graphql");
        InputStream communicationSchemaInput = new FileInputStream("../schema/observability/logs/communication.graphql");
        InputStream codeSignatureSchemaInput = new FileInputStream("../schema/observability/logs/codeSignature.graphql");
        InputStream processSchemaInput = new FileInputStream("../schema/observability/logs/process.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("client",utilsSchemaInput,filterSchemaInput,aggregationSchemaInput, baseSchemaInput, userSchemaInput, communicationSchemaInput, hashSchemaInput, codeSignatureSchemaInput, executableFormatSchemaInput, processSchemaInput);
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
        Assertions.assertTrue(equal(ontologyAccessor.property$("elf"), new Property("elf", "ELF", ObjectType.of("ELF"))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("codeSignature"), new Property("codeSignature", "CodeSignature", ObjectType.of("CodeSignature"))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("previous"), new Property("previous", "Process", ObjectType.ArrayOfObjects.of("Process"))));

    }

    @Test
    /**
     * test the Process is correctly translated into ontology structure
     */
    public void testEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("Process").isAbstract(), false);
        Assertions.assertEquals(ontologyAccessor.entity$("Process").geteType(), "Process");
        Assertions.assertEquals(ontologyAccessor.entity$("Process").getIdField().size(), 1);//todo - fix according to @Key directive
        Assertions.assertEquals(ontologyAccessor.entity$("Process").getProperties().size(), 39);
        Assertions.assertEquals(ontologyAccessor.entity$("Process").getMandatory().size(), 2);
    }

    @Test
    /**
     * test the Process's relations  is correctly translated into ontology structure
     */
    public void testRelationTranslation() {
        Assertions.assertEquals(ontologyAccessor.relation$("has_Source").getrType(), "has_Source");
        Assertions.assertEquals(ontologyAccessor.relation$("has_Source").getDirectives().size(), 0);

        Assertions.assertEquals(ontologyAccessor.relation$("has_ELF").getrType(), "has_ELF");
        Assertions.assertEquals(ontologyAccessor.relation$("has_ELF").getDirectives().size(), 0);

        Assertions.assertEquals(ontologyAccessor.relation$("has_PE").getrType(), "has_PE");
        Assertions.assertEquals(ontologyAccessor.relation$("has_PE").getDirectives().size(), 0);

        Assertions.assertEquals(ontologyAccessor.relation$("has_Hash").getrType(), "has_Hash");
        Assertions.assertEquals(ontologyAccessor.relation$("has_Hash").getDirectives().size(), 0);

        Assertions.assertEquals(ontologyAccessor.relation$("has_CodeSignature").getrType(), "has_CodeSignature");
        Assertions.assertEquals(ontologyAccessor.relation$("has_CodeSignature").getDirectives().size(), 0);

        List<EPair> has_userPairs = ontologyAccessor.relation$("has_User").getePairs();
        Assertions.assertEquals(has_userPairs.size(), 6);

        Assertions.assertTrue(has_userPairs.stream().anyMatch(p->p.getName().equals("Client->User")));
        Assertions.assertTrue(has_userPairs.stream().anyMatch(p->p.getName().equals("Destination->User")));

        Assertions.assertTrue(has_userPairs.stream().anyMatch(p->p.getName().equals("Process->User")));
        Assertions.assertTrue(has_userPairs.stream().filter(p->p.getName().equals("Process->User")).anyMatch(p->p.getSideAFieldName().equals("savedUser")));
        Assertions.assertTrue(has_userPairs.stream().filter(p->p.getName().equals("Process->User")).anyMatch(p->p.getSideAFieldName().equals("user")));

        Assertions.assertTrue(has_userPairs.stream().anyMatch(p->p.getName().equals("Server->User")));
        Assertions.assertTrue(has_userPairs.stream().anyMatch(p->p.getName().equals("Source->User")));

        List<EPair> has_groupPairs = ontologyAccessor.relation$("has_Group").getePairs();
        Assertions.assertEquals(has_groupPairs.size(), 4);

        Assertions.assertTrue(has_groupPairs.stream().anyMatch(p->p.getName().equals("User->Group")));
        Assertions.assertTrue(has_groupPairs.stream().anyMatch(p->p.getName().equals("Process->Group")));
        Assertions.assertTrue(has_groupPairs.stream().filter(p->p.getName().equals("Process->Group")).anyMatch(p->p.getSideAFieldName().equals("group")));
        Assertions.assertTrue(has_groupPairs.stream().filter(p->p.getName().equals("Process->Group")).anyMatch(p->p.getSideAFieldName().equals("savedGroup")));
        Assertions.assertTrue(has_groupPairs.stream().filter(p->p.getName().equals("Process->Group")).anyMatch(p->p.getSideAFieldName().equals("supplementalGroups")));

        List<EPair> has_processPairs = ontologyAccessor.relation$("has_Process").getePairs();
        Assertions.assertEquals(has_processPairs.size(), 5);
        Assertions.assertTrue(has_processPairs.stream().anyMatch(p->p.getName().equals("Process->Process")));
        Assertions.assertTrue(has_processPairs.stream().filter(p->p.getName().equals("Process->Process")).anyMatch(p->p.getSideAFieldName().equals("previous")));
        Assertions.assertTrue(has_processPairs.stream().filter(p->p.getName().equals("Process->Process")).anyMatch(p->p.getSideAFieldName().equals("leader")));
        Assertions.assertTrue(has_processPairs.stream().filter(p->p.getName().equals("Process->Process")).anyMatch(p->p.getSideAFieldName().equals("sessionLeader")));
        Assertions.assertTrue(has_processPairs.stream().filter(p->p.getName().equals("Process->Process")).anyMatch(p->p.getSideAFieldName().equals("groupLeader")));
        Assertions.assertTrue(has_processPairs.stream().filter(p->p.getName().equals("Process->Process")).anyMatch(p->p.getSideAFieldName().equals("parent")));


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
        Assertions.assertEquals(rootEntities.size(), 4);

        Optional<Entity> processEntity = rootEntities.stream().filter(p->p.getType().equals(Type.of("Process"))).findAny();
        Assertions.assertEquals(processEntity.isEmpty(), false);
        Map<String, Entity> nested = processEntity.get().getNested();
        Assertions.assertEquals(nested.size(), 15);

        Assertions.assertTrue(nested.containsKey("source"));
        Assertions.assertEquals(nested.get("source").getType().getName(),"Source");

        Assertions.assertTrue(nested.containsKey("codeSignature"));
        Assertions.assertEquals(nested.get("codeSignature").getType().getName(),"CodeSignature");

        Assertions.assertTrue(nested.containsKey("elf"));
        Assertions.assertEquals(nested.get("elf").getType().getName(),"ELF");

        Assertions.assertTrue(nested.containsKey("pe"));
        Assertions.assertEquals(nested.get("pe").getType().getName(),"PE");

        Assertions.assertTrue(nested.containsKey("user"));
        Assertions.assertEquals(nested.get("user").getType().getName(),"User");
        Assertions.assertTrue(nested.containsKey("savedUser"));
        Assertions.assertEquals(nested.get("savedUser").getType().getName(),"User");
        Assertions.assertTrue(nested.containsKey("group"));

        Assertions.assertEquals(nested.get("group").getType().getName(),"Group");
        Assertions.assertTrue(nested.containsKey("savedGroup"));
        Assertions.assertEquals(nested.get("savedGroup").getType().getName(),"Group");
        Assertions.assertTrue(nested.containsKey("supplementalGroups"));
        Assertions.assertEquals(nested.get("supplementalGroups").getType().getName(),"Group");

        Assertions.assertEquals(nested.get("parent").getType().getName(),"Process");
        Assertions.assertTrue(nested.containsKey("parent"));
        Assertions.assertEquals(nested.get("groupLeader").getType().getName(),"Process");
        Assertions.assertTrue(nested.containsKey("groupLeader"));
        Assertions.assertEquals(nested.get("sessionLeader").getType().getName(),"Process");
        Assertions.assertTrue(nested.containsKey("sessionLeader"));
        Assertions.assertEquals(nested.get("leader").getType().getName(),"Process");
        Assertions.assertTrue(nested.containsKey("leader"));
        Assertions.assertEquals(nested.get("previous").getType().getName(),"Process");
        Assertions.assertTrue(nested.containsKey("previous"));

        Assertions.assertEquals(provider.getRelations().size(), 1);
        Assertions.assertEquals(provider.getRelations().get(0).getType().getName(), "has_User");
    }

}
