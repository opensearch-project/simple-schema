package org.opensearch.query.properties.projection;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IdentityProjection extends Projection {
}