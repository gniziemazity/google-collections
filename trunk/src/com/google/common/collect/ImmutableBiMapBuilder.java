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
import static com.google.common.base.Preconditions.checkState;

/**
 * A convenient way to populate immutable BiMap instances, especially
 * static-final "constant BiMaps".  Code such as
 *
 * <pre>
 *   static final BiMap&lt;String,Integer> ENGLISH_TO_INTEGER_BIMAP
 *       = createNumbersMap();
 *
 *   static BiMap&lt;String,Integer&gt; createNumbersMap() {
 *     BiMap&lt;String,Integer&gt; map = StandardBiMap.newInstance();
 *     map.put("one", 1);
 *     map.put("two", 2);
 *     map.put("three", 3);
 *     return StandardBiMap.unmodifiableBiMap(map);
 *   }
 * </pre>
 * ... can be rewritten far more simply as ...
 * <pre>
 *   static final BiMap&lt;String,Integer&gt; ENGLISH_TO_INTEGER_BIMAP
 *     = new ImmutableBiMapBuilder&lt;String,Integer&gt;()
 *       .put("one", 1)
 *       .put("two", 2)
 *       .put("three", 3)
 *       .getBiMap();
 * </pre>
 *
 * @author dovle@google.com (Alex Dovlecel)
 */
public class ImmutableBiMapBuilder<K,V> {
  // TODO(dovle): move to com.google.common.collect

  /**
   * Temporary bimap used for holding the state of the builder before the
   * map will be created. When the map is created, it is set to {@code null}.
   */
  private BiMap<K,V> biMap;

  /**
   * Creates a new ImmutableBiMapBuilder with an unspecified expected size.
   */
  public ImmutableBiMapBuilder() {
    this(8);
  }

  /**
   * Creates a new ImmutableBiMapBuilder with the given expected size.
   *
   * @param expectedSize the approximate number of key-value pairs you
   *     expect this map to contain
   */
  public ImmutableBiMapBuilder(int expectedSize) {
    biMap = new HashBiMap<K,V>(expectedSize);
  }

  /**
   * Adds a key-value mapping to the map that will be returned by {@code
   * getBiMap}.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @throws IllegalStateException if {@code getBiMap} has already been called
   * @return this map builder (to enable call chaining)
   */
  public ImmutableBiMapBuilder<K,V> put(@Nullable K key, @Nullable V value) {
    checkState(biMap != null, "map has already been created");
    biMap.put(key, value);
    return this;
  }

  /**
   * Returns a newly-created, immutable BiMap instance containing the keys and
   * values that were specified using {@code put}.
   *
   * @return a new, immutable {@code BiMap} instance
   * @throws IllegalStateException if {@code getBiMap} has already been called
   */
  public BiMap<K,V> getBiMap() {
    checkState(biMap != null, "map has already been created");
    try {
      return Maps.unmodifiableBiMap(biMap);
    } finally {
      biMap = null;
    }
  }
}
