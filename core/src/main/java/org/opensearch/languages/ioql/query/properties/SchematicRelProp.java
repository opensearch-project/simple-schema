package org.opensearch.languages.ioql.query.properties;


import org.opensearch.languages.ioql.query.properties.constraint.Constraint;

/**
 *
 * Translates rType to a schematic name such as "stringValue.keyword"
 */
public class SchematicRelProp extends RelProp {
    //region Constructors
    public SchematicRelProp() {

    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con) {
        this(eNum, pType, schematicName, con, 0);
    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con, int b) {
        super(eNum, pType, con, b);
        this.schematicName = schematicName;
    }
    //endregion

    @Override
    public SchematicRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicRelProp clone(int eNum) {
        final SchematicRelProp clone = new SchematicRelProp();
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
    public String getSchematicName() {
        return this.schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //endregion

    //region Fields
    private String schematicName;
    //endregion
}
