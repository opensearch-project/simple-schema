package org.opensearch.schema.validation;

import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.RelationshipType;

/**
 * validate all relations has correlating directives according to the entities they are connecting
 * For example - Author->[has_Books]-Book relation -
 * <br>
 * if the relation directive states JOIN_INDEX_FOREIGN it must dictate that the both entities mapping must be @model (STATIC) - since JOIN_INDEX_FOREIGN relationship can't reference the same index (as the source of the relationship)
 * <br>
 * see docs/physical-mapping.md for additional explanation
 */
public class RelationsJoinIndexForeignEntityDirectivesCorrelationValidationRule implements ValidationRule {

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
        if (accessor.isForeignRelation(relationshipType)) {
            //validate the target entity side of the relationship is actually a @model directive - it has a dedicated index (STATIC)
            return relationshipType.getePairs().stream()
                    .filter(accessor::isForeignRelation)
                    .anyMatch(ePair -> !accessor.isModel(ePair.geteTypeB()));
        }
        return true;
    }

}
