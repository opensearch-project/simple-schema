package org.opensearch.schema.domain.observability.index;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.NoOpClient;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.transform.IndexEntitiesMappingBuilder;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.DirectiveEnumTypes;
import org.opensearch.schema.ontology.Ontology;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This test is verifying that the process of generating an index provider from the ontology is working as expected
 */
public class MappingEntityWithRelationTemplateGeneratorTest {
    static Accessor ontologyAccessor;
    static IndexProvider indexProvider;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

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

        Ontology ontology = transformer.transform("process", utilsSchemaInput, filterSchemaInput, aggregationSchemaInput, baseSchemaInput, userSchemaInput, communicationSchemaInput, hashSchemaInput, codeSignatureSchemaInput, executableFormatSchemaInput, processSchemaInput);
        ontologyAccessor = new Accessor(ontology);
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> true);
    }

    @Test
    /**
     * verify the agent entity index-provider contains basic structure and properties
     */
    public void GenerateAgentEntitySubGroupNestedMappingTest() throws IOException, JSONException {
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        IndexEntitiesMappingBuilder builder = new IndexEntitiesMappingBuilder(indexProvider);
        builder.map(ontologyAccessor, new NoOpClient("test"), requests);

        Assert.assertNotNull(requests.get("process"));
        Assert.assertEquals(1, requests.get("process").getMappings().size());
        Assert.assertNotNull(requests.get("process").getMappings().get("Process"));
        Assert.assertNotNull(((Map) requests.get("process").getMappings().get("Process")).get("properties"));
        Map processPropertiesMap = (Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties");

        // since group field is an array of groups => verify group field is embedded in the process mapping as a nested object
        Assert.assertNull(((Map) ((Map) ((Map) processPropertiesMap).get("group")).get("properties")).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(3, ((Map) ((Map) ((Map) processPropertiesMap).get("group")).get("properties")).size());

        // since group field is an array of groups => verify group field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) processPropertiesMap).get("supplementalGroups"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(3, ((Map) ((Map) ((Map) processPropertiesMap).get("supplementalGroups")).get("properties")).size());

        // since previous field is an array of processes => verify previous field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) processPropertiesMap).get("previous"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(1, ((Map) ((Map) ((Map) processPropertiesMap).get("previous")).get("properties")).size());

        // since previous field is an array of processes => verify previous field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) processPropertiesMap).get("parent"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(1, ((Map) ((Map) ((Map) processPropertiesMap).get("parent")).get("properties")).size());


    }

    @Test
    /**
     * verify the agent entity index-provider contains basic structure and properties
     */
    public void GenerateAgentEntityMappingTest() throws IOException, JSONException {
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        IndexEntitiesMappingBuilder builder = new IndexEntitiesMappingBuilder(indexProvider);
        builder.map(ontologyAccessor, new NoOpClient("test"), requests);

        Assert.assertNotNull(requests.get("process"));
        Assert.assertEquals(1, requests.get("process").getMappings().size());
        Assert.assertNotNull(requests.get("process").getMappings().get("Process"));
        Assert.assertNotNull(((Map) requests.get("process").getMappings().get("Process")).get("properties"));
        Map processPropertiesMap = (Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties");

//      Process.savedUser : is defined as >> user:User @relation(mappingType: "foreign")
//        Assert.assertTrue(processPropertiesMap.containsKey("savedUser"));
//      Process.user : is defined as >> user:User @relation(mappingType: "foreign")
//        Assert.assertTrue(processPropertiesMap.containsKey("user"));

        Assert.assertTrue(processPropertiesMap.containsKey("leader"));
        Assert.assertTrue(processPropertiesMap.containsKey("parent"));
        Assert.assertTrue(processPropertiesMap.containsKey("argsCount"));
        Assert.assertTrue(processPropertiesMap.containsKey("workingDirectory"));
        Assert.assertTrue(processPropertiesMap.containsKey("sessionLeader"));
        Assert.assertTrue(processPropertiesMap.containsKey("envVars"));
        Assert.assertTrue(processPropertiesMap.containsKey("interactive"));
        Assert.assertTrue(processPropertiesMap.containsKey("description"));
        Assert.assertTrue(processPropertiesMap.containsKey("pid"));
        Assert.assertTrue(processPropertiesMap.containsKey("source"));
        Assert.assertTrue(processPropertiesMap.containsKey("title"));
        Assert.assertTrue(processPropertiesMap.containsKey("threadId"));
        Assert.assertTrue(processPropertiesMap.containsKey("exitCode"));
        Assert.assertTrue(processPropertiesMap.containsKey("supplementalGroups"));
        Assert.assertTrue(processPropertiesMap.containsKey("end"));
        Assert.assertTrue(processPropertiesMap.containsKey("groupLeader"));
        Assert.assertTrue(processPropertiesMap.containsKey("elf"));
        Assert.assertTrue(processPropertiesMap.containsKey("timestamp"));
        Assert.assertTrue(processPropertiesMap.containsKey("group"));
        Assert.assertTrue(processPropertiesMap.containsKey("previous"));
        Assert.assertTrue(processPropertiesMap.containsKey("start"));
        Assert.assertTrue(processPropertiesMap.containsKey("entityId"));
        Assert.assertTrue(processPropertiesMap.containsKey("message"));
        Assert.assertTrue(processPropertiesMap.containsKey("executable"));
        Assert.assertTrue(processPropertiesMap.containsKey("threadName"));
        Assert.assertTrue(processPropertiesMap.containsKey("tags"));
        Assert.assertTrue(processPropertiesMap.containsKey("labels"));
        Assert.assertTrue(processPropertiesMap.containsKey("args"));
        Assert.assertTrue(processPropertiesMap.containsKey("upTime"));
        Assert.assertTrue(processPropertiesMap.containsKey("pe"));
        Assert.assertTrue(processPropertiesMap.containsKey("savedGroup"));
        Assert.assertTrue(processPropertiesMap.containsKey("name"));
        Assert.assertTrue(processPropertiesMap.containsKey("tty"));
        Assert.assertTrue(processPropertiesMap.containsKey("attributes"));
        Assert.assertTrue(processPropertiesMap.containsKey("codeSignature"));
        Assert.assertTrue(processPropertiesMap.containsKey("commandLine"));
        Assert.assertTrue(processPropertiesMap.containsKey("hash"));
        //test template generation structure
        XContentBuilder process = requests.get("process").request().toXContent(XContentBuilder.builder(XContentType.JSON.xContent()), ToXContent.EMPTY_PARAMS);
        process.prettyPrint();
        process.flush();
        Assert.assertNotNull(BytesReference.bytes(process).utf8ToString());
        // verify Process entity is mapped correctly
        JSONObject processMapping = new JSONObject(BytesReference.bytes(process).utf8ToString())
                .getJSONObject("mappings").getJSONObject("_doc").getJSONObject("_doc").getJSONObject("Process");

        // compare Process mapping is expected
        Assert.assertNotNull(processMapping);
      }
}
