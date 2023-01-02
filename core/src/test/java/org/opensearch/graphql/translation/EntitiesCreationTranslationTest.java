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

class EntitiesCreationTranslationTest {
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
    void translateAuthor() {
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        ObjectTypeTranslation objectTypeTranslation = new ObjectTypeTranslation();
        objectTypeTranslation.translate(schema, context);

        EntitiesCreationTranslation transformer = new EntitiesCreationTranslation();
        transformer.translate(schema, context);
        Accessor accessor = new Accessor(context.getBuilder().build());
        //verify entities created
        Assert.assertTrue(accessor.entity("Author").isPresent());
        Assert.assertFalse(accessor.entity("Author").get().isAbstract());
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("name"));
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("born"));
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("age"));
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("died"));
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("nationality"));
        Assert.assertTrue(accessor.entity("Author").get().containsProperty("books"));
    }

    @Test
    void translateBook() {
        EntitiesCreationTranslation transformer = new EntitiesCreationTranslation();
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        transformer.translate(schema, context);
        Accessor accessor = new Accessor(context.getBuilder().build());
        //verify entities created
        Assert.assertTrue(accessor.entity("Book").isPresent());
        Assert.assertFalse(accessor.entity("Book").get().isAbstract());
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("title"));
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("ISBN"));
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("genre"));
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("published"));
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("description"));
        Assert.assertTrue(accessor.entity("Book").get().containsProperty("author"));
    }
}