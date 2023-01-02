package org.opensearch.schema.index.schema;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opensearch.schema.ontology.DirectiveType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * the common basic physical index mapping and storage representation
 * @param <T>
 */
public interface BaseTypeElement<T> {
    List<DirectiveType> getDirectives();

    Map<String, T> getNested();

    Props getProps();

    MappingIndexType getMapping();

    NestingType getNesting();

    BaseTypeElement.Type getType();

    boolean hasProperties();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Type {
        public static final BaseTypeElement.Type Unknown = new BaseTypeElement.Type("", Optional.empty(), true);
        private String name;
        private Optional<String> field = Optional.empty();
        private boolean implicit;

        private Type() {
        }

        public Type(String name) {
            this(name, Optional.empty(), false);
        }

        public Type(String name, Optional<String> field, boolean implicit) {
            this.name = name;
            this.field = field;
            this.implicit = implicit;
        }

        @JsonIgnore
        public static BaseTypeElement.Type of(String name) {
            return new BaseTypeElement.Type(name, Optional.empty(), false);
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        @JsonProperty("field")
        public Optional<String> getField() {
            return field;
        }

        @JsonProperty("implicit")
        public boolean isImplicit() {
            return implicit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BaseTypeElement.Type type = (BaseTypeElement.Type) o;
            return implicit == type.implicit && name.equals(type.name) && field.equals(type.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, field, implicit);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class NestedType extends BaseTypeElement.Type {
        private String path;

        @JsonIgnore
        public static BaseTypeElement.NestedType of(String name, String path) {
            return new BaseTypeElement.NestedType(name, path, Optional.empty(), false);
        }

        private NestedType() {}

        public NestedType(String name,String path) {
            super(name);
            this.path = path;
        }

        public NestedType(String name, String path, Optional<String> field, boolean implicit) {
            super(name, field, implicit);
            this.path = path;
        }

        @JsonProperty("path")
        public String getPath() {
            return path;
        }
    }
}
