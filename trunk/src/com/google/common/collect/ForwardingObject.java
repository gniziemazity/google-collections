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

import static com.google.common.base.Objects.nonNull;
import java.io.Serializable;

/**
 * An abstract base class for implementing the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * It is not strictly necessary to override any methods on this class, though
 * typical implementations override {@link #delegate()} to specify the correct
 * return type, and add type-specific forwarding methods.
 *
 * <p>As a very contrived example, to decorate an object to use the default
 * implementation of {@code toString()}, you might say:
 *
 * <pre>  return new ForwardingObject(object) {
 *      {@literal @}Override public String toString() {
 *        return delegate().getClass().getName() + '@'
 *            + Integer.toHexString(hashCode());
 *      }
 *    };</pre>
 *
 * This class does <i>not</i> forward the {@code hashCode} and {@code equals}
 * methods through to the backing object, but relies on {@code Object}'s
 * implementation. This is necessary to preserve the symmetry of {@code equals}.
 *
 * <p>Custom definitions of equality are usually based on an interface, such as
 * {@code Set} or {@code List}, so that the implementation of {@code equals} can
 * cast the object being tested for equality to the custom interface. {@code
 * ForwardingObject} implements no such custom interfaces directly; they
 * are implemented only in subclasses. Therefore, forwarding {@code equals}
 * would break symmetry, as the forwarding object might consider itself equal to
 * the object being tested, but the reverse could not be true.
 *
 * <p>This behavior is consistent with the JDK's collection wrappers, such as
 * {@link java.util.Collections#unmodifiableCollection}. Use an
 * interface-specific subclass of {@code ForwardingObject}, such as {@link
 * ForwardingList} to preserve equality behavior.
 *
 * <p>Although this class implements {@link Serializable}, instances will only
 * be serializable if the delegate is serializable.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class ForwardingObject implements Serializable {
  private static final long serialVersionUID = 2301990993511486937L;
  private Object delegate; // not final to allow clone

  /**
   * Constructs a new object which forwards all methods to the specified {@code
   * delegate}.
   *
   * @param delegate the backing object to which methods are forwarded.
   * @throws NullPointerException if {@code delegate} is {@code null}.
   */
  protected ForwardingObject(Object delegate) {
    this.delegate = nonNull(delegate);
  }

  /**
   * Returns the backing delegate object. This method should be overridden to
   * specify the correct return type. For example:
   *
   * <pre>  {@literal @}SuppressWarnings("unchecked")
   *  {@literal @}Override protected Foo delegate() {
   *    return (Foo) super.delegate();
   *  }</pre>
   *
   * This method should always return the same delegate instance that was passed
   * to the constructor.
   */
  protected Object delegate() {
    return delegate;
  }

  /**
   * {@inheritDoc}
   *
   * <p>To implement {@link Cloneable}, override this method to make it public,
   * drop the {@code throws} clause, and specify the correct return type. For
   * example:
   *
   * <pre>  {@literal @}SuppressWarnings("unchecked")
   *  {@literal @}Override public Foo clone() {
   *    try {
   *      return (Foo) super.clone();
   *    } catch (CloneNotSupportedException e) {
   *      throw new AssertionError(e);
   *    }
   *  }</pre>
   *
   * Since the default ({@code super}) implementation of {@code clone} performs
   * only a shallow copy, you should typically also override {@link
   * ForwardingObject#cloneDelegate} to specify how the backing object is
   * cloned.
   *
   * @see ForwardingObject#cloneDelegate
   */
  protected ForwardingObject clone() throws CloneNotSupportedException {
    ForwardingObject clone = (ForwardingObject) super.clone();
    clone.delegate = cloneDelegate();
    return clone;
  }

  /**
   * Creates and returns a clone of the backing delegate object. This method has
   * the same semantics as {@link Object#clone}, but in regards to the delegate
   * object rather than the decorator object. The default behavior is just to
   * return a reference to the underlying delegate.
   *
   * <p>Override this method, <i>leaving it protected</i>, and specify the
   * correct return type. For example:
   *
   * <pre>  @Override protected Foo cloneDelegate() {
   *    return delegate().clone();
   *  }</pre>
   *
   * @see ForwardingObject#clone
   */
  protected Object cloneDelegate() {
    return delegate;
  }

  @Override public String toString() {
    return delegate().toString();
  }

  /* No equals and hashCode. See class comments for details. */
}
