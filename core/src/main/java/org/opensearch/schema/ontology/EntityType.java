package org.opensearch.schema.ontology;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * ontology entity element type
 */
public class EntityType extends CommonType {
    //region Fields
    private boolean isAbstract;
    private String eType;
    private List<String> display = new ArrayList<>();
    private List<String> parentType = new ArrayList<>();

    public EntityType() {
        super();
    }

    public EntityType(String type, String name, List<String> properties, List<String> metadata, List<String> mandatory, List<String> parentType) {
        super(name,properties,metadata,mandatory);
        this.eType = type;
        this.parentType = parentType;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }

    public List<String> getParentType() {
        return parentType != null ? parentType : Collections.emptyList();
    }

    public void setParentType(List<String> parentType) {
        this.parentType = parentType;
    }

    public List<String> getDisplay() {
        return display;
    }

    public void setDisplay(List<String> display) {
        this.display = display;
    }


    @JsonIgnore
    public String idFieldName() {
        return BaseElement.idFieldName(getIdField());
    }

    @Override
    protected EntityType clone() {
        EntityType entityType = new EntityType();
        entityType.eType = this.eType;
        entityType.name = this.name;
        entityType.isAbstract = this.isAbstract;
        entityType.properties = new ArrayList<>(this.properties);
        entityType.mandatory = new ArrayList<>(this.mandatory);
        entityType.metadata = new ArrayList<>(this.metadata);
        entityType.idField = new ArrayList<>(this.idField);
        entityType.display = new ArrayList<>(this.display);
        entityType.parentType = new ArrayList<>(this.parentType);
        entityType.directives = new ArrayList<>(this.directives);
        return entityType;
    }

    @Override
    public String toString() {
        return "EntityType [idField = " + idField + ",eType = " + eType + ",abstract = " + isAbstract + ", name = " + name + ", display = " + display + ", properties = " + properties + ", metadata = " + metadata + ", mandatory = " + mandatory + ", directives = " + directives + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityType that = (EntityType) o;
        return idField.equals(that.idField) &&
                isAbstract == that.isAbstract &&
                eType.equals(that.eType) &&
                Objects.equals(parentType, that.parentType) &&
                name.equals(that.name) &&
                properties.equals(that.properties) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(directives, that.directives) &&
                display.equals(that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idField, eType, isAbstract, parentType, name, properties, metadata, display);
    }

    @JsonIgnore
    public boolean containsProperty(String key) {
        return properties.contains(key);
    }

    @JsonIgnore
    public boolean containsMetadata(String key) {
        return metadata.contains(key);
    }
    //endregion

    //region Builder
    public static final class Builder {
        private List<String> idField = new ArrayList<>();
        private String eType;
        private String name;
        private List<String> mandatory = new ArrayList<>();
        private List<String> properties = new ArrayList<>();
        private List<String> metadata = new ArrayList<>();
        private List<String> display = new ArrayList<>();
        private List<String> parentType = new ArrayList<>();
        private List<DirectiveType> directives = new ArrayList<>();

        private boolean isAbstract = false;

        private Builder() {
            // id field is no longer a default
            //  - if no id field defined for root level entities -> an error should be thrown.
            /* idField.add(ID); */
        }


        public static Builder get() {
            return new Builder();
        }

        @JsonIgnore
        public Builder withIdField(String... idField) {
            //only populate if fields are not empty so that the default GlobalConstants.ID would not vanish
            if (idField.length > 0) this.idField = Arrays.asList(idField);
            return this;
        }

        @JsonIgnore
        public Builder withEType(String eType) {
            this.eType = eType;
            return this;
        }

        @JsonIgnore
        public Builder withParentTypes(List<String> superTypes) {
            this.parentType = superTypes;
            return this;
        }

        @JsonIgnore
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        @JsonIgnore
        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        @JsonIgnore
        public Builder withProperty(String property) {
            this.properties.add(property);
            return this;
        }

        @JsonIgnore
        public Builder withMandatory(List<String> mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        @JsonIgnore
        public Builder withMandatory(String mandatory) {
            this.mandatory.add(mandatory);
            return this;
        }

        @JsonIgnore
        public Builder withMetadata(List<String> metadata) {
            this.metadata = metadata;
            return this;
        }

        @JsonIgnore
        public Builder withDisplay(List<String> display) {
            this.display = display;
            return this;
        }

        @JsonIgnore
        public Builder withParentType(String parent) {
            this.parentType.add(parent);
            return this;
        }

        @JsonIgnore
        public Builder isAbstract(boolean isAbstract) {
            this.isAbstract = isAbstract;
            return this;
        }

        @JsonIgnore
        public Builder withDirectives(Collection<DirectiveType> values) {
            this.directives.addAll(values);
            return this;
        }

        public EntityType build() {
            EntityType entityType = new EntityType();
            entityType.setName(name);
            entityType.setProperties(properties);
            entityType.setMandatory(mandatory);
            entityType.setMetadata(metadata);
            entityType.setDisplay(display);
            entityType.setParentType(parentType);
            entityType.seteType(eType);
            entityType.setIdField(idField);
            entityType.setAbstract(isAbstract);
            entityType.getDirectives().addAll(this.directives);
            return entityType;
        }
    }
    //endregion

}
