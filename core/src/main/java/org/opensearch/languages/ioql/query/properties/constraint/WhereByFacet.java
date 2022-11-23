package org.opensearch.languages.ioql.query.properties.constraint;


public interface WhereByFacet {

    String getProjectedField();

    JoinType getJoinType();

    String getTagEntity();

    enum JoinType {
        FULL,FOR_EACH
    }
}
