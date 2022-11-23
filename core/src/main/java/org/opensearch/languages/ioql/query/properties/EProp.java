package org.opensearch.languages.ioql.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.properties.constraint.Constraint;
import org.opensearch.languages.ioql.query.properties.projection.Projection;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends BaseProp {
    //region Constructors
    public EProp() {
        super();
    }

    public EProp(int eNum, String pType, Constraint con) {
        super(eNum, pType, con);
    }

    public EProp(int eNum, String pType, Projection proj) {
        super(eNum, pType, proj);
    }
    //endregion


    @Override
    public EProp clone() {
        return clone(geteNum());
    }

    @Override
    public EProp clone(int eNum) {
        EProp clone = new EProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }


    //region Static
    public static EProp of(int eNum, String pType, Constraint con) {
        return new EProp(eNum, pType, con);
    }

    public static EProp of(int eNum, String pType, Projection proj) {
        return new EProp(eNum, pType, proj);
    }
    //endregion
}
