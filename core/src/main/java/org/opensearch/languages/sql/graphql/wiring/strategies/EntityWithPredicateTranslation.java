package org.opensearch.languages.sql.graphql.wiring.strategies;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.sql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.EntityType;

import java.util.Optional;

import static org.opensearch.languages.sql.graphql.wiring.strategies.TranslationUtils.*;


/**
 * this translator is responsible for adding an entity with its predicates to the next query steps
 */
public class EntityWithPredicateTranslation implements QueryTranslationStrategy<Query.Builder> {

    @Override
    public Optional<Object> translate(QueryTranslatorContext<Query.Builder> context, GraphQLType fieldType) {
        ExecutionStepInfo parent = context.getEnv().getExecutionStepInfo().getParent();
        fieldType = extractConcreteFieldType(fieldType);
        if (fieldType instanceof GraphQLObjectType) {
            GraphQLObjectType type = (GraphQLObjectType) fieldType;
            GraphQLObjectType parentType = (GraphQLObjectType) parent.getType();
            //add the start query element
            if (parentType.getName().equalsIgnoreCase(QUERY)) {
                // todo start traversing objects fields and add them to the select clause
            }
            //todo infer the real vertex or relation
            Optional<EntityType> realType = null;
            try {
                addWhereClause(context, realType);
            } catch (Throwable e) {
                throw new SchemaError.SchemaErrorException("During GraphQL to Ontology QL translation, failed on EntityWithPredicateTranslation::addWhereClause", e);
            }
            //todo create concrete union types from abstract interface
            return Optional.of(new Object());
        }
        return Optional.empty();
    }

}
