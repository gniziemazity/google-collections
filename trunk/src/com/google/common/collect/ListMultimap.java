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
 * A {@link Multimap} that can hold duplicate key-value pairs and that maintains
 * the insertion ordering of values for a given key.
 *
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link List} of values. Though the method signature doesn't say
 * so explicitly, the map returned by {@link #asMap} has {@link List} values.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public interface ListMultimap<K,V> extends Multimap<K,V> {

  List<V> get(@Nullable K key);

  List<V> removeAll(@Nullable Object key);

  List<V> replaceValues(@Nullable K key, Iterable<? extends V> values);

  /**
   * {@inheritDoc}
   *
   * <p> Though the method signature doesn't say so explicitly, the returned map
   * has {@link List} values.
   */
  Map<K, Collection<V>> asMap();
}
