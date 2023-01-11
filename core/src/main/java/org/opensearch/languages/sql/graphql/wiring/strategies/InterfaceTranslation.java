package org.opensearch.languages.sql.graphql.wiring.strategies;

import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.sql.query.Query;
import org.opensearch.schema.SchemaError;
import org.opensearch.schema.ontology.EntityType;

import java.util.List;
import java.util.Optional;

import static org.opensearch.languages.sql.graphql.wiring.strategies.TranslationUtils.*;

/**
 * this translator is responsible of translating interfaces to the appropriate ontology concrete entities
 */
public class InterfaceTranslation implements QueryTranslationStrategy<Query.Builder>{
    @Override
    public Optional<Object> translate(QueryTranslatorContext<Query.Builder> context, GraphQLType fieldType)  {
        fieldType = extractConcreteFieldType(fieldType);
        if (fieldType instanceof GraphQLInterfaceType) {
            //select the first implementing of interface (no matter which one since all share same common fields)
            List<GraphQLObjectType> implementations = context.getSchema().getImplementations((GraphQLInterfaceType) fieldType);
            //todo infer the real vertex or relation
            Optional<EntityType> realType = null;

            try {
                addWhereClause(context, realType);
            } catch (Throwable e) {
                throw new SchemaError.SchemaErrorException("During GraphQL to Ontology QL translation, failed on InterfaceTranslation::addWhereClause",e);
            }
//            return fakeObjectValue(accessor, builder, implementations.get(0));
            return Optional.of(new Object());
        }
        return Optional.empty();
    }
}
