package org.opensearch.query.properties.constraint;


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
        switch (op) {
            case lt:
                return ConstraintOp.lt;
            case le:
                return ConstraintOp.le;
            case ge:
                return ConstraintOp.ge;
            case gt:
                return ConstraintOp.gt;
            case eq:
                return ConstraintOp.eq;
            case ne:
                return ConstraintOp.ne;
            case within:
                return ConstraintOp.within;
            case between:
                return ConstraintOp.inRange;
        }
        return null;
    }
}
