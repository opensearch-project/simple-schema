package org.opensearch.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionalUnaryParameterizedConstraint extends ParameterizedConstraint {

    public OptionalUnaryParameterizedConstraint() {}

    public OptionalUnaryParameterizedConstraint(ConstraintOp defaultValue, Set<ConstraintOp> ops, NamedParameter parameter) {
        this(defaultValue,null,ops,parameter);
    }

    public OptionalUnaryParameterizedConstraint(ConstraintOp defaultValue,Object exp, Set<ConstraintOp> ops, NamedParameter parameter) {
        //set defaultValue as the op field of the base class (calling OptionalUnaryParameterizedConstraint.getOps() will result with the default value)
        super(defaultValue,exp,parameter);
        this.operations = ops;
    }

    public Set<ConstraintOp> getOperations() {
        return operations;
    }

    @Override
    public OptionalUnaryParameterizedConstraint clone() {
        return new OptionalUnaryParameterizedConstraint(getOp(),getExpr(),getOperations(), getParameter());
    }

    private Set<ConstraintOp> operations = Collections.emptySet();
}
