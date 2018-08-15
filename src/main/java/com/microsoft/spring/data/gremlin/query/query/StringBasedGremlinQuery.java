package com.microsoft.spring.data.gremlin.query.query;

import com.microsoft.spring.data.gremlin.query.GremlinOperations;
import com.microsoft.spring.data.gremlin.query.paramerter.GremlinParameterAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class StringBasedGremlinQuery extends AbstractGremlinQuery {

    private static final Logger LOG = LoggerFactory.getLogger(StringBasedGremlinQuery.class);
    private static final ParameterBindingParser BINDING_PARSER = ParameterBindingParser.INSTANCE;
    private final String query;
    private final String fieldSpec;
    private final List<ParameterBinding> queryParameterBindings;
    private final List<ParameterBinding> fieldSpecParameterBindings;
    private final ExpressionEvaluatingParameterBinder parameterBinder;

    public StringBasedGremlinQuery(GremlinQueryMethod method, GremlinOperations operations,
                                   SpelExpressionParser expressionParser, EvaluationContextProvider evaluationContextProvider) {
        this(method.getAnnotatedQuery(), method, operations, expressionParser, evaluationContextProvider);
    }

    public StringBasedGremlinQuery(String query, GremlinQueryMethod method, GremlinOperations operations,
                                   SpelExpressionParser expressionParser, EvaluationContextProvider evaluationContextProvider) {
        super(method, operations);

        Assert.notNull(query, "Query must not be null!");
        Assert.notNull(expressionParser, "SpelExpressionParser must not be null!");

        this.queryParameterBindings = new ArrayList<ParameterBinding>();
        this.query = BINDING_PARSER.parseAndCollectParameterBindingsFromQueryIntoBindings(query,
                this.queryParameterBindings);

        this.fieldSpecParameterBindings = new ArrayList<ParameterBinding>();
        this.fieldSpec = BINDING_PARSER.parseAndCollectParameterBindingsFromQueryIntoBindings(
                method.getFieldSpecification(), this.fieldSpecParameterBindings);

        this.parameterBinder = new ExpressionEvaluatingParameterBinder(expressionParser, evaluationContextProvider);

        if (method.hasAnnotatedQuery()) {

            org.springframework.data.mongodb.repository.Query queryAnnotation = method.getQueryAnnotation();

            this.isCountQuery = queryAnnotation.count();
            this.isExistsQuery = queryAnnotation.exists();
            this.isDeleteQuery = queryAnnotation.delete();

            if (hasAmbiguousProjectionFlags(this.isCountQuery, this.isExistsQuery, this.isDeleteQuery)) {
                throw new IllegalArgumentException(String.format(COUNT_EXISTS_AND_DELETE, method));
            }

        } else {

            this.isCountQuery = false;
            this.isExistsQuery = false;
            this.isDeleteQuery = false;
        }
    }

    @Override
    protected GremlinQuery createQuery(GremlinParameterAccessor accessor) {
        String queryString = parameterBinder.bind(this.query, accessor,
                new BindingContext(getQueryMethod().getParameters(), queryParameterBindings));
        String fieldsString = parameterBinder.bind(this.fieldSpec, accessor,
                new BindingContext(getQueryMethod().getParameters(), fieldSpecParameterBindings));

        GremlinQuery query = new BasicQuery(queryString, fieldsString).with(accessor.getSort());

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Created query %s for %s fields.", query.getQueryObject(), query.getFieldsObject()));
        }

        return query;
    }
}
