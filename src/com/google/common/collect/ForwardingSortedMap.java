// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Comparator;
import java.util.SortedMap;

/**
 * A sorted map which forwards all its method calls to another sorted map.
 * Subclasses should override one or more methods to change or add behavior of
 * the backing sorted map as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class ForwardingSortedMap<K, V> extends ForwardingMap<K, V>
    implements SortedMap<K, V> {

  protected ForwardingSortedMap(SortedMap<K, V> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected SortedMap<K, V> delegate() {
    return (SortedMap<K, V>) super.delegate();
  }

  public Comparator<? super K> comparator() {
    return delegate().comparator();
  }

  public K firstKey() {
    return delegate().firstKey();
  }

  public SortedMap<K, V> headMap(K toKey) {
    return delegate().headMap(toKey);
  }

  public K lastKey() {
    return delegate().lastKey();
  }

  public SortedMap<K, V> subMap(K fromKey, K toKey) {
    return delegate().subMap(fromKey, toKey);
  }

  public SortedMap<K, V> tailMap(K fromKey) {
    return delegate().tailMap(fromKey);
  }
}
