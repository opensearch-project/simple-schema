package org.opensearch.languages.oql.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quant1 extends QuantBase {
    //region Constructors
    public Quant1() {
        super();
        this.next = Collections.emptyList();
    }

    public Quant1(int eNum, QuantType quantType) {
        this(eNum,quantType,Collections.emptyList());
    }

    public Quant1(int eNum, QuantType qType, Iterable<Integer> next) {
        this(eNum,qType,next,-1);
    }

    public Quant1(int eNum, QuantType qType, Iterable<Integer> next, int b) {
        super(eNum, qType);
        this.next = Stream.ofAll(next).toJavaList();
        this.b = b;
    }

    //endregion


    @Override
    public Quant1 clone() {
        return clone(geteNum());
    }

    @Override
    public Quant1 clone(int eNum) {
        final Quant1 clone = new Quant1();
        clone.seteNum(eNum);
        clone.setqType(getqType());
        return clone;
    }

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    public void addNext(int next) {
        this.next.add(next);
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Quant1 quant1 = (Quant1) o;

        if (b != quant1.b) return false;
        return next.equals(quant1.next);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + b;
        result = 31 * result + next.hashCode();
        return result;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    private List<Integer> next;
    //endregion
}
