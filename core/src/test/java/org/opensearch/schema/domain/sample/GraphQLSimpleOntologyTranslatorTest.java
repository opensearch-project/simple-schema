package org.opensearch.schema.domain.sample;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.schema.graphql.GraphQLEngineFactory;
import org.opensearch.schema.graphql.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.BaseTypeElement;
import org.opensearch.schema.index.schema.Entity;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.Property;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.opensearch.schema.ontology.PrimitiveType.Types.*;
import static org.opensearch.schema.ontology.Property.equal;


/**
 * This test is verifying that the (example) simple SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLSimpleOntologyTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    /**
     * load sample graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");

        InputStream simpleSchemaInput = new FileInputStream("../schema/sample/simple.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("simple", utilsSchemaInput, filterSchemaInput, aggregationSchemaInput, simpleSchemaInput);
        Assertions.assertNotNull(ontology);
        ontologyAccessor = new Accessor(ontology);
    }

    /**
     * test creation of an index provider using the predicate conditions for top level entity will be created an index
     */
    @Test
    public void testIndexProviderBuilder() {
        IndexProvider provider = IndexProvider.Builder.generate(ontology);
        List<String> names = provider.getEntities().stream().map(Entity::getType).map(BaseTypeElement.Type::getName).toList();
        Assertions.assertTrue(names.contains("Author"));
        Assertions.assertTrue(names.contains("Book"));
        Assertions.assertTrue(names.contains("AuthorResults"));
        Assertions.assertTrue(names.contains("AuthorAggregationResults"));
        Assertions.assertTrue(names.contains("BookResults"));
        Assertions.assertTrue(names.contains("BookAggregationResults"));
    }


    /**
     * test properties are correctly translated (sample properties are selected for comparison)
     */
//    @Test
    @Ignore("Fix overriding properties by different schema types with similar names")
    public void fixTestSimplePropertiesTranslation() {
        Assertions.assertTrue(equal(ontologyAccessor.property$("ISBN"),
                new Property.MandatoryProperty(new Property("ISBN", "ISBN", ID.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("title"),
                new Property.MandatoryProperty(new Property("title", "title", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("name"),
                new Property.MandatoryProperty(new Property("name", "name", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("published"),
                new Property.MandatoryProperty(new Property("published", "published", DATETIME.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("born"),
                new Property.MandatoryProperty(new Property("born", "born", DATETIME.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("died"),
                new Property(new Property("died", "died", DATETIME.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("nationality"),
                new Property("nationality", "nationality", STRING.asType())));
    }

    /**
     * test the schema is correctly translated into ontology structure
     */
    @Test
    public void testEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("Book").geteType(), "Book");
        Assertions.assertEquals(ontologyAccessor.entity$("Author").geteType(), "Author");
        Assertions.assertEquals(ontologyAccessor.entity$("Book").getProperties().size(), 4);
        Assertions.assertEquals(ontologyAccessor.entity$("Author").getProperties().size(), 5);
    }

    @Test
    public void testQueryTranslation() {
        //todo
    }

}
