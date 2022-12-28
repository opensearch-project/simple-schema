package org.opensearch.schema.domain.sample.index;

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
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This test is verifying that the process of generating an index provider from the ontology is working as expected
 */
public class MappingSimpleEntityIndexProviderTest {
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
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/sample/simple.json");
        ontology = new ObjectMapper().readValue(stream, Ontology.class);
        indexProvider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("model"))
                , r -> r.getDirectives().stream()
                        .anyMatch(d -> d.getName().equals("relation") && d.containsArgVal("foreign")));
    }

    @Test
    /**
     * verify the author entity index-provider contains basic structure and properties
     */
    public void testAuthorEntityIndexProviderTest()  {
        //todo
    }

    @Test
    /**
     * verify the book entity index-provider contains basic structure and properties
     */
    public void testBooksEntityIndexProviderTest()  {
        //todo
    }

}
