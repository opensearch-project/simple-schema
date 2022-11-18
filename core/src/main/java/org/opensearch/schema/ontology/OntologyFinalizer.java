package org.opensearch.schema.ontology;

public class OntologyFinalizer {
    public static final String STRING = "string"; //represents keyword

    public static final String ID_FIELD_PTYPE = "id";
    public static final String TYPE_FIELD_PTYPE = "type";

    public static final String ID_FIELD_NAME = "id";

    /**
     * verify mandatory fields ID,TYPE exist for all entities & relations on the ontology
     * generate nested fields for all entities & relations
     *
     * @param ontology
     * @return
     */
    public static Ontology finalize(Ontology ontology) {
        Accessor accessor = new Accessor(ontology);

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(ID_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(ID_FIELD_NAME).withPType(ID_FIELD_PTYPE)
                    .withType(new PrimitiveType(STRING,String.class))
                    .build());

        if (ontology.getProperties().stream().noneMatch(p -> p.getpType().equals(TYPE_FIELD_PTYPE)))
            ontology.getProperties().add(Property.Builder.get().withName(TYPE_FIELD_PTYPE)
                    .withPType(TYPE_FIELD_PTYPE).withType(new PrimitiveType(STRING,String.class))
                    .build());

        return ontology;
    }
}
