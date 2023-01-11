package org.opensearch.languages.oql.graphql.wiring.strategies;

import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.schema.GraphQLType;
import org.opensearch.languages.QueryTranslationStrategy;
import org.opensearch.languages.oql.query.Query;
import org.opensearch.languages.oql.query.quant.QuantBase;
import org.opensearch.languages.oql.query.quant.QuantType;

import java.util.Optional;

import static org.opensearch.languages.oql.graphql.wiring.strategies.TranslationUtils.isParentObjectType;

/**
 * this translator is responsible of adding a quantifier prior to the next query steps
 */
public class QuantifierTranslation implements QueryTranslationStrategy<Query.Builder> {
    @Override
    public Optional<Object> translate(QueryTranslatorContext<Query.Builder> context, GraphQLType fieldType) {
        // if parent is of type vertex and current query element not quant -> add quant
        ExecutionStepInfo parent = context.getEnv().getExecutionStepInfo().getParent();
        if (parent.getFieldDefinition() != null) {
            ResultPath parentPath = context.getEnv().getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd();
            if (isParentObjectType(parent.getFieldDefinition()) &&
                    //assume a path was already entered
                    context.getPathContext().containsKey(parentPath.getPathWithoutListEnd().toString()) &&
                    //validate no quant related to the
                    !(context.getBuilder().current(context.getPathContext().get(parentPath.getPathWithoutListEnd().toString())) instanceof QuantBase)) {
                if (!(context.getBuilder().current() instanceof QuantBase)) {
                    context.getBuilder().quant(QuantType.all);
                }
                context.getPathContext().put(parent.getPath().getPathWithoutListEnd().toString(), context.getBuilder().currentIndex());
            }
        }
        return Optional.empty();
    }
}
