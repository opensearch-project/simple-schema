package org.opensearch.languages.oql.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.oql.query.EBase;
import org.opensearch.languages.oql.query.Container;

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
