package org.opensearch.schema.ontology;

/**
 * Marker interface for a tagged element (Entity / Relation)
 */
public interface Tagged {
    String TAG_EVAL = "$:{}";

    static String tagSeq(String value) {
        return value + TAG_EVAL;
    }

    static boolean isSeq(Tagged value) {
        return value.geteTag().contains(TAG_EVAL);
    }

    static Tagged setSeq(int eNum,Tagged value) {
        value.seteTag(value.geteTag().replace("{}", Integer.toString(eNum)));
        return value;
    }

    String geteTag();

    void seteTag(String eTag);

}
