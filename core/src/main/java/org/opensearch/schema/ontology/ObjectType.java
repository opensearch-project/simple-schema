package org.opensearch.schema.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
/**
 * representing an object field for an entity or relation
 */
public class ObjectType extends PrimitiveType{

    public static ObjectType of(String name) {
        return new ObjectType(name);
    }

    ObjectType() {
        super();
    }
    public ObjectType(String type) {
        this(type,Object.class);
    }
    public ObjectType(String type, Class javaType) {
        super(type, javaType);
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    /**
     *   representing an objects array field for an entity or relation
     */
    public static class ArrayOfObjects extends ObjectType {
        public static ArrayOfObjects of(String name) {
            return new ArrayOfObjects(name);
        }

        ArrayOfObjects() {
            super();
        }

        public ArrayOfObjects(String type) {
            super(type);
        }

        public ArrayOfObjects(String type, Class javaType) {
            super(type, javaType);
        }

        @Override
        public boolean isArray() {
            return true;
        }
    }
}
