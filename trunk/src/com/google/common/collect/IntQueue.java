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
 * High speed queue of integer elements.  Implemented atop a power-of-two
 * length array of ints that is used as a circular buffer, and grown as
 * necessary.
 *
 * @author jjb@google.com (Josh Bloch)
 */
public final class IntQueue implements Serializable, Cloneable {
  private static final long serialVersionUID = 7259425973867757295L;

  /**
   * The array in which the elements of in the queue are stored.
   * The capacity of the queue is the length of this array, which
   * is always a power of two.
   */
  private int[] elements;

  /**
   * The number of elements in the queue.
   */
  private int size = 0;

  /**
   * The index of the next element to be removed from the queue (or
   * The index at which the next element would be added to the queue,
   * if the queue is empty).
   */
  private int head = 0;

  /**
   * The index at which the next element would be added to the queue
   * (or the index of the next element to be removed from the queue
   * if the queue is full).
   */
  private int tail = 0;

  /**
   * The minimum capacity that we'll use for a newly created queue.
   * Must be a power of 2.
   */
  private static final int MIN_INITIAL_CAPACITY = 8;

  /**
   * Constructs an empty queue with the an initial capacity sufficient to
   * hold the specified number of elements.
   *
   * @param expectedSize lower bound on initial capacity of the queue
   */
  public IntQueue(int expectedSize) {
    int initialCapacity = MIN_INITIAL_CAPACITY;
    if (expectedSize > initialCapacity) {
      // Calculate smallest power of 2 >= expectedSize
      initialCapacity = expectedSize - 1;
      initialCapacity |= (initialCapacity >>>  1);
      initialCapacity |= (initialCapacity >>>  2);
      initialCapacity |= (initialCapacity >>>  4);
      initialCapacity |= (initialCapacity >>>  8);
      initialCapacity |= (initialCapacity >>> 16);
      initialCapacity++;

      if (initialCapacity < 0) { // Two many elements, must back off
        initialCapacity >>>= 1;  // Good luck allocating 2 ^ 30 ints
      }
    }
    elements = new int[initialCapacity];
  }

  /**
   * Constructs an empty queue with the an initial capacity sufficient to
   * hold 16 elements.
   */
  public IntQueue() {
    elements = new int[16];
  }

  /**
   * Puts the specified element on this queue.
   */
  public void add(int element) {
    if (head == tail && size != 0) { // Array is full
      growAndAdd(element);
    } else {
      elements[tail] = element;
      tail = (tail + 1) & (elements.length - 1);
      size++;
    }
  }

  /**
   * Puts the specified element on this queue.
   *
   * @deprecated Replaced by {@link #add}.
   */
  @Deprecated public void enqueue(int element) {
    add(element);
  }

  /**
   * Retrieves and removes the head of this queue.
   *
   * @return the head of this queue
   * @throws NoSuchElementException if this queue is empty
   */
  public int remove() {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    int result = elements[head];
    head = (head + 1) & (elements.length - 1);
    size--;
    return result;
  }

  /**
   * Retrieves and removes the head of this queue.
   *
   * @return the head of this queue
   * @throws NoSuchElementException if this queue is empty
   * @deprecated Replaced by {@link #add}.
   */
  @Deprecated public int dequeue() {
    return remove();
  }

  /**
   * Retrieves, but does not remove, the head of this queue.
   *
   * @return the head of this queue
   * @throws NoSuchElementException if this queue is empty
   */
  public int element() {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    return elements[head];
  }

  /**
   * Retrieves, but does not remove, the head of this queue.
   *
   * @return the head of this queue
   * @throws NoSuchElementException if this queue is empty
   * @deprecated Replaced by {@link #element}.
   */
  @Deprecated public int peek() {
    return element();
  }

  /**
   * Returns the number of elements in this queue.
   */
  public int size() {
    return size;
  }

  /**
   * Returns {@code true} if this queue contains no elements.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Removes all elements from the queue.
   */
  public void clear() {
    head = tail = size = 0;
  }

  /**
   * Returns an array containing all of the elements in this queue. The elements
   * are returned in the order they would be returned by successive calls to
   * {@link #remove}. The returned array is "safe" in that no references to it
   * are maintained by this queue. The caller is thus free to modify the
   * returned array.
   */
  public int[] toArray() {
    int[] array = new int[size];
    for (int i = 0; i < size; i++) {
      array[i] = elements[(head + i) & (elements.length - 1)];
    }
    return array;
  }

  /**
   * Returns a string representation of this queue. The string representation
   * consists of a list of the queue's elements in the order they would be
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
    for (int i = 0; i < size; i++) {
      buf.append(elements[(head + i) & (elements.length - 1)]).append(", ");
    }
    if (size > 0) {
      buf.setLength(buf.length() - 2); // delete trailing comma and space
    }
    buf.append(']');
    return buf.toString();
  }

  @SuppressWarnings("unchecked")
  @Override public IntQueue clone() {
    IntQueue clone;
    try {
      clone = (IntQueue) super.clone();
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

    elements[tail] = element;
    tail = (tail + 1) & (elements.length - 1);
    size++;
  }

  /**
   * Double the capacity of this queue.
   */
  private void doubleCapacity() {
    int oldCapacity = elements.length;
    int newCapacity = oldCapacity << 1;
    if (newCapacity < 0) {
      throw new IllegalStateException("Sorry, queue too big");
    }
    int[] newElements = new int[newCapacity];
    System.arraycopy(elements, head, newElements, 0, oldCapacity - head);
    System.arraycopy(elements, 0, newElements, oldCapacity - head, head);
    head = 0;
    tail = oldCapacity;
    elements = newElements;
  }
}

