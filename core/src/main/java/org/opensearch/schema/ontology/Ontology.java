package org.opensearch.schema.ontology;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.PrimitiveType.ArrayOfPrimitives;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.opensearch.schema.ontology.PrimitiveType.Types.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"primitiveTypes"})
/**
 * representing the schematic logical structure of the domain in a higher level definition language
 */
public class Ontology {

    public Ontology(Ontology source) {
        this();
        //copy
        entityTypes.addAll(source.getEntityTypes().stream().map(EntityType::clone).collect(Collectors.toList()));
        relationshipTypes.addAll(source.getRelationshipTypes().stream().map(RelationshipType::clone).collect(Collectors.toList()));
        enumeratedTypes.addAll(source.getEnumeratedTypes().stream().map(EnumeratedType::clone).collect(Collectors.toList()));
        metadata.addAll(source.metadata.stream().map(Property::clone).collect(Collectors.toList()));
        properties.addAll(source.getProperties().stream().map(Property::clone).collect(Collectors.toSet()));
        directives.addAll(source.getDirectives());
    }

    public Ontology() {
        initCollections();
        initPrimitives();
    }

    private void initCollections() {
        directives = new ArrayList<>();
        entityTypes = new ArrayList<>();
        relationshipTypes = new ArrayList<>();
        enumeratedTypes = new ArrayList<>();
        properties = new HashSet<>();
        metadata = new HashSet<>();
    }

    private void initPrimitives() {
        primitiveTypes = new HashSet<>();
        primitiveTypes.add(new PrimitiveType(ID.tlc(), String.class));
        primitiveTypes.add(new PrimitiveType(BOOLEAN.tlc(), Boolean.class));
        primitiveTypes.add(new ArrayOfPrimitives(BOOLEAN.tlc(), Boolean.class));
        primitiveTypes.add(new PrimitiveType(INT.tlc(), Integer.class));
        primitiveTypes.add(new ArrayOfPrimitives(INT.tlc(), Integer.class));
        primitiveTypes.add(new PrimitiveType(LONG.tlc(), Long.class));
        primitiveTypes.add(new ArrayOfPrimitives(LONG.tlc(), Long.class));
        primitiveTypes.add(new PrimitiveType(STRING.tlc(), String.class));
        primitiveTypes.add(new ArrayOfPrimitives(STRING.tlc(), String.class));
        primitiveTypes.add(new PrimitiveType(TEXT.tlc(), String.class));
        primitiveTypes.add(new PrimitiveType(FLOAT.tlc(), Double.class));
        primitiveTypes.add(new ArrayOfPrimitives(FLOAT.tlc(), Double.class));
        primitiveTypes.add(new PrimitiveType(TIME.tlc(), Long.class));
        primitiveTypes.add(new ArrayOfPrimitives(TIME.tlc(), Long.class));
        primitiveTypes.add(new PrimitiveType(DATE.tlc(), Date.class));
        primitiveTypes.add(new ArrayOfPrimitives(DATE.tlc(), Date.class));
        primitiveTypes.add(new PrimitiveType(DATETIME.tlc(), Date.class));
        primitiveTypes.add(new ArrayOfPrimitives(DATETIME.tlc(), Date.class));
        primitiveTypes.add(new PrimitiveType(IP.tlc(), String.class));
        primitiveTypes.add(new ArrayOfPrimitives(IP.tlc(), String.class));
        primitiveTypes.add(new PrimitiveType(GEOPOINT.tlc(), Point2D.class));
        primitiveTypes.add(new ArrayOfPrimitives(GEOPOINT.tlc(), Point2D.class));
        primitiveTypes.add(new PrimitiveType(JSON.tlc(), Map.class));
        primitiveTypes.add(new ArrayOfPrimitives(JSON.tlc(), Map.class));
        primitiveTypes.add(new PrimitiveType(ARRAY.tlc(), Array.class));
    }

