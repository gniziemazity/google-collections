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
import java.util.Arrays;

/**
 * A convenient way to populate immutable Multimap instances, especially
 * static-final "constant Multimaps". Code such as
 *
 * <pre>
 *   static final Multimap&lt;String,Integer&gt; STRING_TO_INTEGER_MULTIMAP
 *       = createNumbersMap();
 *
 *   static Multimap&lt;String,Integer&gt; createNumbersMap() {
 *     Multimap&lt;String,Integer&gt; multimap = Multimaps.newHashMultimap();
 *     multimap.put("one", 1);
 *     multimap.putAll("several", Arrays.asList(1, 2, 3));
 *     multimap.putAll("many", Arrays.asList(1, 2, 3, 4, 5));
 *     return Multimaps.unmodifiableMultimap(multimap);
 *   }
 * </pre>
 * ... can be rewritten far more simply as ...
 * <pre>
 *   static final Multimap&lt;String,Integer&gt; STRING_TO_INTEGER_MULTIMAP
 *     = new ImmutableMultimapBuilder&lt;String,Integer&gt;()
 *       .put("one", 1)
 *       .putAll("several", 1, 2, 3)
 *       .putAll("many", 1, 2, 3, 4, 5)
 *       .getMultimap();
 * </pre>
 * 
 * <p>The generated multimap is a {@link ListMultimap}, which allows duplicate
 * key-value pairs and maintains the value ordering for each key.
 *
 * @author Laura Werner
 * @author Kevin Bourrillion
 */
public class ImmutableMultimapBuilder<K, V> {
  /** A place to accumulate keys and values for the immutable Multimap */
  private ListMultimap<K, V> multimap;

  /** Creates a new ImmutableMultimapBuilder */
  public ImmutableMultimapBuilder() {
    multimap = new ArrayListMultimap<K, V>();
  }

  /**
   * Adds a key-value mapping to the multimap that will be returned by {@code
   * getMultimap}.
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return this multimap builder (to enable call chaining)
   * @throws IllegalStateException if {@code getMultimap} has already been
   *     called
   */
  public ImmutableMultimapBuilder<K, V> put(@Nullable K key, @Nullable V value)
  {
    checkState(multimap != null, "multimap has already been created");
    multimap.put(key, value);
    return this;
  }

  /**
   * Stores a collection of values with the same key into the multimap that will
   * be returned by {@code getMultimap}.
   *
   * @param key key to store in the multimap.
   * @param values values to store in the multimap.
   * @return this multimap builder (to enable call chaining)
   * @throws IllegalStateException if {@code getMultimap} has already been
   *     called
   */
  public ImmutableMultimapBuilder<K, V> putAll(
      @Nullable K key, Iterable<? extends V> values) {
    checkState(multimap != null, "multimap has already been created");
    multimap.putAll(key, values);
    return this;
  }

  /**
   * Stores an array of values with the same key into the multimap that will be
   * returned by {@code getMultimap}.
   *
   * @param key key to store in the multimap.
   * @param values values to store in the multimap.
   * @return this multimap builder (to enable call chaining)
   * @throws IllegalStateException if {@code getMultimap} has already been
   *     called
   */
  public ImmutableMultimapBuilder<K, V> putAll(@Nullable K key, V... values) {
    checkState(multimap != null, "multimap has already been created");
    multimap.putAll(key, Arrays.asList(values));
    return this;
  }

  /**
   * Returns a newly-created, immutable Multimap instance containing the keys
   * and values that were specified using {@code put} and {@code putAll}.
   *
   * @return a new, immutable {@link ListMultimap} instance
   * @throws IllegalStateException if {@code getMultimap} has already been
   *     called
   */
  public ListMultimap<K, V> getMultimap() {
    checkState(multimap != null, "multimap has already been created");
    try {
      return Multimaps.unmodifiableListMultimap(multimap);
    } finally {
      multimap = null;
    }
  }
}
