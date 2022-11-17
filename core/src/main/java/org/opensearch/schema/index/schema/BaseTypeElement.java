package org.opensearch.schema.index.schema;


import java.util.Map;

/**
 * the common basic physical index mapping and storage representation
 * @param <T>
 */
public interface BaseTypeElement<T> {
    Map<String, T> getNested();

    Props getProps();

    MappingIndexType getMapping();

    NestingType getNesting();

    String getType();
}
