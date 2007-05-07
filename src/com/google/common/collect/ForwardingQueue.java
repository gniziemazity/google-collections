// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Queue;

/**
 * A queue which forwards all its method calls to another queue. Subclasses
 * should override one or more methods to change or add behavior of the backing
 * queue as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class ForwardingQueue<E> extends ForwardingCollection<E>
    implements Queue<E> {

  protected ForwardingQueue(Queue<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Queue<E> delegate() {
    return (Queue<E>) super.delegate();
  }

  public boolean offer(E o) {
    return delegate().offer(o);
  }

  public E poll() {
    return delegate().poll();
  }

  public E remove() {
    return delegate().remove();
  }

  public E peek() {
    return delegate().peek();
  }

  public E element() {
    return delegate().element();
  }
}
