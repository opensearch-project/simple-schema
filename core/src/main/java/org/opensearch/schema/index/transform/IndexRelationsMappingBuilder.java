package org.opensearch.schema.index.transform;

import javaslang.Tuple2;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.index.schema.BaseTypeElement;
import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.index.schema.MappingIndexType;
import org.opensearch.schema.index.schema.Relation;
import org.opensearch.schema.index.template.PutIndexTemplateRequestBuilder;
import org.opensearch.schema.index.template.SettingBuilder;
import org.opensearch.schema.index.template.TemplateMapping;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.PrimitiveType;
import org.opensearch.schema.ontology.RelationshipType;

import java.util.*;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.transform.IndexMappingUtils.*;
import static org.opensearch.schema.index.transform.OntologyIndexGenerator.EdgeSchemaConfig.*;
import static org.opensearch.schema.index.transform.OntologyIndexGenerator.IndexSchemaConfig.*;

/**
 * the relationship index provider responsible for transforming the ontology logical relationships' definition into an opensearch mapping index template
 */
public class IndexRelationsMappingBuilder implements TemplateMapping<RelationshipType, Relation> {
    private IndexProvider indexProvider;

    public IndexRelationsMappingBuilder(IndexProvider indexProvider) {
        this.indexProvider = indexProvider;
    }

