package org.opensearch.languages.oql.query;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.opensearch.languages.oql.query.properties.constraint.NamedParameter;

import java.util.Collection;
import java.util.List;

/**
 * A specialized query which has a list of dynamic parameters that can be replaced during actual query execution
 * This query can be saved and called in a similar manner to stored procedure in a relational database
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = ParameterizedQuery.Builder.class)
public class ParameterizedQuery extends Query {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<NamedParameter> params;

    public ParameterizedQuery(Query query,Collection<NamedParameter> params) {
        this.params = params;
        this.setElements(query.getElements());
        this.setName(query.getName());
        this.setOnt(query.getOnt());
        this.setNonidentical(query.getNonidentical());
    }

    public Collection<NamedParameter> getParams() {
        return params;
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        private Collection<NamedParameter> params;
        private Query.Builder builder;

        private Builder() {
            builder = Query.Builder.instance();
        }

        public static Builder instance() {
            return new Builder();
        }

        public Builder withOnt(String ont) {
            this.builder.withOnt(ont);
            return this;
        }

        public Builder withName(String name) {
            this.builder.withName(name);
            return this;
        }

        public Builder withElements(List<EBase> elements) {
            this.builder.withElements(elements);
            return this;
        }

        public Builder withParams(Collection<NamedParameter> params) {
            this.params = params;
            return this;
        }

        public Builder withNonidentical(List<List<String>> nonidentical) {
            this.builder.withNonidentical(nonidentical);
            return this;
        }

        public ParameterizedQuery build() {
            return new ParameterizedQuery(this.builder.build(),params);
        }

    }

}
