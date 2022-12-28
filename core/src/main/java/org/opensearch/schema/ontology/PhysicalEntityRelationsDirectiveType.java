package org.opensearch.schema.ontology;

import java.util.stream.Stream;

/**
 * a list of possible physical relationship type of containers
 * <br>
 * - embedded - physical entities (documents) are embedded in the parent
 * <br>
 * - nested - physical entities (documents) are nested in the parent
 * <br>
 * - child - physical entities (documents) are using parent-child relationship
 * <br>
 * - foreign - physical entities (documents) are saved in separate indices - references to the remote ID is saved in the index
 * in case of referencing many elements - the reference will be saved in a nested form
 * <br>
 * - join_index_foreign - physical entities (documents) are saved in separate indices - references to the remote ID is saved in the index
 * this indicates an additional index is used for the storage of the relation document outside the actual entities indices themselves
 * <br>
 * - reverse - physical entities (documents) are the inner relationship subject of the main entity and therefore reverse pointing back to it
 * <br>
 */
public enum PhysicalEntityRelationsDirectiveType {
    EMBEDDED, NESTED, CHILD, FOREIGN, JOIN_INDEX_FOREIGN, REVERSE;

    public static PhysicalEntityRelationsDirectiveType from(String value) {
        return Stream.of(PhysicalEntityRelationsDirectiveType.values()).filter(v -> v.isSame(value)).findAny().orElse(EMBEDDED);
    }

    public String getName() {
        return this.name().toLowerCase();
    }

    public boolean isSame(String name) {
        return this.getName().equals(name);
    }

}
