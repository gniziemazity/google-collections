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
import java.util.Set;

/**
 * Basic implementation of the {@code SetMultimap} interface. It's a wrapper
 * around {@link AbstractMultimap} that converts the returned collections into
 * {@code Set}s.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public abstract class AbstractSetMultimap<K,V>
    extends AbstractMultimap<K,V> implements SetMultimap<K,V> {

  /**
   * Creates a new AbstractMultimap that uses the provided map.
   *
   * @param map place to store the mapping from each key to its corresponding
   *     values
   */
  protected AbstractSetMultimap(Map<K, Collection<V>> map) {
    super(map);
  }

  @Override protected abstract Set<V> createCollection();

  @Override public Set<V> get(@Nullable K key) {
    return (Set<V>) super.get(key);
  }

  @Override public Set<Map.Entry<K, V>> entries() {
    return (Set<Map.Entry<K, V>>) super.entries();
  }

  @Override public Set<V> removeAll(@Nullable Object key) {
    return (Set<V>) super.removeAll(key);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Any duplicates in {@code values} will be stored in the multimap once.
   */
  @Override public Set<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return (Set<V>) super.replaceValues(key, values);
  }
}
