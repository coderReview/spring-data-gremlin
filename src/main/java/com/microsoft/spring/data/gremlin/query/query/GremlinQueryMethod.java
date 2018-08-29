/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.query.query;

import com.microsoft.spring.data.gremlin.annotation.Query;
import com.microsoft.spring.data.gremlin.query.GremlinEntityMetadata;
import com.microsoft.spring.data.gremlin.query.SimpleGremlinEntityMetadata;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class GremlinQueryMethod extends QueryMethod {

    private GremlinEntityMetadata<?> metadata;
    private final Method method;
    private final Map<Class<? extends Annotation>, Optional<Annotation>> annotationCache;

    public GremlinQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);

        this.method = method;
        this.annotationCache = new ConcurrentReferenceHashMap<>();
    }

    @Override
    public EntityMetadata<?> getEntityInformation() {
        @SuppressWarnings("unchecked") final Class<Object> domainClass = (Class<Object>) super.getDomainClass();

        this.metadata = new SimpleGremlinEntityMetadata<>(domainClass);

        return this.metadata;
    }

    /**
     * Returns whether the method has an annotated query.
     *
     * @return
     */
    public boolean hasAnnotatedQuery() {
        return findAnnotatedQuery().isPresent();
    }

    /**
     * Returns the query string declared in a {@link Query} annotation or {@literal null}
     * if neither the annotation found
     * nor the attribute was specified.
     *
     * @return
     */
    @Nullable
    String getAnnotatedQuery() {
        return findAnnotatedQuery().orElse(null);
    }

    private Optional<String> findAnnotatedQuery() {

        return lookupQueryAnnotation() //
                .map(Query::value) //
                .filter(StringUtils::hasText);
    }

    /**
     * Returns the {@link Query} annotation that is applied to the method or {@code null} if none available.
     *
     * @return
     */
    @Nullable
    Query getQueryAnnotation() {
        return lookupQueryAnnotation().orElse(null);
    }

    Optional<Query> lookupQueryAnnotation() {
        return doFindAnnotation(Query.class);
    }

    TypeInformation<?> getReturnType() {
        return ClassTypeInformation.fromReturnTypeOf(method);
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> Optional<A> doFindAnnotation(Class<A> annotationType) {

        return (Optional<A>) this.annotationCache.computeIfAbsent(annotationType,
                it -> Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(method, it)));
    }
}
