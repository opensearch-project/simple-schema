package org.opensearch.languages.oql.query.properties;


/**
 * marker interface stating the following property is actually a nested entity
 */
public interface NestingProp {
    /**
     * get the path name of the nesting entity
     * @return
     */
    String getPath();

}
