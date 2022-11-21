package org.opensearch.query.properties.constraint;


public interface WhereByFacet {

    String getProjectedField();

    JoinType getJoinType();

    String getTagEntity();

    enum JoinType {
        FULL,FOR_EACH
    }
}
