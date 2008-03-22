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

import java.util.Set;

/**
 * A set which forwards all its method calls to another set. Subclasses should
 * override one or more methods to modify the behavior of the backing set as
 * desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author Kevin Bourrillion
 */
public abstract class ForwardingSet<E> extends ForwardingCollection<E>
    implements Set<E> {

  /**
   * Constructs a forwarding set that forwards to the provided delegate.
   */
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
   * @see java.util.AbstractSet#equals(Object)
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
