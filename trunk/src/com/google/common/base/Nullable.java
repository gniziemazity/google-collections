package com.google.common.base;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The presence of this annotation on a method parameter indicates that
 * {@code null} is an acceptable value for that parameter.  It should not be
 * used for parameters of primitive types.
 *
 * @author kevinb
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Nullable { }
