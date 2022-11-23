package org.opensearch.schema.graphql;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.Transformer;
import org.opensearch.languages.ioql.query.descriptor.QueryDescriptor;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.GraphQLToOntologyTransformer;
import org.opensearch.languages.ioql.graphql.GraphQLToQueryTransformer;
import org.opensearch.languages.ioql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.Ontology;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * this test translated the GQL schema into an ontology and takes a GQL query and using the generated ontology generates an intermediate query - Using the IOQL
 * the Intermediate Ontological Query Language
 */
public class GraphQLSimpleQueryExecuterTest {
    public static Accessor accessor;
    private static List<InputStream> streams;
    public static Transformer<Query> transformer;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }

    /**
     * load sample graphQL SDL files, transform them into the ontology & index-provider components
     */
    @BeforeAll
    public static void setUp() throws Exception {
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");

        InputStream simpleSchemaInput = new FileInputStream("../schema/sample/simple.graphql");
        streams = Arrays.asList(filterSchemaInput, aggregationSchemaInput, utilsSchemaInput, simpleSchemaInput);
        //expect engine not yet created
        assertTrue(GraphQLEngineFactory.engine().isEmpty());
        //create a GQL to Query IOQL transformer
        transformer = new GraphQLToQueryTransformer();

        // first create an ontology from the GQL SDL
        GraphQLToOntologyTransformer graphQLToOntologyTransformer = new GraphQLToOntologyTransformer();
        GraphQLEngineFactory.generateSchema(streams);
        assertTrue(GraphQLEngineFactory.schema().isPresent());

        //next generate the ontology from the GQL schema
        Ontology ontology = graphQLToOntologyTransformer.transform("Simple", GraphQLEngineFactory.schema().get());
        assertNotNull(ontology);
        accessor = new Accessor(ontology);

        //next generate the actual GQL engine
        GraphQLEngineFactory.generateEngine(GraphQLEngineFactory.schema().get());
        //expect engine created correctly
        assertTrue(GraphQLEngineFactory.engine().isPresent());
    }


    @Test
    public void testWrongTypeQueryExpectingInformativeError() {
        String q = " {\n" +
                "    human {\n" +
                "        name,\n" +
                "        description\n" +
                "    }\n" +
                "}";

        Query query = null;
        try {
            query = transformer.transform(accessor, q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'human' in type 'Query' is undefined @ 'human'", err.getError().getErrorDescription());
        }
        assertNull(query);
    }

    @Test
    public void testCorrectTypeWithWrongFieldsQueryExpectingInformativeError() {
        String q = " {\n" +
                "    book {\n" +
                "        name\n" +
                "    }\n" +
                "}";

        Query query = null;
        try {
            query = transformer.transform(accessor, q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'name' in type 'Book' is undefined @ 'book/name'", err.getError().getErrorDescription());
        }
        assertNull(query);
    }

    @Test
    public void testCorrectTypeWithWrongInnerTypeFieldsQueryExpectingInformativeError() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        person \n" +
                "    }\n" +
                "}";

        Query query = null;
        try {
            query = transformer.transform(accessor, q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'person' in type 'Author' is undefined @ 'author/person'", err.getError().getErrorDescription());
        }
        assertNull(query);
    }

    @Test
    public void testCorrectTypeWithWrongInnerTypeInnerFieldsQueryExpectingInformativeError() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        books { \n" +
                "            name \n" +
                "                 }\n" +
                "    }\n" +
                "}";

        Query query = null;
        try {
            query = transformer.transform(accessor, q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'name' in type 'Book' is undefined @ 'author/books/name'", err.getError().getErrorDescription());
        }
        assertNull(query);
    }


    @Test
    public void testCorrectTypeWithPartialField() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        born\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(accessor, q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Author:1]──Q[2]:{3|4}, \n" +
                "                           └─?[3]:[name<IdentityProjection>], \n" +
                "                           └─?[4]:[born<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testCorrectTypeWithAllField() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        born,\n" +
                "        died,\n" +
                "        nationality,\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(accessor, q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Author:1]──Q[2]:{3|4|5|6}, \n" +
                "                               └─?[3]:[name<IdentityProjection>], \n" +
                "                               └─?[4]:[born<IdentityProjection>], \n" +
                "                               └─?[5]:[died<IdentityProjection>], \n" +
                "                               └─?[6]:[nationality<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testCorrectTypeWithAllFieldIncludingInnerNestedPartialField() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        born\n" +
                "        died\n" +
                "        nationality\n" +
                "        books {\n" +
                "           title\n"+
                "         }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(accessor, q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Author:1]──Q[2]:{3|4|5|6|7}, \n" +
                "                                 └─?[3]:[name<IdentityProjection>], \n" +
                "                                 └─?[4]:[born<IdentityProjection>], \n" +
                "                                 └─?[5]:[died<IdentityProjection>], \n" +
                "                                 └─?[6]:[nationality<IdentityProjection>], \n" +
                "                                 └─Typ[Book:7]──Q[8]:{9}, \n" +
                "                                                    └─?[9]:[title<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testCorrectTypeWithAllFieldIncludingInnerNestedAllField() {
        String q = " {\n" +
                "    author {\n" +
                "        name,\n" +
                "        born\n" +
                "        died\n" +
                "        nationality\n" +
                "        books {\n" +
                "           ISBN\n"+
                "           title\n"+
                "           published\n"+
                "         }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(accessor, q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Author:1]──Q[2]:{3|4|5|6|7}, \n" +
                "                                 └─?[3]:[name<IdentityProjection>], \n" +
                "                                 └─?[4]:[born<IdentityProjection>], \n" +
                "                                 └─?[5]:[died<IdentityProjection>], \n" +
                "                                 └─?[6]:[nationality<IdentityProjection>], \n" +
                "                                 └─Typ[Book:7]──Q[8]:{9|10|11}, \n" +
                "                                                          └─?[9]:[ISBN<IdentityProjection>], \n" +
                "                                                          └─?[10]:[title<IdentityProjection>], \n" +
                "                                                          └─?[11]:[published<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }
}
