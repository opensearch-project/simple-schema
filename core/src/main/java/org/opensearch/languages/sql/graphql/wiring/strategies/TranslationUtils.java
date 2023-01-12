package org.opensearch.languages.sql.graphql.wiring.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import org.opensearch.languages.QueryTranslationStrategy.QueryTranslatorContext;
import org.opensearch.languages.sql.query.Query;
import org.opensearch.schema.ontology.EntityType;

import java.io.IOException;
import java.util.Optional;

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
     * adds a where clause
     *
     * @param context
     * @param realType
     * @throws IOException
     */
    public static void addWhereClause(QueryTranslatorContext<Query.Builder> context, Optional<EntityType> realType) {
        // todo add where clause
    }

    /**
     * populates the property graph value
     *
     * @param context
     * @return
     */
    public static String populateGraphValue(QueryTranslatorContext<Query.Builder> context) {
        //todo implement
        return null;
    }

    public static Object fakeEnumValue(String fieldName, GraphQLEnumType enumType) {
        return fieldName;
    }

}
