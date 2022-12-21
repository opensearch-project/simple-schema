package org.opensearch.schema.index.transform;


import javaslang.Tuple2;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.client.Client;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.schema.NestingType;
import org.opensearch.schema.index.schema.Relation;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.ontology.*;

import java.util.*;

/**
 * the projection index provider responsible for transforming the ontology logical projection type structures definition into an opensearch mapping index template
 */
public class IndexProjectionMappingBuilder {

    private IndexRelationsMappingBuilder relationsMappingBuilder;
    private IndexProvider indexProvider;

    public IndexProjectionMappingBuilder(IndexRelationsMappingBuilder relationsMappingBuilder, IndexProvider indexProvider) {
        this.relationsMappingBuilder = relationsMappingBuilder;
        this.indexProvider = indexProvider;
    }

    /**
     * wrap entities with projection related metadata fields for the purpose of the projection index mapping creation
     *
     * @param ontology
     * @return
     */
    Accessor generateProjectionOntology(Ontology ontology) {
        //adding projection related metadata
        Ontology clone = new Ontology(ontology);
        //add projection related metadata
        clone.getEntityTypes().forEach(e -> e.setMetadata(Collections.singletonList("tag")));
        clone.getRelationshipTypes().forEach(r -> r.setMetadata(Collections.singletonList("tag")));
        clone.getRelationshipTypes().forEach(r -> r.setMetadata(Collections.singletonList(OntologyIndexGenerator.EdgeSchemaConfig.DEST_TYPE)));
        clone.getRelationshipTypes().forEach(r -> r.setMetadata(Collections.singletonList(OntologyIndexGenerator.EdgeSchemaConfig.DEST_ID)));

        clone.getProperties().add(new Property("tag", "tag", PrimitiveType.Types.STRING.asType()));
        clone.getProperties().add(new Property(OntologyIndexGenerator.EdgeSchemaConfig.DEST_TYPE, OntologyIndexGenerator.EdgeSchemaConfig.DEST_TYPE, PrimitiveType.Types.STRING.asType()));
        clone.getProperties().add(new Property(OntologyIndexGenerator.EdgeSchemaConfig.DEST_ID, OntologyIndexGenerator.EdgeSchemaConfig.DEST_ID, PrimitiveType.Types.STRING.asType()));
        return new Accessor(clone);
    }


    /**
     * add the mapping part of the template according to the ontology relations
     *
     * @return
     */


    /**
     * add the mapping part of the template according to the ontology
     * This projection mapping is a single unified index containing the entire ontology wrapped into a single index so that
     * every type of query result can be indexed and queried for slice & dice type of questions
     *
     * @param client
     * @return
     */
    public Collection<PutIndexTemplateRequestBuilder> map(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests) {
        PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, "projection");
        request.setSettings(IndexMappingUtils.getDefaultSettings().build()).setPatterns(Collections.singletonList(String.format("%s*", OntologyIndexGenerator.ProjectionConfigs.PROJECTION)));

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> rootMapping = new HashMap<>();
        Map<String, Object> rootProperties = new HashMap<>();
        rootMapping.put(OntologyIndexGenerator.IndexSchemaConfig.PROPERTIES, rootProperties);

        //populate the query id
        rootProperties.put(OntologyIndexGenerator.ProjectionConfigs.QUERY_ID, IndexMappingUtils.parseType(ontology, PrimitiveType.Types.STRING.asType()));
        rootProperties.put(OntologyIndexGenerator.ProjectionConfigs.CURSOR_ID, IndexMappingUtils.parseType(ontology, PrimitiveType.Types.STRING.asType()));
        rootProperties.put(OntologyIndexGenerator.ProjectionConfigs.EXECUTION_TIME, IndexMappingUtils.parseType(ontology, PrimitiveType.Types.DATE.asType()));
        //populate index fields
        jsonMap.put(OntologyIndexGenerator.ProjectionConfigs.PROJECTION, rootMapping);

        IndexProvider projection = new IndexProvider(this.indexProvider);
        //remove nested entities since we upgraded them to the root level
        projection.getEntities().forEach(e -> e.getNested().clear());

        projection.getEntities()
                .forEach(entity -> {
                    //todo remove nested entities since they already appear as a qualified ontological entity
                    try {
                        //generate entity mapping - each entity should be a nested objects array
                        entity.setNesting(NestingType.NESTED);
                        Map<String, Object> objectMap = IndexMappingUtils.generateNestedEntityMapping(ontology, rootProperties,
                                new Tuple2<>(entity.getType().getName(), entity));
                        //generate relation mapping - each entity's relation should be a nested objects array inside the entity
                        List<RelationshipType> relationshipTypes = ontology.relationBySideA(entity.getType().getName());
                        relationshipTypes.forEach(rel -> {
                            Relation relation = this.indexProvider.getRelation(rel.getName()).get();
                            relation.setNesting(NestingType.NESTED);
                            relationsMappingBuilder.generateNestedRelationMapping(ontology, (Map<String, Object>) objectMap.get(OntologyIndexGenerator.IndexSchemaConfig.PROPERTIES),
                                    new Tuple2<>(relation.getType().getName(),relation));
                        });
                    } catch (Throwable typeNotFound) {
                        //log error
                    }
                });
        request.addMapping(OntologyIndexGenerator.ProjectionConfigs.PROJECTION, jsonMap);
        //add response to list of responses
        requests.put(OntologyIndexGenerator.ProjectionConfigs.PROJECTION, request);
        return requests.values();
    }
}