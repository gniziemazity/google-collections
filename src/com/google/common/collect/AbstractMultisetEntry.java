// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import com.google.common.base.Objects;
import com.google.common.collect.Multiset.Entry;

/**
 * Implementation of the {@code equals}, {@code hashCode}, and {@code toString}
 * methods of {@link Entry}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class AbstractMultisetEntry<E> implements Entry<E> {

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Entry<?>)) {
      return false;
    }
    Entry<?> e = (Entry<?>) o;
    return Objects.equal(e.getElement(), getElement())
        && (e.getCount() == getCount());
  }

  @Override public int hashCode() {
    E e = getElement();
    return ((e == null) ? 0 : e.hashCode()) ^ getCount();
  }

  /**
   * Returns a string representation of this multiset entry. The string
   * representation consists of the associated element if the associated count
   * is one, and otherwise the associated element followed by the characters " x
   * " (space, x and space) followed by the count. Elements and counts are
   * converted to strings as by {@code String.valueOf}.
   */
  @Override public String toString() {
    String text = String.valueOf(getElement());
    int n = getCount();
    return (n == 1) ? text : (text + " x " + n);
  }

}
