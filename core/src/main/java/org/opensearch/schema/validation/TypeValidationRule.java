package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.schema.ontology.OntologyFinalizer.ID_FIELD_PTYPE;
import static org.opensearch.schema.ontology.OntologyFinalizer.TYPE_FIELD_PTYPE;

/**
 * validate all entities & relations has a Type field
 */
public class TypeValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        // type field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e -> e.containsProperty(TYPE_FIELD_PTYPE))
                .collect(Collectors.toList())
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing TYPE field", e.getName()))));

        accessor.relations().stream()
                .filter(r -> r.containsProperty(TYPE_FIELD_PTYPE))
                .collect(Collectors.toList())
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing TYPE field", r.getName()))));

        return results.isValid();
    }
}
