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

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code BiMap} backed by two {@code HashMap} instances.
 *
 * @see HashMap
 * @author mbostock@google.com (Mike Bostock)
 */
public final class HashBiMap<K,V> extends StandardBiMap<K,V>
    implements Cloneable {

  /**
   * Constructs a new empty bimap with the default initial capacity (16) and
   * the default load factor (0.75).
   */
  public HashBiMap() {
    super(new HashMap<K,V>(), new HashMap<V,K>());
  }

  /**
   * Constructs a new empty bimap with the specified expected size and the
   * default load factor (0.75).
   *
   * @param expectedSize the expected number of entries
   * @throws IllegalArgumentException if the specified expected size is negative
   */
  public HashBiMap(int expectedSize) {
    super(new HashMap<K,V>(Maps.capacity(expectedSize)),
        new HashMap<V,K>(Maps.capacity(expectedSize)));
  }

  /**
   * Constructs a new empty bimap with the specified initial capacity and load
   * factor.
   *
   * @param initialCapacity the initial capacity
   * @param loadFactor the load factor
   * @throws IllegalArgumentException if the initial capacity is negative or the
   * load factor is nonpositive
   */
  public HashBiMap(int initialCapacity, float loadFactor) {
    super(new HashMap<K,V>(initialCapacity, loadFactor),
        new HashMap<V,K>(initialCapacity, loadFactor));
  }

  /**
   * Constructs a new bimap with the same mappings as the specified map. The
   * bimap is created with the default load factor (0.75) and an initial
   * capacity sufficient to hold the mappings in the specified map.
   *
   * @param map the map whose mappings are to be placed in this map
   * @throws NullPointerException if the specified map is null
   */
  public HashBiMap(Map<? extends K, ? extends V> map) {
    this(map.size());
    putAll(map); // careful if we make this class non-final
  }

  @SuppressWarnings("unchecked")
  @Override public HashBiMap<K,V> clone() {
    try {
      return (HashBiMap<K,V>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }
}
