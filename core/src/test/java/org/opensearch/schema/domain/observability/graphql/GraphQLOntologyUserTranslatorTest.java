package org.opensearch.schema.domain.observability.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.translation.GraphQLToOntologyTransformer;
import org.opensearch.schema.index.schema.BaseTypeElement.Type;
import org.opensearch.schema.index.schema.Entity;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.opensearch.schema.ontology.PrimitiveType.Types.ID;
import static org.opensearch.schema.ontology.PrimitiveType.Types.STRING;
import static org.opensearch.schema.ontology.Property.equal;

/**
 * This test is verifying that the user SDL is correctly transformed into ontology & index-provider components
 */
public class GraphQLOntologyUserTranslatorTest {
    public static Ontology ontology;
    public static Accessor ontologyAccessor;

    @AfterAll
    public static void tearDown() throws Exception {
        GraphQLEngineFactory.reset();
    }
    @BeforeAll
    /**
     * load base & user graphQL SDL files, transform them into the ontology & index-provider components
     */
    public static void setUp() throws Exception {
        InputStream filterSchemaInput = new FileInputStream("../schema/filter.graphql");
        InputStream aggregationSchemaInput = new FileInputStream("../schema/aggregation.graphql");
        InputStream utilsSchemaInput = new FileInputStream("../schema/utils.graphql");
        InputStream baseSchemaInput = new FileInputStream("../schema/observability/logs/base.graphql");
        InputStream userSchemaInput = new FileInputStream("../schema/observability/logs/user.graphql");
        GraphQLToOntologyTransformer transformer = new GraphQLToOntologyTransformer();

        ontology = transformer.transform("user",utilsSchemaInput,filterSchemaInput,aggregationSchemaInput, baseSchemaInput, userSchemaInput);
        ontologyAccessor = new Accessor(ontology);
        Assertions.assertNotNull(ontology);
        String valueAsString = new ObjectMapper().writeValueAsString(ontology);
        Assertions.assertNotNull(valueAsString);
    }

    /**
     * test properties are correctly translated (sample properties are selected for comparison)
     */
    @Test
    public void testSamplePropertiesTranslation() {
        Assertions.assertTrue(equal(ontologyAccessor.property$("id"), new Property.MandatoryProperty(new Property("id", "id", ID.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("name"), new Property.MandatoryProperty(new Property("name", "name", STRING.asType()))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("group"), new Property.MandatoryProperty(new Property("group", "Group", ObjectType.of("Group")))));
        Assertions.assertTrue(equal(ontologyAccessor.property$("email"), new Property("email", "email", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("fullName"), new Property("fullName", "fullName", STRING.asType())));
        Assertions.assertTrue(equal(ontologyAccessor.property$("roles"), new Property("roles", "roles", STRING.asListType())));
    }

    /**
     * test the User entity is correctly translated into ontology structure
     */
    @Test
    public void testEntityTranslation() {
        Assertions.assertEquals(ontologyAccessor.entity$("User").isAbstract(), false);
        Assertions.assertEquals(ontologyAccessor.entity$("User").getIdField().size(), 1);
        Assertions.assertEquals(ontologyAccessor.entity$("User").idFieldName(), "id");
        Assertions.assertEquals(ontologyAccessor.entity$("User").geteType(), "User");
        Assertions.assertEquals(ontologyAccessor.entity$("User").getProperties().size(), 8);
        Assertions.assertEquals(ontologyAccessor.entity$("User").getMandatory().size(), 1);
        //implemented as nested object
        Assertions.assertTrue(equal(ontologyAccessor.property$("group"), new Property("group", "Group", ObjectType.of("Group"))));
    }

    /**
     * test the User's relation (has_Group) is correctly translated into ontology structure
     */
    @Test
    public void testRelationTranslation() {
        Assertions.assertEquals(ontologyAccessor.relations().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getName(), "has_Group");
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().size(), 1);
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().get(0).getName(), "User->Group");
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().get(0).geteTypeA(), "User");
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().get(0).getSideAFieldName(), "group");
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().get(0).geteTypeB(), "Group");
        Assertions.assertEquals(ontologyAccessor.relations().get(0).getePairs().get(0).getSideBIdField(), "id");

        Assertions.assertEquals(ontologyAccessor.entity$("Group").geteType(), "Group");
        Assertions.assertEquals(ontologyAccessor.entity$("Group").getIdField().size(), 1);
        Assertions.assertEquals(ontologyAccessor.entity$("Group").idFieldName(), "id");
        Assertions.assertEquals(ontologyAccessor.entity$("Group").getProperties().size(), 3);
        Assertions.assertEquals(ontologyAccessor.entity$("Group").getMandatory().size(), 1);
    }

    /**
     * test creation of an index provider using the predicate conditions for top level entity will be created an index
     */
    @Test
    public void testIndexProviderBuilder() throws Exception {
        IndexProvider provider = IndexProvider.Builder.generate(ontology
                , e -> e.getDirectives().stream().anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()))
                , r -> true);

        String valueAsString = new ObjectMapper().writeValueAsString(provider);
        Assert.assertNotNull(valueAsString);

        List<Entity> rootEntities = new ArrayList<>(provider.getEntities());
        Assertions.assertEquals(rootEntities.size(), 1);
        Assertions.assertEquals(rootEntities.get(0).getType(), Type.of("User"));

        Map<String, Entity> nested = rootEntities.get(0).getNested();
        Assertions.assertEquals(nested.size(), 1);
        Assertions.assertEquals(nested.get("group").getType(), Type.of("Group"));
        Assertions.assertEquals(provider.getRelations().size(), 0);
    }

}
