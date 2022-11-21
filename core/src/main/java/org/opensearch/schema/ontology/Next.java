package org.opensearch.schema.ontology;

public interface Next<T> {
    T getNext();

    void setNext(T next);

    boolean hasNext();

}
