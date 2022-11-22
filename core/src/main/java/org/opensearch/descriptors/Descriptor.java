package org.opensearch.descriptors;


public interface Descriptor<Q> {
    String describe(Q item);

}
