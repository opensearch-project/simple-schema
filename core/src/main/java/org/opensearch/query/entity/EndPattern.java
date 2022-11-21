package org.opensearch.query.entity;


import org.opensearch.query.EBase;
import org.opensearch.query.properties.EProp;

import java.util.List;

public interface EndPattern<T extends EBase> {
    T getEndEntity();
    List<EProp> getFilter();
}
