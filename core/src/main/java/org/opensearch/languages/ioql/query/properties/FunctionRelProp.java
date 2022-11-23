package org.opensearch.languages.ioql.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.properties.constraint.Constraint;

/**
 *
 * a calculated field based on tag associated with the entity or entity relation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FunctionRelProp extends RelProp {
    //region Constructors
    public FunctionRelProp() {  }

    public FunctionRelProp(int eNum, String expression, Constraint con) {
        super(eNum, expression, con);
    }
    //endregion

    //region Override Methods
    //endregion

    @Override
    public FunctionRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public FunctionRelProp clone(int eNum) {
        FunctionRelProp clone = new FunctionRelProp();
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

    public static FunctionRelProp of(int eNum, String expression, Constraint con ) {
        return new FunctionRelProp(eNum, expression, con);
    }

}
