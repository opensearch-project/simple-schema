package org.opensearch.graphql.translation;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.EchoingWiringFactory;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.ontology.Accessor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

class PropertiesTypeTranslationTest {
    static GraphQLSchema schema;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

    @BeforeAll
    static void setup() throws FileNotFoundException {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/sample/simpleGQLForeignBooks.graphql");
        schema = GraphQLEngineFactory.generateSchema(new EchoingWiringFactory(), Arrays.asList(utilsSchemaInput,stream));
    }

    @Test
    void translate() {
        Accessor accessor = init();
        //verify properties created - author
        Assert.assertTrue(accessor.$property("name").isPresent());
        Assert.assertTrue(accessor.$property("age").isPresent());
        Assert.assertTrue(accessor.$property("born").isPresent());
        Assert.assertTrue(accessor.$property("died").isPresent());
        Assert.assertTrue(accessor.$property("nationality").isPresent());

        //verify properties created - book
        Assert.assertTrue(accessor.$property("ISBN").isPresent());
        Assert.assertTrue(accessor.$property("title").isPresent());
        Assert.assertTrue(accessor.$property("description").isPresent());
        Assert.assertTrue(accessor.$property("genre").isPresent());
        Assert.assertTrue(accessor.$property("published").isPresent());

        Assert.assertTrue(accessor.$property("Author").isPresent());
        Assert.assertTrue(accessor.$property("Book").isPresent());
    }


    private Accessor init() {
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        ObjectTypeTranslation objectTypeTranslation = new ObjectTypeTranslation();
        objectTypeTranslation.translate(schema, context);

        EntitiesCreationTranslation entitiesCreationTranslation = new EntitiesCreationTranslation();
        entitiesCreationTranslation.translate(schema, context);

        PropertiesTranslation propertiesTranslation = new PropertiesTranslation();
        propertiesTranslation.translate(schema, context);
        return new Accessor(context.getBuilder().build());
    }

}