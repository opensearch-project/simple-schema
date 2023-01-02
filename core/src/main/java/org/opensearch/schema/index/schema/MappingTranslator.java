package org.opensearch.schema.index.schema;

import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.CommonType;

import java.util.*;

public interface MappingTranslator<IN extends CommonType, OUT extends BaseTypeElement> {
    List<OUT> translate(IN t, MappingTranslatorContext<OUT> context);

    class MappingTranslatorContext<OUT> {
        private Accessor accessor;

        private Set<OUT> elements;

        public MappingTranslatorContext(Accessor accessor) {
            this.accessor = accessor;
            this.elements = new LinkedHashSet<>();
        }

        public OUT putElement(OUT value) {
            this.elements.add(value);
            return value;
        }

        public boolean contains(OUT element) {
            return this.elements.contains(element);
        }

        public Accessor getAccessor() {
            return accessor;
        }
    }
}
