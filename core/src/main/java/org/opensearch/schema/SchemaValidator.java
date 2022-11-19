package org.opensearch.schema;


import org.opensearch.schema.index.schema.IndexProvider;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.validation.ValidationResult;
import org.opensearch.schema.validation.ValidationResult.ValidationResults;

import java.util.stream.StreamSupport;

import static org.opensearch.schema.ontology.OntologyFinalizer.ID_FIELD_PTYPE;
import static org.opensearch.schema.ontology.OntologyFinalizer.TYPE_FIELD_PTYPE;


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

    public ValidationResults validate(IndexProvider provider, Accessor accessor) {
        ValidationResults results = new ValidationResults();

        /*
         * Ontology-entities / relations - checks
         *  verify each top level entity has both ID & TYPE metadata fields
         *  verify each top level entity all its properties
         *  verify that if a top level entity has nested entities - these entities has top level representation in addition to the nesting
         *  verify all cascading fields appear in the properties
         *  verify properties has a valid type (primitive or entity type)
         */

        // ID field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e -> e.containsProperty(ID_FIELD_PTYPE)).toList()
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing ID field", e.getName()))));

        accessor.relations().stream()
                .filter(r -> r.containsProperty(ID_FIELD_PTYPE)).toList()
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing ID field", r.getName()))));

        // type field verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .filter(e -> e.containsProperty(TYPE_FIELD_PTYPE)).toList()
                .forEach(e -> results.with(new ValidationResult(false, String.format("%s entity missing TYPE field", e.getName()))));

        accessor.relations().stream()
                .filter(r -> r.containsProperty(TYPE_FIELD_PTYPE)).toList()
                .forEach(r -> results.with(new ValidationResult(false, String.format("%s relation missing TYPE field", r.getName()))));

        // general entities/relations properties verification
        StreamSupport.stream(accessor.entities().spliterator(), false)
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> accessor.pType(p).isEmpty()).toList()
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s entity missing %s property definition",e.getName(), p))))
                );

        accessor.relations()
                .forEach(e -> e.getProperties()
                        .stream().filter(p -> accessor.pType(p).isEmpty()).toList()
                        .forEach(p -> results.with(new ValidationResult(false, String.format("%s relation missing %s property definition",e.getName(), p))))
                );


         accessor.relations()
                .forEach(r -> r.getePairs()
                    .forEach(pair -> {
                        if(accessor.entity(pair.geteTypeA()).isEmpty())
                            results.with(new ValidationResult(false, String.format("%s relation pair sideA %s type is missing from ontology",r.getrType(),pair.geteTypeA())));
                        if(accessor.entity(pair.geteTypeB()).isEmpty())
                            results.with(new ValidationResult(false, String.format("%s relation pair sideB %s type is missing from ontology",r.getrType(),pair.geteTypeB())));
                        })
                );

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


        provider.getRelations()
                .forEach(r->r.getRedundant().stream()
                        .filter(rp-> accessor.pType(rp.getName()).isEmpty())
                        .forEach(rp->results.with(new ValidationResult(false, String.format("%s Redundant index %s relation property definition is missing from ontology",r.getType(),rp))))
                );

        return results;
    }
}
