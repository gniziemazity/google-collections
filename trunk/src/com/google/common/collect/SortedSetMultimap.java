// Copyright 2007 Google Inc.  All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * A {@link Multimap} whose values for a given are key sorted. It cannot hold
 * duplicate key-value pairs; adding a key-value pair that's already in the
 * multimap has no effect. This interface does not specify the ordering of the
 * multimap's keys.
 *
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link SortedSet} of values, while {@code #entries} returns a
 * {@link Set} of map entries. Though the method signature doesn't say so
 * explicitly, the map returned by {@link #asMap} has {@link SortedSet} values.
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
