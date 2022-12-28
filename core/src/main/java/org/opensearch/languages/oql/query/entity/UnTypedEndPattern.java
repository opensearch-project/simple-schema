package org.opensearch.languages.oql.query.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.oql.query.properties.EProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UnTypedEndPattern<T extends EUntyped> extends EUntyped implements EndPattern<T> {
    private List<EProp> filter = new ArrayList<>();
    private T endEntity;

    //region Constructors
    public UnTypedEndPattern() {}

    public UnTypedEndPattern(T endEntity) {
        this(endEntity,new ArrayList<>());
    }

    public UnTypedEndPattern(T endEntity, List<EProp> filter) {
        super(endEntity.geteNum(),endEntity.geteTag(),endEntity.getNext(),endEntity.getB());
        this.endEntity = endEntity;
        this.filter = filter;
    }

    @Override
    public void setvTypes(Set<String> vTypes) {
        super.setvTypes(vTypes);
        if(Objects.nonNull(endEntity))
            endEntity.setvTypes(vTypes);
    }

    @Override
    public void setNvTypes(Set<String> nvTypes) {
        super.setNvTypes(nvTypes);
        if(Objects.nonNull(endEntity))
            endEntity.setNvTypes(nvTypes);
    }

    public T getEndEntity() {
        return endEntity;
    }

    @JsonIgnore
    public void seteTag(String eTag) {
        this.endEntity.seteTag(eTag);
    }

    @Override
    @JsonIgnore
    public String geteTag() {
        return this.endEntity.geteTag();
    }

    public List<EProp> getFilter() {
        return filter;
    }

    @Override
    public UnTypedEndPattern<T> clone() {
        return new UnTypedEndPattern<>((T) getEndEntity().clone(),getFilter());
    }

    @Override
    public UnTypedEndPattern<T > clone(int eNum) {
        return new UnTypedEndPattern<>((T) getEndEntity().clone(eNum),getFilter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnTypedEndPattern)) return false;
        if (!super.equals(o)) return false;
        UnTypedEndPattern<?> that = (UnTypedEndPattern<?>) o;
        return Objects.equals(endEntity, that.endEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endEntity);
    }
}
