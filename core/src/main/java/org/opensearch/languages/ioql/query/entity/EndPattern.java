package org.opensearch.languages.ioql.query.entity;


import org.opensearch.languages.ioql.query.properties.EProp;
import org.opensearch.languages.ioql.query.EBase;

import java.util.List;

public interface EndPattern<T extends EBase> {
    T getEndEntity();
    List<EProp> getFilter();
}
