package org.opensearch.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.query.properties.constraint.Constraint;

/**
 *
 * a calculated field based on tag associated with the entity or entity relation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FunctionEProp extends EProp {
    //region Constructors
    public FunctionEProp() {  }

    public FunctionEProp(int eNum, String expression, Constraint con) {
        super(eNum, expression, con);
    }
    //endregion

    //region Override Methods
    //endregion

    @Override
    public FunctionEProp clone() {
        return clone(geteNum());
    }

    @Override
    public FunctionEProp clone(int eNum) {
        FunctionEProp clone = new FunctionEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    @Override
    public boolean isAggregation() {
        return true;
    }

    public static FunctionEProp of(int eNum, String expression, Constraint con ) {
        return new FunctionEProp(eNum, expression, con);
    }

}
