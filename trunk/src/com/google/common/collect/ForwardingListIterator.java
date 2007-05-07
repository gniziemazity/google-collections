// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.ListIterator;

/**
 * A list iterator which forwards all its method calls to another list
 * iterator. Subclasses should override one or more methods to change or add
 * behavior of the backing iterator as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class ForwardingListIterator<E> extends ForwardingIterator<E>
    implements ListIterator<E> {

  protected ForwardingListIterator(ListIterator<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected ListIterator<E> delegate() {
    return (ListIterator<E>) super.delegate();
  }

  public void add(E element) {
    delegate().add(element);
  }

  public boolean hasPrevious() {
    return delegate().hasPrevious();
  }

  public int nextIndex() {
    return delegate().nextIndex();
  }

  public E previous() {
    return delegate().previous();
  }

  public int previousIndex() {
    return delegate().previousIndex();
  }

  public void set(E element) {
    delegate().set(element);
  }
}
