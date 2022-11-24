package org.opensearch.descriptors;

/**
 * general purpose description interface - may be used by any concrete component that wants to share a string representation of its state
 * @param <Q>
 */
public interface Descriptor<Q> {
    String describe(Q item);

}
