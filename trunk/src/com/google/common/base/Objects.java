// Copyright 2006 Google Inc. All Rights Reserved.

package com.google.common.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Helper functions for operating on {@code Object}s.
 *
 * @author laurence@google.com (Laurence Gonsalves)
 */
public class Objects {
  private Objects() {}

  /**
   * Determines whether the two, possibly {@code null}, objects are equal.
   *
   * <p>This method will return:
   * <ul>
   * <li>{@code true} if o1 and o2 are both null.
   * <li>{@code true} if o1 and o2 are both non-null and they are equal
   * according to {@link Object#equals(Object)}.
   * <li>{@code false} in all other situations.
   * </ul>
   * <p>Note that this assumes that all non-null objects passed to this
   * function fully conform to the contract specified by {@link
   * Object#equals(Object)}.
   */
  public static boolean equal(Object o1, Object o2) {
    return (o1 == null) ? (o2 == null) : o1.equals(o2);
  }

  /**
   * Generates a hashcode for multiple values.
   *
   * <p>This is useful for implementing Object.hashCode(). For example, in an
   * object that has three properties, x, y and z, one could write:
   * <pre>
   * public int hashCode() {
   *   return Objects.hashCode(getX(), getY(), getZ());
   * }
   * </pre>
   */
  public static int hashCode(Object... objects) {
    return Arrays.hashCode(objects);
  }

  /**
   * @param o the object to check for nullness.
   * @return {@code o} if not null.
   * @throws NullPointerException if {@code o} is null.
   */
  public static <T> T nonNull(T o) {
    if (o == null) {
      throw new NullPointerException();
    }
    return o;
  }

  /**
   * Checks that the specified object is not {@code null}.
   *
   * @param o the object to check for nullness.
   * @param message exception message used in the event that a {@code
   * NullPointerException} is thrown.
   * @return {@code o} if not null.
   * @throws NullPointerException if {@code o} is null.
   */
  public static <T> T nonNull(T o, String message) {
    if (o == null) {
      throw new NullPointerException(message);
    }
    return o;
  }

  /**
   * Returns the result of invoking {@link Object#clone} on {@code object}. It
   * is preferable to invoke {@link Object#clone} directly, however, this is not
   * always possible. Often, your reference to an object is of an interface type
   * which does not declare a {@link Object#clone} method, in which case you
   * cannot clone the object without first knowing which concrete type you
   * should downcast to. This helper method provides a workaround for this
   * problem. As a bonus, you won't have to cast the result as you do with
   * {@link Object#clone}.
   *
   * @throws ClassCastException if the object's clone method returns an instance
   *     of a class which is neither the same as, nor a subclass of, the
   *     original object's class
   * @throws CloneNotSupportedException if thrown by {@link Object#clone}, or
   *     if {@link Object#clone} has not been overridden and made public
   */
  public static <T> T clone(T object) throws CloneNotSupportedException {
    Object clone = null;

    // Use reflection, because there is no other way
    try {
      Method method = object.getClass().getMethod("clone");
      clone = method.invoke(object);
    } catch (InvocationTargetException e) {
      rethrow(e.getCause());
    } catch (Exception cause) {
      rethrow(cause);
    }
    if (object.getClass().isInstance(clone)) {
      @SuppressWarnings("unchecked") // clone class <= object class <= T
      T t = (T) clone;
      return t;
    } else {
      throw new ClassCastException(clone.getClass().getName());
    }
  }

  private static void rethrow(Throwable cause)
      throws CloneNotSupportedException {
    if (cause instanceof RuntimeException) {
      throw (RuntimeException) cause;
    }
    if (cause instanceof Error) {
      throw (Error) cause;
    }
    if (cause instanceof CloneNotSupportedException) {
      throw (CloneNotSupportedException) cause;
    }
    CloneNotSupportedException e = new CloneNotSupportedException();
    e.initCause(cause);
    throw e;
  }
}
