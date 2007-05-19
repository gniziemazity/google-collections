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

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multiset implementation backed by a TreeMap.
 *
 * @author nkanodia (Neal Kanodia)
 */
public final class TreeMultiset<E> extends AbstractMapBasedMultiset<E>
    implements Cloneable {

  /**
   * Constructs a new, empty multiset, sorted according to the elements' natural
   * order.  All elements inserted into the multiset must implement the
   * <tt>Comparable</tt> interface.  Furthermore, all such elements must be
   * <i>mutually comparable</i>: <tt>e1.compareTo(e2)</tt> must not throw a
   * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
   * <tt>e2</tt> in the multiset.  If the user attempts to add an element to the
   * multiset that violates this constraint (for example, the user attempts to
   * add a string element to a set whose elements are integers), the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @see Comparable
   * @see java.util.TreeSet
   */
  public TreeMultiset() {
    super(new TreeMap<E, AtomicInteger>());
  }

  /**
   * Constructs a new, empty multiset, sorted according to the specified
   * comparator.  All elements inserted into the multiset must be <i>mutually
   * comparable</i> by the specified comparator: <tt>comparator.compare(e1,
   * e2)</tt> must not throw a <tt>ClassCastException</tt> for any elements
   * <tt>e1</tt> and <tt>e2</tt> in the multiset.  If the user attempts to add
   * an element to the multiset that violates this constraint, the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @param comparator the comparator that will be used to sort this multiset.
   *     A {@code null} value indicates that the elements' <i>natural
   *     ordering</i> should be used.
   */
  public TreeMultiset(Comparator<? super E> comparator) {
    super(new TreeMap<E, AtomicInteger>(comparator));
  }

  /**
   * Constructs an empty multiset containing the given initial elements.
   */
  public TreeMultiset(Collection<? extends E> initialElements) {
    this();
    addAll(initialElements); // careful if we ever make this class nonfinal
  }

  @SuppressWarnings("unchecked")
  @Override public TreeMultiset<E> clone() {
    try {
      return (TreeMultiset<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected Map<E, AtomicInteger> cloneBackingMap() {
    return (Map<E, AtomicInteger>) ((TreeMap<E, AtomicInteger>) backingMap()).clone();
  }

  private static final long serialVersionUID = 980261132547708887L;
}
