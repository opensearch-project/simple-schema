package org.opensearch.schema.ontology;

import java.util.StringJoiner;

/**
 * printable marker interface
 */
public interface Printable {
    void print(StringJoiner joiner);
}
