package org.opensearch.languages.oql.query;


import org.opensearch.languages.oql.query.quant.QuantType;
import org.opensearch.schema.ontology.Next;

public interface Container<T> extends Next<T> {
    QuantType getqType();
}