    //region Getters & Setters

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public Set<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    public void setEntityTypes(List<EntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public List<RelationshipType> getRelationshipTypes() {
        return relationshipTypes;
    }

    public List<DirectiveType> getDirectives() {
        return directives;
    }

    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    public void setRelationshipTypes(List<RelationshipType> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    public List<EnumeratedType> getEnumeratedTypes() {
        return enumeratedTypes;
    }

    public void setEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
        this.enumeratedTypes = enumeratedTypes;
    }

    public List<PrimitiveType> getPrimitiveTypes() {
        return new ArrayList<>(primitiveTypes);
    }

//endregion

    //region Public Methods

    @Override
    public String toString() {
        return "Ontology [enumeratedTypes = " + enumeratedTypes + ", ont = " + ont + ", relationshipTypes = " + relationshipTypes + ", entityTypes = " + entityTypes + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ontology ontology = (Ontology) o;
        return ont.equals(ontology.ont) &&
                directives.equals(ontology.directives) &&
                entityTypes.equals(ontology.entityTypes) &&
                relationshipTypes.equals(ontology.relationshipTypes) &&
                properties.equals(ontology.properties) &&
                metadata.equals(ontology.metadata) &&
                enumeratedTypes.equals(ontology.enumeratedTypes) &&
                primitiveTypes.equals(ontology.primitiveTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ont, directives, entityTypes, relationshipTypes, properties, metadata, enumeratedTypes, primitiveTypes);
    }

    //endregion

    //region Fields
    private String ont;
    private List<DirectiveType> directives;
    private List<EntityType> entityTypes;
    private List<RelationshipType> relationshipTypes;
    private Set<Property> properties;
    private Set<Property> metadata;
    private List<EnumeratedType> enumeratedTypes;
    private Set<PrimitiveType> primitiveTypes;
    //endregion

    //region Builder

    public static final class OntologyBuilder {
        private String name = "Generic";
        private List<DirectiveType> directives;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
        private LinkedHashSet<Property> properties;
        private List<EnumeratedType> enumeratedTypes;

        private Set<PrimitiveType> primitiveTypes;

        private OntologyBuilder() {
            this.directives = new ArrayList<>();
            this.entityTypes = new ArrayList<>();
            this.relationshipTypes = new ArrayList<>();
            this.properties = new LinkedHashSet<>();
            this.enumeratedTypes = new ArrayList<>();
            this.primitiveTypes = new HashSet<>();
        }

        public static OntologyBuilder anOntology() {
            return new OntologyBuilder();
        }

        public static OntologyBuilder anOntology(String ontologyName) {
            return anOntology().withName(ontologyName);
        }

        public OntologyBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public OntologyBuilder addEntityTypes(List<EntityType> entityTypes) {
            this.entityTypes.addAll(entityTypes);
            return this;
        }

        public Optional<EntityType> getEntityType(String entityType) {
            return this.entityTypes.stream().filter(et -> et.geteType().equals(entityType)).findAny();
        }

        public OntologyBuilder addRelationshipType(RelationshipType relationshipType) {
            this.relationshipTypes.add(relationshipType);
            return this;
        }

        public Optional<Property> getProperty(String property) {
            return this.properties.stream().filter(et -> et.getName().equals(property)).findAny();
        }

        public OntologyBuilder withEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
            this.enumeratedTypes = enumeratedTypes;
            return this;
        }

        public OntologyBuilder withProperties(Set<Property> properties) {
            this.properties = new LinkedHashSet<>(properties);
            return this;
        }

        public OntologyBuilder withPrimitives(Set<PrimitiveType> primitives) {
            this.primitiveTypes = new LinkedHashSet<>(primitives);
            return this;
        }


        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(name);
            ontology.setDirectives(directives);
            ontology.setEntityTypes(entityTypes);
            ontology.setRelationshipTypes(relationshipTypes);
            ontology.setEnumeratedTypes(enumeratedTypes);
            ontology.setProperties(properties);
            //add all primitives to set
            ontology.primitiveTypes.addAll(this.primitiveTypes);
            return ontology;
        }

    }

    //endregion

    public enum OntologyPrimitiveType {
        STRING,
        TEXT,
        DATE,
        LONG,
        INT,
        FLOAT,
        DOUBLE,
        GEO;

    }
    //endregion

    public static class Accessor implements Supplier<Ontology> {
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
         * @param source
         * @return
         */
        public Collection<EPair> relationsPairsBySourceEntity(EntityType source) {
            return relations().stream()
                    .flatMap(p->p.getePairs().stream())
                    .filter(p->source.geteType().equals(p.geteTypeA()))
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
            return this.ontology.properties;
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

}
