package org.opensearch.languages.ioql.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "OptionalUnaryParameterizedConstraint", value = OptionalUnaryParameterizedConstraint.class),
        @JsonSubTypes.Type(name = "JoinParameterizedConstraint", value = JoinParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterizedConstraint extends Constraint {
    private NamedParameter parameter;

    public ParameterizedConstraint() {}

    public ParameterizedConstraint(ConstraintOp op,Object expression, NamedParameter parameter) {
        super(op,expression);
        this.parameter = parameter;
    }

    @Override
    public ParameterizedConstraint clone() {
        return new ParameterizedConstraint(getOp(),getExpr(),getParameter());
    }

    public NamedParameter getParameter() {
        return parameter;
    }

    public static ParameterizedConstraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp) {
        return new ParameterizedConstraint(op,null,exp);
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp, String iType) {
        ParameterizedConstraint constraint = new ParameterizedConstraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }
}
