package org.opensearch.schema.domain.sample.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
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
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.transform.IndexEntitiesMappingBuilder;
import org.opensearch.schema.index.transform.IndexRelationsMappingBuilder;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.DirectiveEnumTypes;
import org.opensearch.schema.ontology.Ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This test is verifying that the process of generating a mapping template from the index provider using the ontology is working as expected
 */
public class MappingSimpleForeignIndexProviderTest {
    static Ontology ontology;
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
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simpleSchemaForeignBooks.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> DirectiveEnumTypes.RELATION.isSame(d.getName())));
    }

    @Test
    /**
     * verify the process entity index-provider contains basic structure and properties
     */
    public void GenerateAuthorEntityNestedBooksMappingTest() {
        IndexEntitiesMappingBuilder builder = new IndexEntitiesMappingBuilder(indexProvider);
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        builder.map(new Accessor(ontology), new NoOpClient("test"), requests);

        Assert.assertNotNull(requests.get("author"));
        Assert.assertEquals(1, requests.get("author").getMappings().size());
        Assert.assertNotNull(requests.get("author").getMappings().get("Author"));

        Map author = (Map) requests.get("author").getMappings().get("Author");
        Assert.assertNotNull(author.get("properties"));

        Map authorPropertiesMap = (Map) author.get("properties");
        //books is defined as foreign relation to author therefor it will not consider as a property
        // it will have a dedicated relation index named has_books
        Assert.assertFalse(authorPropertiesMap.containsKey("books"));
    }

    @Test
    /**
     * verify the process entity index-provider contains basic structure and properties
     */
    public void GenerateAuthorEntityMappingTest() throws IOException, JSONException {
        IndexEntitiesMappingBuilder builder = new IndexEntitiesMappingBuilder(indexProvider);
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        builder.map(new Accessor(ontology), new NoOpClient("test"), requests);

        Assert.assertNotNull(requests.get("author"));
        Assert.assertEquals(1, requests.get("author").getMappings().size());
        Assert.assertNotNull(requests.get("author").getMappings().get("Author"));

        Map author = (Map) requests.get("author").getMappings().get("Author");
        Assert.assertNotNull(author.get("properties"));

        Map authorPropertiesMap = (Map) author.get("properties");
        //books are a foreign index
        Assert.assertFalse(authorPropertiesMap.containsKey("books"));
        Assert.assertTrue(authorPropertiesMap.containsKey("nationality"));
        Assert.assertTrue(authorPropertiesMap.containsKey("name"));
        Assert.assertTrue(authorPropertiesMap.containsKey("born"));
        Assert.assertTrue(authorPropertiesMap.containsKey("died"));
        Assert.assertTrue(authorPropertiesMap.containsKey("age"));


        //test template generation structure
        XContentBuilder xContent = requests.get("author").request().toXContent(XContentBuilder.builder(XContentType.JSON.xContent()), ToXContent.EMPTY_PARAMS);
        xContent.prettyPrint();
        xContent.flush();
        Assert.assertNotNull(BytesReference.bytes(xContent).utf8ToString());
    }


    @Test
    /**
     * verify the process relations index-provider contains basic structure and properties
     */
    public void GenerateAuthorBooksRelationsMappingTest() {
        IndexRelationsMappingBuilder builder = new IndexRelationsMappingBuilder(indexProvider);
        HashMap<String, PutIndexTemplateRequestBuilder> requests = new HashMap<>();
        builder.map(new Accessor(ontology), new NoOpClient("test"), requests);

        //TODO - Fix According to specific tests - we expect here the relationship table be symmetric for both author->book & book->author
/*
        Assert.assertNotNull(requests.get("has_Author"));
        Assert.assertEquals(1, requests.get("author").getMappings().size());
        Assert.assertNotNull(requests.get("author").getMappings().get("Author"));
*/

    }

}
