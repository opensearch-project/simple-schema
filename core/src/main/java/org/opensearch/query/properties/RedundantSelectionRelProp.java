package org.opensearch.query.properties;


import org.opensearch.query.properties.projection.Projection;

public class RedundantSelectionRelProp extends RelProp {
    //region Constructors
    public RedundantSelectionRelProp() {
        super();
    }

    public RedundantSelectionRelProp(int eNum, String pType, String redundantPropName, Projection proj, int b) {
        super(eNum, pType, proj, b);
        this.redundantPropName = redundantPropName;
    }
    //endregion

    @Override
    public RedundantSelectionRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RedundantSelectionRelProp clone(int eNum) {
        final RedundantSelectionRelProp clone = new RedundantSelectionRelProp();
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
    public String getRedundantPropName() {
        return redundantPropName;
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantSelectionRelProp of(int eNum, String pType, String redundantPropName, Projection proj){
        RedundantSelectionRelProp relProp = new RedundantSelectionRelProp(eNum, pType, redundantPropName, proj, 0);
        relProp.seteNum(eNum);
        relProp.setpType(pType);
        return relProp;
    }
}
