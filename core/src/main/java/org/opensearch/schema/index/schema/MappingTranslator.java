package org.opensearch.schema.index.schema;

import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.CommonType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MappingTranslator<IN extends CommonType,OUT extends BaseTypeElement> {
    List<OUT> translate(IN t, MappingTranslatorContext context);

    class MappingTranslatorContext<OUT> {
        private Accessor accessor;

        private Map<String,OUT> elements;
        public MappingTranslatorContext(Accessor accessor) {
            this.accessor = accessor;
            this.elements = new LinkedHashMap<>();
        }

        public void putElement(String name,OUT value) {
            this.elements.put(name,value);
        }

        public Optional<OUT> getElement(String name) {
            return Optional.ofNullable(this.elements.get(name));
        }

        public Accessor getAccessor() {
            return accessor;
        }
    }
}
