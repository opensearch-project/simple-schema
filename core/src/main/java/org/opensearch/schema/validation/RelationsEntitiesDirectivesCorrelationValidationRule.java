package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

/**
 * validate all relations has correlating directives according to the entities they are connecting
 * For example - Author->[has_Books]-Book relation - if the relation directive states REFERENCE it must dictate that the Book entity mapping
 * must be STATIC - since relationship can't reference the same index (as the source of the relationship)
 *
 */
public class RelationsEntitiesDirectivesCorrelationValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        accessor.relations()
                .forEach(r -> r.getePairs()
                        .forEach(pair -> {
                            if(accessor.entity(pair.geteTypeA()).isEmpty())
                                results.with(new ValidationResult(false, String.format("%s relation pair sideA %s type is missing from ontology",r.getrType(),pair.geteTypeA())));
                            if(accessor.entity(pair.geteTypeB()).isEmpty())
                                results.with(new ValidationResult(false, String.format("%s relation pair sideB %s type is missing from ontology",r.getrType(),pair.geteTypeB())));
                        })
                );
        return results.isValid();
    }
}
