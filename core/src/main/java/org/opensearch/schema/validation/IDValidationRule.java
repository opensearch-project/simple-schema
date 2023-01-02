package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.schema.ontology.OntologyFinalizer.ID_FIELD_PTYPE;

/**
 * validate all entities & relations has an ID field
 */
public class IDValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        // ID field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e -> e.containsProperty(ID_FIELD_PTYPE))
                .collect(Collectors.toList())
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing ID field", e.getName()))));

        accessor.relations().stream()
                .filter(r -> r.containsProperty(ID_FIELD_PTYPE))
                .collect(Collectors.toList())
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing ID field", r.getName()))));

        return results.isValid();
    }
}
