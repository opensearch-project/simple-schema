package org.opensearch.graphql;

import org.opensearch.schema.ontology.Accessor;

/**
 * general purpose transformer for query
 * @param <T>
 */
public interface Transformer<T> {
    /**
     * transforms a Query into a specific typed <T> query using the Ontology accessor helper
     * @param accessor
     * @param query
     * @return
     */
    T transform(Accessor accessor, String query);
}
