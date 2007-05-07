// Copyright 2006 Google Inc.  All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Objects;

import java.util.Map.Entry;

/**
 * Implementation of the {@code equals}, {@code hashCode}, and {@code toString}
 * methods of {@link Entry}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public abstract class AbstractMapEntry<K,V> implements Entry<K,V> {

  /**
   * {@inheritDoc}
   *
   * <p>This implementation throws an {@link UnsupportedOperationException}.
   * Override this method to support mutable map entries.
   */
  public V setValue(V value) {
    throw new UnsupportedOperationException();
  }

  @Override public String toString() {
    return getKey() + "=" + getValue();
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Entry<?,?>)) {
      return false;
    }
    Entry<?,?> e = (Entry<?,?>) o;
    return Objects.equal(e.getKey(), getKey())
        && Objects.equal(e.getValue(), getValue());
  }

  @Override public int hashCode() {
    K k = getKey();
    V v = getValue();
    return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
  }

}
