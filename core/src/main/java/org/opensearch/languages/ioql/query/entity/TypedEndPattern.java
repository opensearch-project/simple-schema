package org.opensearch.languages.ioql.query.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.languages.ioql.query.properties.EProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TypedEndPattern<T extends ETyped> extends ETyped implements EndPattern<T> {
    private List<EProp> filter = new ArrayList<>();
    private T endEntity;

    //region Constructors
    public TypedEndPattern() {
    }

    public TypedEndPattern(T endEntity) {
        this(endEntity, new ArrayList<>());
    }

    public TypedEndPattern(T endEntity, List<EProp> filter) {
        super(endEntity.geteNum(), endEntity.geteTag(), endEntity.geteType(), endEntity.getNext(), endEntity.getB());
        this.endEntity = endEntity;
        this.filter = filter;
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
    public void seteType(String eType) {
        super.seteType(eType);
        if(Objects.nonNull(endEntity))
            endEntity.seteType(eType);
    }

    @Override
    public void setParentType(String[] parentType) {
        super.setParentType(parentType);
        if(Objects.nonNull(endEntity))
            endEntity.setParentType(parentType);
    }

    @Override
    public TypedEndPattern<T> clone() {
        return new TypedEndPattern<>((T) getEndEntity().clone(), getFilter());
    }

    @Override
    public TypedEndPattern<T> clone(int eNum) {
        return new TypedEndPattern<>((T) getEndEntity().clone(eNum), getFilter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedEndPattern)) return false;
        if (!super.equals(o)) return false;
        TypedEndPattern<?> that = (TypedEndPattern<?>) o;
        return Objects.equals(endEntity, that.endEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endEntity);
    }
}
