package org.opensearch.schema.ontology;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.schema.SchemaError;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * General helper utility that helps to query and investigate the Ontology metadata
 */
public class Accessor implements Supplier<Ontology> {
    //region Constructors
    public Accessor(Ontology ontology) {
        this.ontology = ontology;

        this.entitiesByEtype = Stream.ofAll(ontology.getEntityTypes())
                .toJavaMap(entityType -> new Tuple2<>(entityType.geteType(), entityType));
        this.entitiesByName = Stream.ofAll(ontology.getEntityTypes())
                .toJavaMap(entityType -> new Tuple2<>(entityType.getName(), entityType));

        this.relationsByRtype = Stream.ofAll(ontology.getRelationshipTypes())
                .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getrType(), relationshipType));
        this.relationsByName = Stream.ofAll(ontology.getRelationshipTypes())
                .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getName(), relationshipType));

        this.propertiesByPtype = Stream.ofAll(ontology.getProperties())
                .toJavaMap(property -> new Tuple2<>(property.getpType(), property));
        this.propertiesByName = Stream.ofAll(ontology.getProperties())
                .toJavaMap(property -> new Tuple2<>(property.getName(), property));
    }
    //endregion

    //region Public Methods
    @Override
    public Ontology get() {
        return this.ontology;
    }

    public String name() {
        return this.ontology.getOnt();
    }

    public Optional<EntityType> entity(String entityName) {
        return Optional.ofNullable(this.entitiesByName.get(entityName));
    }

    public EntityType entity$(String entityName) {
        return entity(entityName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName + "]")));
    }

    public Optional<String> eType(String entityName) {
        EntityType entityType = this.entitiesByName.get(entityName);
        return entityType == null ? Optional.empty() : Optional.of(entityType.geteType());
    }

    /**
     * returns property by its name - returns optional empty if not found
     *
     * @param propertyName
     * @return
     */
    public Optional<Property> pName(String propertyName) {
        return Optional.ofNullable(this.propertiesByName.get(propertyName));
    }

    /**
     * return property (no matter which element it belongs to) - throws exception if not found
     *
     * @param propertyName
     * @return
     */
    public Property pName$(String propertyName) {
        return pName(propertyName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology propertyName for value ", "No Ontology propertyName for value[" + propertyName + "]")));
    }

    public String eType$(String entityName) {
        return eType(entityName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName + "]")));
    }

    public Optional<RelationshipType> $relation(String rType) {
        return Optional.ofNullable(this.relationsByRtype.get(rType));
    }

    public Optional<RelationshipType> relation(String relationName) {
        return Optional.ofNullable(this.relationsByName.get(relationName));
    }

    /**
     * get relationship pairs in which the source entity type if given as a parameter
     *
     * @param source
     * @return
     */
    public Collection<EPair> relationsPairsBySourceEntity(EntityType source) {
        return relations().stream()
                .flatMap(p -> p.getePairs().stream())
                .filter(p -> source.geteType().equals(p.geteTypeA()))
                .collect(Collectors.toList());
    }

    public RelationshipType relation$(String relationName) {
        return relation(relationName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName + "]")));
    }

    public Optional<String> rType(String relationName) {
        RelationshipType relationshipType = this.relationsByName.get(relationName);
        return relationshipType == null ? Optional.empty() : Optional.of(relationshipType.getrType());
    }

    public String rType$(String relationName) {
        return rType(relationName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName + "]")));
    }

    public Optional<Property> $property(String pType) {
        return Optional.ofNullable(this.propertiesByPtype.get(pType));
    }

    public Optional<Property> property(String propertyName) {
        return Optional.ofNullable(this.propertiesByName.get(propertyName));
    }

    public Set<Property> properties() {
        return this.ontology.getProperties();
    }

    public Property property$(String propertyName) {
        return property(propertyName)
                .orElseThrow(() -> new SchemaError.SchemaErrorException(new SchemaError("No Ontology propertyName for value ", "No Ontology propertyName for value[" + propertyName + "]")));
    }

    public Optional<String> pType(String propertyName) {
        Property property = this.propertiesByName.get(propertyName);
        return property == null ? Optional.empty() : Optional.of(property.getpType());
    }

    public Iterable<EntityType> entities() {
        return Stream.ofAll(ontology.getEntityTypes()).toJavaList();
    }

    public Optional<RelationshipType> getNestedRelationByPropertyName(String prop) {
        if (property(prop).isEmpty()) return Optional.empty();
        return $relation(property$(prop).getpType());
    }

    public List<RelationshipType> relations() {
        return Stream.ofAll(ontology.getRelationshipTypes()).toJavaList();
    }

    public List<RelationshipType> relationBySideA(String eType) {
        return Stream.ofAll(ontology.getRelationshipTypes()).filter(r -> r.hasSideA(eType)).toJavaList();
    }

    public Optional<PrimitiveType> primitiveType(String typeName) {
        return Stream.ofAll(ontology.getPrimitiveTypes())
                .filter(type -> type.getType().equals(typeName))
                .toJavaOptional();
    }

    public List<EnumeratedType> getEnumeratedTypes() {
        return ontology.getEnumeratedTypes();
    }

    public Optional<EnumeratedType> enumeratedType(String typeName) {
        return Stream.ofAll(ontology.getEnumeratedTypes())
                .filter(type -> type.isOfType(typeName))
                .toJavaOptional();
    }

    public EnumeratedType enumeratedType$(String typeName) {
        return enumeratedType(typeName).get();
    }
    //endregion

    //region Fields
    private Ontology ontology;

    private Map<String, EntityType> entitiesByEtype;
    private Map<String, EntityType> entitiesByName;

    private Map<String, RelationshipType> relationsByRtype;
    private Map<String, RelationshipType> relationsByName;

    private Map<String, Property> propertiesByName;
    private Map<String, Property> propertiesByPtype;

    /**
     * match named element to true type (included typed value identifier)
     *
     * @param name
     * @return
     */
    public Optional<Tuple2<NodeType, String>> matchNameToType(String name) {
        //ENUMERATED TYPE
        if (enumeratedType(name).isPresent())
            return Optional.of(Tuple.of(NodeType.ENUM, enumeratedType$(name).geteType()));
        //entity TYPE
        if (eType(name).isPresent())
            return Optional.of(Tuple.of(NodeType.ENTITY, eType$(name)));
        //relation TYPE
        if (rType(name).isPresent())
            return Optional.of(Tuple.of(NodeType.RELATION, rType$(name)));
        //property TYPE
        if (property(name).isPresent())
            return Optional.of(Tuple.of(NodeType.PROPERTY, property$(name).getpType()));

        return Optional.empty();
    }

    public enum NodeType {
        ENUM, PROPERTY, RELATION, ENTITY
    }

    //endregion
}
