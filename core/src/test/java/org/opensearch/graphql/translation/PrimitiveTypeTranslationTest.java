package org.opensearch.graphql.translation;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.EchoingWiringFactory;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Value;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class PrimitiveTypeTranslationTest {
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
        //verify enum created
        Assert.assertTrue(accessor.primitiveType("url").isPresent());
        Assert.assertTrue(accessor.primitiveType("ip").isPresent());
        Assert.assertTrue(accessor.primitiveType("int").isPresent());
        Assert.assertTrue(accessor.primitiveType("datetime").isPresent());
        Assert.assertTrue(accessor.primitiveType("date").isPresent());
        Assert.assertTrue(accessor.primitiveType("long").isPresent());
        Assert.assertTrue(accessor.primitiveType("string").isPresent());
        Assert.assertTrue(accessor.primitiveType("float").isPresent());
        Assert.assertTrue(accessor.primitiveType("json").isPresent());
        Assert.assertTrue(accessor.primitiveType("time").isPresent());
        Assert.assertTrue(accessor.primitiveType("array").isPresent());
        Assert.assertTrue(accessor.primitiveType("text").isPresent());
        Assert.assertTrue(accessor.primitiveType("geopoint").isPresent());
        Assert.assertTrue(accessor.primitiveType("boolean").isPresent());
        Assert.assertTrue(accessor.primitiveType("id").isPresent());
    }

    private Accessor init() {
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        ObjectTypeTranslation objectTypeTranslation = new ObjectTypeTranslation();
        objectTypeTranslation.translate(schema, context);

        EntitiesCreationTranslation entitiesCreationTranslation = new EntitiesCreationTranslation();
        entitiesCreationTranslation.translate(schema, context);

        PrimitivesTranslation transformer = new PrimitivesTranslation();
        transformer.translate(schema, context);
        return new Accessor(context.getBuilder().build());
    }
}