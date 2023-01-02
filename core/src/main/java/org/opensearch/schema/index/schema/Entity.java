package org.opensearch.schema.index.schema;


import com.fasterxml.jackson.annotation.*;
import org.opensearch.schema.ontology.DirectiveType;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "partition",
        "props",
        "nested"
})
/**
 * the logical entity's physical index mapping and storage representation
 * @param <T>
 */
public class Entity implements BaseTypeElement<Entity> {

    @JsonProperty("directives")
    protected List<DirectiveType> directives;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("nesting")
    private NestingType nesting;
    @JsonProperty("mapping")
    private MappingIndexType mapping;
    @JsonProperty("props")
    private Props props;
    @JsonProperty("nested")
    private Map<String, Entity> nested = Collections.EMPTY_MAP;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Entity() {
    }

    public Entity(Type type, NestingType nesting, MappingIndexType mapping,List<DirectiveType> directives, Props props, Map<String,Entity> nested, Map<String, Object> additionalProperties) {
        this.type = type;
        this.nesting = nesting;
        this.mapping = mapping;
        this.directives = directives;
        this.props = props;
        this.nested = nested;
        this.additionalProperties = additionalProperties;
    }

    public Entity(Type type, NestingType nestingType, MappingIndexType mapping, Props props) {
        this(type,nestingType,mapping,Collections.emptyList(),props,Collections.EMPTY_MAP,Collections.EMPTY_MAP);
    }

    @Override
    public List<DirectiveType> getDirectives() {
        return directives;
    }

    @JsonProperty("directives")
    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    @JsonProperty("nested")
    public Map<String, Entity> getNested() {
        return nested;
    }

    @JsonProperty("nested")
    public void setNested(Map<String, Entity> nested) {
        this.nested = nested;
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

    @JsonProperty("mapping")
    public void setMapping(MappingIndexType mapping) {
        this.mapping = mapping;
    }

    @JsonProperty("mapping")
    public MappingIndexType getMapping() {
        return mapping;
    }

    @JsonProperty("partition")
    public void setNesting(NestingType nesting) {
        this.nesting = nesting;
    }

    @JsonProperty("props")
    public Props getProps() {
        return props;
    }

    @JsonProperty("props")
    public void setProps(Props props) {
        this.props = props;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    protected Entity clone()  {
        return new Entity(this.type,this.nesting,this.mapping,
                this.directives,
                this.props.clone(),
                new HashMap<>(this.nested),
                new HashMap<>(this.additionalProperties));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return Objects.equals(type, entity.type) && Objects.equals(nesting, entity.nesting) && Objects.equals(mapping, entity.mapping) &&
                Objects.equals(directives, entity.directives) && Objects.equals(props, entity.props) &&
                Objects.equals(nested, entity.nested) && Objects.equals(additionalProperties, entity.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, nesting, mapping,directives, props, nested, additionalProperties);
    }
}
