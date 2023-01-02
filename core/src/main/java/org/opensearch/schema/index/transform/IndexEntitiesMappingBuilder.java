package org.opensearch.schema.index.transform;


import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.index.schema.Entity;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.schema.MappingIndexType;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.template.SettingBuilder;
import org.opensearch.schema.index.template.TemplateMapping;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EntityType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.schema.index.transform.IndexMappingUtils.getDefaultSettings;
import static org.opensearch.schema.index.transform.IndexMappingUtils.populateProperty;

/**
 * the entity index provider responsible for transforming the ontology logical entity's definition into an opensearch mapping index template
 */
public class IndexEntitiesMappingBuilder implements TemplateMapping<EntityType, Entity> {
    private IndexProvider indexProvider;

    public IndexEntitiesMappingBuilder(IndexProvider indexProvider) {
        this.indexProvider = indexProvider;
    }

    /**
     * add the mapping part of the template according to the ontology entities
     *
     * @param client
     * @return
     */
    public Collection<PutIndexTemplateRequestBuilder> map(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests) {
        StreamSupport.stream(ontology.entities().spliterator(), false)
                //ignore abstract entities
                .filter(e -> !e.isAbstract())
                // ignore none top-level entities (only top level element will appear in the index provider config file)
                .filter(e -> this.indexProvider.getEntity(e.getName()).isPresent())
                .forEach(e -> generateMapping(e, ontology, client, requests));
        return requests.values();
    }

    private void generateMapping(EntityType e, Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests) {
        MappingIndexType mapping = this.indexProvider.getEntity(e.getName()).orElseThrow(
                        () -> new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                .getMapping();

        Entity entity = this.indexProvider.getEntity(e.getName()).get();
        try {
            switch (mapping) {//common general index - unifies all entities under the same physical index
                case UNIFIED:
                    buildUnifiedMapping(ontology, client, requests, entity, e);
                    break;
//static index
                case STATIC:
                    buildStaticMapping(ontology, client, requests, e, entity);
                    break;
                case PARTITIONED:
                    buildPartitionedMapping(ontology, client, requests, e, entity);
                    break;
            }
        } catch (Throwable typeNotFound) {
            //log error
        }
    }

    private void buildPartitionedMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, EntityType indexProviderEntity, Entity ontologyEntity) {
        //time partitioned index
        PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, indexProviderEntity.getName().toLowerCase());
        String label = ontologyEntity.getType().getName();
        request.setPatterns(new ArrayList<>(Arrays.asList(indexProviderEntity.getName().toLowerCase(), label, indexProviderEntity.getName(), String.format(ontologyEntity.getProps().getIndexFormat(), "*"))))
                .setSettings(generateSettings(ontology, indexProviderEntity, ontologyEntity, label));
        request.addMapping(label, generateElementMapping(ontology, indexProviderEntity, ontologyEntity, label.toLowerCase()));
        //dedup patterns -
        request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

        //add response to list of responses
        requests.put(indexProviderEntity.getName().toLowerCase(), request);
    }

    private void buildStaticMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, EntityType indexProviderEntity, Entity ontologyEntity) {
        ontologyEntity.getProps().getValues().forEach(v -> {
            String label = indexProviderEntity.geteType();
            PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, v.toLowerCase());
            request.setPatterns(new ArrayList<>(Arrays.asList(indexProviderEntity.getName().toLowerCase(), label, indexProviderEntity.getName(), String.format("%s%s", v, "*"))))
                    .setSettings(generateSettings(ontology, indexProviderEntity, ontologyEntity, label));
            request.addMapping(label, generateElementMapping(ontology, indexProviderEntity, ontologyEntity, label));

            //dedup patterns -
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

            //add response to list of responses
            requests.put(v.toLowerCase(), request);
        });
    }

    private void buildUnifiedMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, Entity ontologyEntity, EntityType indexProviderEntity) {
        ontologyEntity.getProps().getValues().forEach(v -> {
            String label = indexProviderEntity.geteType();
            String unifiedName = ontologyEntity.getProps().getValues().isEmpty() ? label : ontologyEntity.getProps().getValues().get(0);
            PutIndexTemplateRequestBuilder request = requests.computeIfAbsent(unifiedName, s -> new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, unifiedName));

            List<String> patterns = new ArrayList<>(Arrays.asList(indexProviderEntity.getName().toLowerCase(), label, indexProviderEntity.getName(), String.format("%s%s", v, "*")));
            if (Objects.isNull(request.request().patterns())) {
                request.setPatterns(new ArrayList<>(patterns));
            } else {
                request.request().patterns().addAll(patterns);
            }
            //dedup patterns -
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));
            //no specific index sort order since it contains multiple ontologyEntity types -
            if (request.request().settings().isEmpty()) {
                request.setSettings(getDefaultSettings().build());
            }
            //create new mapping only when no prior ontologyEntity set this mapping before
            if (request.request().mappings().isEmpty()) {
                request.addMapping(unifiedName, generateElementMapping(ontology, indexProviderEntity, ontologyEntity, unifiedName));
            } else {
                populateProperty(ontology, ontologyEntity, request.getMappingsProperties(unifiedName), indexProviderEntity);
            }
        });
    }

    private Map<String, Object> populateMappingIndexFields(Accessor ontology, Entity ontologyEntity, Optional<EntityType> indexProviderEntity) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(OntologyIndexGenerator.IndexSchemaConfig.PROPERTIES, properties);
        //populate fields & metadata
        EntityType entityType = indexProviderEntity.get();

        //generate field id -> only if field id array size > 1
        if (entityType.getIdField().size() > 1) {
            properties.put(entityType.idFieldName(), Collections.singletonMap("type", "keyword"));
        }//otherwise that field id is already a part of the regular fields

        populateProperty(ontology, ontologyEntity, properties, entityType);
        return mapping;
    }

    /**
     * generate specific entity type mapping
     *
     * @param indexProviderEntity
     * @param ontologyEntity
     * @param label
     * @return
     */
    public Map<String, Object> generateElementMapping(Accessor ontology, EntityType indexProviderEntity, Entity ontologyEntity, String label) {
        Optional<EntityType> entity = ontology.entity(indexProviderEntity.getName());
        if (!entity.isPresent())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();
        //populate index fields
        jsonMap.put(label, populateMappingIndexFields(ontology, ontologyEntity, entity));

        return jsonMap;
    }

    /**
     * add the index entity settings part of the template according to the ontology relations
     *
     * @return
     */
    private Settings generateSettings(Accessor ontology, EntityType indexProviderEntity, Entity ontologyEntity, String label) {
        ontology.entity(indexProviderEntity.getName()).get().getIdField().forEach(idField -> {
            if (!ontology.entity(indexProviderEntity.getName()).get().fields().contains(idField))
                throw new SchemaError.SchemaErrorException(new SchemaError("Entity Schema generation exception", " Entity " + label + " not containing id metadata property "));
        });
        // TODO: - use index provider to correctly build index settings
        return builder(ontology, ontologyEntity);
    }

    private Settings builder(Accessor ontology, Entity entity) {
        SettingBuilder settings = getDefaultSettings();
        if (entity.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            settings.sortByField(ontology.entity$(entity.getType().getName()).idFieldName(), true);
        }
        return settings.build();
    }

}
