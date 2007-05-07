// Copyright 2005 Google Inc. All rights reserved.

package com.google.common.base;

/**
 * A Predicate can determine a true or false value for any input of its
 * parameterized type. For example, a {@code RegexPredicate} might implement
 * {@code Predicate<String>}, and return true for any String that matches its
 * given regular expression.
 * 
 * <p>
 * Implementors of Predicate which may cause side effects upon evaluation are
 * strongly encouraged to state this fact clearly in their API documentation.
 * 
 * <p>
 * <b>NOTE:</b> This interface <i>could</i> technically extend
 * {@link Function}, since a predicate is just a special case of a fuction (one
 * that returns a boolean). However, since implementing this would entail
 * changing the signature of the {@link #apply} method to return a
 * {@link Boolean} instead of a {@code boolean}, which would in turn allow
 * people to return {@code null} from their Predicate, which would in turn
 * enable code that looks like this
 * {@code if (myPredicate.apply(myObject)) ... } to throw a
 * {@link NullPointerException}, it was decided not to make this change.
 * 
 * @author kevinb
 */
public interface Predicate<T> {

  /**
   * Applies this Predicate to the given object.
   *
   * @return the value of this Predicate when applied to input {@code t}
   */
  boolean apply(@Nullable T t);
}
