package org.opensearch.schema.validation;


import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.validation.ValidationResult.ValidationResults;

import java.util.List;


/**
 * schema validation utility - this code will verify both logical & physical config is valid and in sync with each other
 * <p><br>
 *
 * Ontology-entities - checks
 * <br> - verify each top level entity has both ID & TYPE metadata fields
 * <br> - verify each top level entity all its properties
 * <br> - verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
 * <br> - verify all cascading fields appear in the properties
 * <br> - verify properties has a valid type (primitive or entity type)
 *
 * <p><br>
 * Ontology-relations - checks
 *  <br>- verify each top level relation has both ID & TYPE metadata fields
 *  <br>- verify each top level relation all its properties
 *  <br>- verify that if a top level relation has nested relations - these relations has top level representation in addition to the nesting
 *  <br>- verify all cascading fields appear in the properties
 *  <br>- verify all relation pairs has matching existing entities
 *
 * <p><br>
 * Index-provider - checks
 * <br> - verify all index entities has corresponding ontology entities
 * <br> - verify all index relations has corresponding ontology relations
 * <br> - verify relation redundant fields have correct type and legit entity side
 */
public class SchemaValidator {

    private List<ValidationRule> rules;

    public SchemaValidator() {
        this(List.of(
                new IDValidationRule(),
                new TypeValidationRule(),
                new PropertiesValidationRule(),
                new RelationsPairsValidationRule(),
                new RelationsForeignEntityDirectivesCorrelationValidationRule(),
                new RelationsNestedEntityDirectivesCorrelationValidationRule(),
                new IndexProviderTypesValidationRule(),
                new IndexProviderRelationsRedundantPropertiesValidationRule()
                ));
    }
    public SchemaValidator(List<ValidationRule> rules) {
        this.rules = rules;
    }

    /*
     * Ontology-entities / relations - checks
     *  verify each top level entity has both ID & TYPE metadata fields
     *  verify each top level entity all its properties
     *  verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
     *  verify all cascading fields appear in the properties
     *  verify properties has a valid type (primitive or entity type)
     */
    public ValidationResults validate(IndexProvider provider, Accessor accessor) {
        ValidationResults results = new ValidationResults();
        rules.forEach(r->r.validate(results,provider,accessor));
        return results;

         //todo  relations - entities interaction validations - verify directive are corresponding
         // verify that once an entity has a reference typed relationship for another entity - that other (referenced) entity must have an index of its own

    }
}
