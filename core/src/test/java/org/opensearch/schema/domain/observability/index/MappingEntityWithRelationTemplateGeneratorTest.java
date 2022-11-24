package org.opensearch.schema.domain.observability.index;

import com.fasterxml.jackson.databind.json.JsonMapper;
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
import org.opensearch.graphql.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.transform.IndexEntitiesMappingBuilder;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;
import org.skyscreamer.jsonassert.JSONAssert;

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
                        .anyMatch(d -> d.getName().equals("model"))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("relation") && d.containsArgVal("foreign")));
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
        Assert.assertEquals(39, ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).size());

        // since group field is an array of groups => verify group field is embedded in the process mapping as a nested object
        Assert.assertNull(((Map) ((Map) ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("group")).get("properties")).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(3, ((Map) ((Map) ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("group")).get("properties")).size());

        // since group field is an array of groups => verify group field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("supplementalGroups"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(3, ((Map) ((Map) ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("supplementalGroups")).get("properties")).size());

        // since previous field is an array of processes => verify previous field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("previous"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(1, ((Map) ((Map) ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("previous")).get("properties")).size());

        // since previous field is an array of processes => verify previous field is embedded in the process mapping as a nested object
        Assert.assertEquals("nested", ((Map) (((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("parent"))).get("type"));
        // since process has other process fields it is referencing to - all these fields must be created as mapping nested objects of reference type (reference type only contains ID & Name fields)
        Assert.assertEquals(1, ((Map) ((Map) ((Map) ((Map) requests.get("process").getMappings().get("Process")).get("properties")).get("parent")).get("properties")).size());

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
        // expected process index template mapping
        JSONAssert.assertEquals(new JSONObject("{\"properties\":{\"savedUser\":{\"properties\":{\"roles\":{\"type\":\"keyword\"},\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"fullName\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"email\":{\"type\":\"keyword\"},\"group\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}}}},\"parent\":{\"type\":\"nested\",\"properties\":{\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"leader\":{\"type\":\"nested\",\"properties\":{\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"argsCount\":{\"type\":\"long\"},\"workingDirectory\":{\"type\":\"keyword\"},\"sessionLeader\":{\"type\":\"nested\",\"properties\":{\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"interactive\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"envVars\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"keyword\"},\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"source\":{\"properties\":{\"address\":{\"type\":\"keyword\"},\"ip\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"description\":{\"type\":\"keyword\"},\"natIpp\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"message\":{\"type\":\"keyword\"},\"packets\":{\"type\":\"long\"},\"mac\":{\"type\":\"keyword\"},\"labels\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"tags\":{\"type\":\"keyword\"},\"geo\":{\"properties\":{\"cityName\":{\"type\":\"keyword\"},\"countryIsoCode\":{\"type\":\"keyword\"},\"timezone\":{\"type\":\"keyword\"},\"regionName\":{\"type\":\"keyword\"},\"postalCode\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"location\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"countryName\":{\"type\":\"keyword\"},\"continentName\":{\"type\":\"keyword\"},\"regionIsoCode\":{\"type\":\"keyword\"},\"continentCode\":{\"type\":\"keyword\"}}},\"topLevelDomain\":{\"type\":\"keyword\"},\"as\":{\"properties\":{\"number\":{\"type\":\"long\"},\"organizationName\":{\"type\":\"keyword\"}}},\"port\":{\"type\":\"long\"},\"bytes\":{\"type\":\"long\"},\"domain\":{\"type\":\"keyword\"},\"subdomain\":{\"type\":\"keyword\"},\"registeredDomain\":{\"type\":\"keyword\"},\"attributes\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"user\":{\"properties\":{\"roles\":{\"type\":\"keyword\"},\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"fullName\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"email\":{\"type\":\"keyword\"},\"group\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}}}},\"natPort\":{\"type\":\"long\"},\"timestamp\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"title\":{\"type\":\"keyword\"},\"threadId\":{\"type\":\"long\"},\"exitCode\":{\"type\":\"long\"},\"supplementalGroups\":{\"type\":\"nested\",\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"end\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"groupLeader\":{\"type\":\"nested\",\"properties\":{\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"elf\":{\"properties\":{\"headerData\":{\"type\":\"keyword\"},\"cpuType\":{\"type\":\"keyword\"},\"imports\":{\"type\":\"keyword\"},\"headerOS_Abi\":{\"type\":\"keyword\"},\"exports\":{\"type\":\"keyword\"},\"creationDate\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"headerClass\":{\"type\":\"keyword\"},\"telfhash\":{\"type\":\"keyword\"},\"sharedLibraries\":{\"type\":\"keyword\"},\"headerObjVersion\":{\"type\":\"keyword\"},\"selections\":{\"type\":\"nested\",\"properties\":{\"physicalSize\":{\"type\":\"keyword\"},\"chi2\":{\"type\":\"long\"},\"physicalOffset\":{\"type\":\"keyword\"},\"entropy\":{\"type\":\"long\"},\"name\":{\"type\":\"keyword\"},\"flags\":{\"type\":\"keyword\"},\"virtualAddress\":{\"type\":\"long\"},\"virtualSize\":{\"type\":\"long\"}}},\"headerABI_Version\":{\"type\":\"keyword\"},\"headerVersion\":{\"type\":\"keyword\"},\"segment\":{\"type\":\"nested\",\"properties\":{\"type\":{\"type\":\"keyword\"},\"sections\":{\"type\":\"keyword\"}}},\"headerType\":{\"type\":\"keyword\"},\"headerEntryPoint\":{\"type\":\"long\"},\"byteOrder\":{\"type\":\"keyword\"},\"architecture\":{\"type\":\"keyword\"}}},\"timestamp\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"group\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"previous\":{\"type\":\"nested\",\"properties\":{\"pid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"start\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"entityId\":{\"type\":\"keyword\"},\"message\":{\"type\":\"keyword\"},\"executable\":{\"type\":\"keyword\"},\"threadName\":{\"type\":\"keyword\"},\"labels\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"tags\":{\"type\":\"keyword\"},\"args\":{\"type\":\"keyword\"},\"upTime\":{\"type\":\"long\"},\"pe\":{\"properties\":{\"originalFileName\":{\"type\":\"keyword\"},\"product\":{\"type\":\"keyword\"},\"peHash\":{\"type\":\"keyword\"},\"importHash\":{\"type\":\"keyword\"},\"description\":{\"type\":\"keyword\"},\"company\":{\"type\":\"keyword\"},\"fileVersion\":{\"type\":\"keyword\"},\"architecture\":{\"type\":\"keyword\"}}},\"savedGroup\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}},\"name\":{\"type\":\"keyword\"},\"tty\":{\"type\":\"long\"},\"attributes\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"codeSignature\":{\"properties\":{\"valid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"trusted\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"teamId\":{\"type\":\"keyword\"},\"exists\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"digestAlgorithm\":{\"type\":\"keyword\"},\"signingId\":{\"type\":\"keyword\"},\"timestamp\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"subjectName\":{\"type\":\"keyword\"},\"status\":{\"type\":\"keyword\"}}},\"commandLine\":{\"type\":\"keyword\"},\"user\":{\"properties\":{\"roles\":{\"type\":\"keyword\"},\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"fullName\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"email\":{\"type\":\"keyword\"},\"group\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}}}},\"hash\":{\"type\":\"nested\",\"properties\":{\"sha1\":{\"type\":\"keyword\"},\"sha382\":{\"type\":\"keyword\"},\"sha256\":{\"type\":\"keyword\"},\"sha512\":{\"type\":\"keyword\"},\"tlsh\":{\"type\":\"keyword\"},\"ssdeep\":{\"type\":\"keyword\"},\"md5\":{\"type\":\"keyword\"}}}}}"),
                processMapping, false);
    }
}
