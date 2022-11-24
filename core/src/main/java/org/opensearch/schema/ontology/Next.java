package org.opensearch.schema.ontology;

/**
 * Next interface marks an iterable component which have next elements in chain
 * @param <T>
 */
public interface Next<T> {
    T getNext();

    void setNext(T next);

    boolean hasNext();

}
