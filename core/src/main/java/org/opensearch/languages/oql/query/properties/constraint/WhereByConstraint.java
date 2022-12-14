package org.opensearch.languages.oql.query.properties.constraint;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhereByConstraint extends Constraint implements WhereByFacet {

    private String tagEntity;
    private String projectedField;
    private JoinType joinType;

    public WhereByConstraint() {
    }

    public WhereByConstraint(ConstraintOp op,String tagEntity, String projectedField) {
        super(op, null);
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
        this.joinType = ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL;
    }

    public WhereByConstraint(ConstraintOp op, Object expression,String tagEntity, String projectedField) {
        super(op, expression);
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
        this.joinType = ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL;
    }

    public WhereByConstraint(ConstraintOp op, Object expression, String tagEntity,JoinType joinType, String projectedField) {
        super(op, expression);
        this.tagEntity = tagEntity;
        this.joinType = joinType;
        this.projectedField = projectedField;
    }

    public String getProjectedField() {
        return projectedField;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    public String getTagEntity() {
        return tagEntity;
    }

    @Override
    public WhereByConstraint clone()  {
        return new WhereByConstraint(getOp(), getExpr(),getTagEntity(), projectedField);
    }

    public static WhereByConstraint of(ConstraintOp op,String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, tagEntity,projectedFields);
    }

    public static WhereByConstraint of(ConstraintOp op, Object expression,String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, expression, tagEntity,projectedFields);
    }

    public static WhereByConstraint of(ConstraintOp op, Object expression,JoinType joinType, String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, expression,tagEntity,joinType, projectedFields);
    }

}
