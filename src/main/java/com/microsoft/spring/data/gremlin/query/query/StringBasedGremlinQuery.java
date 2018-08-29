/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query.query;

import com.microsoft.spring.data.gremlin.query.GremlinOperations;
import com.microsoft.spring.data.gremlin.query.paramerter.GremlinParameterAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.Parameter;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

public class StringBasedGremlinQuery extends AbstractGremlinQuery {

    private static final Logger LOG = LoggerFactory.getLogger(StringBasedGremlinQuery.class);
    private String queryString;
    private boolean countQuery;
    private boolean modifyingQuery;
    private DefaultParameters parameters;

    public StringBasedGremlinQuery(GremlinQueryMethod method, GremlinOperations operations,
                                   SpelExpressionParser expressionParser,
                                   EvaluationContextProvider evaluationContextProvider) {
        this(method.getAnnotatedQuery(), method, operations, expressionParser, evaluationContextProvider);
    }

    public StringBasedGremlinQuery(String query, GremlinQueryMethod method, GremlinOperations operations,
                                   SpelExpressionParser expressionParser,
                                   EvaluationContextProvider evaluationContextProvider) {
        super(method, operations);

        Assert.notNull(query, "Query must not be null!");
        Assert.notNull(expressionParser, "SpelExpressionParser must not be null!");

        this.queryString = query;
        this.parameters = (DefaultParameters) method.getParameters();
        // this.countQuery = method.hasAnnotatedQuery() && method.getQueryAnnotation().count();
        // this.modifyingQuery = method.hasAnnotatedQuery() && method.getQueryAnnotation().modify();
    }

    @Override
    protected String createQuery(GremlinParameterAccessor accessor) {
        String queryString = this.queryString;

        for (final Parameter param : parameters.getBindableParameters()) {
            String placeholder = param.getPlaceholder();
            final Object val = accessor.getValues()[param.getIndex()];
            if (!param.getName().isPresent()) {
                placeholder = "placeholder_" + param.getIndex();
                queryString = queryString.replaceFirst("\\?", placeholder);
                // bindings.put(placeholder, val);
            } else {
                final String paramName = param.getName().get();
                queryString = queryString.replaceFirst(placeholder, paramName);
                // bindings.put(paramName, val);
            }
        }

        final Pageable pageable = accessor.getPageable();
        // if (pageable != null && !ignorePaging) {
        // if (pageable != null) {
        queryString = String.format("%s[%d..%d]", queryString, pageable.getOffset(),
                pageable.getOffset() + pageable.getPageSize() - 1);
        // }

        return queryString;
    }
}
