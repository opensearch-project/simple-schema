package org.opensearch.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.query.EBase;
import org.opensearch.schema.ontology.Container;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class QuantBase extends EBase implements Container<List<Integer>> {
    //region Constructors
    public QuantBase() {
        super();
    }

    public QuantBase(int eNum, QuantType qType) {
        super(eNum);
        this.qType = qType;
    }
    //endregion

    //region Properties
    public QuantType getqType() {
        return qType;
    }

    public void setqType(QuantType qType) {
        this.qType = qType;
    }
    //endregion

    //region Fields
    private QuantType qType;
    //endregion

}
