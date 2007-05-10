/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

/**
 * Interface for defining a constraint on the types of elements that are allowed
 * to be added to a {@code Collection}. For example, to enforce that a
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
   * Implement this method to throw a suitable {@code RuntimeException} if the
   * specified element is illegal. Typically this is either a {@link
   * NullPointerException}, an {@link IllegalArgumentException}, or a {@link
   * ClassCastException}, though a more application-specific exception class may
   * be used as appropriate.
   */
  void checkElement(E element);
}
