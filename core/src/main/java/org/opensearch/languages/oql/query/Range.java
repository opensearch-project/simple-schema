package org.opensearch.languages.oql.query;

public class Range {
    //region Constructors
    public Range() {

    }

    public Range(long lower, long upper) {
        this.lower = lower;
        this.upper = upper;
    }
    //endregion

    //region Properties
    public long getUpper() {
        return upper;
    }

    public void setUpper(long upper) {
        this.upper = upper;
    }

    public long getLower() {
        return lower;
    }

    public void setLower(long lower) {
        this.lower = lower;
    }
    //endregion

    //region Fields
    private long upper;
    private long lower;

    //endregion
    public static class StatefulRange {
        private Range range;
        private long index;

        public StatefulRange(Range range) {
            this.range = range;
            this.index = range.lower;
        }

        public long current() {
            return index;
        }

        public long next() {
            if(index>=range.upper)
                return -1;
            return index++;
        }

        public boolean hasNext() {
            return index<range.upper;
        }
    }
}
