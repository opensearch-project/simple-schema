package org.opensearch.languages.oql.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Set;

public enum CountConstraintOp {

    @JsonProperty("eq")
    eq,

    @JsonProperty("ne")
    ne,

    @JsonProperty("gt")
    gt,

    @JsonProperty("ge")
    ge,

    @JsonProperty("lt")
    lt,

    @JsonProperty("le")
    le,

    @JsonProperty("within")
    between,

    @JsonProperty("within")
    within;

 public static Set<Class<? extends Constraint>> ignorableConstraints;
    public static Set<CountConstraintOp> noValueOps;
    public static Set<CountConstraintOp> singleValueOps;
    public static Set<CountConstraintOp> multiValueOps;
    public static Set<CountConstraintOp> exactlyTwoValueOps;

    static {
        ignorableConstraints = Stream.of(ParameterizedConstraint.class,
                JoinParameterizedConstraint.class,
                InnerQueryConstraint.class).toJavaSet();
        singleValueOps = Stream.of(eq, ne, gt, ge, lt, le).toJavaSet();
        multiValueOps = Collections.emptySet();
        exactlyTwoValueOps = Stream.of(between,within).toJavaSet();
    }

    public static ConstraintOp translate(CountConstraintOp op) {
        return switch (op) {
            case lt -> ConstraintOp.lt;
            case le -> ConstraintOp.le;
            case ge -> ConstraintOp.ge;
            case gt -> ConstraintOp.gt;
            case eq -> ConstraintOp.eq;
            case ne -> ConstraintOp.ne;
            case within -> ConstraintOp.within;
            case between -> ConstraintOp.inRange;
        };
    }
}
