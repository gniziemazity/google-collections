// Copyright 2006 Google Inc.  All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.List;

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
