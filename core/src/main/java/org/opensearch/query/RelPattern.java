package org.opensearch.query;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelPattern extends Rel{
    private Range length;

    public RelPattern() {
    }

    public RelPattern(int eNum, String rType,Range length, Direction dir) {
        super(eNum, rType, dir, null, 0 );
        this.length = length;
    }
    public RelPattern(int eNum, String rType,Range length, Direction dir, String wrapper, int next) {
        super(eNum, rType, dir, wrapper, next);
        this.length = length;
    }

    public RelPattern(int eNum, String rType,Range length, Direction dir, String wrapper, int next, int b) {
        super(eNum, rType, dir, wrapper, next, b);
        this.length = length;
    }

    public void setLength(Range length) {
        this.length = length;
    }

    public Range getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelPattern)) return false;
        if (!super.equals(o)) return false;
        RelPattern that = (RelPattern) o;
        return Objects.equals(length, that.length);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), length);
    }
}
