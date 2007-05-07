// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Comparator;
import java.util.SortedSet;

/**
 * A sorted set which forwards all its method calls to another sorted set.
 * Subclasses should override one or more methods to change or add behavior of
 * the backing sorted set as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class ForwardingSortedSet<E> extends ForwardingSet<E>
    implements SortedSet<E> {

  protected ForwardingSortedSet(SortedSet<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected SortedSet<E> delegate() {
    return (SortedSet<E>) super.delegate();
  }

  public Comparator<? super E> comparator() {
    return delegate().comparator();
  }

  public E first() {
    return delegate().first();
  }

  public SortedSet<E> headSet(E toElement) {
    return delegate().headSet(toElement);
  }

  public E last() {
    return delegate().last();
  }

  public SortedSet<E> subSet(E fromElement, E toElement) {
    return delegate().subSet(fromElement, toElement);
  }

  public SortedSet<E> tailSet(E fromElement) {
    return delegate().tailSet(fromElement);
  }
}
