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

class InterfaceTypeTranslationTest {
    static GraphQLSchema schema;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    static void setup() throws FileNotFoundException {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("graphql/sample/simpleInterfaceGQLForeignBooks.graphql");
        schema = GraphQLEngineFactory.generateSchema(new EchoingWiringFactory(), Arrays.asList(utilsSchemaInput,stream));
    }

    @Test
    void translatePerson() {
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext("test");
        ObjectTypeTranslation objectTypeTranslation = new ObjectTypeTranslation();
        objectTypeTranslation.translate(schema, context);

        InterfaceTypeTranslation transformer = new InterfaceTypeTranslation();
        transformer.translate(schema, context);
        Accessor accessor = new Accessor(context.getBuilder().build());
        //verify interface created
        Assert.assertTrue(accessor.entity("Person").isPresent());
        Assert.assertTrue(accessor.entity("Person").get().isAbstract());
        Assert.assertTrue(accessor.entity("Person").get().containsProperty("name"));
        Assert.assertTrue(accessor.entity("Person").get().containsProperty("born"));
        Assert.assertTrue(accessor.entity("Person").get().containsProperty("age"));
        Assert.assertTrue(accessor.entity("Person").get().containsProperty("died"));
    }

}