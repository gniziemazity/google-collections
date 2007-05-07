// Copyright 2006 Google Inc.  All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A {@link Multimap} that cannot hold duplicate key-value pairs. Adding a
 * key-value pair that's already in the multimap has no effect.
 *
 * <p>The {@link #get}, {@link #removeAll}, and {@link #replaceValues} methods
 * each return a {@link Set} of values, while {@link #entries} returns a {@link
 * Set} of map entries. Though the method signature doesn't say so explicitly,
 * the map returned by {@link #asMap} has {@link Set} values.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public interface SetMultimap<K,V> extends Multimap<K,V> {

  Set<V> get(@Nullable K key);

  Set<V> removeAll(@Nullable Object key);

  /**
   * {@inheritDoc}
   *
   * <p>If the multimap previously contained a mapping for this key and value,
   * this method has no effect.
   */
  boolean put(@Nullable K key, @Nullable V value);

  /**
   * {@inheritDoc}
   *
   * <p>If the multimap previously contained a mapping for one of the specified
   * key-value pairs, that value is ignored.
   */
  void putAll(@Nullable K key, Iterable<? extends V> values);

  /**
   * {@inheritDoc}
   *
   * <p>If the multimap previously contained a mapping for one of the specified
   * key-value pairs, that key-value pair is ignored.
   */
  void putAll(Multimap<? extends K, ? extends V> multimap);

  /**
   * {@inheritDoc}
   *
   * <p>Any duplicates in {@code values} will be stored in the multimap once.
   */
  Set<V> replaceValues(@Nullable K key, Iterable<? extends V> values);

  /**
   * {@inheritDoc}
   *
   * <p>Though the method signature doesn't say so explicitly, the returned map
   * entries have values that are {@link Set}s.
   */
  Set<Map.Entry<K, Collection<V>>> collectionEntries();

  Set<Map.Entry<K, V>> entries();
}
