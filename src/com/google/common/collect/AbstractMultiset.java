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

import com.google.common.base.Nullable;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class provides a skeletal implementation of the {@link Multiset}
 * interface.  A new multiset implementation can be created easily by extending
 * this class and implementing the {@link #entrySet} method, plus optionally
 * overriding {@link #add(Object, int)} and {@link #remove(Object, int)} to
 * enable modifications to the multiset.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public abstract class AbstractMultiset<E> extends AbstractCollection<E>
    implements Multiset<E> {

  /**
   * {@inheritDoc}
   *
   * This implementation checks to see if the collection being added is a
   * multiset.  If so, it iterates over that multiset's entry set to add the
   * appropriate number of occurrences of each of its elements to this multiset.
   * Otherwise, it iterates over the individual elements of that collection,
   * adding one occurrence at a time.
   */
  public boolean addAll(Collection<? extends E> elementsToAdd) {
    if (elementsToAdd.isEmpty()) {
      return false;
    }
    if (elementsToAdd instanceof Multiset<?>) {
      Multiset<? extends E> that = (Multiset<? extends E>) elementsToAdd;
      for (Entry<? extends E> entry : that.entrySet()) {
        add(entry.getElement(), entry.getCount());
      }
    } else {
      for (E element : elementsToAdd) {
        add(element);
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public boolean contains(@Nullable Object element) {
    return elementSet().contains(element);
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public boolean add(@Nullable E element) {
    add(element, 1);
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public boolean add(E element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public boolean containsAll(Collection<?> elements) {
    checkNotNull(elements);
    return elementSet().containsAll(elements);
  }

  // TODO(kevinb): see about not overriding this anymore
  @Override public boolean removeAll(Collection<?> elementsToRemove) {
    checkNotNull(elementsToRemove);
    boolean modified = false;
    for (Object element : elementsToRemove) {
      modified |= (removeAllOccurrences(element) != 0);
    }
    return modified;
  }

  // TODO(kevinb): see about not overriding this anymore
  @Override public boolean retainAll(Collection<?> elementsToRetain) {
    checkNotNull(elementsToRetain);
    Iterator<Entry<E>> entries = entrySet().iterator();
    boolean modified = false;
    while (entries.hasNext()) {
      Entry<E> entry = entries.next();
      if (!elementsToRetain.contains(entry.getElement())) {
        entries.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public int remove(Object element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public int removeAllOccurrences(Object element) {
    return remove(element, Integer.MAX_VALUE);
  }

  /**
   * {@inheritDoc}
   *
   *  <p>This implementation calls {@link java.util.Set#clear} on the
   * {@link #elementSet}.
   */
  public void clear() {
    entrySet().clear();
  }
  
  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public boolean equals(@Nullable Object other) {
    if (other instanceof Multiset<?>) {
      Multiset<?> that = (Multiset<?>) other;

      // TODO(kevinb): the following should work
      // return entrySet().equals(that.entrySet());
      
      if (this.size() != that.size()) {
        return false;
      }
      for (Entry<E> entry : entrySet()) {
        if (that.count(entry.getElement()) != entry.getCount()) {
          return false;
        }
        return true;
      }

    }
    return false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation returns the hash code of {@link #entrySet}.
   */
  public int hashCode() {
    return entrySet().hashCode();
  }

  /**
   * Returns a string representation of this multiset.  The string
   * representation consists of a list of element-frequency pairs in the order
   * returned by {@link #iterator}, enclosed in brackets ({@code "[]"}).
   * Adjacent pairs are separated by the characters {@code ", "} (comma and
   * space).  Each element-frequency pair is rendered as the element (using
   * {@link String#valueOf}, followed by the string {@code " x "} (space,
   * lowercase letter x, space), followed by the frequency.  When the frequency
   * is 1, the {@code " x 1"} is elided.
   *
   * <p>Example:
   *
   * <pre>
   *    Multiset&lt;String> multiset = new HashMultiset&lt;String>();
   *    multiset.add("a", 3);
   *    multiset.add("b", 2);
   *    multiset.add("c");
   *    return multiset.toString();
   * </pre>
   * might return the string {@code "[a x 3, c, b x 2]"} -- subject to the
   * iteration order.
   *
   * @return a String representation of this multiset
   */
  public String toString() {
    return entrySet().toString();
  }

  /**
   * {@inheritDoc}
   *
   * This implementation
   */
  public Iterator<E> iterator() {
    return new MultisetIterator();
  }

  private class MultisetIterator implements Iterator<E> {
    private final Iterator<Entry<E>> entryIterator;
    private Entry<E> currentEntry;
    private int occurrencesLeft;

    MultisetIterator() {
      this.entryIterator = entrySet().iterator();
    }

    public boolean hasNext() {
      // TODO(kevinb): uh oh, this breaks if we allow zero-count entries
      return occurrencesLeft > 0 || entryIterator.hasNext();
    }

    public E next() {
      if (occurrencesLeft == 0) { // change to while (zero-count entries)
        currentEntry = entryIterator.next();
        occurrencesLeft = currentEntry.getCount();
      }
      occurrencesLeft--;
      return currentEntry.getElement();
    }

    public void remove() {
      AbstractMultiset.this.remove(currentEntry.getElement());
    }
  }

  public int size() {
    long sum = 0L;
    for (Entry<E> entry : entrySet()) {
      sum += entry.getCount();
    }
    return (int) Math.min(sum, Integer.MAX_VALUE);
  }

  public boolean remove(Object element) {
    return remove(element, 1) == 1;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation uses {@link Collections#frequency} and may exhibit
   * phenomenally bad performance in some implementations; you are highly
   * encouraged to override it.
   */
  public int count(Object element) {
    return Collections.frequency(this, element);
  }

  // TODO(kevinb): implement elementSet() in terms of entrySet()

}
