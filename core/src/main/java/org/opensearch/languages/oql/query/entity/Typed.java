package org.opensearch.languages.oql.query.entity;

public interface Typed {

    String getTyped();

    String[] getParentTyped();

    interface eTyped extends Typed{
        void seteType(String eType);

        String geteType();
    }

    interface rTyped extends Typed{
        void setrType(String rType);

        String getrType();
    }
}
