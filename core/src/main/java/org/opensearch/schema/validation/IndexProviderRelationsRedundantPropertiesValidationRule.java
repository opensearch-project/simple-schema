package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

/**
 * validate all relations appearing in the index provider schema have redundant properties that correspond to actual properties in the ontology
 */
public class IndexProviderRelationsRedundantPropertiesValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {

        provider.getRelations()
                .forEach(r->r.getRedundant().stream()
                        .filter(rp-> accessor.pType(rp.getName()).isEmpty())
                        .forEach(rp->results.with(new ValidationResult(false, String.format("%s Redundant index %s relation property definition is missing from ontology",r.getType(),rp))))
                );

        return results.isValid();
    }
}
