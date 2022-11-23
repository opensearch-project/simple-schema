package org.opensearch.languages.ioql.query.properties;


import org.opensearch.languages.ioql.query.properties.constraint.Constraint;

public class RedundantRelProp extends SchematicRelProp {
    //region Constructors
    public RedundantRelProp() {

    }

    public RedundantRelProp(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        this.setSchematicName(redundantPropName);
    }

    public RedundantRelProp(int eNum, String pType, String redundantPropName, String schematicName, Constraint con) {
        super(eNum, pType, schematicName, con);
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion
    @Override
    public RedundantRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RedundantRelProp clone(int eNum) {
        final RedundantRelProp clone = new RedundantRelProp();
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

    public void setRedundantPropName(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantRelProp of(int eNum, String redundantPropName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, redundantPropName, constraint);
    }

    public static RedundantRelProp of(int eNum, String redundantPropName, String schematicName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, schematicName, constraint);
    }
}
