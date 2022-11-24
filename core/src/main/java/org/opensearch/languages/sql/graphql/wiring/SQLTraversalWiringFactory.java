package org.opensearch.languages.sql.graphql.wiring;


import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Internal;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.*;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.InterfaceWiringEnvironment;
import graphql.schema.idl.UnionWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import org.opensearch.graphql.GraphQLEngineFactory;
import org.opensearch.graphql.wiring.InputTypeConstraint;
import org.opensearch.graphql.wiring.InputTypeWhereClause;
import org.opensearch.languages.sql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.Accessor;
import org.opensearch.schema.ontology.EntityType;

import java.io.IOException;
import java.util.*;

import static org.opensearch.graphql.GraphQLSchemaUtils.fakeScalarValue;


@Internal
public class SQLTraversalWiringFactory implements WiringFactory {

    public static final String WHERE = "where";
    public static final String QUERY = "query";
    //wiring jackson mapper
    public static final ObjectMapper mapper = new ObjectMapper();

    private Query.Builder builder;
    private Accessor accessor;
    private Map<String, Integer> pathContext;
    private GraphQLSchema schema;

    public SQLTraversalWiringFactory(Accessor accessor, Query.Builder builder) {
        this.schema = GraphQLEngineFactory.schema()
                .orElseThrow(() -> new SchemaError.SchemaErrorException("GraphQL schema not present", "Expecting the GraphQL schema to be created during this stage"));

        this.builder = builder;
        this.accessor = accessor;
        this.pathContext = new HashMap<>();
    }

    public Query.Builder getBuilder() {
        return builder;
    }

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return env -> schema.getImplementations((GraphQLInterfaceType) env.getFieldType()).get(0);
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return env -> env.getSchema().getQueryType();
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        return env -> {
            GraphQLType fieldType = env.getFieldType();
            if (fieldType instanceof GraphQLList) {
                return Arrays.asList(getObject(env, ((GraphQLList) fieldType).getWrappedType()));
            } else {
                return getObject(env, fieldType);
            }
        };
    }

    private Object getObject(DataFetchingEnvironment env, GraphQLType fieldType) throws IOException {
        fieldType = extractConcreteFieldType(fieldType);
        // in parent is of type vertex and current query element not quant -> add quant
        ExecutionStepInfo parent = env.getExecutionStepInfo().getParent();
        if (parent.getFieldDefinition() != null) {
            ResultPath parentPath = env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd();
            if (isParentObjectType(parent.getFieldDefinition()) &&
                    //assume a path was already entered
                    pathContext.containsKey(parentPath.getPathWithoutListEnd().toString())) {
                    //validate no quant related to the
//                    !(builder.current(pathContext.get(parentPath.getPathWithoutListEnd().toString())) instanceof QuantBase)) {
                //todo add quant
//                if (!(builder.current() instanceof QuantBase)) {
//                    builder.quant(QuantType.all);
//                }
//                pathContext.put(parent.getPath().getPathWithoutListEnd().toString(), builder.currentIndex());
            }
        }

        if (fieldType instanceof GraphQLObjectType) {
            GraphQLObjectType type = (GraphQLObjectType) fieldType;
            GraphQLObjectType parentType = (GraphQLObjectType) parent.getType();
            //add the start query element
            if (parentType.getName().toLowerCase().equals(QUERY)) {
//                builder.start();
            }
            //populate vertex or relation
//            Optional<EntityType> realType = populateGraphObject(env, type.getName());
//            addWhereClause(env, realType);
//            return fakeObjectValue(accessor, builder, (GraphQLObjectType) fieldType);
            return new Object();
            //todo create concrete union types from abstract interface
        } else if (fieldType instanceof GraphQLInterfaceType) {
            //select the first implementing of interface (no matter which one since all share same common fields)
            List<GraphQLObjectType> implementations = schema.getImplementations((GraphQLInterfaceType) fieldType);
            //populate vertex or relation
//            Optional<EntityType> realType = populateGraphObject(env, ((GraphQLInterfaceType) fieldType).getName());
//            addWhereClause(env, realType);
//            return fakeObjectValue(accessor, builder, implementations.get(0));
            return new Object();
        }
        //populate values
        if (fieldType instanceof GraphQLScalarType) {
//            String name = populateGraphValue(env);
            return fakeScalarValue("name", (GraphQLScalarType) fieldType);
        } else if (fieldType instanceof GraphQLEnumType) {
//            String name = populateGraphEnum(env);
            return fakeEnumValue("name", (GraphQLEnumType) fieldType);
        }

        return new Object();
    }

    /**
     * @param env
     * @param realType
     * @throws IOException
     */
    private void addWhereClause(DataFetchingEnvironment env, Optional<EntityType> realType) throws IOException {
        //arguments
        if (realType.isPresent() && env.getArguments().containsKey(WHERE)) {
            Object argument = env.getArguments().get(WHERE);
            InputTypeWhereClause whereClause = mapper.readValue(mapper.writeValueAsString(argument), InputTypeWhereClause.class);
            //verify fields exist within entity type
            List<InputTypeConstraint> nonFoundFields = whereClause.getConstraints().stream()
                    .filter(c -> !realType.get().containsProperty(c.getOperand())).toList();

            if (!nonFoundFields.isEmpty())
                throw new IllegalArgumentException("Fields " + nonFoundFields + " are not a part of the queried entity " + realType.get().getName());

            //build the where clause query
//            List<Tuple2<String, Optional<Constraint>>> constraints = whereClause.getConstraints().stream()
//                    .map(c -> new Tuple2<>(c.getOperand(),
//                            Optional.of(new Constraint(ConstraintOp.valueOf(c.getOperator()), c.getExpression()))))
//                    .collect(Collectors.toList());


            //if no quant exists - add one
//            if (getBuilder().pop(eBase -> eBase instanceof QuantBase).isEmpty()) {
//                builder.quant(QuantType.all);
//            }

            //add to group
//            builder.ePropGroup(constraints, asQuantType(whereClause.getOperator()));

        }
    }

/*
    private QuantType asQuantType(WhereOperator operator) {
        switch (operator) {
            case AND:
                return QuantType.all;
            case OR:
                return QuantType.some;
            default:
                return QuantType.all;
        }
    }
*/

    private boolean isParentObjectType(GraphQLFieldDefinition parentField) {
        return (extractConcreteFieldType(parentField.getType()) instanceof GraphQLInterfaceType) ||
                (extractConcreteFieldType(parentField.getType()) instanceof GraphQLObjectType);
    }


    /**
     * get concrete friend type
     *
     * @param fieldType
     * @return
     */
    private GraphQLType extractConcreteFieldType(GraphQLType fieldType) {
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


    private Object fakeEnumValue(String fieldName, GraphQLEnumType enumType) {
        return fieldName;
    }

}


