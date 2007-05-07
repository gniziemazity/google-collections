// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Iterator;

/**
 * Provides an implementation of {@link #toString} for {@code Iterable}
 * instances.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public abstract class AbstractIterable<E> implements Iterable<E> {

  /**
   * Returns a string representation of this iterable. The string representation
   * consists of a list of the iterable's elements in the order they are
   * returned by its iterator, enclosed in square brackets ("[]"). Adjacent
   * elements are separated by the characters ", " (comma and space). Elements
   * are converted to strings as by {@link String#valueOf(Object)}.
   */
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    Iterator<E> i = iterator();
    boolean hasNext = i.hasNext();
    while (hasNext) {
      E o = i.next();
      buf.append((o == this) ? "(this Iterable)" : String.valueOf(o));
      hasNext = i.hasNext();
      if (hasNext) {
        buf.append(", ");
      }
    }
    buf.append("]");
    return buf.toString();
  }
}
