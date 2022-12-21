package org.opensearch.schema.index.schema;

import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.CommonType;

public interface MappingTranslator<IN extends CommonType,OUT extends BaseTypeElement> {
    OUT translate(IN t,MappingTranslatorContext context);

    class MappingTranslatorContext {
        private Accessor accessor;

        public MappingTranslatorContext(Accessor accessor) {
            this.accessor = accessor;
        }

        public Accessor getAccessor() {
            return accessor;
        }
    }
}
