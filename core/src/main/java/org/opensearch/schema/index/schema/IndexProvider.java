package org.opensearch.schema.index.schema;


import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import javaslang.Tuple2;
import org.opensearch.schema.index.schema.BaseTypeElement.Type;
import org.opensearch.schema.ontology.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.opensearch.schema.index.schema.NestingType.NONE;
import static org.opensearch.schema.ontology.DirectiveEnumTypes.RELATION;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "entities",
        "relations"
})
/**
 * The general notion of a physical-schema mapping index representation of the ontology
 */
public class IndexProvider {

    @JsonProperty("ontology")
    private String ontology;
    @JsonProperty("entities")
    private Set<Entity> entities = new TreeSet<>();
    @JsonProperty("relations")
    private Set<Relation> relations = new TreeSet<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public IndexProvider() {
    }

    public IndexProvider(IndexProvider source) {
        this.ontology = source.ontology;
        this.additionalProperties.putAll(source.additionalProperties);
        this.entities.addAll(source.getEntities().stream().map(Entity::clone).collect(Collectors.toList()));
        this.relations.addAll(source.getRootRelations().stream().map(Relation::clone).collect(Collectors.toList()));
    }

    @JsonProperty("entities")
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }

    @JsonProperty("rootEntities")
    public void setEntities(Set<Entity> entities) {
        this.entities = entities;
    }

    @JsonProperty("rootRelations")
    public Set<Relation> getRootRelations() {
        return relations;
    }

    @JsonProperty("relations")
    public List<Relation> getRelations() {
        return new ArrayList<>(relations);
    }

    @JsonProperty("rootRelations")
    public void setRelations(Set<Relation> relations) {
        this.relations = relations;
    }

    @JsonProperty("ontology")
    public String getOntology() {
        return ontology;
    }

    @JsonProperty("ontology")
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonIgnore
    public IndexProvider withEntity(Entity entity) {
        entities.add(entity);
        return this;
    }

    @JsonIgnore
    public IndexProvider withRelation(Relation relation) {
        relations.add(relation);
        return this;
    }

    @JsonIgnore
    public Optional<Entity> getEntity(String label) {
        return getEntities().stream().filter(e -> e.getType().getName().equals(label)).findFirst();
    }

    @JsonIgnore
    public Optional<Relation> getRelation(String label) {
        return getRelations().stream().filter(e -> e.getType().getName().equals(label)).findAny();
    }

    public static class Builder {

        public static IndexProvider generate(Ontology ontology) {
            return generate(ontology, e -> true, r -> true);
        }

        /**
         * creates default index provider according to the given ontology - simple static index strategy
         *
         * @param ontology
         * @return
         */
        public static IndexProvider generate(Ontology ontology, Predicate<EntityType> entityPredicate, Predicate<RelationshipType> relationPredicate) {
            Accessor accessor = new Accessor(ontology);
            IndexProvider provider = new IndexProvider();
            provider.ontology = ontology.getOnt();
            //generate entities
            provider.entities = ontology.getEntityTypes().stream()
                    .filter(entityPredicate)
                    //simplified assumption of top level entities are static and without nesting
                    .map(e -> createEntity(e, MappingIndexType.STATIC, NONE, accessor))
                    .collect(Collectors.toSet());
            //generate relations
            provider.relations = ontology.getRelationshipTypes().stream()
                    .filter(relationPredicate)
                    .map(r -> createRelation(r, accessor))
                    .collect(Collectors.toSet());

            return provider;
        }

        private static Relation createRelation(RelationshipType r, Accessor accessor) {
            Optional<DirectiveType> relationDirective = r.getDirectives().stream().filter(d -> RELATION.isSame(d.getName())).findAny();
            if (relationDirective.isEmpty()) {
                //simplified assumption of top level entities are static and without nesting
                return createRelation(r, MappingIndexType.STATIC, NONE, accessor);
            }
            if (relationDirective.get().getArguments().stream().noneMatch(arg -> arg.name.equals(RELATION.getArguments().get(0)))) {
                //simplified assumption of default embedded relation hierarchy between parent & child object
                return createRelation(r, MappingIndexType.NONE, NestingType.EMBEDDED, accessor);
            }

            // get the directive instruction of how this relationship is to be physically stored
            DirectiveType.Argument argument = relationDirective.get().getArguments().stream().filter(arg -> arg.name.equals(RELATION.getArguments().get(0))).findAny().get();
            PhysicalEntityRelationsDirectiveType relationsDirective = PhysicalEntityRelationsDirectiveType.from(argument.value.toString());

            return createRelation(r, MappingIndexType.NONE, NestingType.translate(relationsDirective), accessor);
        }

        private static Relation createRelation(RelationshipType r, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
            return new Relation(Type.of(r.getName()), nestingType, mappingIndexType,
                    false,
                    r.getDirectives(),
                    createNestedElements(r, accessor),
                    // indices need to be lower cased
                    createProperties(r.getName(), accessor),
                    Collections.emptySet(), Collections.emptyMap());
        }

        ;
    }

    private static Entity createEntity(EntityType e, MappingIndexType mappingIndexType, NestingType nestingType, Accessor accessor) {
        switch (nestingType) {
            case NESTED:
                REFERENCE:
                NESTED_REFERENCE:
                // create minimal representation  - TODO add redundant here
                // indices need to be lower cased
                return new Entity(Type.of(e.getName()), nestingType, mappingIndexType,
                        createProperties(e.getName(), accessor));
            default:
                return new Entity(Type.of(e.getName()), nestingType, mappingIndexType,
                        e.getDirectives(),
                        // indices need to be lower cased
                        createProperties(e.getName(), accessor),
                        createNestedElements(e, accessor),
                        Collections.emptyMap());
        }
    }

    private static Map<String, Relation> createNestedElements(RelationshipType r, Accessor accessor) {
        return r.getProperties().stream()
                .filter(p -> accessor.getNestedRelationByPropertyName(p).isPresent())
                .map(p ->
                        new Tuple2<>(p, Builder.createRelation(
                                accessor.getNestedRelationByPropertyName(p).get(),//relation type
                                MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                accessor.property$(p).getType().isArray() ? NestingType.NESTED : NestingType.EMBEDDED,
                                accessor))
                ).collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }

    private static Map<String, Entity> createNestedElements(EntityType e, Accessor accessor) {
        //first filter out the logical nested entities which have physical foreign directives
        return accessor.relationsPairsBySourceEntity(e, r -> !accessor.isForeignRelation(r))
                .stream()
                .map(p -> {
                            //in case of mutual reference - using the REFERENCE type mapping
                            if (p.geteTypeA().equals(p.geteTypeB())) {
                                return new Tuple2<>(p.getSideAFieldName(), createEntity(
                                        accessor.entity$(p.geteTypeB()),// relation destination entity type
                                        MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                        accessor.property$(p.getSideAFieldName()).getType().isArray() ? NestingType.NESTED_REFERENCE : NestingType.REFERENCE,
                                        accessor));
                            }
                            //other types of inner entities that are contained inside the wrapping element
                            return new Tuple2<>(p.getSideAFieldName(), createEntity(
                                    accessor.entity$(p.geteTypeB()),// relation destination entity type
                                    MappingIndexType.STATIC,// simplified assumption of nested types to be in the same static index of owning entity
                                    accessor.property$(p.getSideAFieldName()).getType().isArray() ? NestingType.NESTED : NestingType.EMBEDDED,
                                    accessor));
                        }
                ).collect(Collectors.toMap(p -> p._1, p -> p._2()));
    }

    private static Props createProperties(String name, Accessor accessor) {
        return new Props(ImmutableList.of(name));
    }
}
