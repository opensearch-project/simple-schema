package org.opensearch.schema.ontology;

import java.util.List;

/**
 * an enumeration of the possible supported types of directives
 */
public enum DirectiveEnumTypes {
    MODEL(), RELATION("mappingType"),KEY("fields","name"),AUTOGEN();

    private List<String> arguments;

    DirectiveEnumTypes(String ... arguments) {
        this.arguments = List.of(arguments);
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isSame(String name) {
        return this.name().equalsIgnoreCase(name);
    }

    public String getName() {
        return this.name().toLowerCase();
    }

}
