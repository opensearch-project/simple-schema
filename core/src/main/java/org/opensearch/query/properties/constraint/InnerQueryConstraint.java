package org.opensearch.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opensearch.query.Query;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerQueryConstraint extends Constraint implements WhereByFacet {

    private JoinType joinType;
    private Query innerQuery;
    private String tagEntity;
    private String projectedField;

    public InnerQueryConstraint() {
    }

    public InnerQueryConstraint(ConstraintOp op, Query innerQuery, String tagEntity, String projectedField) {
        this(op, null, ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL, innerQuery, tagEntity, projectedField);
    }

    public InnerQueryConstraint(ConstraintOp op, Object expression,JoinType joinType, Query innerQuery, String tagEntity, String projectedField) {
        super(op, expression);
        this.joinType = joinType;
        this.innerQuery = innerQuery;
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
    }

    public Query getInnerQuery() {
        return innerQuery;
    }

    public String getProjectedField() {
        return projectedField;
    }

    public String getTagEntity() {
        return tagEntity;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    public InnerQueryConstraint clone() {
        return new InnerQueryConstraint(getOp(),getExpr(),getJoinType(), getInnerQuery(), getTagEntity(), getProjectedField());
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery, String tagEntity, String projectedFields) {
        return new InnerQueryConstraint(op, innerQuery, tagEntity, projectedFields);
    }

    public static InnerQueryConstraint of(ConstraintOp op, Object expression,JoinType joinType, Query innerQuery, String tagEntity, String projectedFields) {
        return new InnerQueryConstraint(op, expression, joinType,innerQuery, tagEntity, projectedFields);
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery) {
        return of(op, innerQuery, "", "");
    }


}
