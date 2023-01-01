package org.opensearch.schema.ontology;

import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.opensearch.schema.SchemaError;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.schema.index.schema.IndexMappingUtils.MAPPING_TYPE;
import static org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType.*;

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

    public Optional<? extends BaseElement> $element(String type) {
        if (this.entitiesByEtype.get(type) != null)
            return Optional.of(this.entitiesByEtype.get(type));
        if (this.relationsByName.get(type) != null)
            return Optional.of(this.relationsByName.get(type));
        return Optional.empty();
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
     * get relationship by source & target pairs
     *
     * @param source
     * @param target
     * @return
     */
    public List<EPair> relationsPairs(EntityType source, EntityType target) {
        return relations().stream()
                .flatMap(p -> p.getePairs().stream())
                .filter(p -> source.geteType().equals(p.geteTypeA()) && target.geteType().equals(p.geteTypeB()))
                .collect(Collectors.toList());
    }
    /**
     * get relationship pairs in which the source entity type if given as a parameter
     *
     * @param source
     * @param relationPredicate
     * @return
     */
    public Collection<EPair> relationsPairsBySourceEntity(EntityType source, Predicate<RelationshipType> relationPredicate) {
        return relations().stream()
                .filter(relationPredicate::test)
                .flatMap(p -> p.getePairs().stream())
                .filter(p -> source.geteType().equals(p.geteTypeA()))
                .collect(Collectors.toList());
    }

    /**
     * get relationship in which pair's target entity type if given as a parameter
     * @param target
     * @return
     */
    public List<RelationshipType> relationByTargetEntity(EntityType target) {
        return relations().stream()
                .filter(r -> r.getePairs().stream()
                        .anyMatch(p->p.geteTypeB().equals(target.geteType())))
                .collect(Collectors.toList());

    }

    /**
     * get relationship in which pair's source entity type if given as a parameter
     * @param source
     * @return
     */
    public List<RelationshipType> relationBySourceEntity(EntityType source) {
        return relations().stream()
                .filter(r -> r.getePairs().stream()
                        .anyMatch(p->p.geteTypeA().equals(source.geteType())))
                .collect(Collectors.toList());

    }
    /**
     * get relationship pairs in which the target entity type if given as a parameter
     *
     * @param target
     * @param relationPredicate
     * @return
     */
    public Collection<EPair> relationsPairsByTargetEntity(EntityType target, Predicate<RelationshipType> relationPredicate) {
        return relations().stream()
                .filter(relationPredicate::test)
                .flatMap(p -> p.getePairs().stream())
                .filter(p -> target.geteType().equals(p.geteTypeB()))
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

    public boolean isSelfReference(EPair relationshipType) {
        return relationshipType.geteTypeA().equals(relationshipType.geteTypeB());
    }
    public boolean isMutualReference(EPair relationshipType) {
        return relationshipType.geteTypeA().equals(relationshipType.geteTypeB());
    }

    /**
     * verify is an entity type has @model directive
     * @param type
     * @return
     */
    public boolean isModel(String type) {
        if(entity(type).isEmpty())
            return false;

        if (entity$(type).getDirectives().isEmpty())
            return false;

        return  entity$(type).getDirectives().stream().anyMatch(d -> DirectiveEnumTypes.MODEL.isSame(d.getName()));
    }

    /**
     * check is the given pair is a foreign entity to other entity
     *
     * @param pair
     * @return
     */
    public boolean isForeignRelation(EPair pair) {
        return isOfGivenRelation(pair.getDirectives(), FOREIGN);

    }

    /**
     * check is the given pair is a join_index_foreign entity to other entity
     *
     * @param pair
     * @return
     */
    public boolean isJoinIndexForeignRelation(EPair pair) {
        return isOfGivenRelation(pair.getDirectives(), JOIN_INDEX_FOREIGN);

    }

    /**
     * check is the given pair is a reverse back to main entity type of relation
     *
     * @param pair
     * @return
     */
    public boolean isReverseRelation(EPair pair) {
        return isOfGivenRelation(pair.getDirectives(), REVERSE);

    }

    private boolean isOfGivenRelation(List<DirectiveType> pair, PhysicalEntityRelationsDirectiveType foreign) {
        if (pair.isEmpty())
            return false;

        if (pair.stream().noneMatch(d -> DirectiveEnumTypes.RELATION.isSame(d.getName())))
            return false;

        return pair
                .stream()
                .filter(d -> DirectiveEnumTypes.RELATION.isSame(d.getName()))
                .filter(d -> d.getArgument(MAPPING_TYPE).isPresent())
                .anyMatch(d -> d.getArgument(MAPPING_TYPE).get()
                        .equalsValue(foreign.getName()));
    }

    /**
     * check is the given type is a foreign entity to other entity
     *
     * @param relationshipType
     * @return
     */
    public boolean isForeignRelation(RelationshipType relationshipType) {
        return isRelationDirectiveOfType(relationshipType, FOREIGN);
    }

    /**
     * check is the given type is a mutual-foreign entity to other entity
     *
     * @param relationshipType
     * @return
     */
    public boolean isMutualForeignRelation(RelationshipType relationshipType) {
        return isRelationDirectiveOfType(relationshipType, JOIN_INDEX_FOREIGN);
    }

    /**
     * check is the given type is an embedded entity to other entity
     *
     * @param relationshipType
     * @return
     */
    public boolean isEmbeddedRelation(RelationshipType relationshipType) {
        return isRelationDirectiveOfType(relationshipType,PhysicalEntityRelationsDirectiveType.EMBEDDED);
    }

    /**
     * check is the given type is a nested entity to other entity
     *
     * @param relationshipType
     * @return
     */
    public boolean isNestedRelation(RelationshipType relationshipType) {
        return isRelationDirectiveOfType(relationshipType,PhysicalEntityRelationsDirectiveType.NESTED);
    }

    /**
     * check is the given type is a child entity to other entity
     *
     * @param relationshipType
     * @return
     */
    public boolean isChildRelation(RelationshipType relationshipType) {
        return isRelationDirectiveOfType(relationshipType,PhysicalEntityRelationsDirectiveType.CHILD);
     }

    /**
     * test is the given relationship type holds the specific directive out of @org.opensearch.schema.ontology.PhysicalEntityRelationsDirectiveType
     * @param relationshipType
     * @param directiveType
     * @return
     */
    public boolean isRelationDirectiveOfType(RelationshipType relationshipType,PhysicalEntityRelationsDirectiveType directiveType) {
        return isOfGivenRelation(relationshipType.getDirectives(), directiveType);
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

    public List<RelationshipType> relationsByPair(EntityType source, EntityType target) {
        return relations().stream()
                .filter(p -> p.getePairs().stream()
                        .anyMatch(pair -> source.geteType().equals(pair.geteTypeA())
                                      && target.geteType().equals(pair.geteTypeB())))
                .collect(Collectors.toList());
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

    /**
     * check is the given type is a nested entity under any other entity
     *
     * @param eType
     * @return
     */
    public boolean isNestedEntity(String eType) {
        if (!entity(eType).isPresent()) return false;
        // for each existing entity, try each field and verify is its of an entity type - therefor a nested entity of some other entity
        return StreamSupport.stream(entities().spliterator(), false)
                .anyMatch(en -> en.fields().stream()
                        .filter(p -> property(p).isPresent())
                        .filter(p -> property(p).get().getType().getType().equals(eType))
                        .anyMatch(p->entity(property(p).get().getType().getType())
                                .isPresent())
                );
    }


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

    /**
     * search a named directive in an entity's directives
     *
     * @param element
     * @param directiveName
     * @return
     */
    public Optional<DirectiveType> getDirective(CommonType element, String directiveName) {
        return element.getDirectives().stream().filter(d -> d.getName().equals(directiveName)).findFirst();
    }

    /**
     * search a named directive in a relation's ePair's directives
     *
     * @param pair
     * @param directiveName
     * @return
     */
    public Optional<DirectiveType> getDirective(EPair pair,String directiveName) {
        return pair.getDirectives().stream().filter(d->d.getName().equals(directiveName)).findFirst();
    }

    /**
     * search a named directive in the relationship's ePair's directives
     *
     * @param relation
     * @param directive
     * @return
     */
    public List<DirectiveType> getRelationsInnerDirective(RelationshipType relation, DirectiveEnumTypes directive) {
        return relation.getePairs().stream()
                .filter(pair->getDirective(pair,directive.getName()).isPresent())
                .map(pair->getDirective(pair,directive.getName()).get())
                .collect(Collectors.toList());
    }

    /**
     * Search an EPair by a named directive in the relationship's ePair's
     *
     * @param relation
     * @param directive
     * @return
     */
    public List<EPair> getEpairByDirective(RelationshipType relation, DirectiveEnumTypes directive) {
        return relation.getePairs().stream()
                .filter(pair->getDirective(pair,directive.getName()).isPresent())
                .collect(Collectors.toList());
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



    public enum NodeType {
        ENUM, PROPERTY, RELATION, ENTITY
    }

    //endregion
}
