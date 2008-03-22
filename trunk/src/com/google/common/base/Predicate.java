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

package com.google.common.base;

/**
 * Determines a true or false value for any input of its parameterized type. For
 * example, a {@code RegexPredicate} might implement {@code Predicate<String>},
 * and return true for any String that matches its given regular expression.
 *
 * <p>Implementations which may cause side effects upon evaluation are strongly
 * encouraged to state this fact clearly in their API documentation.
 *
 * <p><b>NOTE:</b> This interface <i>could</i> technically extend
 * {@link Function}, since a predicate is just a special case of a function (one
 * that returns a boolean). However, since implementing this would entail
 * changing the signature of the {@link #apply} method to return a
 * {@link Boolean} instead of a {@code boolean}, which would in turn allow
 * people to return null from their predicate, which would in turn enable code
 * that looks like {@code if (myPredicate.apply(myObject)) ... } to throw a
 * {@link NullPointerException}, it was decided not to make this change.
 *
 * @author Kevin Bourrillion
 */
public interface Predicate<T> {

  /**
   * Applies this predicate to the given object.
   *
   * @return the value of this predicate when applied to the input {@code t}
   */
  boolean apply(@Nullable T t);


  /**
   * Indicates whether some other object is equal to this {@code Predicate}.
   * This method can return {@code true} <em>only</em> if the specified object
   * is also a {@code Predicate} and, for every input object {@code o}, it
   * returns exactly the same value.  Thus,
   * {@code predicate1.equals(predicate2)} implies that either
   * {@code predicate1.apply(o)} and {@code predicate2.apply(o)} are both
   * {@code true}, or both {@code false}.
   *
   * <p>Note that it is <em>always</em> safe <em>not</em> to override
   * {@code Object.equals(Object)}.
   */
  boolean equals(Object obj);
}
