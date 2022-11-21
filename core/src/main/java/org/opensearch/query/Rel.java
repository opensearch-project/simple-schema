package org.opensearch.query;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensearch.query.entity.Typed;
import org.opensearch.schema.ontology.Below;
import org.opensearch.schema.ontology.Next;
import org.opensearch.schema.ontology.Tagged;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "RelUntyped", value = RelUntyped.class),
        @JsonSubTypes.Type(name = "RelPattern", value = RelPattern.class)})
public class Rel extends EBase implements Next<Integer>, Below<Integer>, Typed.rTyped, Tagged {

    @Override
    public String getTyped() {
        return getrType();
    }

    @Override
    public String geteTag() {
        return getWrapper();
    }

    @Override
    public void seteTag(String eTag) {
        this.setWrapper(eTag);
    }

    public enum Direction {
        R,
        L,
        RL;

        public Direction reverse() {
            if (this == RL)
                return R;
            return L == this ? R : L;
        }
    }

    //region Constructors
    public Rel() {
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, int next) {
        this(eNum,rType,dir,wrapper,next,-1);
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, int next, int b) {
        super(eNum);
        this.rType = rType;
        this.dir = dir;
        this.wrapper = wrapper;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Properties
    @Override
    public String[] getParentTyped() {
        return parentType;
    }

    public void setParentType(String[] parentType) {
        this.parentType = parentType;
    }

    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
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

    //region Override Methods
    @Override
    public Rel clone() {
        return clone(geteNum());
    }

    @Override
    public Rel clone(int eNum) {
        return new Rel(eNum,getrType(),getDir(),getWrapper(),getNext(),getB());
    }
    //endregion

    //region Fields
    private String rType;
    private String[] parentType;

    private Direction dir;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String wrapper;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rel rel = (Rel) o;

        if (!rType.equals(rel.rType)) return false;
        if (next != rel.next) return false;
        if (b != rel.b) return false;
        if (dir != rel.dir) return false;
        return wrapper != null ? wrapper.equals(rel.wrapper) : rel.wrapper == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rType.hashCode();
        result = 31 * result + dir.hashCode();
        result = 31 * result + (wrapper != null ? wrapper.hashCode() : 0);
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
