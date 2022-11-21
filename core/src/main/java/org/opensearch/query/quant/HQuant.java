package org.opensearch.query.quant;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HQuant extends QuantBase {
    private List<Integer> next;

    public List<Integer> getB() {
        return b;
    }

    public void setB(List<Integer> b) {
        this.b = b;
    }

    //region Fields
    private List<Integer> b;

    @Override
    public List<Integer> getNext() {
        return next;
    }

    @Override
    public void setNext(List<Integer> next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }
//endregion
}
