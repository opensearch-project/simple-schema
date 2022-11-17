package org.opensearch.schema.index.indexPartitions;


import java.util.Date;

public interface TimeSeriesIndexPartitions extends IndexPartitions {
    String getDateFormat();
    String getIndexPrefix();
    String getIndexFormat();
    String getTimeField();
    String getIndexName(Date date);
}
