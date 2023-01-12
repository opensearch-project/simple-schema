package org.opensearch.languages.sql.graphql.wiring.strategies;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.sql.query.Query;

import java.util.Optional;

import static org.opensearch.graphql.GraphQLSchemaUtils.fakeScalarValue;
import static org.opensearch.languages.sql.graphql.wiring.strategies.TranslationUtils.*;

/**
 * this translator is responsible of translating values (scalars & primitives) to the appropriate ontology concrete values
 */
public class ValuesTranslation implements QueryTranslationStrategy<Query.Builder> {
    @Override
    public Optional<Object> translate(QueryTranslatorContext<Query.Builder> context, GraphQLType fieldType)  {
        //populate values
        fieldType = extractConcreteFieldType(fieldType);
        if (fieldType instanceof GraphQLScalarType) {
            String name = populateGraphValue(context);
            return Optional.of(fakeScalarValue(name, (GraphQLScalarType) fieldType));
        } else if (fieldType instanceof GraphQLEnumType) {
            String name = populateGraphEnum(context);
            return Optional.of(fakeEnumValue(name, (GraphQLEnumType) fieldType));
        }
        return Optional.empty();
    }


    /**
     * populates the appropriate enum ordinal value
     * @param context
     * @return
     */
    private String populateGraphEnum(QueryTranslatorContext<Query.Builder> context) {
        //todo implement
        return null;
     }
}
