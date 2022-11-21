package org.opensearch.query.properties;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.query.properties.constraint.Constraint;
import org.opensearch.query.properties.projection.Projection;

/**
 * Eprop with a boost to rank the query results according to the desired boost
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScoreEProp extends EProp implements RankingProp {
    //region Constructors

    public ScoreEProp() {}

    public ScoreEProp(EProp eProp, long boost) {
        this(eProp.geteNum(),eProp.getpType(),eProp.getCon(),boost);
    }

    public ScoreEProp(int eNum, String pType, Constraint con, long boost) {
        super(eNum, pType, con);
        this.boost = boost;
    }

    public ScoreEProp(int eNum, String pType, Projection proj, long boost) {
        super(eNum, pType, proj);
        this.boost = boost;
    }

    @Override
    public ScoreEProp clone() {
        return clone(geteNum());
    }

    @Override
    public ScoreEProp clone(int eNum) {
        ScoreEProp clone = new ScoreEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    //endregion
    public long getBoost() {
        return boost;
    }
    //region Properties

    //region Fields
    private long boost;
    //endregion

}
