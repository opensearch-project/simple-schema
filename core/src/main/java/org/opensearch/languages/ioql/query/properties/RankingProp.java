package org.opensearch.languages.ioql.query.properties;

/**
 * boost-able interface to allow boosting a property with a constant amount
 */
public interface RankingProp {
    long getBoost();

}
