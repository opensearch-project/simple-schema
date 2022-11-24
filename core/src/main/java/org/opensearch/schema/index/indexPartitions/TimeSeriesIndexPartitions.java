package org.opensearch.schema.index.indexPartitions;


import java.util.Date;

/*
 * Index Partitioning represents the concept of a time based partition key - which each partition spans across a time window
 */
public interface TimeSeriesIndexPartitions extends IndexPartitions {
    String getDateFormat();
    String getIndexPrefix();
    String getIndexFormat();
    String getTimeField();
    String getIndexName(Date date);
}
