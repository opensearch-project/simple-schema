package org.opensearch.schema.ontology;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 *  ontology relation element type
 */
public class RelationshipType extends CommonType {

    public RelationshipType() {
        super();
    }

    public RelationshipType(String name, String rType, boolean directional) {
        super(name);
        this.rType = rType;
        this.directional = directional;
        this.ePairs = new ArrayList<>();
    }

    //region Getters & Setters
    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    @JsonProperty("DBrName")
    public String getDbRelationName() {
        return dbRelationName;
    }

    @JsonProperty("DBrName")
    public void setDbRelationName(String dbRelationName) {
        this.dbRelationName = dbRelationName;
    }

    public void setePairs(List<EPair> ePairs) {
        this.ePairs = ePairs;
    }

    public List<EPair> getePairs() {
        return ePairs;
    }

    @JsonIgnore
    public Set<String> getSidesA() {
        return ePairs.stream().map(EPair::geteTypeA).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<String> getSidesB() {
        return ePairs.stream().map(EPair::geteTypeB).collect(Collectors.toSet());
    }

    @JsonIgnore
    public String idFieldName() {
        return BaseElement.idFieldName(getIdField());
    }

    @JsonIgnore
    public boolean containsProperty(String key) {
        return properties.contains(key);
    }

    @JsonIgnore
    public boolean hasSideA(String eType) {
        return ePairs.stream().anyMatch(ep -> ep.geteTypeA().equals(eType));
    }

    @JsonIgnore
    public boolean hasSideB(String eType) {
        return ePairs.stream().anyMatch(ep -> ep.geteTypeB().equals(eType));
    }

    //endregion

    @Override
    protected RelationshipType clone() {
        RelationshipType relationshipType = new RelationshipType();
        relationshipType.directional = this.directional;
        relationshipType.dbRelationName = this.dbRelationName;
        relationshipType.rType = this.rType;
        relationshipType.name = this.name;
        relationshipType.properties = new ArrayList<>(this.properties);
        relationshipType.mandatory = new ArrayList<>(this.mandatory);
        relationshipType.metadata = new ArrayList<>(this.metadata);
        relationshipType.idField = new ArrayList<>(this.idField);
        relationshipType.directives = new ArrayList<>(this.directives);
        relationshipType.ePairs = this.ePairs.stream().map(EPair::clone).collect(Collectors.toList());
        return relationshipType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipType that = (RelationshipType) o;
        return directional == that.directional &&
                idField.equals(that.idField) &&
                rType.equals(that.rType) &&
                name.equals(that.name) &&
                mandatory.equals(that.mandatory) &&
                ePairs.equals(that.ePairs) &&
                properties.equals(that.properties) &&
                metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idField, rType, name, directional, mandatory, ePairs, properties, metadata);
    }

    @Override
    public String toString() {
        return "RelationshipType [name = " + name + ", ePairs = " + ePairs + ", idField = " + idField + ", rType = " + rType + ", directional = " + directional + ", properties = " + properties + ", metadata = " + metadata + ", mandatory = " + mandatory + "]";
    }

    //region Fields
    private List<String> idField = Collections.singletonList(ID);
    private String rType;
    private boolean directional;
    private String dbRelationName;
    private List<EPair> ePairs;

//endregion

    //region Builder
    public static final class Builder {
        private List<String> idField = new ArrayList<>();
        private String rType;
        private String name;
        private boolean directional;
        private String DBrName;
        private List<String> mandatory = new ArrayList<>();
        private List<EPair> ePairs = new ArrayList<>();
        private List<String> properties = new ArrayList<>();
        private List<String> metatada = new ArrayList<>();
        private List<DirectiveType> directives = new ArrayList<>();


        private Builder() {
            idField.add(ID);
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withRType(String rType) {
            this.rType = rType;
            return this;
        }

        public Builder withDirectives(Collection<DirectiveType> values) {
            this.directives.addAll(values);
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public Builder withMandatory(List<String> mandatory) {
            this.mandatory = mandatory;
            return this;
        }

        public Builder withDBrName(String DBrName) {
            this.DBrName = DBrName;
            return this;
        }

        public Builder withEPairs(List<EPair> ePairs) {
            this.ePairs = ePairs;
            return this;
        }

        public Builder withEPair(EPair ePair) {
            this.ePairs.add(ePair);
            return this;
        }

        public Builder withProperties(List<String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withProperty(String property) {
            this.properties.add(property);
            return this;
        }

        public Builder withIdField(String... idField) {
            return withIdField(Arrays.asList(idField));
        }

        public Builder withIdField(List<String> idFields) {
            this.idField = idFields;
            return this;
        }


        public Builder withMetadata(List<String> metatada) {
            this.metatada = metatada;
            return this;
        }

        public RelationshipType build() {
            RelationshipType relationshipType = new RelationshipType();
            relationshipType.setrType(this.rType);
            relationshipType.setIdField(idField);
            relationshipType.setName(name);
            relationshipType.setDirectional(directional);
            relationshipType.setDirectional(directional);
            relationshipType.setDbRelationName(DBrName);
            relationshipType.setProperties(properties);
            relationshipType.setMetadata(metatada);
            relationshipType.setMandatory(mandatory);
            relationshipType.setePairs(ePairs);
            relationshipType.getDirectives().addAll(directives);
            return relationshipType;
        }
    }
    //endregion

}
