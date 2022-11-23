package org.opensearch.languages.ioql.query.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.properties.constraint.Constraint;
import org.opensearch.languages.ioql.query.properties.projection.Projection;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelProp extends BaseProp {
    //region Constructors
    public RelProp() {
        super();
    }

    public RelProp(int eNum, String pType, Constraint con) {
        this(eNum,pType,con,-1);
    }

    public RelProp(int eNum, String pType, Constraint con, int b) {
        super(eNum, pType, con);
        this.b = b;
    }

    public RelProp(int eNum, String pType, Projection proj) {
        super(eNum, pType, proj);
    }

    public RelProp(int eNum, String pType, Projection proj, int b) {
        super(eNum, pType, proj);
        this.b = b;
    }
    //endregion

    @Override
    public RelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RelProp clone(int eNum) {
        final RelProp clone = new RelProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setB(getB());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int b;
    //endregion

    //region Static
    public static RelProp of(int eNum, String pType, Constraint con) {
        return new RelProp(eNum, pType, con, 0);
    }

    public static RelProp of(int eNum, String pType, Projection proj) {
        return new RelProp(eNum, pType, proj, 0);
    }
    //endregion
}
