package org.opensearch.languages.ioql.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;

import java.util.Set;

public enum ConstraintOp {

    @JsonProperty("empty")
    empty,

    @JsonProperty("not empty")
    notEmpty,

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

    @JsonProperty("in set")
    inSet,

    @JsonProperty("not in set")
    notInSet,

    @JsonProperty("in range")
    inRange,

    @JsonProperty("within")
    within,

    @JsonProperty("not in range")
    notInRange,

    @JsonProperty("contains")
    contains,

    @JsonProperty("distinct")
    distinct,

    @JsonProperty("not contains")
    notContains,

    @JsonProperty("starts with")
    startsWith,

    @JsonProperty("not starts with")
    notStartsWith,

    @JsonProperty("ends with")
    endsWith,

    @JsonProperty("not ends with")
    notEndsWith,

    @JsonProperty("match")
    match,

    @JsonProperty("match_phrase")
    match_phrase,

    @JsonProperty("query_string")
    query_string,

    @JsonProperty("not match")
    notMatch,

    @JsonProperty("fuzzy eq")
    fuzzyEq,

    @JsonProperty("fuzzy ne")
    fuzzyNe,

    @JsonProperty("like")
    like,

    @JsonProperty("like any")
    likeAny;

    public static Set<Class<? extends Constraint>> ignorableConstraints;
    public static Set<ConstraintOp> noValueOps;
    public static Set<ConstraintOp> singleValueOps;
    public static Set<ConstraintOp> multiValueOps;
    public static Set<ConstraintOp> exactlyTwoValueOps;

    static {
        ignorableConstraints = Stream.of(ParameterizedConstraint.class,
                JoinParameterizedConstraint.class,
                InnerQueryConstraint.class).toJavaSet();

        noValueOps = Stream.of(empty,notEmpty).toJavaSet();

        singleValueOps = Stream.of(eq, ne, gt, ge, lt, le, contains, startsWith, notContains, notStartsWith, notEndsWith,
                fuzzyEq, fuzzyNe, match, match_phrase, notMatch, empty, notEmpty, query_string).toJavaSet();

        multiValueOps = Stream.of(inRange, notInRange, inSet, notInSet, likeAny).toJavaSet();

        exactlyTwoValueOps = Stream.of(inRange, notInRange).toJavaSet();
    }

}
