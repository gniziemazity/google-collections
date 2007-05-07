// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

/**
 * Interface for defining a constraint on the types of elements that are
 * allowed to be added to a {@link Collection}. For example, to enforce that a
 * collection contains no {@code null} elements, you might say:
 *
 * <pre>  public void checkElement(Object element) {
 *    if (element == null) {
 *      throw new NullPointerException();
 *    }
 *  }</pre>
 *
 * Then use {@link Constraints#constrainedCollection} to enforce the
 * constraint. This example is contrived; to check for {@code null} use {@link
 * Constraints#NOT_NULL}.
 *
 * <p>In order to be effective, constraints should be determinstic; that is,
 * they should not depend on state that can change (e.g., external state,
 * random variables, time), and should only depend on the value of the
 * passed-in element. A non-deterministic constraint cannot reliably enforce
 * that all the collection's elements meet the constraint, since the constraint
 * is only enforced when elements are added.
 *
 * @see Constraints
 * @see MapConstraint
 * @author mbostock@google.com (Mike Bostock)
 */
public interface Constraint<E> {

  /**
   * Implement this method to throw a suitable {@link RuntimeException} if the
   * specified element is illegal. Typically this is either a {@link
   * NullPointerException}, an {@link IllegalArgumentException}, or a {@link
   * ClassCastException}, though a more application-specific exception class may
   * be used as appropriate.
   */
  void checkElement(E element);
}
