package org.opensearch.schema.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import static org.opensearch.schema.index.schema.IndexMappingUtils.MAPPING_TYPE;

/**
 * an enumeration of the possible supported types of directives
 */
public enum DirectiveEnumTypes {
    MODEL(), RELATION(MAPPING_TYPE),KEY("fields","name"),AUTOGEN();

    private List<String> arguments;

    DirectiveEnumTypes(String ... arguments) {
        this.arguments = List.of(arguments);
    }

    public List<String> getArguments() {
        return arguments;
    }
    @JsonIgnore
    public String getArgument(int index) {
        return arguments.get(index);
    }

    public boolean isSame(String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public String getName() {
        return this.name().toLowerCase();
    }

}
