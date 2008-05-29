/*
 * Copyright (C) 2008 Google Inc.
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
import java.util.List;

/**
 * A subclass of this class is used to serialize each {@link Multiset} instance,
 * regardless of implementation type. It captures their "logical contents" and
 * they are reconstructed using public methods.
 * 
 * @author Jared Levy
 */
abstract class MultisetSerializedForm<E> implements Serializable {
  private final Object[] elements;
  private final int[] counts;

  protected MultisetSerializedForm(Multiset<E> multiset) {
    int entryCount = multiset.entrySet().size();
    List<E> elementList = Lists.newArrayListWithExpectedSize(entryCount);
    List<Integer> countList = Lists.newArrayListWithExpectedSize(entryCount);
    for (Multiset.Entry<E> entry : multiset.entrySet()) {
      elementList.add(entry.getElement());
      countList.add(entry.getCount());
    }
    elements = elementList.toArray();
    counts = PrimitiveArrays.toIntArray(countList);
  }

  /** Create an empty multiset of the correct type. */
  protected abstract Multiset<E> createEmpty();
 
  protected Object readResolve() {
    Multiset<E> multiset = createEmpty();
    populate(multiset);
    return multiset;
  }
  
  /** Copy the serialized contents into the provided empty multiset. */
  @SuppressWarnings("unchecked")
  private void populate(Multiset<E> multiset) {
    for (int i = 0; i < elements.length; i++) {
      multiset.add((E) elements[i], counts[i]);
    }
  }
  
  /** Returns the number of distinct elements in the multiset. */
  protected int elementCount() {
    return elements.length;
  }
  
  private static final long serialVersionUID = 0;
}
