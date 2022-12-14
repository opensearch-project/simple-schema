package org.opensearch.schema.index.transform;

import javaslang.Tuple2;

import java.util.List;

/**
 * Index generator interfaces for the ontology (logical) schema
 */
public interface OntologyIndexGenerator {

    class IndexSchemaConfig {
        public static final String ID = "id";
        public static final String TYPE = "type";
        public static final String PROPERTIES = "properties";
        public static final String NESTED = "nested";

    }
    class ProjectionConfigs {
        public static final String PROJECTION = "projection";
        public static final String TAG = "tag";
        public static final String QUERY_ID = "queryId";
        public static final String CURSOR_ID = "cursorId";
        public static final String EXECUTION_TIME = "timestamp";

    }

    class EdgeSchemaConfig {
        public static String DIRECTION = "direction";

        public static String SOURCE = "entityA";
        public static String SOURCE_ID = "entityA.id";
        public static String SOURCE_TYPE = "entityA.type";
        public static String SOURCE_NAME = "entityA.name";

        public static String DEST = "entityB";//formally was target
        public static String DEST_ID = "entityB.id";//formally was target.id
        public static String DEST_TYPE = "entityB.type";//formally was target.type
        public static String DEST_NAME = "entityB.name";//formally was target.name
    }
}