    public Collection<PutIndexTemplateRequestBuilder> map(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests) {
        ontology.relations()
                .forEach(r -> {
                    MappingIndexType mapping = indexProvider.getRelation(r.getName()).orElseThrow(
                                    () -> new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + r + " found in ontology")))
                            .getMapping();

                    Relation relation = indexProvider.getRelation(r.getName()).get();
                    switch (mapping) {
                        case UNIFIED:
                            //common general index - unifies all entities under the same physical index
                            createUnifiedMapping(ontology, client, requests, r, relation);
                            break;
                        case STATIC:
                            createStaticMapping(ontology, client, requests, r, relation);
                            break;
                        case PARTITIONED:
                            createTimePartitionMapping(ontology, client, requests, r, relation);
                            break;
                        default:
                            String result = "No mapping found";
                            break;
                    }
                });
        return requests.values();
    }

    private void createTimePartitionMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, RelationshipType r, Relation relation) {
        String label = r.getrType();
        PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, relation.getType().getName().toLowerCase());
        //todo - Only the time based partition will have a template suffix with astrix added to allow numbering and dates as part of the naming convention
        request.setPatterns(new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format(relation.getProps().getIndexFormat(), "*"))))
                .setSettings(generateSettings(ontology, r, relation, label));
        request.addMapping(label, generateElementMapping(ontology, r, relation, label));
        //add response to list of responses

        //dedup patterns -
        request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

        //add the request
        requests.put(relation.getType().getName(), request);
    }

    private void createStaticMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, RelationshipType r, Relation relation) {
        //static index
        relation.getProps().getValues().forEach(v -> {
            String label = r.getrType();
            PutIndexTemplateRequestBuilder request = new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, label.toLowerCase());
            request.setPatterns(new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format("%s%s", v, "*"))))
                    .setSettings(generateSettings(ontology, r, relation, label));

            request.addMapping(label, generateElementMapping(ontology, r, relation, label));
            //dedup patterns -
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));
            //add response to list of responses
            requests.put(label.toLowerCase(), request);
        });
    }

    private void createUnifiedMapping(Accessor ontology, Client client, Map<String, PutIndexTemplateRequestBuilder> requests, RelationshipType r, Relation relation) {
        relation.getProps().getValues().forEach(v -> {
            String label = r.getrType();
            String unifiedName = relation.getProps().getValues().isEmpty() ? label : relation.getProps().getValues().get(0);
            PutIndexTemplateRequestBuilder request = requests.computeIfAbsent(unifiedName, s -> new PutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, unifiedName));

            List<String> patterns = new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format("%s%s", v, "*")));
            if (Objects.isNull(request.request().patterns())) {
                request.setPatterns(new ArrayList<>(patterns));
            } else {
                request.request().patterns().addAll(patterns);
            }
            //dedup patterns
            request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

            //no specific index sort order since it contains multiple entity types -
            if (request.request().settings().isEmpty()) {
                request.setSettings(getDefaultSettings().build());
            }
            //create new mapping only when no prior entity set this mapping before
            if (request.request().mappings().isEmpty()) {
                request.addMapping(unifiedName, generateElementMapping(ontology, r, relation, unifiedName));
            } else {
                populateProperty(ontology, relation, request.getMappingsProperties(unifiedName), r);
            }
        });
    }

    /**
     * generate specific relation type mapping
     *
     * @param relationshipType
     * @param label
     * @return
     */
    public Map<String, Object> generateElementMapping(Accessor ontology, RelationshipType relationshipType, Relation rel, String label) {
        Optional<RelationshipType> relation = ontology.relation(relationshipType.getName());
        if (!relation.isPresent())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No relation    with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put(PROPERTIES, properties);

        //generate field id -> only if field id array size > 1
        if (relationshipType.getIdField().size() > 1) {
            properties.put(relationshipType.idFieldName(), Collections.singletonMap("type", "keyword"));
        }//otherwise that field id is already a part of the regular fields


        //populate fields & metadata
        relation.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology, ontology.property$(v).getType())));
        relation.get().getProperties().forEach(v -> properties.put(v, parseType(ontology, ontology.property$(v).getType())));
        //set direction
        properties.put(DIRECTION, parseType(ontology, PrimitiveType.Types.STRING.asType()));
        //populate  sideA (entityA)
        populateRedundant(ontology, SOURCE, relationshipType.getName(), properties);
        //populate  sideB (entityB)
        populateRedundant(ontology, DEST, relationshipType.getName(), properties);
        //populate nested documents
        rel.getNested().forEach((key, value)
                -> generateNestedRelationMapping(ontology, properties, new Tuple2<>(key, value)));

        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            jsonMap.put(label, mapping);
        }
        return jsonMap;
    }

    void generateNestedRelationMapping(Accessor ontology, Map<String, Object> parent, Tuple2<String, BaseTypeElement<? extends BaseTypeElement>> nest) {
        Optional<RelationshipType> relation = ontology.relation(nest._2().getType().getName());
        if (!relation.isPresent())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No relation with name " + nest._2().getType() + " found in ontology"));

        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        switch (nest._2().getNesting()) {
            case NESTED:
                mapping.put(TYPE, NESTED);
                break;
        }
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        populateProperty(ontology, nest._2(), properties, relation.get());
        //assuming single value exists (this is the field name)
        if (nest._2().getProps().getValues().isEmpty())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "Nested Rel with name " + nest._2().getType() + " has no property value in mapping file"));

        //inner child nested population
        nest._2().getNested().forEach((key, value)
                -> generateNestedRelationMapping(ontology, properties, new Tuple2<>(key, value)));
        //assuming single value exists (this is the field name)
        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            //populate field name as the mapping element
            parent.put(nest._1(), mapping);
        }
    }

    private void populateRedundant(Accessor ontology, String side, String label, Map<String, Object> properties) {
        HashMap<String, Object> sideProperties = new HashMap<>();
        properties.put(side, sideProperties);
        HashMap<String, Object> values = new HashMap<>();
        sideProperties.put(PROPERTIES, values);

        //add side ID
        values.put(ID, parseType(ontology, ontology.property$(ID).getType()));
        //add side TYPE
        values.put(TYPE, parseType(ontology, ontology.property$(TYPE).getType()));
        indexProvider.getRelation(label).get().getRedundant(side)
                .forEach(r -> values.put(r.getName(), parseType(ontology, ontology.property$(r.getName()).getType())));
    }

    /**
     * add the index relation settings part of the template according to the ontology relations
     *
     * @return
     */
    private Settings generateSettings(Accessor ontology, RelationshipType relationType, Relation rel, String label) {
        ontology.relation(relationType.getName()).get().getIdField().forEach(idField -> {
            if (!ontology.relation(relationType.getName()).get().fields().contains(idField))
                throw new SchemaError.SchemaErrorException(new SchemaError("Relation Schema generation exception", " Relationship " + label + " not containing id metadata property "));
        });
        return builder(ontology, rel);
    }

    private Settings builder(Accessor ontology, Relation relation) {
        SettingBuilder settings = getDefaultSettings();
        if (relation.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            settings.sortByField(ontology.relation$(relation.getType().getName()).idFieldName(), true);
        }
        return settings.build();
    }
}
