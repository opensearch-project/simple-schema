package org.opensearch.languages.ioql.query.properties;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.descriptor.QueryDescriptor;
import org.opensearch.languages.ioql.query.properties.constraint.Constraint;
import org.opensearch.languages.ioql.query.properties.projection.Projection;
import org.opensearch.languages.ioql.query.EBase;
import org.opensearch.schema.ontology.Printable;

import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BaseProp extends EBase implements Printable {
    //region Consructors
    public BaseProp() {

    }

    public BaseProp(int eNum, String pType, Constraint con) {
        super(eNum);
        this.pType = pType;
        this.con = con;
    }

    public BaseProp(int eNum, String pType, Projection proj) {
        super(eNum);
        this.pType = pType;
        this.proj = proj;
    }
    //endregion


    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseProp eProp = (BaseProp) o;

        if (pType == null) {
            if (eProp.pType != null) {
                return false;
            }
        } else {
            if (!pType.equals(eProp.pType)) {
                return false;
            }
        }

        if (pTag == null) {
            if (eProp.pTag != null) {
                return false;
            }
        } else {
            if (!pTag.equals(eProp.pTag)) {
                return false;
            }
        }

        if (con == null) {
            if (eProp.con != null) {
                return false;
            }
        } else {
            if (!con.equals(eProp.con)) {
                return false;
            }
        }

        return f != null ? f.equals(eProp.f) : eProp.f == null;
    }

    @Override
    public abstract EBase clone();

    @Override
    public void print(StringJoiner joiner) {
        QueryDescriptor.shortLabel(this,joiner,true);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (pType!=null ? pType.hashCode() : 0);
        result = 31 * result + (pTag!=null ? pTag.hashCode() : 0);
        result = 31 * result + (con!=null ? con.hashCode() : 0);
        result = 31 * result + (proj!=null ? proj.hashCode() : 0);
        result = 31 * result + (f != null ? f.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getpTag() {
        return pTag;
    }

    public void setpTag(String pTag) {
        this.pTag = pTag;
    }

    public Constraint getCon() {
        return con;
    }

    /**
     * set constraint (projection & constraints are exclusives)
     * @param con
     */
    public void setCon(Constraint con) {
        this.con = con;
        this.proj = null;
    }

    public Projection getProj() {
        return proj;
    }

    /**
     * state is this property constraint an aggregation
     * @return
     */
    @JsonIgnore
    public boolean isAggregation() {
        return false;
    }
    /**
     * set projection (projection & constraints are exclusives)
     * @param proj
     */
    public void setProj(Projection proj) {
        this.proj = proj;
        this.con = null;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public boolean isProjection() {
        return getProj()!=null;
    }

    public boolean isConstraint() {
        return getCon()!=null;
    }
    //endregion

    //region Fields
    private String pType;
    private String pTag;
    private Constraint con;
    private Projection proj;
    private String f;
    //endregion
}
