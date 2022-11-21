package org.opensearch.query.properties;



import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.query.properties.constraint.Constraint;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicRankedEProp extends SchematicEProp implements RankingProp {

    public SchematicRankedEProp() {}

    public SchematicRankedEProp(int eNum, String pType, String schematicName, Constraint con, long boost) {
        super(eNum, pType, schematicName, con);
        this.boost = boost;
    }

    public SchematicRankedEProp(SchematicEProp schematicEProp, long boost) {
        this(schematicEProp.geteNum(), schematicEProp.getpType(), schematicEProp.getSchematicName(), schematicEProp.getCon(), boost);
    }


    @Override
    public SchematicRankedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicRankedEProp clone(int eNum) {
        SchematicRankedEProp clone = new SchematicRankedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        clone.boost = boost;
        return clone;
    }

    @Override
    public long getBoost() {
        return boost;
    }

    private long boost;
}
