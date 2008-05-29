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

import java.util.Collection;
import java.util.Iterator;

/**
 * A non-serializable collection which forwards all its method calls to another
 * collection. Subclasses should override one or more methods to modify the
 * behavior of the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * Except for the absence of serialization and static methods, this class is the
 * same as {@link ForwardingCollection}.
 * 
 * @see ForwardingObject
 * @author Jared Levy
 */
abstract class NonSerializableForwardingCollection<E>
    extends NonSerializableForwardingObject implements Collection<E> {

  /**
   * Constructs a forwarding collection that forwards to the provided delegate.
   */
  protected NonSerializableForwardingCollection(Collection<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Collection<E> delegate() {
    return (Collection<E>) super.delegate();
  }

  public Iterator<E> iterator() {
    return delegate().iterator();
  }

  public int size() {
    return delegate().size();
  }

  public boolean removeAll(Collection<?> collection) {
    return delegate().removeAll(collection);
  }

  public boolean isEmpty() {
    return delegate().isEmpty();
  }

  public boolean contains(Object object) {
    return delegate().contains(object);
  }

  public Object[] toArray() {
    return delegate().toArray();
  }

  public <T>T[] toArray(T[] array) {
    return delegate().toArray(array);
  }

  public boolean add(E element) {
    return delegate().add(element);
  }

  public boolean remove(Object object) {
    return delegate().remove(object);
  }

  public boolean containsAll(Collection<?> collection) {
    return delegate().containsAll(collection);
  }

  public boolean addAll(Collection<? extends E> collection) {
    return delegate().addAll(collection);
  }

  public boolean retainAll(Collection<?> c) {
    return delegate().retainAll(c);
  }

  public void clear() {
    delegate().clear();
  }
}
