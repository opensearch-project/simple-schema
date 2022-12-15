package org.opensearch.languages.oql.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.oql.query.EBase;
import org.opensearch.schema.ontology.Below;
import org.opensearch.schema.ontology.Next;
import org.opensearch.schema.ontology.Tagged;

public abstract class EEntityBase extends EBase implements Next<Integer>, Below<Integer>, Tagged {
    //region Constructors
    public EEntityBase() {
    }

    public EEntityBase(int eNum, String eTag, int next, int b) {
        super(eNum);

        this.eTag = eTag;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EEntityBase that = (EEntityBase) o;
        if (eTag == null) {
            if (that.eTag != null)
                return false;
        } else {
            if (!eTag.equals(that.eTag)) return false;
        }
        if (next != that.next) return false;
        if (b != that.b) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + next;
        result = 31 * result + b;

        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        return result;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public EBase clone(int eNum) {
        return super.clone(eNum);
    }
    //endregion

    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return next > -1;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private String eTag;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next = -1;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion
}
