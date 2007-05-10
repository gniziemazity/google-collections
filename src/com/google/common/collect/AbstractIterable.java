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

import java.util.Iterator;

/**
 * Provides an implementation of {@code Object#toString} for {@code Iterable}
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
