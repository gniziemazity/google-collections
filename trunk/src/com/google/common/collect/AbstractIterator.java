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

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An abstract Adapter that provides an Iterator interface for certain types
 * of data which are conceptually iterable, but require single-element
 * preloading (e.g. {@link java.io.BufferedReader}). Subclasses need to
 * implement only the {@link #computeNext} template method.
 *
 * <p>Example:
 *
 * <pre>
 * public static Iterator&lt;String> readLines(final BufferedReader in) {
 *   return new AbstractIterator&lt;String>() {
 *     protected String computeNext() {
 *       try {
 *         String result = in.readLine();
 *         if (result == null) {
 *           endOfData();
 *           in.close();
 *         }
 *         return result;
 *       } catch (IOException e) {
 *         throw new RuntimeException(e);
 *       }
 *     }
 *   };
 * }
 * </pre>
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public abstract class AbstractIterator<T> implements Iterator<T> {

  private enum State {
    /** We have computed the next element and haven't returned it yet. */
    READY,

    /** We haven't yet computed or have already returned the element. */
    NOT_READY,

    /** We have reached the end of the data and are finished. */
    DONE,

    /** We've suffered an exception and are kaput. */
    FAILED
  }

  private State state = State.NOT_READY;

  private T next;

  /**
   * Returns the next element. <b>Note:</b> the implementor must call
   * {@link #endOfData} when it has reached the end of the data. Failure to do
   * so could result in an infinite loop.
   *
   * <p>This class invokes {@link #computeNext} during the caller's initial
   * invocation of {@link #hasNext} or {@link #next}, and on the first
   * invocation of {@link #hasNext} or {@link #next} that follows each
   * successful call to {@link #next}. Once the implementor either invokes
   * {@link #endOfData} or throws any exception, {@link #computeNext} is
   * guaranteed to never be called again.
   *
   * <p>If this method throws an exception, it will propagate outward to the
   * {@code hasNext} or {@code next} invocation that invoked this method. Any
   * further attempts to use the iterator will result in
   * {@code IllegalStateException}.
   *
   * @return the next element if there was one.  {@code null} is a valid
   *     element value.  If {@link #endOfData} was called during execution,
   *     the return value will be ignored.
   */
  protected abstract T computeNext();

  /**
   * Implementors of {@link #computeNext} <b>must</b> invoke this method when
   * there is no data left.
   */
  protected final void endOfData() {
    state = State.DONE;
  }

  // Iterator interface

  public final boolean hasNext() {
    checkState(state != State.FAILED);
    switch (state) {
      case DONE:
        return false;
      case READY:
        return true;
    }
    return tryToComputeNext();
  }

  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    state = State.NOT_READY;
    return next;
  }

  /**
   * Unsupported.
   *
   * @throws UnsupportedOperationException always
   */
  public final void remove() {
    throw new UnsupportedOperationException();
  }

  // private helpers

  /** Attempts to get the next element from the implementation class. */
  private boolean tryToComputeNext() {
    state = State.FAILED; // temporary pessimism
    next = computeNext();
    if (state == State.FAILED) {
      state = State.READY; // ok, whew
      return true;
    }
    return false;
  }
}
