package org.opensearch.languages.oql.query.entity;


import org.opensearch.languages.oql.query.properties.EProp;
import org.opensearch.languages.oql.query.EBase;

import java.util.List;

public interface EndPattern<T extends EBase> {
    T getEndEntity();
    List<EProp> getFilter();
}
