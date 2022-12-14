package org.opensearch.schema.index.schema;


import com.fasterxml.jackson.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "partition",
        "symmetric",
        "props",
        "nested",
        "redundant"
})
/**
 * the logical relation's physical index mapping and storage representation
 * @param <T>
 */
public class Relation implements BaseTypeElement<Relation> {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("partition")
    private NestingType nesting;
    @JsonProperty("mapping")
    private MappingIndexType mapping;
    @JsonProperty("symmetric")
    private boolean symmetric = false;
    @JsonProperty("nested")
    private Map<String,Relation> nested = Collections.EMPTY_MAP;
    @JsonProperty("props")
    private Props props;
    @JsonProperty("redundant")
    private Set<Redundant> redundant = Collections.EMPTY_SET;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Relation() {}

    public Relation(Type type, NestingType nesting, MappingIndexType mapping, boolean symmetric, Map<String,Relation> nested, Props props, Set<Redundant> redundant, Map<String, Object> additionalProperties) {
        this.type = type;
        this.nesting = nesting;
        this.mapping = mapping;
        this.symmetric = symmetric;
        this.nested = nested;
        this.props = props;
        this.redundant = redundant;
        this.additionalProperties = additionalProperties;
    }

    public Relation(Type type, NestingType nestingType, MappingIndexType mapping, boolean symmetric, Props props) {
        this(type,nestingType,mapping,symmetric,Collections.EMPTY_MAP,props,Collections.EMPTY_SET,Collections.EMPTY_MAP);
    }


    @JsonProperty("symmetric")
    public boolean isSymmetric() {
        return symmetric;
    }

    @JsonProperty("symmetric")
    public void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    @Override
    public boolean hasProperties() {
        return props != null && !props.getValues().isEmpty();
    }

    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty("partition")
    public NestingType getNesting() {
        return nesting;
    }

    @JsonProperty("partition")
    public void setNesting(NestingType nesting) {
        this.nesting = nesting;
    }


    @JsonProperty("mapping")
    public void setMapping(MappingIndexType mapping) {
        this.mapping = mapping;
    }

    @JsonProperty("mapping")
    public MappingIndexType getMapping() {
        return mapping;
    }

    @JsonProperty("props")
    public Props getProps() {
        return props;
    }

    @JsonProperty("props")
    public void setProps(Props props) {
        this.props = props;
    }

    @JsonProperty("nested")
    public Map<String,Relation> getNested() {
        return nested;
    }

    @JsonProperty("nested")
    public void setNested(Map<String,Relation> nested) {
        this.nested = nested;
    }


    @JsonProperty("redundant")
    public Set<Redundant> getRedundant() {
        return redundant;
    }

    @JsonProperty("redundant")
    public void setRedundant(Set<Redundant> redundant) {
        this.redundant = redundant;
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
    public List<Redundant> getRedundant(String side) {
        return getRedundant().stream().filter(r->r.getSide().contains(side)).collect(Collectors.toList());
    }

    @JsonIgnore
    public Relation withMapping(MappingIndexType mapping) {
        this.mapping = mapping;
        return this;
    }

    @JsonIgnore
    public Relation withType(Type type) {
        this.type = type;
        return this;
    }

    @JsonIgnore
    public Relation withNesting(NestingType nestingType) {
        this.nesting = nestingType;
        return this;
    }

    @Override
    protected Relation clone()  {
        return new Relation(this.type,this.nesting,this.mapping,this.symmetric,
                new HashMap<>(this.nested),
                this.props.clone(),this.redundant.stream().map(Redundant::clone).collect(Collectors.toSet()),
                new HashMap<>(this.additionalProperties));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return symmetric == relation.symmetric && Objects.equals(type, relation.type) && Objects.equals(nesting, relation.nesting) && Objects.equals(mapping, relation.mapping) && Objects.equals(nested, relation.nested) && Objects.equals(props, relation.props) && Objects.equals(redundant, relation.redundant) && Objects.equals(additionalProperties, relation.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, nesting, mapping, symmetric, nested, props, redundant, additionalProperties);
    }
}
