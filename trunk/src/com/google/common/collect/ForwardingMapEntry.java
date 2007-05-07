// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Map;

/**
 * A map entry which forwards all its method calls to another map entry.
 * Subclasses should override one or more methods to change or add behavior of
 * the backing map entry as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author mbostock@google (Mike Bostock)
 */
public abstract class ForwardingMapEntry<K, V> extends ForwardingObject
    implements Map.Entry<K, V> {

  protected ForwardingMapEntry(Map.Entry<K, V> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Map.Entry<K, V> delegate() {
    return (Map.Entry<K, V>) super.delegate();
  }

  public K getKey() {
    return delegate().getKey();
  }

  public V getValue() {
    return delegate().getValue();
  }

  public V setValue(V value) {
    return delegate().setValue(value);
  }

  @Override public boolean equals(Object obj) {
    return delegate().equals(obj);
  }

  @Override public int hashCode() {
    return delegate().hashCode();
  }
}
