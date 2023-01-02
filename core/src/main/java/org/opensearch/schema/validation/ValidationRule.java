package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

/**
 * interface for a general schema validation rule
 */
public interface ValidationRule {
    boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor);
}
