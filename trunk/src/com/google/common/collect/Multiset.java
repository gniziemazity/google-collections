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

import com.google.common.base.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * An unordered collection similar to a {@link java.util.Set}, but which may
 * contain duplicate elements.
 *
 * @author Kevin Bourrillion
 */
public interface Multiset<E> extends Collection<E> {
  // Query Operations

  /**
   * Returns the number of occurrences of the specified element in this
   * multiset.
   *
   * @param element the element to look for
   * @return the nonnegative number of occurrences of the element
   * @see Iterables#frequency
   */
  int count(@Nullable Object element);

  // Modification Operations

  /**
   * Adds a number of occurrences of the specified element to this multiset.
   *
   * @param element the element to add
   * @param occurrences the number of occurrences to add
   * @return {@code true} if the collection changed as a result (this should
   *     always be the case unless {@code occurrences} is zero)
   * @throws IllegalArgumentException if {@code occurrences} is negative
   */
  boolean add(@Nullable E element, int occurrences);

  /**
   * Removes a number of occurrences of the specified element from this
   * multiset. If the multiset contains fewer than this number of occurrences to
   * begin with, all occurrences will be removed.
   *
   * @param element the element whose occurrences should be removed
   * @param occurrences the number of occurrences of this element to remove
   * @return the number of occurrences that were successfully removed (zero if
   *     the element was not present)
   * @throws IllegalArgumentException if {@code occurrences} is negative
   */
  int remove(@Nullable Object element, int occurrences);

  /**
   * Removes <b>all</b> occurrences of the specified element from this multiset.
   * This method complements {@link Multiset#remove}, which removes only one
   * occurrence at a time.
   *
   * @param element the element whose occurrences should all be removed
   * @return the number of occurrences successfully removed, possibly zero
   */
  int removeAllOccurrences(@Nullable Object element);

  // Views

  /**
   * Returns a view of the elements of this multiset as a set. All remove
   * operations on the returned set "write through" to the underlying multiset,
   * removing <b>all</b> occurrences of the given elements. No add operations
   * are supported. Note that you can use {@code elementSet().size()} to find
   * the number of distinct elements in this multiset.
   *
   * @return the distinct elements of this multiset, viewed as a set
   */
  Set<E> elementSet();

  /**
   * Returns the data of this multiset as a set of {@code Entry} instances. This
   * set contains precisely one {@code Entry} instance for each distinct element
   * of the multiset. The iteration order of this set is
   * implementation-dependent.
   *
   * @return the entries of this multiset, viewed as a set
   */
  Set<Multiset.Entry<E>> entrySet();

  /**
   * A multiset entry (element-count pair). The {@link Multiset#entrySet} method
   * returns a view of the multiset whose elements are of this class. The
   * behavior of a previously retrieved Entry instance after the underlying
   * multiset has been modified is undefined.
   */
  interface Entry<E> {

    /**
     * Returns the multiset element corresponding to this entry.
     *
     * @return the element corresponding to this entry.
     */
    E getElement();

    /**
     * Returns the number of occurrences of the corresponding element in the
     * multiset.
     *
     * @return the count for the element; always strictly greater than zero
     */
    int getCount();

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code true} if the given object is also a multiset entry and
     * the two entries represent the same element and count. More formally, two
     * entries {@code a} and {@code b} are equal if:
     *
     * <pre>  ((a.getElement() == null)
     *      ? (b.getElement() == null) : a.getElement().equals(b.getElement()))
     *    && (a.getCount() == b.getCount())</pre>
     */
    boolean equals(Object o);

    /**
     * {@inheritDoc}
     *
     * <p>The hash code of a multiset entry {@code e} is defined to be:
     *
     * <pre>  ((e.getElement() == null) ? 0 : e.getElement().hashCode())
     *    ^ e.getCount()</pre>
     */
    int hashCode();
  }

  // Comparison and hashing

  /**
   * Compares the specified object with this multiset for equality. Returns
   * {@code true} if the given object is also a multiset and contains equal
   * elements with equal counts.
   */
  boolean equals(@Nullable Object object);

  /**
   * Returns the hash code for this multiset. This is defined as the sum of
   *
   * <pre>  (element == null ? 0 : element.hashCode()) ^ count(element)</pre>
   *
   * over all elements in the multiset.
   */
  int hashCode();
}
