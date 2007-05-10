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
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkArgument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Implementation of {@code Multimap} that uses an {@code ArrayList} to store
 * the values for a given key. A {@link HashMap} associates each key with an
 * {@link ArrayList} of values.
 *
 * <p>When iterating through the collections supplied by this class, the
 * ordering of values for a given key agrees with the order in which the values
 * were added.
 *
 * <p>This multimap allows duplicate key-value pairs. After adding a new
 * key-value pair equal to an existing key-value pair, the {@code
 * ArrayListMultimap} will contain entries for both the new value and the old
 * value.
 *
 * <p>Keys and values may be null.
 *
 * <p>These methods runs in constant time: {@code asMap}, {@code containsKey},
 * {@code entries}, {@code get}, {@code isEmpty}, {@code keySet}, {@code put},
 * {@code remove}, {@code removeAll}, {@code size}, and {@code values}. The
 * {@code containsEntry} method has a processing time proportional to the number
 * of values for the provided key.  The processing time of the {@code putAll}
 * and {@code removeAll} methods is proportional to the number of added
 * values. The {@code clear}, and {@code keys} runtime is proportional to the
 * number of distinct keys. The {@code containsValue}, {@code equals}, {@code
 * hashCode}, {@code toString} and {@code clone} processing time scales with the
 * total number of values in the multimap.
 *
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedListMultimap}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public final class ArrayListMultimap<K,V> extends
    AbstractListMultimap<K,V> implements Cloneable {
  private static final long serialVersionUID = -3840170139986607881L;
  private final int collectionCapacity;

  /** Constructs an empty {@code ArrayListMultimap}. */
  public ArrayListMultimap() {
    super(new HashMap<K, Collection<V>>());
    collectionCapacity = 10; // default from ArrayList
  }

  /**
   * Constructs an empty {@code ArrayListMultimap} with the expected number of
   * distinct keys and the expected number of values per distinct key.
   *
   * @param distinctKeys the expected number of distinct keys
   * @param valuesPerKey the expected number of values per distinct key
   * @throws IllegalArgumentException if either argument is negative
   */
  public ArrayListMultimap(int distinctKeys, int valuesPerKey) {
    super(new HashMap<K, Collection<V>>(Maps.capacity(distinctKeys)));
    checkArgument(valuesPerKey >= 0);
    collectionCapacity = valuesPerKey;
  }

  /**
   * Constructs an {@code ArrayListMultimap} with the same mappings as the
   * specified {@code Multimap}.
   */
  public ArrayListMultimap(Multimap<? extends K, ? extends V> multimap) {
    this(); // TODO(mbostock): preserve capacity
    putAll(Objects.nonNull(multimap));
  }

  /**
   * Creates an empty {@code ArrayList} for a collection of values for one key.
   *
   * @return A new {@code ArrayList} containing a collection of values for one
   *     key
   */
  @Override protected List<V> createCollection() {
    return new ArrayList<V>(collectionCapacity);
  }

  @Override public ArrayListMultimap<K,V> clone() {
    return new ArrayListMultimap<K,V>(this);   // okay because we're final
  }

  /*
   * The following methods simply call the superclass methods and are included
   * here for documentation purposes only.
   */

  /**
   * Stores a key-value pair in the multimap.
   *
   * @param key key to store in the multimap
   * @param value value to store in the multimap
   * @return true always
   */
  @Override public boolean put(@Nullable K key, @Nullable V value) {
    return super.put(key, value);
  }

  /**
   * Stores a collection of values with the same key.
   *
   * @param key key to store in the multimap
   * @param values to store in the multimap
   */
  @Override public void putAll(@Nullable K key, Iterable<? extends V> values) {
    super.putAll(key, values);
  }

  /**
   * Copies all of another multimap's key-value pairs into this multimap. The
   * order in which the mappings are added is determined by {@code
   * multimap.entries()}.
   *
   * @param multimap mappings to store in this multimap
   */
  @Override public void putAll(Multimap<? extends K, ? extends V> multimap) {
    super.putAll(multimap);
  }

  /**
   * Stores a collection of values with the same key, replacing any existing
   * multimap values for that key.
   *
   * @param key key to store in the multimap
   * @param values values to store in the multimap
   * @return the collection of replaced values, or an empty collection if no
   *     values were previously associated with the key. The collection is
   *     modifiable, but updating it will have no effect on the multimap.
   */
  @Override public List<V> replaceValues(@Nullable K key,
      Iterable<? extends V> values) {
    return super.replaceValues(key, values);
  }

  /**
   * Returns a collection of all values in the multimap.
   *
   * <p>The iterator generated by the returned collection traverses the values
   * for one key, followed by the values of a second key, and so on. The
   * traversal order for a key's values corresponds to the order in which the
   * values were added to the multimap.
   *
   * <p>Elements may be removed from the returned set. Changes to the set can
   * alter the multimap and visa-versa.
   *
   * @return collection of values, which may include the same value multiple
   *     times if it occurs in multiple mappings
   */
  @Override public Collection<V> values() {
    return super.values();
  }

  /**
   * Returns a collection of all key-value pairs.
   *
   * <p>The iterator generated by the returned collection traverses the values
   * for one key, followed by the values of a second key, and so on. The
   * traversal order for a key's values corresponds to the order in which the
   * values were added to the multimap.
   *
   * <p>The returned collection may be modified. Changes to the collection can
   * alter the multimap and visa-versa.
   *
   * @return the collection of map entries consisting of key-value pairs, which
   *     may include the same key-value entry multiple times if it occurs in
   *     multiple mappings
   */
  @Override public Collection<Entry<K,V>> entries() {
    return super.entries();
  }

  /**
   * Compares the specified object to this multimap for equality.
   *
   * <p>Two {@code ArrayMultimap}s are equal if, for each key, they contain the
   * same values is the same order. If the value orderings disagree, the
   * multimaps will not be considered equal.
   *
   * @return true if the object equals this multimap
   */
  @Override public boolean equals(@Nullable Object other) {
    return super.equals(other);
  }
}
