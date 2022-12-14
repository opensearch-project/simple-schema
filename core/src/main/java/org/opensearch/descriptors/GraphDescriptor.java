package org.opensearch.descriptors;

/**
 * general purpose info-graphics interface - may be used by any concrete component that wants to share a string representation of its state
 * should be viewed in a visual context such as
 * see <a href="https://dreampuf.github.io/GraphvizOnline/">Visualize</a>
 * @param <Q>
 */
public interface GraphDescriptor<Q> {
    String visualize(Q item);

}
