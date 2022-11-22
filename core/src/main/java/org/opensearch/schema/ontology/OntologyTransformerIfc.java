package org.opensearch.schema.ontology;

import java.io.InputStream;

/**
 * interface for transforming the ontology schema into another type of schema representation specification
 * @param <OntIn>
 * @param <OntOut>
 */
public interface OntologyTransformerIfc<OntIn,OntOut> {
    OntOut transform(String ontologyName, OntIn source);
    OntOut transform(String ontologyName, InputStream ... source);
    OntIn translate(OntOut source);
}
