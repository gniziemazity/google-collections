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

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * High speed stack of integer elements.  Implemented atop an array of ints
 * that is grown as necessary.
 *
 * @author jjb@google.com (Josh Bloch)
 */
public final class IntStack implements Serializable, Cloneable {
  private static final long serialVersionUID = 5201766662515143823L;

  /**
   * The array in which the stack elements are stored.  The capacity of
   * the stack is the length of this array, which is always a power of two.
   */
  private int[] elements;

  /**
   * The number of elements in the stack.
   */
  private int size = 0;

  /**
   * The minimum capacity that we'll use for a newly created stack.
   */
  private static final int MIN_INITIAL_CAPACITY = 8;

  /**
   * Constructs an empty stack with an initial capacity sufficient to hold the
   * specified number of elements.
   *
   * @param expectedSize lower bound on initial capacity of the stack
   */
  public IntStack(int expectedSize) {
    elements = new int[Math.max(expectedSize, MIN_INITIAL_CAPACITY)];
  }

  /**
   * Constructs an empty stack with an initial capacity sufficient to
   * hold 16 elements.
   */
  public IntStack() {
    elements = new int[16];
  }

  /**
   * Pushes the specified element on this stack.
   */
  public void add(int element) {
    if (size == elements.length) { // Array is full
      growAndAdd(element);
    } else {
      elements[size++] = element;
    }
  }

  /**
   * Pushes the specified element on this stack.
   *
   * @deprecated Replaced by {@link #add}.
   */
  @Deprecated public void push(int element) {
    add(element);
  }

  /**
   * Pops the head of this stack.
   *
   * @return the head of this stack.
   * @throws NoSuchElementException if this stack is empty.
   */
  public int remove() {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    return elements[--size];
  }

  /**
   * Pops the head of this stack.
   *
   * @return the head of this stack.
   * @throws NoSuchElementException if this stack is empty.
   * @deprecated Replaced by {@link #remove}.
   */
  @Deprecated public int pop() {
    return remove();
  }

  /**
   * Retrieves, but does not remove, the head of this stack.
   *
   * @return the head of this stack.
   * @throws NoSuchElementException if this stack is empty.
   */
  public int element() {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    return elements[size - 1];
  }

  /**
   * Returns the number of elements in this stack.
   */
  public int size() {
    return size;
  }

  /**
   * Returns {@code true} if this stack contains no elements.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Removes all elements from the stack.
   */
  public void clear() {
    size = 0;
  }

  /**
   * Returns an array containing all of the elements in this stack. The elements
   * are returned in the order they would be returned by successive calls to
   * {@link #remove}. The returned array is "safe" in that no references to it are
   * maintained by this stack. The caller is thus free to modify the returned
   * array.
   */
  public int[] toArray() {
    int[] array = new int[size];
    for (int i = size - 1, j = 0; i >= 0; i--, j++) {
      array[j] = elements[i];
    }
    return array;
  }

  /**
   * Returns a string representation of this stack. The string representation
   * consists of a list of the stack's elements in the order they would be
   * returned by successive calls to {@link #remove}, enclosed in square
   * brackets ("[]"). Adjacent elements are separated by the characters ", "
   * (comma and space). Elements are converted to strings as by {@link
   * String#valueOf(int)}.
   *
   * <p>This method is equivalent to {@code Arrays.toString(this.toArray())},
   * but is typically faster.
   */
  @Override public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append('[');
    for (int i = size - 1; i >= 0; i--) {
      buf.append(elements[i]).append(", ");
    }
    if (size > 0) {
      buf.setLength(buf.length() - 2); // delete trailing comma and space
    }
    buf.append(']');
    return buf.toString();
  }

  @SuppressWarnings("unchecked")
  @Override public IntStack clone() {
    IntStack clone;
    try {
      clone = (IntStack) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
    clone.elements = (int[]) elements.clone();
    return clone;
  }

  /**
   * This method is really only here to help the optimizer.
   */
  private void growAndAdd(int element) {
    doubleCapacity();
    elements[size++] = element;
  }

  /**
   * Doubles the capacity of the stack.
   */
  private void doubleCapacity() {
    int oldCapacity = elements.length;
    int newCapacity = oldCapacity << 1;
    if (newCapacity < 0) {
      if (oldCapacity == Integer.MAX_VALUE) {
        throw new IllegalStateException("Sorry, stack too big");
      } else {
        newCapacity = Integer.MAX_VALUE;
      }
    }
    int[] newElements = new int[newCapacity];
    System.arraycopy(elements, 0, newElements, 0, oldCapacity);
    elements = newElements;
  }
}
