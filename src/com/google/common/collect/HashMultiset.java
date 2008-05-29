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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multiset implementation backed by a {@link HashMap}.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 */
@SuppressWarnings("serial") // we're overriding default serialization
public final class HashMultiset<E> extends AbstractMapBasedMultiset<E> {
  /**
   * Constructs a new empty {@code HashMultiset} using the default initial
   * capacity (16 distinct elements) and load factor (0.75).
   */
  public HashMultiset() {
    super(new HashMap<E, AtomicInteger>());
  }

  /**
   * Constructs a new empty {@code HashMultiset} with the specified expected
   * number of distinct elements and the default load factor (0.75).
   *
   * @param distinctElements the expected number of distinct elements
   * @throws IllegalArgumentException if {@code distinctElements} is negative
   */
  public HashMultiset(int distinctElements) {
    super(new HashMap<E, AtomicInteger>(Maps.capacity(distinctElements)));
  }

  /**
   * Constructs a new {@code HashMultiset} containing the specified elements.
   *
   * @param elements the elements that the multiset should contain
   */
  public HashMultiset(Iterable<? extends E> elements) {
    this(Multisets.inferDistinctElements(elements));
    Iterables.addAll(this, elements); // careful if we make this class non-final
  }

  private static class SerializedForm<E> extends MultisetSerializedForm<E> {
    SerializedForm(Multiset<E> multiset) {
      super(multiset);
    }
    @Override protected Multiset<E> createEmpty() {
      return new HashMultiset<E>(elementCount());
    }
    private static final long serialVersionUID = 0;
  }

  private void readObject(ObjectInputStream stream)
      throws InvalidObjectException {
    throw new InvalidObjectException("Use SerializedForm");
  }

  private Object writeReplace() {
    return new SerializedForm<E>(this);
  }
}
