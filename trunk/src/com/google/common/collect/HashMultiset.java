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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Multiset implementation backed by a {@code HashMap}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public final class HashMultiset<E> extends AbstractMultiset<E>
    implements Serializable, Cloneable {
  private static final long serialVersionUID = 2422072640108355431L;

  /**
   * Constructs a new empty {@code HashMultiset} using the default initial
   * capacity (16 distinct elements) and load factor (0.75).
   */
  public HashMultiset() {
    super(new HashMap<E, Frequency>());
  }

  /**
   * Constructs a new empty {@code HashMultiset} with the specified expected
   * number of distinct elements and the default load factor (0.75).
   *
   * @param distinctElements the expected number of distinct elements
   * @throws IllegalArgumentException if {@code distinctElements} is negative
   */
  public HashMultiset(int distinctElements) {
    super(new HashMap<E, Frequency>(Maps.capacity(distinctElements)));
  }

  /**
   * Constructs a new empty {@code HashMultiset} using the specified initial
   * capacity (distinct elements) and load factor.
   *
   * @param initialCapacity the initial capacity
   * @param loadFactor the load factor
   * @throws IllegalArgumentException if the initial capacity is negative
   */
  public HashMultiset(int initialCapacity, float loadFactor) {
    super(new HashMap<E, Frequency>(initialCapacity, loadFactor));
  }

  /**
   * Constructs a new {@code HashMultiset} containing the specified elements. If
   * the specified elements is a {@code Multiset} instance, this constructor
   * behaves identically to {@link #HashMultiset(Multiset)}. Otherwise, the
   * default initial capacity (16 distinct elements) and load factor (0.75) is
   * used.
   *
   * @param elements the elements that the multiset should contain
   * @throws NullPointerException if {@code elements} is null
   */
  public HashMultiset(Iterable<? extends E> elements) {
    this(inferDistinctElements(elements));
    Iterables.addAll(this, elements); // careful if we make this class non-final
  }

  /**
   * Constructs a new {@code HashMultiset} containing the specified elements.
   * The multiset is created with the default load factor (0.75) and an initial
   * capacity sufficient to hold the specified elements.
   *
   * @param elements the elements that the multiset should contain
   * @throws NullPointerException if {@code elements} is null
   */
  public HashMultiset(Multiset<? extends E> elements) {
    this(elements.elementSet().size());
    addAll(elements); // careful if we make this class nonfinal
  }

  @SuppressWarnings("unchecked")
  @Override public HashMultiset<E> clone() {
    try {
      return (HashMultiset<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected Map<E, Frequency> cloneBackingMap() {
    return (Map<E, Frequency>) ((HashMap<E, Frequency>) backingMap()).clone();
  }

  /**
   * Returns the expected number of distinct elements given the specified
   * elements. The number of distinct elements is only computed if {@code
   * elements} is an instance of {@code Multiset}; otherwise the default value
   * of 11 is returned.
   */
  private static int inferDistinctElements(Iterable<?> elements) {
    if (elements instanceof Multiset<?>) {
      return ((Multiset<?>) elements).elementSet().size();
    }
    return 11; // initial capacity will be rounded up to 16
  }
}
