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
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link BiMap} backed by two {@link HashMap} instances. This implementation
 * allows null keys and values.
 *
 * @author Mike Bostock
 */
public final class HashBiMap<K, V> extends StandardBiMap<K, V> {
  /**
   * Constructs a new empty bimap with the default initial capacity (16) and the
   * default load factor (0.75).
   */
  public HashBiMap() {
    super(new HashMap<K, V>(), new HashMap<V, K>());
  }

  /**
   * Constructs a new empty bimap with the specified expected size and the
   * default load factor (0.75).
   *
   * @param expectedSize the expected number of entries
   * @throws IllegalArgumentException if the specified expected size is
   *     negative
   */
  public HashBiMap(int expectedSize) {
    super(new HashMap<K, V>(Maps.capacity(expectedSize)),
        new HashMap<V, K>(Maps.capacity(expectedSize)));
  }

  /**
   * Constructs a new empty bimap with the specified initial capacity and load
   * factor.
   *
   * @param initialCapacity the initial capacity
   * @param loadFactor the load factor
   * @throws IllegalArgumentException if the initial capacity is negative or the
   *     load factor is nonpositive
   */
  public HashBiMap(int initialCapacity, float loadFactor) {
    super(new HashMap<K, V>(initialCapacity, loadFactor),
        new HashMap<V, K>(initialCapacity, loadFactor));
  }

  /**
   * Constructs a new bimap containing initial values from {@code map}. The
   * bimap is created with the default load factor (0.75) and an initial
   * capacity sufficient to hold the mappings in the specified map.
   */
  public HashBiMap(Map<? extends K, ? extends V> map) {
    this(map.size());
    putAll(map); // careful if we make this class non-final
  }

  // Override these two methods to show that keys and values may be null

  @Override public V put(@Nullable K key, @Nullable V value) {
    return super.put(key, value);
  }

  @Override public V forcePut(@Nullable K key, @Nullable V value) {
    return super.forcePut(key, value);
  }
}
