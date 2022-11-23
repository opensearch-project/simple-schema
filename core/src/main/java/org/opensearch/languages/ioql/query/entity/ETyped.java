package org.opensearch.languages.ioql.query.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.EBase;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed.eTyped {
    //region Constructors
    public ETyped() {}

    public ETyped(int eNum, String eTag, String eType, int next) {
        this(eNum,eTag,eType,next,-1);
    }

    public ETyped(int eNum, String eTag, String eType, int next, int b) {
        super(eNum, eTag, next, b);
        this.eType = eType;
    }

    public ETyped(EEntityBase base,String eType) {
        this(base.geteNum(),base.geteTag(),eType,base.getNext());
    }

    //endregion

    //region Properties
    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ETyped eTyped = (ETyped) o;

        return eType.equals(eTyped.eType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType.hashCode();
        return result;
    }

    @Override
    public String getTyped() {
        return geteType();
    }

    @Override
    public String[] getParentTyped() {
        return parentType;
    }

    public void setParentType(String[] parentType) {
        this.parentType = parentType;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public ETyped clone(int eNum) {
        return propClone(eNum, new ETyped());
    }

    protected ETyped propClone(int eNum, ETyped clone) {
        clone.seteNum(eNum);
        clone.seteTag(geteTag());
        clone.eType = eType;
        return clone;
    }
    //endregion

    //region Fields
    private String	eType;
    private String[]	parentType;
    //endregion
}
