package org.opensearch.languages.ioql.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.properties.constraint.Constraint;
import org.opensearch.languages.ioql.query.properties.projection.Projection;

import java.util.Objects;

/**
 * Eprop which represents a nesting entity
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NestedEProp extends EProp implements NestingProp {
    //region Constructors

    public NestedEProp() {
    }

    public NestedEProp(EProp eProp, String path) {
        this(eProp.geteNum(), eProp.getpType(), eProp.getCon(), path);
    }

    public NestedEProp(int eNum, String pType, Constraint con, String path) {
        super(eNum, pType, con);
        this.path = path;
    }

    public NestedEProp(int eNum, String pType, Projection proj, String path) {
        super(eNum, pType, proj);
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NestedEProp that = (NestedEProp) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }

    @Override
    public NestedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public NestedEProp clone(int eNum) {
        NestedEProp clone = new NestedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    public static NestedEProp of(int eNum, String parent, String pType, Constraint con) {
        return new NestedEProp(new EProp(eNum,pType,con),parent);
    }
    //endregion
    public String getPath() {
        return path;
    }
    //region Properties

    //region Fields
    private String path;
    //endregion

}
