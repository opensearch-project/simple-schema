package org.opensearch.schema.ontology;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.schema.index.schema.MappingIndexType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.opensearch.schema.ontology.EPair.RelationReferenceType.ONE_TO_MANY;
import static org.opensearch.schema.ontology.EPair.RelationReferenceType.ONE_TO_ONE;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Epair represents the connection between two entities in the ontology
 * - TypeA states the side-A entity type
 * - sideAIdField states the side-A related (FK) field name as it appears in the connecting table
 * (the actual field name on the Side-A entity if stated according to that entity's own fieldID - PK )
 * - TypeB states the side-B entity type
 * - sideBIdField states the side-B related(FK)  field name as it appears in the connecting table
 * (the actual field name on the Side-B entity if stated according to that entity's own fieldID - PK )
 */
public class EPair {
    /**
     * describes the nature of the relationship
     */
    public enum RelationReferenceType {
        ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY
    }

    /**
     * relationship name formatting method
     * @param eTypeA
     * @param eTypeB
     * @return
     */
    public static String formatName(String eTypeA, String eTypeB) {
        return String.format("%s->%s", eTypeA, eTypeB);
    }

    /**
     * DO-NOT-REMOVE - @Jackson required
     */
    public EPair() {
    }

    public EPair(String eTypeA, String eTypeB) {
        this(formatName(eTypeA, eTypeB), eTypeA, eTypeB);
    }


    public EPair(String name, String eTypeA, String eTypeB) {
        this(new ArrayList<>(),name,ONE_TO_MANY,eTypeA,null,eTypeB,null,null);
    }

    public EPair(List<DirectiveType> directives, String eTypeA, RelationReferenceType referenceType, String sideAFieldName, String sideAIdField, String eTypeB, String sideBIdField) {
        this(directives, formatName(eTypeA, eTypeB), referenceType, eTypeA, sideAFieldName, sideAIdField, eTypeB, sideBIdField);
    }

    public EPair(String name, RelationReferenceType referenceType, String eTypeA, String sideAFieldName, String sideAIdField, String eTypeB, String sideBIdField) {
        this(new ArrayList<>(), name, referenceType, eTypeA, sideAFieldName, sideAIdField, eTypeB, sideBIdField);
    }

    public EPair(List<DirectiveType> directives, String name, RelationReferenceType referenceType, String eTypeA, String sideAFieldName, String sideAIdField, String eTypeB, String sideBIdField) {
        this.directives = directives;
        this.referenceType = referenceType;
        this.name = name;
        this.eTypeA = eTypeA;
        this.sideAFieldName = sideAFieldName;
        this.sideAIdField = sideAIdField;
        this.eTypeB = eTypeB;
        this.sideBIdField = sideBIdField;
    }

    public List<DirectiveType> getDirectives() {
        return directives;
    }

    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String geteTypeA() {
        return eTypeA;
    }

    public void seteTypeA(String eTypeA) {
        this.eTypeA = eTypeA;
    }

    public String geteTypeB() {
        return eTypeB;
    }

    public void seteTypeB(String eTypeB) {
        this.eTypeB = eTypeB;
    }

    public String getSideAIdField() {
        return sideAIdField;
    }

    public void setSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
    }

    public String getSideBIdField() {
        return sideBIdField;
    }

    public void setSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
    }

    public String getSideAFieldName() {
        return sideAFieldName;
    }

    public void setSideAFieldName(String sideAFieldName) {
        this.sideAFieldName = sideAFieldName;
    }

    public MappingIndexType getMappingTypeSideA() {
        return mappingTypeSideA;
    }

    public void setMappingTypeSideA(MappingIndexType mappingTypeSideA) {
        this.mappingTypeSideA = mappingTypeSideA;
    }

    public MappingIndexType getMappingTypeSideB() {
        return mappingTypeSideB;
    }

    public void setMappingTypeSideB(MappingIndexType mappingTypeSideB) {
        this.mappingTypeSideB = mappingTypeSideB;
    }

    public RelationReferenceType getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(RelationReferenceType referenceType) {
        this.referenceType = referenceType;
    }

    @JsonIgnore
    public EPair withSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
        return this;
    }

    @JsonIgnore
    public EPair withSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EPair ePair = (EPair) o;
        return
                Objects.equals(name, ePair.name) &&
                        Objects.equals(directives, ePair.directives) &&
                        Objects.equals(referenceType, ePair.referenceType) &&
                        Objects.equals(eTypeA, ePair.eTypeA) &&
                        Objects.equals(mappingTypeSideA, ePair.mappingTypeSideA) &
                                Objects.equals(mappingTypeSideB, ePair.mappingTypeSideB) &
                                Objects.equals(sideAFieldName, ePair.sideAFieldName) &
                                Objects.equals(sideAIdField, ePair.sideAIdField) &
                                Objects.equals(eTypeB, ePair.eTypeB) &
                                Objects.equals(sideBIdField, ePair.sideBIdField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, referenceType, eTypeA, sideAFieldName, sideAIdField, eTypeB, sideBIdField, mappingTypeSideA, mappingTypeSideB, directives);
    }

    @Override
    public String toString() {
        return "EPair [name= " + name + ",referenceType= " + referenceType + ",eTypeA= " + eTypeA + ",sideAId= " + sideAIdField + ",sideAField= " + sideAFieldName + ",mappingTypeSideA= " + mappingTypeSideA + ", eTypeB = " + eTypeB + ", sideBId = " + sideBIdField + ", mappingTypeSideB = " + mappingTypeSideB + ", directives = " + directives + "]";
    }

    @Override
    public EPair clone() {
        return new EPair(directives, name, referenceType, eTypeA, sideAFieldName, sideAIdField, eTypeB, sideBIdField);
    }

    private RelationReferenceType referenceType;
    //region Fields
    private String name;
    private String eTypeA;

    private MappingIndexType mappingTypeSideA;

    protected List<DirectiveType> directives = new ArrayList<>();

    private MappingIndexType mappingTypeSideB;
    private String sideAFieldName = "field_name";
    private String sideAIdField = "source_id";
    private String eTypeB;
    private String sideBIdField = "dest_id";

    //endregion

}
