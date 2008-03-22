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
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class provides a skeletal implementation of the {@link Multiset}
 * interface. A new multiset implementation can be created easily by extending
 * this class and implementing the {@link Multiset#entrySet()} method, plus
 * optionally overriding {@link #add(Object, int)} and
 * {@link #remove(Object, int)} to enable modifications to the multiset.
 *
 * <p>The {@link #contains}, {@link #containsAll}, {@link #count}, and
 * {@link #size} implementations all iterate across the set returned by
 * {@link Multiset#entrySet()}, as do many methods acting on the set returned by
 * {@link #elementSet}. Override those methods for better performance.
 * 
 * @author Kevin Bourrillion
 */
public abstract class AbstractMultiset<E> extends AbstractCollection<E>
    implements Multiset<E> {
  public abstract Set<Entry<E>> entrySet();

  // Query Operations

  /**
   * {@inheritDoc}
   *
   * <p>This implementation iterates across {@link Multiset#entrySet()} and
   * sums the counts of the entries.
   */
  @Override public int size() {
    long sum = 0L;
    for (Entry<E> entry : entrySet()) {
      sum += entry.getCount();
    }
    return (int) Math.min(sum, Integer.MAX_VALUE);
  }

  @Override public boolean isEmpty() {
    return entrySet().isEmpty();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation checks whether {@link #elementSet} contains the
   * element.
   */
  @Override public boolean contains(@Nullable Object element) {
    return elementSet().contains(element);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation usually invokes methods of the
   * {@link Multiset#entrySet()} iterator. As the only exception, the iterator's
   * {@code remove} method sometimes calls the multiset's {@code remove}.
   */
  @Override public Iterator<E> iterator() {
    return new MultisetIterator();
  }

  private class MultisetIterator implements Iterator<E> {
    private final Iterator<Entry<E>> entryIterator;
    private Entry<E> currentEntry;
    /** Count of subsequent elements equal to current element */
    private int laterCount;
    /** Count of all elements equal to current element */
    private int totalCount;
    private boolean canRemove;

    MultisetIterator() {
      this.entryIterator = entrySet().iterator();
    }

    public boolean hasNext() {
      return laterCount > 0 || entryIterator.hasNext();
    }

    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      if (laterCount == 0) {
        currentEntry = entryIterator.next();
        totalCount = laterCount = currentEntry.getCount();
      }
      laterCount--;
      canRemove = true;
      return currentEntry.getElement();
    }

    public void remove() {
      checkState(canRemove,
          "no calls to next() since the last call to remove()");
      if (totalCount == 1) {
        entryIterator.remove();
      } else {
        AbstractMultiset.this.remove(currentEntry.getElement());
      }
      totalCount--;
      canRemove = false;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation iterates across {@link Multiset#entrySet()} and
   * sums the count of all entries.
   */
  public int count(Object element) {
    for (Entry<E> entry : entrySet()) {
      if (Objects.equal(entry.getElement(), element)) {
        return entry.getCount();
      }
    }
    return 0;
  }

  // Modification Operations

  /**
   * {@inheritDoc}
   *
   * <p>This implementation calls {@link #add(Object, int)} with one occurrence.
   */
  @Override public boolean add(@Nullable E element) {
    add(element, 1);
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation always throws an
   * {@link UnsupportedOperationException}. To support adding elements, override
   * it.
   */
  public boolean add(E element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation calls {@link #remove(Object,int)} with 1 occurrence.
   */
  @Override public boolean remove(Object element) {
    return remove(element, 1) == 1;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation always throws an
   * {@link UnsupportedOperationException}. To support removing elements,
   * override it.
   */
  public int remove(Object element, int occurrences) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation calls {@link #remove(Object, int)} with
   * {@code Integer.MAX_VALUE} occurrences.
   */
  public int removeAllOccurrences(Object element) {
    return remove(element, Integer.MAX_VALUE);
  }

  // Bulk Operations

  /**
   * {@inheritDoc}
   *
   * <p>This implementation checks whether {@link #elementSet} contains the
   * elements.
   */
  @Override public boolean containsAll(Collection<?> elements) {
    return elementSet().containsAll(elements);
  }

  /**
   * {@inheritDoc}
   *
   * <p>If the collection being added is a multiset, this implementation
   * iterates over that multiset's entry set to add the appropriate number of
   * occurrences of each of its elements to this multiset. Otherwise, it calls
   * {@link AbstractCollection#addAll}.
   */
  @Override public boolean addAll(Collection<? extends E> elementsToAdd) {
    if (elementsToAdd.isEmpty()) {
      return false;
    }
    if (elementsToAdd instanceof Multiset<?>) {
      @SuppressWarnings("unchecked")
      Multiset<? extends E> that = (Multiset<? extends E>) elementsToAdd;
      for (Entry<? extends E> entry : that.entrySet()) {
        add(entry.getElement(), entry.getCount());
      }
    } else {
      super.addAll(elementsToAdd);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation iterates over the elements in the collection and
   * calls {@link #removeAllOccurrences} on each element. In some cases, this
   * approach has better performance than {@link AbstractCollection#removeAll}.
   */
  @Override public boolean removeAll(Collection<?> elementsToRemove) {
    boolean modified = false;
    for (Object element : elementsToRemove) {
      if (removeAllOccurrences(element) != 0) {
        modified = true;
      }
    }
    return modified;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation iterates over {@link #entrySet()}, checking each
   * entry's element to see if it's contained in the provided collection.
   * If it's not found, the {@code remove} method of the entry set's
   * iterator is invoked. In some cases, this approach has better performance
   * than {@link AbstractCollection#removeAll}.
   */
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
   * <p>This implementation calls {@code clear} on {@link Multiset#entrySet()}.
   */
  @Override public void clear() {
    entrySet().clear();
  }

  // Views

  private transient volatile Set<E> elementSet;

  /**
   * {@inheritDoc}
   *
   * <p>The returned set's methods are implemented by calling
   * {@link Multiset#entrySet()} methods.
   */
  public Set<E> elementSet() {
    if (elementSet == null) {
      elementSet = createElementSet();
    }
    return elementSet;
  }

  /**
   * Creates a new instance of this multiset's element set, which will be
   * returned by {@link #elementSet}.
   */
  protected Set<E> createElementSet() {
    return new ElementSet();
  }

  private class ElementSet extends AbstractSet<E> {
    @Override public Iterator<E> iterator() {
      final Iterator<Entry<E>> entryIterator = entrySet().iterator();
      return new Iterator<E>() {
        public boolean hasNext() {
          return entryIterator.hasNext();
        }
        public E next() {
          return entryIterator.next().getElement();
        }
        public void remove() {
          entryIterator.remove();
        }
      };
    }
    @Override public int size() {
      return entrySet().size();
    }
  }

  // Object methods

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns {@code true} if {@code other} is a multiset
   * of the same size and if, for each element, the two multisets have the same
   * count.
   */
  @Override public boolean equals(@Nullable Object other) {
    if (other instanceof Multiset<?>) {
      Multiset<?> that = (Multiset<?>) other;
      /*
       * We can't simply check whether the entry sets are equal, since that
       * approach fails when a TreeMultiset has a comparator that returns 0
       * when passed unequal elements.
       */
      
      if (this.size() != that.size()) {
        return false;
      }
      
      for (Entry<?> entry : that.entrySet()) {
        if (count(entry.getElement()) != entry.getCount()) {
          return false;
        }
      }
      
      return true;
    }
    
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the hash code of {@link
   * Multiset#entrySet()}.
   */
  @Override public int hashCode() {
    return entrySet().hashCode();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the result of invoking {@code toString} on
   * {@link Multiset#entrySet()}.
   */
  @Override public String toString() {
    return entrySet().toString();
  }
}
