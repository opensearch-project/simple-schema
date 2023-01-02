package org.opensearch.schema.ontology;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.StringJoiner;

/**
 * root element of the ontology data model - parent of entity & relationship
 */
public interface BaseElement {
    String ID = "@id";

    String getName();

    List<String> getIdField();

    List<String> getMetadata();

    List<String> fields();

    List<String> getProperties();

    List<DirectiveType> getDirectives();

    boolean containsProperty(String key);
    boolean containsMetadata(String key);

    @JsonIgnore
    static String idFieldName(List<String> values) {
        StringJoiner joiner = new StringJoiner("_");
        values.forEach(joiner::add);
        return joiner.toString().length() > 0 ?
                joiner.toString() : ID;
    }
}
