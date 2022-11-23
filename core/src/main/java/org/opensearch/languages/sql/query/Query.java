package org.opensearch.languages.sql.query;

import org.opensearch.languages.QueryBuilder;

/**
 * this is the SQL b
 */
public class Query {
    private String name;
    private LogicalPlan query;


    public static class Builder implements QueryBuilder<Query> {

        public static Builder instance() {
            return null;
        }

        @Override
        public Query build() {
            return null;
        }

        @Override
        public QueryBuilder withName(String name) {
            return null;
        }
    }
}
