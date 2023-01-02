package org.opensearch.languages.oql.graphql.wiring;

import graphql.schema.*;
import org.opensearch.schema.ontology.EntityType;
import org.opensearch.schema.ontology.Property;

import java.util.List;
import java.util.Optional;

import static org.opensearch.graphql.GraphQLSchemaUtils.fakeScalarValue;
import static org.opensearch.languages.oql.graphql.wiring.TranslationUtils.*;

/**
 * this translator is responsible of translating values (scalars & primitives) to the appropriate ontology concrete values
 */
public class ValuesTranslation implements QueryTranslationStrategy{
    @Override
    public Optional<Object> translate(QueryTranslatorContext context, GraphQLType fieldType)  {
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
     * @param context
     * @return
     */
    private String populateGraphEnum(QueryTranslatorContext context) {
        //pop to the correct index according to path
        if (context.getPathContext().containsKey(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            context.getBuilder().currentIndex(context.getPathContext().get(context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }
        Property enumProp = context.getAccessor().pName$(context.getEnv().getField().getName());
        context.getBuilder().eProp(context.getEnv().getField().getName());
        //select first value since no matter which value selected for mock data
        return context.getAccessor().enumeratedType$(enumProp.getType().getType()).getValues().get(0).getName();
    }
}
