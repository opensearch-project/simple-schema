package org.opensearch.schema.graphql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opensearch.descriptors.QueryDescriptor;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.GraphQLToOntologyTransformer;
import org.opensearch.graphql.GraphQLToQueryTransformer;
import org.opensearch.query.Query;
import org.opensearch.schema.SchemaError;
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
    public static Ontology ontology;
    private static List<InputStream> streams;
    public static GraphQLToQueryTransformer transformer;

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

        // first create an ontology from the GQL SDL
        GraphQLToOntologyTransformer graphQLToOntologyTransformer = new GraphQLToOntologyTransformer();
        GraphQLEngineFactory.generateSchema(streams);
        assertTrue(GraphQLEngineFactory.schema().isPresent());

        //next generate the actual GQL engine
        GraphQLEngineFactory.generateEngine(GraphQLEngineFactory.schema().get());
        //expect engine created correctly
        assertTrue(GraphQLEngineFactory.engine().isPresent());

        //next generate the ontology from the GQL schema
        ontology = graphQLToOntologyTransformer.transform("Simple", GraphQLEngineFactory.schema().get());
        assertNotNull(ontology);
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
            query = transformer.transform(q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'human' in type 'Query' is undefined @ 'human'",err.getError().getErrorDescription());
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
            query = transformer.transform(q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'name' in type 'Book' is undefined @ 'book/name'",err.getError().getErrorDescription());
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
            query = transformer.transform(q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'person' in type 'Author' is undefined @ 'author/person",err.getError().getErrorDescription());
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
            query = transformer.transform(q);
        } catch (SchemaError.SchemaErrorException err) {
            assertEquals("Validation error of type FieldUndefined: Field 'name' in type 'Book' is undefined @ 'author/books/name'",err.getError().getErrorDescription());
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
        Query query = transformer.transform(q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4}, \n" +
                "                          └─?[3]:[name<IdentityProjection>], \n" +
                "                          └─?[4]:[description<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }


    @Test
    public void testConstraintByIdQuerySingleVertexWithFewProperties() {
        String q = "{\n" +
                "    human (where: {\n" +
                "        operator: AND,\n" +
                "        constraints: [{\n" +
                "            operand: \"name\",\n" +
                "            operator: \"like\",\n" +
                "            expression: \"jhone\"\n" +
                "        },\n" +
                "        {\n" +
                "            operand: \"description\",\n" +
                "            operator: \"notEmpty\"\n" +
                "        }]\n" +
                "    }) {\n" +
                "\n" +
                "        name,\n" +
                "        description\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|5}, \n" +
                "                            └─?[3]:[name<like,jhone>, description<notEmpty,null>], \n" +
                "                            └─?[4]:[name<IdentityProjection>], \n" +
                "                            └─?[5]:[description<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithSinleRelation() {
        String q = " {\n" +
                "    human {\n" +
                "       friends {\n" +
                "            name\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3}, \n" +
                "                        └-> Rel(friends:3)──Typ[Character:4]──Q[5]:{6}, \n" +
                "                                                                  └─?[6]:[name<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoRelationAndProperties() {
        String q = " {\n" +
                "    human {\n" +
                "        name,\n" +
                "        friends {\n" +
                "            name\n" +
                "        },\n" +
                "        owns {\n" +
                "            name,\n" +
                "            appearsIn\n" +
                "            }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|8}, \n" +
                "                            └─?[3]:[name<IdentityProjection>], \n" +
                "                            └-> Rel(friends:4)──Typ[Character:5]──Q[6]:{7}, \n" +
                "                                                                      └─?[7]:[name<IdentityProjection>]──Typ[Droid:9]──Q[10]:{11|12}, \n" +
                "                            └-> Rel(owns:8), \n" +
                "                                       └─?[11]:[name<IdentityProjection>], \n" +
                "                                       └─?[12]:[appearsIn<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testQuerySingleVertexWithTwoHopesRelationAndProperties() {
        String q = "{\n" +
                "    human {\n" +
                "        name,\n" +
                "        friends {\n" +
                "            name\n" +
                "        }\n" +
                "        owns {\n" +
                "            name,\n" +
                "            appearsIn,\n" +
                "            friends {\n" +
                "                name,\n" +
                "                description\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Query query = transformer.transform(q);
        String expected = "[└── Start, \n" +
                "    ──Typ[Human:1]──Q[2]:{3|4|8}, \n" +
                "                            └─?[3]:[name<IdentityProjection>], \n" +
                "                            └-> Rel(friends:4)──Typ[Character:5]──Q[6]:{7}, \n" +
                "                                                                      └─?[7]:[name<IdentityProjection>]──Typ[Droid:9]──Q[10]:{11|12|13}, \n" +
                "                            └-> Rel(owns:8), \n" +
                "                                       └─?[11]:[name<IdentityProjection>], \n" +
                "                                       └─?[12]:[appearsIn<IdentityProjection>], \n" +
                "                                       └-> Rel(friends:13)──Typ[Character:14]──Q[15]:{16|17}, \n" +
                "                                                                                        └─?[16]:[name<IdentityProjection>], \n" +
                "                                                                                        └─?[17]:[description<IdentityProjection>]]";
        assertEquals(expected, QueryDescriptor.print(query));
    }
}
