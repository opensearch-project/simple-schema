package org.opensearch.schema.index.transform;

import javaslang.Tuple2;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.index.schema.BaseTypeElement;
import org.opensearch.schema.index.template.SettingBuilder;
import org.opensearch.schema.ontology.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;

/**
 * common utility functionality
 */
public abstract class IndexMappingUtils {

    static SettingBuilder getDefaultSettings() {
        return SettingBuilder.create().shards(3).replicas(1);
    }

    /**
     * parse ontology primitive type to opensearch primitive type
     *
     * @param nameType
     * @return
     */
    public static Map<String, Object> parseType(Accessor ontology, PropertyType nameType) {
        Map<String, Object> map = new HashMap<>();
        try {
            Ontology.OntologyPrimitiveType type = Ontology.OntologyPrimitiveType.valueOf(nameType.getType());
            switch (type) {
                case STRING -> map.put("type", "keyword");
                case TEXT -> {
                    map.put("type", "text");
                    map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
                }
                case DATETIME,DATE -> {
                    map.put("type", "date");
                    map.put("format", "epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS");
                }
                case LONG -> map.put("type", "long");
                case INT -> map.put("type", "integer");
                case FLOAT -> map.put("type", "float");
                case DOUBLE -> map.put("type", "double");
                case GEO -> map.put("type", "geo_point");
                default -> throw new IllegalStateException("Unexpected value: " + type);
            }
        } catch (Throwable typeNotFound) {
            // manage non-primitive type such as enum or nested typed
            Optional<Tuple2<Accessor.NodeType, String>> type = ontology.matchNameToType(nameType.getType());
            if (type.isPresent()) {
                // nested & relational elements are being populated outside this scope
                if (type.get()._1() == Accessor.NodeType.ENUM) {
                    //enum is always backed by integer
                    map.put("type", "integer");
                }
            } else {
                //default
                map.put("type", "text");
                map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
            }
        }
        return map;
    }

    static void populateProperty(Accessor ontology, BaseTypeElement<? extends BaseTypeElement> element, Map<String, Object> properties, BaseElement entityType) {
        switch (element.getNesting()) {
            case REFERENCE, NESTED_REFERENCE ->
                //populate only minimal (redundant) fields
                    entityType.getIdField().forEach(v -> {
                        //todo add redundant fields here as well
                        Map<String, Object> parseType = parseType(ontology, ontology.property$(v).getType());
                        if (!parseType.isEmpty()) properties.put(v, parseType);
                    });
            default -> {
                //populate mandatory fields
                entityType.getMetadata().forEach(v -> {
                    Map<String, Object> parseType = parseType(ontology, ontology.property$(v).getType());
                    if (!parseType.isEmpty()) properties.put(v, parseType);
                });
                //populate mandatory fields
                entityType.getProperties().forEach(v -> {
                    Map<String, Object> parseType = parseType(ontology, ontology.property$(v).getType());
                    if (!parseType.isEmpty()) properties.put(v, parseType);
                });
            }
        }
        //populate nested documents -
        element.getNested().forEach((key, value)
                -> generateNestedEntityMapping(ontology, properties, new Tuple2<>(key, value)));
    }

    /**
     * populate the nested object entity as port of the wrapping entity mapping's
     * @param ontology
     * @param parent
     * @param nest
     * @return
     */
    static Map<String, Object> generateNestedEntityMapping(Accessor ontology, Map<String, Object> parent, Tuple2<String, BaseTypeElement<? extends BaseTypeElement>> nest) {
        Optional<EntityType> entity = ontology.entity(nest._2().getType().getName());
        if (!entity.isPresent())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "No entity with name " + nest._2().getType() + " found in ontology"));

        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        switch (nest._2().getNesting()) {
            case NESTED, REFERENCE, NESTED_REFERENCE ->
                    mapping.put(OntologyIndexGenerator.IndexSchemaConfig.TYPE, OntologyIndexGenerator.IndexSchemaConfig.NESTED);
        }
        mapping.put(OntologyIndexGenerator.IndexSchemaConfig.PROPERTIES, properties);
        //populate fields & metadata
        populateProperty(ontology, nest._2(), properties, entity.get());
        //assuming single value exists (this is the field name)
        if (nest._2().getProps().getValues().isEmpty())
            throw new SchemaError.SchemaErrorException(new SchemaError("Mapping generation exception", "Nested entity with name " + nest._2().getType() + " has no property value in mapping file"));

        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            //put field name as it appears on the containing entity
            parent.put(nest._1(), mapping);
        }
        return mapping;
    }
}
