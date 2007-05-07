// Copyright 2006 Google Inc.  All Rights Reserved.

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
