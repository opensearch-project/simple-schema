package org.opensearch.languages.oql.graphql.wiring.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.*;
import javaslang.Tuple2;
import org.opensearch.graphql.wiring.InputTypeConstraint;
import org.opensearch.graphql.wiring.InputTypeWhereClause;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.QueryTranslationStrategy.QueryTranslatorContext;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.languages.oql.query.Rel;
import org.opensearch.languages.oql.query.properties.constraint.Constraint;
import org.opensearch.languages.oql.query.properties.constraint.ConstraintOp;
import org.opensearch.languages.oql.query.quant.QuantBase;
import org.opensearch.languages.oql.query.quant.QuantType;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Property;
import org.opensearch.schema.ontology.RelationshipType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TranslationUtils {
    public static final String WHERE = "where";
    public static final String QUERY = "query";
    public static final ObjectMapper mapper = new ObjectMapper();

    /**
     * get concrete friend type
     *
     * @param fieldType
     * @return
     */
    public static GraphQLType extractConcreteFieldType(GraphQLType fieldType) {
        //list to wrapping type
        if (fieldType instanceof GraphQLList) {
            //inner field type
            fieldType = ((GraphQLList) fieldType).getWrappedType();
        }
        //non-null to wrapping type
        if (fieldType instanceof GraphQLNonNull) {
            //inner field type
            fieldType = ((GraphQLNonNull) fieldType).getWrappedType();
            //list to wrapping type
            if (fieldType instanceof GraphQLList) {
                //inner field type
                fieldType = ((GraphQLList) fieldType).getWrappedType();
            }
        }
        return fieldType;
    }

    /**
     * verify whether this definition is a GQL parent entity type
     * @param parentField
     * @return
     */
    public static boolean isParentObjectType(GraphQLFieldDefinition parentField) {
        return (extractConcreteFieldType(parentField.getType()) instanceof GraphQLInterfaceType) ||
                (extractConcreteFieldType(parentField.getType()) instanceof GraphQLObjectType);
    }

    /**
     * translate where operator into the appropriate Quant operator type
     * @param operator
     * @return
     */
    public static QuantType asQuantType(InputTypeWhereClause.WhereOperator operator) {
        switch (operator) {
            case AND:
                return QuantType.all;
            case OR:
                return QuantType.some;
            default:
                return QuantType.all;
        }
    }

    /**
     * traverse the current type name into the appropriate path location in the query builder tree
     * @param context
     * @param typeName
     * @return
     */
    public static Optional<EntityType> populateGraphObject(QueryTranslatorContext<Query.Builder> context, String typeName) {

        //pop to the correct index according to path
        if (context.getPathContext().containsKey(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            context.getBuilder().currentIndex(context.getPathContext().get(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }

        if (context.getAccessor().relation(context.getEnv().getField().getName()).isPresent()) {
            //relation
            RelationshipType relationshipType = context.getAccessor().relation$(context.getEnv().getField().getName());
            context.getBuilder().rel(relationshipType.getrType(), Rel.Direction.R, context.getEnv().getField().getName());
            //right after will be the vertex
            EntityType entityType = context.getAccessor().entity$(typeName);
            context.getBuilder().eType(entityType.geteType(), typeName);
            context.getPathContext().put(context.getEnv().getExecutionStepInfo().getPath().getPathWithoutListEnd().toString(),
                    context.getBuilder().currentIndex());
            return Optional.of(entityType);
        } else if (context.getAccessor().entity(typeName).isPresent()) {
            EntityType entityType = context.getAccessor().entity$(typeName);
            context.getBuilder().eType(entityType.geteType(), typeName);
            context.getPathContext().put(context.getEnv().getExecutionStepInfo().getPath().getPathWithoutListEnd().toString(),
                    context.getBuilder().currentIndex());
            return Optional.of(entityType);
        }
        return Optional.empty();
    }

    /**
     * adds a where clause using the Ontology Query Constraints operator and components
     * @param context
     * @param realType
     * @throws IOException
     */
    public static void addWhereClause(QueryTranslatorContext<Query.Builder> context, Optional<EntityType> realType) throws Throwable {
        //arguments
        if (realType.isPresent() && context.getEnv().getArguments().containsKey(WHERE)) {
            Object argument = context.getEnv().getArguments().get(WHERE);
            InputTypeWhereClause whereClause = mapper.readValue(mapper.writeValueAsString(argument), InputTypeWhereClause.class);
            //verify fields exist within entity type
            List<InputTypeConstraint> nonFoundFields = whereClause.getConstraints().stream()
                    .filter(c -> !realType.get().containsProperty(c.getOperand()))
                    .collect(Collectors.toList());

            if (!nonFoundFields.isEmpty())
                throw new IllegalArgumentException("Fields " + nonFoundFields + " are not a part of the queried entity " + realType.get().getName());

            //build the where clause query
            List<Tuple2<String, Optional<Constraint>>> constraints = whereClause.getConstraints().stream()
                    .map(c -> new Tuple2<>(c.getOperand(),
                            Optional.of(new Constraint(ConstraintOp.valueOf(c.getOperator()), c.getExpression()))))
                    .collect(Collectors.toList());


            //if no quant exists - add one
            if (context.getBuilder().pop(eBase -> eBase instanceof QuantBase).isEmpty()) {
                context.getBuilder().quant(QuantType.all);
            }

            //add to group
            context.getBuilder().ePropGroup(constraints, asQuantType(whereClause.getOperator()));

        }
    }


    /**
     * populates the property graph value
     * @param context
     * @return
     */
    public static String populateGraphValue(QueryTranslatorContext<Query.Builder> context) {
        //pop to the correct index according to path
        if (context.getPathContext().containsKey(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            context.getBuilder().currentIndex(context.getPathContext().get(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }
        String name = context.getEnv().getField().getName();
        Property property = context.getAccessor().pName$(name);
        context.getBuilder().eProp(property.getpType());
        return name;
    }

    public static Object fakeEnumValue(String fieldName, GraphQLEnumType enumType) {
        return fieldName;
    }


}
