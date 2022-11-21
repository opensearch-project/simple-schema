package org.opensearch.schema.ontology;


import org.opensearch.query.quant.QuantType;

public interface Container<T> extends Next<T> {
    QuantType getqType();
}
