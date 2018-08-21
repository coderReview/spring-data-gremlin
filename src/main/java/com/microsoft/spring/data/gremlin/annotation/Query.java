/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.annotation;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the query annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface Query {
    /**
     * Defines the JPA query to be executed when the annotated method is called.
     */
    String value() default "";

    /**
     * Configures whether the given query is a native one. Defaults to {@literal false}.
     */
    boolean nativeQuery() default false;
}
