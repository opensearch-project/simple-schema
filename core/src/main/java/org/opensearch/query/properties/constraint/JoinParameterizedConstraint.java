package org.opensearch.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinParameterizedConstraint extends ParameterizedConstraint{
    private WhereByFacet.JoinType joinType;

    public JoinParameterizedConstraint() {
    }

    public JoinParameterizedConstraint(ConstraintOp op, Object expression, NamedParameter parameter, WhereByFacet.JoinType joinType) {
        super(op, expression, parameter);
        this.joinType = joinType;
    }

    public WhereByFacet.JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(WhereByFacet.JoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public JoinParameterizedConstraint clone() {
        return new JoinParameterizedConstraint(getOp(),getExpr(),getParameter(),getJoinType());
    }

    public static JoinParameterizedConstraint of(ConstraintOp op, NamedParameter exp, WhereByFacet.JoinType joinType) {
        return of(op,exp,"[]",joinType);
    }

    public static JoinParameterizedConstraint of(ConstraintOp op, NamedParameter exp, String iType,WhereByFacet.JoinType joinType) {
        JoinParameterizedConstraint constraint = new JoinParameterizedConstraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        constraint.setJoinType(joinType);
        return constraint;
    }

}
