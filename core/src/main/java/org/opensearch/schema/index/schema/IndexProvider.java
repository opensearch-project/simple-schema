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

            MappingTranslator.MappingTranslatorContext<Entity> entitiesContext = new MappingTranslator.MappingTranslatorContext<>(accessor);
            MappingTranslator.MappingTranslatorContext<Relation> relationsContext = new MappingTranslator.MappingTranslatorContext<Relation>(accessor);
            EntityMappingTranslator entityTranslator = new EntityMappingTranslator();
            RelationMappingTranslator relationTranslator = new RelationMappingTranslator();

            //generate entities
            provider.entities = ontology.getEntityTypes().stream()
                    .filter(entityPredicate)
                    .flatMap(e -> entityTranslator.translate(e,entitiesContext).stream())
                    .collect(Collectors.toSet());
            //generate relations
            provider.relations = ontology.getRelationshipTypes().stream()
                    .filter(relationPredicate)
                    .flatMap(r -> relationTranslator.translate(r, relationsContext).stream())
                    .collect(Collectors.toSet());

            return provider;
        }
    }
}
