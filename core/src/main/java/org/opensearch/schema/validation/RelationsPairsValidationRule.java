package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * validate all relations has correctly structured pairs with both sides of entities existing in the ontology
 */
public class RelationsPairsValidationRule implements ValidationRule {

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
