package org.opensearch.languages.ioql.query;


import org.opensearch.languages.ioql.query.quant.QuantType;
import org.opensearch.schema.ontology.Next;

public interface Container<T> extends Next<T> {
    QuantType getqType();
}
