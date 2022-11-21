package org.opensearch.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.Objects;

@JsonSubTypes({
        @JsonSubTypes.Type(name = "QueryNamedParameter", value = QueryNamedParameter.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedParameter {
    public static final String $VAL = "$val";

    private String name;
    private Object value;

    public NamedParameter() {}

    public NamedParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }
    public NamedParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedParameter)) return false;
        NamedParameter that = (NamedParameter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
