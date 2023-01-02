package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.opensearch.schema.ontology.OntologyFinalizer.ID_FIELD_PTYPE;

/**
 * validate all entities & relations has corresponding properties with the ones apperaing in the ontology
 */
public class PropertiesValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        // general entities/relations properties verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> accessor.pType(p).isEmpty())
                        .collect(Collectors.toList())
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s entity missing %s property definition",e.getName(), p))))
                );

        accessor.relations()
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> accessor.pType(p).isEmpty())
                        .collect(Collectors.toList())
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s relation missing %s property definition",e.getName(), p))))
                );

        return results.isValid();
    }
}
