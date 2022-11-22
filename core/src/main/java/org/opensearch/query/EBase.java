package org.opensearch.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensearch.query.entity.EConcrete;
import org.opensearch.query.entity.ETyped;
import org.opensearch.query.entity.EUntyped;
import org.opensearch.query.optional.OptionalComp;
import org.opensearch.query.properties.*;
import org.opensearch.query.quant.Quant1;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Start", value = Start.class),
        @JsonSubTypes.Type(name = "EConcrete", value = EConcrete.class),
        @JsonSubTypes.Type(name = "EProp", value = EProp.class),
        @JsonSubTypes.Type(name = "EPropGroup", value = EPropGroup.class),
        @JsonSubTypes.Type(name = "ETyped", value = ETyped.class),
        @JsonSubTypes.Type(name = "EUntyped", value = EUntyped.class),
        @JsonSubTypes.Type(name = "Quant1", value = Quant1.class),
        @JsonSubTypes.Type(name = "Rel", value = Rel.class),
        @JsonSubTypes.Type(name = "RelPattern", value = RelPattern.class),
        @JsonSubTypes.Type(name = "RelProp", value = RelProp.class),
        @JsonSubTypes.Type(name = "RedundantRelProp", value = RedundantRelProp.class),
        @JsonSubTypes.Type(name = "RedundantSelectionRelProp", value = RedundantSelectionRelProp.class),
        @JsonSubTypes.Type(name = "RelPropGroup", value = RelPropGroup.class),
        @JsonSubTypes.Type(name = "OptionalComp", value = OptionalComp.class),
        @JsonSubTypes.Type(name = "SchematicEProp", value = SchematicEProp.class),
        @JsonSubTypes.Type(name = "FunctionRelProp", value = FunctionRelProp.class),
        @JsonSubTypes.Type(name = "FunctionEProp", value = FunctionEProp.class),
        @JsonSubTypes.Type(name = "SchematicRankedEProp", value = SchematicRankedEProp.class),
        @JsonSubTypes.Type(name = "SchematicNestedEProp", value = SchematicNestedEProp.class),
        @JsonSubTypes.Type(name = "ScoreEProp", value = ScoreEProp.class),
        @JsonSubTypes.Type(name = "NestedEProp", value = NestedEProp.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EBase {
    //region Constructor
    public EBase() {}

    public EBase(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Properties
    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EBase eBase = (EBase) o;

        return eNum == eBase.eNum;
    }

    public EBase clone() {
        return new EBase(eNum);
    }

    public EBase clone(int eNum) {
        return new EBase(eNum);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + this.geteNum() + ")";
    }

    @Override
    public int hashCode() {
        return this.eNum;
    }
    //endregion

    //region Fields
    private int eNum;
    //endregion

}
