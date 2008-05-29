/*
 * Copyright (C) 2008 Google Inc.
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
 * A non-serializable set which forwards all its method calls to another
 * collection. Subclasses should override one or more methods to modify the
 * behavior of the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * Except for the absence of serialization and static methods, this class is the
 * same as {@link ForwardingSet}.
 *
 * @see ForwardingObject
 * @author Jared Levy
 */
abstract class NonSerializableForwardingSet<E>
    extends NonSerializableForwardingCollection<E> implements Set<E> {

  /**
   * Constructs a forwarding set that forwards to the provided delegate.
   */
  protected NonSerializableForwardingSet(Set<E> delegate) {
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
}
