// Copyright 2005, Google Inc. All rights reserved.

package com.google.common.base;

/**
 * A Function provides a transformation on an object and returns the resulting
 * object.  For example, a {@code StringToIntegerFunction} may implement
 * <code>Function&lt;String,Integer&gt;</code> and transform integers in String
 * format to Integer format.
 *
 * <p>The transformation on the source object does not necessarily result in
 * an object of a different type.  For example, a
 * {@code FarenheitToCelciusFunction} may implement
 * <code>Function&lt;Float,Float&gt;</code>.
 *
 * <p>Implementors of Function which may cause side effects upon evaluation are
 * strongly encouraged to state this fact clearly in their API documentation.
 *
 * @author kevinb
 * @author bonneau
 */
public interface Function<F,T> {

  /**
   * Applys the function to an object of type {@code F}, resulting in an object
   * of type {@code T}.  Note that types {@code F} and {@code T} may or may not
   * be the same.
   * @param from The source object.
   * @return The resulting object.
   */
  T apply(@Nullable F from);
}
