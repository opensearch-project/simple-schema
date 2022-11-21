package org.opensearch.query.properties;

/**
 * boost-able interface to allow boosting a property with a constant amount
 */
public interface RankingProp {
    long getBoost();

}
