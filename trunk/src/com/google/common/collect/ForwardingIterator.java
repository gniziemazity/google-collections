// Copyright 2006 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Iterator;

/**
 * An iterator which forwards all its method calls to another iterator.
 * Subclasses should override one or more methods to change or add behavior of
 * the backing iterator as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public abstract class ForwardingIterator<T> extends ForwardingObject
    implements Iterator<T> {

  protected ForwardingIterator(Iterator<T> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Iterator<T> delegate() {
    return (Iterator<T>) super.delegate();
  }

  public boolean hasNext() {
    return delegate().hasNext();
  }

  public T next() {
    return delegate().next();
  }

  public void remove() {
    delegate().remove();
  }
}
