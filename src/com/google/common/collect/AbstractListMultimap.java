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
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the {@link ListMultimap} interface. It's a wrapper
 * around {@link AbstractMultimap} that converts the returned collections into
 * {@code Lists}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public abstract class AbstractListMultimap<K,V>
    extends AbstractMultimap<K,V> implements ListMultimap<K,V> {

  /**
   * Creates a new AbstractMultimap that uses the provided map.
   *
   * @param map place to store the mapping from each key to its corresponding
   *     values
   */
  protected AbstractListMultimap(Map<K, Collection<V>> map) {
    super(map);
  }

  @Override protected abstract List<V> createCollection();

  @Override public List<V> get(@Nullable K key) {
    return (List<V>) super.get(key);
  }

  @Override public List<V> removeAll(@Nullable Object key) {
    return (List<V>) super.removeAll(key);
  }

  @Override public List<V> replaceValues(
      @Nullable K key, Iterable<? extends V> values) {
    return (List<V>) super.replaceValues(key, values);
  }
}
