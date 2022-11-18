package org.opensearch.schema.domain.observability.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.NoOpClient;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.transform.IndexEntitiesMappingBuilder;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This test is verifying that the process of generating an index provider from the ontology is working as expected
 */
public class MappingSimpleEntityTemplateGeneratorTest {
    static Ontology ontology;
    static IndexProvider indexProvider;

    @BeforeAll
    /**
     * load process (including all it's dependencies) graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/user.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("model"))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("relation") && d.containsArgVal("foreign")));
    }

    @Test
    /**
     * verify the process entity index-provider contains basic structure and properties
     */
    public void GenerateAgentEntityMappingTest() throws IOException, JSONException {
        IndexEntitiesMappingBuilder builder = new IndexEntitiesMappingBuilder(indexProvider);
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        Collection<PutIndexTemplateRequestBuilder> results = builder.map(new Accessor(ontology), new NoOpClient("test"), requests);
        Assert.assertNotNull(requests.get("user"));
        Assert.assertEquals(1, requests.get("user").getMappings().size());
        Assert.assertNotNull(requests.get("user").getMappings().get("User"));
        Assert.assertNotNull(((Map) requests.get("user").getMappings().get("User")).get("properties"));
        Assert.assertEquals(8, ((Map) ((Map) requests.get("user").getMappings().get("User")).get("properties")).size());

        //test template generation structure
        XContentBuilder user = requests.get("user").request().toXContent(XContentBuilder.builder(XContentType.JSON.xContent()), ToXContent.EMPTY_PARAMS);
        user.prettyPrint();
        user.flush();
        Assert.assertNotNull(BytesReference.bytes(user).utf8ToString());
        // expected user index template mapping
        JSONAssert.assertEquals("{\"index_patterns\":[\"user\",\"User\",\"User*\"],\"order\":0,\"settings\":{},\"mappings\":{\"_doc\":{\"_doc\":{\"User\":{\"properties\":{\"roles\":{\"type\":\"keyword\"},\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"fullName\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"email\":{\"type\":\"keyword\"},\"hash\":{\"type\":\"keyword\"},\"group\":{\"properties\":{\"domain\":{\"type\":\"keyword\"},\"name\":{\"type\":\"keyword\"},\"id\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}}}}}}}}},\"aliases\":{}}", BytesReference.bytes(user).utf8ToString(),false);
    }
}
