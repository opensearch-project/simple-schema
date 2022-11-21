package org.opensearch.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.query.properties.constraint.Constraint;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicNestedEProp extends SchematicEProp implements NestingProp {

    public SchematicNestedEProp() {}

    public SchematicNestedEProp(int eNum, String pType, String schematicName, Constraint con, String path) {
        super(eNum, pType, schematicName, con);
        this.path = path;
    }

    public SchematicNestedEProp(SchematicEProp schematicEProp, String path) {
        this(schematicEProp.geteNum(), schematicEProp.getpType(), schematicEProp.getSchematicName(), schematicEProp.getCon(), path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SchematicNestedEProp that = (SchematicNestedEProp) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }

    @Override
    public SchematicNestedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicNestedEProp clone(int eNum) {
        SchematicNestedEProp clone = new SchematicNestedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        clone.path = path;
        return clone;
    }

    @Override
    public String getPath() {
        return path;
    }

    private String path;
}
