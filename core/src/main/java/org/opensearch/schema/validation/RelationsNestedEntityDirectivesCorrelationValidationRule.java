package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.RelationshipType;

/**
 * validate all relations has correlating directives according to the entities they are connecting
 * For example - Author->[has_Books]-Book relation -
 * <br>
 * if the relation directive states any nested type (embedded / child / nested ) it must dictate that the container entity mapping must be @model (STATIC) and the inner entity must not...
 * <br>
 * see docs/physical-mapping.md for additional explanation
 */
public class RelationsNestedEntityDirectivesCorrelationValidationRule implements ValidationRule {

    @Override
    public boolean validate(ValidationResult.ValidationResults results, IndexProvider provider, Accessor accessor) {
        accessor.relations()
                .forEach(r -> {
                    if (validateRelation(accessor, r))
                        results.with(new ValidationResult(false,
                                String.format("%s relation pair sideA type is not model as expected from the relationship REFERENCE directive", r.getrType())));
                });
        return results.isValid();
    }

    private boolean validateRelation(Accessor accessor, RelationshipType relationshipType) {
        //todo implement
        return false;
    }

}
