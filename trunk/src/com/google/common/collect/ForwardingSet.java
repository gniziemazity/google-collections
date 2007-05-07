// Copyright 2005 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Set;

/**
 * A set which forwards all its method calls to another set. Subclasses should
 * override one or more methods to change or add behavior of the backing set as
 * desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public abstract class ForwardingSet<E> extends ForwardingCollection<E>
    implements Set<E> {

  protected ForwardingSet(Set<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Set<E> delegate() {
    return (Set<E>) super.delegate();
  }

  @Override public boolean equals(Object obj) {
    return delegate().equals(obj);
  }

  @Override public int hashCode() {
    return delegate().hashCode();
  }

  /* Standard implementations from AbstractSet. */

  /**
   * Compares the specified object with the specified set for equality. Returns
   * true if the specified object is also set, the two sets have the same size,
   * and every member of the set {@code o} is contained in the set {@code s}.
   *
   * <p>This method first checks if the object {@code o} is the set {@code s};
   * if so it returns true. Then, it checks if {@code o} is a set whose size is
   * identical to the size of {@code s}; if not, it returns false. If so, it
   * returns {@code s.containsAll((Collection) o)}.
   *
   * @param s the set to be compared for equality with the specified object
   * @param o the object to be compared for equality with the specified set
   * @return true if the object {@code o} is equal to the set {@code s}
   * @see AbstractSet#equals
   */
  @SuppressWarnings("unchecked")
  static boolean equalsImpl(Set<?> s, Object o) {
    if (o == s) {
      return true;
    }
    if (!(o instanceof Set<?>)) {
      return false;
    }
    Set<?> os = (Set) o;
    if (os.size() != s.size()) {
      return false;
    }
    return s.containsAll(os);
  }
}
