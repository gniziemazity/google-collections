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
import java.util.Map;
import java.util.SortedSet;

/**
 * Basic implementation of the {@code SortedSetMultimap} interface. It's a
 * wrapper around {@link AbstractMultimap} that converts the returned
 * collections into {@code SortedSet}s.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public abstract class AbstractSortedSetMultimap<K,V>
    extends AbstractSetMultimap<K,V>
    implements SortedSetMultimap<K,V> {

  /**
   * Creates a new multimap that uses the provided map.
   *
   * @param map place to store the mapping from each key to its corresponding
   *     values
   */
  protected AbstractSortedSetMultimap(Map<K, Collection<V>> map) {
    super(map);
  }

  @Override protected abstract SortedSet<V> createCollection();

  @Override public SortedSet<V> get(@Nullable K key) {
    return (SortedSet<V>) super.get(key);
  }

  @Override public SortedSet<V> removeAll(@Nullable Object key) {
    return (SortedSet<V>) super.removeAll(key);
  }

  @Override public SortedSet<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return (SortedSet<V>) super.replaceValues(key, values);
  }
}
