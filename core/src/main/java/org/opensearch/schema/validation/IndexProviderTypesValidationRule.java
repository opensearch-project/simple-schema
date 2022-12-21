package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * validate all types appearing in the index provider schema actually exist in the ontology as either entities / relations
 */
public class IndexProviderTypesValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        /*
         *        Index-provider - checks
         *             verify all index entities has corresponding ontology entities
         *             verify all index relations has corresponding ontology relations
         *             verify all nested index entities / relations has corresponding top level index definitions as embedded
         *             verify relation redundant fields have correct type and legit entity side
         */

        provider.getEntities().stream()
                .filter(i-> accessor.entity(i.getType().getName()).isEmpty())
                .forEach(i->results.with(new ValidationResult(false, String.format("%s entity index definition is missing from ontology",i.getType().getName()))));

        provider.getRelations().stream()
                .filter(i-> accessor.relation(i.getType().getName()).isEmpty())
                .forEach(i->results.with(new ValidationResult(false, String.format("%s relation index definition is missing from ontology",i.getType().getName()))));

        return results.isValid();
    }
}
