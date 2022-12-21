package org.opensearch.schema.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * this common class represents the common attributes of both EntityType & RelationshipType
 */
public abstract class CommonType implements BaseElement {
    protected String name;
    protected List<String> idField = new ArrayList<>();
    protected List<DirectiveType> directives = new ArrayList<>();
    protected List<String> mandatory = new ArrayList<>();
    protected List<String> properties = new ArrayList<>();
    protected List<String> metadata = new ArrayList<>();

    public CommonType() {}

    public CommonType(String name) {
        this.name = name;
    }

    public CommonType(String name, List<String> properties, List<String> metadata, List<String> mandatory) {
        this.name = name;
        this.properties = properties;
        this.metadata = metadata;
        this.mandatory = mandatory;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<String> getMandatory() {
        return mandatory;
    }

    @Override
    public List<String> getIdField() {
        return idField;
    }

    @Override
    public List<String> getMetadata() {
        return metadata;
    }

    @JsonIgnore
    public List<String> fields() {
        return Stream.concat(properties.stream(), metadata.stream()).collect(Collectors.toList());
    }

    @Override
    public List<String> getProperties() {
        return properties;
    }

    @Override
    public List<DirectiveType> getDirectives() {
        return directives;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdField(List<String> idField) {
        this.idField = idField;
    }

    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    public void setMandatory(List<String> mandatory) {
        this.mandatory = mandatory;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
    }

    public static class Accessor {
        public static Optional<DirectiveType> getDirective(CommonType type, String name) {
            return type.getDirectives().stream().filter(d->d.getName().equals(name)).findAny();
        }

    }
}
