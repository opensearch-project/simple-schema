package org.opensearch.languages.oql.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.oql.query.properties.constraint.Constraint;

/**
 *
 * Translates pType to a schematic name such as "stringValue.keyword"
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicEProp extends EProp {
    //region Constructors
    public SchematicEProp() {

    }

    public SchematicEProp(int eNum, String pType, String schematicName, Constraint con) {
        super(eNum, pType, con);
        this.schematicName = schematicName;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof SchematicEProp)) {
            return false;
        }

        SchematicEProp other = (SchematicEProp)o;
        if (!this.schematicName.equals(other.schematicName)) {
            return false;
        }

        return true;
    }
    //endregion

    @Override
    public SchematicEProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicEProp clone(int eNum) {
        SchematicEProp clone = new SchematicEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        clone.schematicName = schematicName;
        return clone;
    }

    //region Properties
    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //enregion

    //region Fields
    private String schematicName;
    //endregion
}
