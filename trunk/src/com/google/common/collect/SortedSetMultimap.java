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
import java.util.SortedSet;

/**
 * A {@code Multimap} whose values for a given are key sorted. It cannot hold
 * duplicate key-value pairs; adding a key-value pair that's already in the
 * multimap has no effect. This interface does not specify the ordering of the
 * multimap's keys.
 *
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link SortedSet} of values, while {@code #entries} returns a
 * {@link Set} of map entries. Though the method signature doesn't say so
 * explicitly, the map returned by {@link #asMap} has {@code SortedSet} values.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public interface SortedSetMultimap<K,V> extends SetMultimap<K,V> {

  SortedSet<V> get(@Nullable K key);

  SortedSet<V> removeAll(@Nullable Object key);

  SortedSet<V> replaceValues(@Nullable K key, Iterable<? extends V> values);

  /**
   * {@inheritDoc}
   *
   * <p>Though the method signature doesn't say so explicitly, the returned map
   * has {@link SortedSet} values.
   */
  Map<K, Collection<V>> asMap();
}
