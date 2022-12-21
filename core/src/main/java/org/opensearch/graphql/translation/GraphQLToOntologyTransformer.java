package org.opensearch.graphql.translation;


import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.EchoingWiringFactory;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.GraphQLSchemaUtils;
import org.opensearch.schema.ontology.Ontology;
import org.opensearch.schema.ontology.OntologyTransformerIfc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This component is the transformation element which takes a GQL SDL files and translates them into the ontological structure
 * This structure is defined by the next basic elements:
 * <br>
 * <br>
 * - Entity
 * <br>
 * --- Concrete type entity - specific labeled entity with ID
 * <br>
 * --- Typed entity - - specific labeled entity
 * <br>
 * --- UnTyped entity - - multi labeled entity
 * <br>
 * - Relations
 * <br>
 * --- Typed relation - - specific labeled relation
 * <br>
 * --- UnTyped relation - - multi labeled relation
 *
 * <br>
 * - Properties
 * <br>
 * --- primitive property
 * <br>
 * - Constraints
 * <br>
 * - Quantifiers
 */

public class GraphQLToOntologyTransformer implements OntologyTransformerIfc<String, Ontology>, GraphQLSchemaUtils {
    private List<TranslationStrategy> chain;

    public GraphQLToOntologyTransformer() {
        chain = List.of(
                new ObjectTypeTranslation(),
                new PrimitivesTranslation(),
                new InterfaceTypeTranslation(),
                new EntitiesCreationTranslation(),
                new RelationsCreationTranslation(),
                new PropertiesTranslation(),
                new EnumTypeTranslation()
        );
    }

    /**
     * API that will transform a GraphQL schema into opengraph ontology schema
     *
     * @param source
     * @return
     */
    public Ontology transform(String ontologyName, String source) throws RuntimeException {
        try {
            Ontology ontology = transform(ontologyName, new FileInputStream(source));
            ontology.setOnt(ontologyName);
            return ontology;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * API that will translate a opengraph ontology schema to GraphQL schema
     */
    public String translate(Ontology source) {
        //Todo
        throw new RuntimeException("Not Implemented");
    }

    /**
     * API that will transform a GraphQL schema into opengraph ontology schema
     *
     * @param streams
     * @return
     */
    public Ontology transform(String ontologyName, InputStream... streams) {
        if (GraphQLEngineFactory.schema().isEmpty()) {
            GraphQLEngineFactory.generateSchema(new EchoingWiringFactory(), Arrays.asList(streams));
        }  //create a curated list of names for typed schema elements
        return transform(ontologyName, GraphQLEngineFactory.schema().get());

    }

    /**
     * @param graphQLSchema
     * @return
     */
    public Ontology transform(String ontologyName, GraphQLSchema graphQLSchema) {
        //translation context
        TranslationStrategy.TranslationContext context = new TranslationStrategy.TranslationContext(ontologyName);
        //validate language type
        validateLanguageType(graphQLSchema, context);
        //run translations strategy chain
        chain.forEach(element -> element.translate(graphQLSchema, context));
        //generate the ontology
        return context.build();
    }


    private void validateLanguageType(GraphQLSchema graphQLSchema, TranslationStrategy.TranslationContext context) {
        List<GraphQLNamedType> types = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> context.getLanguageTypes().contains(p.getName())).collect(Collectors.toList());

        if (types.size() != context.getLanguageTypes().size())
            throw new IllegalArgumentException("GraphQL schema doesnt include Query/Where types");
    }
}
