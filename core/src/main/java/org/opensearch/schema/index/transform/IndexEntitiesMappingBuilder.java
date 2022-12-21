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
                .forEach(e -> mappingFunc(e, ontology, client, requests));
        return requests.values();
    }

    private void mappingFunc(EntityType e, Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests) {
        MappingIndexType mapping = this.indexProvider.getEntity(e.getName()).orElseThrow(
                        () -> new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                .getMapping();

        Entity entity = this.indexProvider.getEntity(e.getName()).get();
        try {
            switch (mapping) {//common general index - unifies all entities under the same physical index
                case UNIFIED:
                    buildUnifiedMapping(ontology, client, requests, e, entity);
                    break;
//static index
                case STATIC:
                    buildStaticMapping(ontology, client, requests, e, entity);
                    break;
                case TIME:
                    buildTimebasedMapping(ontology, client, requests, e, entity);
                    break;
            }
        } catch (Throwable typeNotFound) {
            //log error
        }
    }

    private void buildTimebasedMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, EntityType e, Entity entity) {
        //time partitioned index
        PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, e.getName().toLowerCase());
        String label = entity.getType().getName();
        request.setPatterns(new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format(entity.getProps().getIndexFormat(), "*"))))
                .setSettings(generateSettings(ontology, e, entity, label));
        request.addMapping(label, generateElementMapping(ontology, e, entity, label.toLowerCase()));
        //dedup patterns -
        request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

        //add response to list of responses
        requests.put(e.getName().toLowerCase(), request);
    }

    private void buildStaticMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, EntityType e, Entity entity) {
        entity.getProps().getValues().forEach(v -> {
            String label = e.geteType();
            PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, v.toLowerCase());
            request.setPatterns(new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*"))))
                    .setSettings(generateSettings(ontology, e, entity, label));
            request.addMapping(label, generateElementMapping(ontology, e, entity, label));

            //dedup patterns -
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

            //add response to list of responses
            requests.put(v.toLowerCase(), request);
        });
    }

    private void buildUnifiedMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, EntityType e, Entity entity) {
        entity.getProps().getValues().forEach(v -> {
            String label = e.geteType();
            String unifiedName = entity.getProps().getValues().isEmpty() ? label : entity.getProps().getValues().get(0);
            PutIndexTemplateRequestBuilder request = requests.computeIfAbsent(unifiedName, s -> new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, unifiedName));

            List<String> patterns = new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*")));
            if (Objects.isNull(request.request().patterns())) {
                request.setPatterns(new ArrayList<>(patterns));
            } else {
                request.request().patterns().addAll(patterns);
            }
            //dedup patterns -
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));
            //no specific index sort order since it contains multiple entity types -
            if (request.request().settings().isEmpty()) {
                request.setSettings(getDefaultSettings().build());
            }
            //create new mapping only when no prior entity set this mapping before
            if (request.request().mappings().isEmpty()) {
                request.addMapping(unifiedName, generateElementMapping(ontology, e, entity, unifiedName));
            } else {
                populateProperty(ontology, entity, request.getMappingsProperties(unifiedName), e);
            }
        });
    }

    private Map<String, Object> populateMappingIndexFields(Accessor ontology, Entity ent, Optional<EntityType> entity) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(OntologyIndexGenerator.IndexSchemaConfig.PROPERTIES, properties);
        //populate fields & metadata
        EntityType entityType = entity.get();

        //generate field id -> only if field id array size > 1
        if (entityType.getIdField().size() > 1) {
            properties.put(entityType.idFieldName(), Collections.singletonMap("type", "keyword"));
        }//otherwise that field id is already a part of the regular fields

        populateProperty(ontology, ent, properties, entityType);
        return mapping;
    }

    /**
     * generate specific entity type mapping
     *
     * @param entityType
     * @param ent
     * @param label
     * @return
     */
    public Map<String, Object> generateElementMapping(Accessor ontology, EntityType entityType, Entity ent, String label) {
        Optional<EntityType> entity = ontology.entity(entityType.getName());
        if (!entity.isPresent())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();
        //populate index fields
        jsonMap.put(label, populateMappingIndexFields(ontology, ent, entity));

        return jsonMap;
    }

    /**
     * add the index entity settings part of the template according to the ontology relations
     *
     * @return
     */
    private Settings generateSettings(Accessor ontology, EntityType entityType, Entity entity, String label) {
        ontology.entity(entityType.getName()).get().getIdField().forEach(idField -> {
            if (!ontology.entity(entityType.getName()).get().fields().contains(idField))
                throw new SchemaError.SchemaErrorException(new SchemaError("Entity Schema generation exception", " Entity " + label + " not containing id metadata property "));
        });
        // TODO: 05/12/2019  - use index provider to correctly build index settings
        return builder(ontology, entity);
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
