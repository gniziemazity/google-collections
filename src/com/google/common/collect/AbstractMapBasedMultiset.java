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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;

/**
 * Basic implementation of {@code Multiset<E>} backed by an instance of
 * {@code Map<E, AtomicInteger>}.
 *
 * @author kevinb
 */
abstract class AbstractMapBasedMultiset<E> extends AbstractMultiset<E>
    implements Serializable {
  private Map<E, AtomicInteger> backingMap; // final, except for clone

  /*
   * Cache the size for efficiency. Using a long lets us avoid the need for
   * overflow checking and ensures that size() will function correctly even if
   * the multiset had once been larger than Integer.MAX_VALUE.
   */
  private long size;

  protected AbstractMapBasedMultiset(Map<E, AtomicInteger> backingMap) {
    this.backingMap = backingMap;
    this.size = super.size();
  }

  protected Map<E, AtomicInteger> backingMap() {
    return backingMap;
  }

  public int count(@Nullable Object element) {
    AtomicInteger frequency = backingMap.get(element);
    return (frequency == null) ? 0 : frequency.get();
  }

  public boolean add(@Nullable E element, int occurrences) {
    checkArgument(occurrences >= 0,
        "occurrences cannot be negative: " + occurrences);
    if (occurrences == 0) {
      return false;
    }
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      backingMap.put(element, new AtomicInteger(occurrences));
    } else {
      checkArgument(occurrences <= Integer.MAX_VALUE - frequency.get(),
          "overflow: cannot add " + occurrences + " to " + frequency.get());
      frequency.getAndAdd(occurrences);
    }
    size += occurrences;
    assert sizeInvariant();
    return true;
  }

  public int remove(@Nullable Object element, int occurrences) {
    checkArgument(occurrences >= 0,
        "occurrences cannot be negative: " + occurrences);
    if (occurrences == 0) {
      return 0;
    }
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = decrement(frequency, occurrences)
        ? occurrences
        : backingMap.remove(element).get();
    size -= numberRemoved;
    assert sizeInvariant();
    return numberRemoved;
  }

  public int removeAllOccurrences(@Nullable Object element) {
    AtomicInteger frequency = backingMap.remove(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = frequency.get();
    size -= numberRemoved;
    assert sizeInvariant();
    return numberRemoved;
  }

  public int size() {
    int size = (int) Math.min(this.size, Integer.MAX_VALUE);
    assert size == super.size();
    return size;
  }

  /**
   * Checks that we have cached the size correctly.
   */
  private boolean sizeInvariant() {
    size(); // this contains the assertion we want
    return true;
  }

  // Overrides AbstractCollection
  @Override public boolean remove(@Nullable Object element) {
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      return false;
    }
    // TODO(kevinb): check if this works in the case that freq could be zero
    if (!decrement(frequency, 1)) {
      backingMap.remove(element);
    }
    size--;
    assert sizeInvariant();
    return true;
  }

  // Override AbstractMultiset to avoid infinite recursion :)
  @Override public void clear() {
    backingMap.clear();
    size = 0L;
    assert sizeInvariant();
  }

  transient volatile Set<E> elementSet;

  public Set<E> elementSet() {
    if (elementSet == null) {
      elementSet = new ForwardingSet<E>(backingMap.keySet()) {
        @Override public Iterator<E> iterator() {
          final Iterator<Map.Entry<E, AtomicInteger>> entries
              = backingMap.entrySet().iterator();
          return new Iterator<E>() {
            Map.Entry<E, AtomicInteger> toRemove = null;

            public boolean hasNext() {
              // TODO(kevinb): figure out what to do if 0-count entries are
              // allowed to exist
              return entries.hasNext();
            }

            public E next() {
              toRemove = entries.next();
              return toRemove.getKey();
            }

            public void remove() {
              checkState(toRemove != null,
                  "no calls to next() since the last call to remove()");
              size -= toRemove.getValue().get();
              entries.remove();
              toRemove = null;
              assert sizeInvariant();
            }
          };
        }

        @Override public boolean remove(Object element) {
          return AbstractMapBasedMultiset.this.removeAllOccurrences(element) != 0;
        }
        @Override public boolean removeAll(Collection<?> elementsToRemove) {
          return AbstractMapBasedMultiset.this.removeAll(elementsToRemove);
        }
        @Override public boolean retainAll(Collection<?> elementsToRetain) {
          return AbstractMapBasedMultiset.this.retainAll(elementsToRetain);
        }
        @Override public void clear() {
          AbstractMapBasedMultiset.this.clear();
        }
      };
    }
    return elementSet;
  }

  transient volatile EntrySet entrySet;

  public Set<Multiset.Entry<E>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  // Override AbstractMultiset ... why doesn't it work otherwise?
  // TODO(kevinb): check for bug
  @Override public boolean retainAll(Collection<?> elementsToRetain) {
    checkNotNull(elementsToRetain);
    Iterator<Map.Entry<E, AtomicInteger>> entries
        = backingMap.entrySet().iterator();
    boolean modified = false;
    while (entries.hasNext()) {
      Map.Entry<E, AtomicInteger> entry = entries.next();
      if (!elementsToRetain.contains(entry.getKey())) {
        size -= entry.getValue().get();
        entries.remove();
        modified = true;
      }
    }
    assert sizeInvariant();
    return modified;
  }

  private class EntrySet extends AbstractSet<Multiset.Entry<E>> {

    @Override
    public int size() {
      return backingMap.size();
    }

    @Override
    public Iterator<Multiset.Entry<E>> iterator() {
      final Iterator<Map.Entry<E, AtomicInteger>> backingEntries
          = backingMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
        Map.Entry<E, AtomicInteger> toRemove = null;

        public boolean hasNext() {
          return backingEntries.hasNext();
        }

        public Multiset.Entry<E> next() {
          // TODO(kevinb): figure out how to skip zero-count entries
          toRemove = backingEntries.next();
          return new EntryImpl<E>(toRemove);
        }

        public void remove() {
          checkState(toRemove != null,
              "no calls to next() since the last call to remove()");
          size -= toRemove.getValue().get();
          backingEntries.remove();
          toRemove = null;
          assert sizeInvariant();
        }
      };
    }

    // This one alone is a significant optimization
    @Override public void clear() {
      AbstractMapBasedMultiset.this.clear();
    }
  }

  private static class EntryImpl<E> extends AbstractMultisetEntry<E> {
    final Map.Entry<E, AtomicInteger> mapEntry;

    EntryImpl(Map.Entry<E, AtomicInteger> mapEntry) {
      this.mapEntry = mapEntry;
    }

    public E getElement() {
      return mapEntry.getKey();
    }
    public int getCount() {
      return mapEntry.getValue().get();
    }
  }

  @Override public Iterator<E> iterator() {
    return new MapBasedMultisetIterator();
  }

  protected class MapBasedMultisetIterator implements Iterator<E> {
    private final Iterator<Map.Entry<E, AtomicInteger>> entryIterator;
    private Map.Entry<E, AtomicInteger> currentEntry;
    private int occurrencesLeft;
    private boolean canRemove;

    MapBasedMultisetIterator() {
      this.entryIterator = backingMap.entrySet().iterator();
    }

    public boolean hasNext() {
      // TODO(kevinb): uh oh, this breaks if we allow zero-count entries
      return occurrencesLeft > 0 || entryIterator.hasNext();
    }

    public E next() {
      if (occurrencesLeft == 0) { // change to while (zero-count entries)
        currentEntry = entryIterator.next();
        occurrencesLeft = currentEntry.getValue().get();
      }
      occurrencesLeft--;
      canRemove = true;
      return currentEntry.getKey();
    }

    public void remove() {
      checkState(canRemove,
          "no calls to next() since the last call to remove()");
      if (!decrement(currentEntry.getValue(), 1)) {
        entryIterator.remove();
      }
      size--;
      canRemove = false;
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>To implement {@link Cloneable}, override this method to make it public,
   * drop the {@code throws} clause, and specify the correct return type. For
   * example:
   *
   * <pre>  @SuppressWarnings("unchecked")
   *  @Override public FooMultiset clone() {
   *    try {
   *      return (FooMultiset) super.clone();
   *    } catch (CloneNotSupportedException e) {
   *      throw new AssertionError(e);
   *    }
   *  }</pre>
   *
   * Since the default ({@code super}) implementation of {@code clone} performs
   * only a shallow copy, you should typically also override {@link
   * #cloneBackingMap} to specify how the backing map is cloned.
   *
   * @see #cloneBackingMap
   */
  @SuppressWarnings("unchecked")
  @Override protected AbstractMapBasedMultiset<E> clone()
      throws CloneNotSupportedException {
    AbstractMapBasedMultiset<E> clone = (AbstractMapBasedMultiset<E>) super.clone();
    clone.backingMap = clone.cloneBackingMap();
    clone.elementSet = null;
    clone.entrySet = null;
    return clone;
  }

  /**
   * Creates and returns a clone of the backing map. This method has the same
   * semantics as {@link #clone}, but in regards to the backing map rather than
   * the multiset. The default behavior is just to return a reference to the
   * backing map.
   *
   * <p>Override this method, <i>leaving it protected</i>, and specify the
   * correct return type. For example:
   *
   * <pre>  @SuppressWarnings("unchecked")
   *  @Override protected Map&lt;E, AtomicInteger> cloneBackingMap() {
   *    HashMap&lt;E, AtomicInteger> map = (HashMap&lt;E, AtomicInteger>) backingMap();
   *    return (Map&lt;E, AtomicInteger>) map.clone();
   *  }</pre>
   *
   * @see #clone
   */
  protected Map<E, AtomicInteger> cloneBackingMap() {
    return backingMap;
  }

  private static boolean decrement(AtomicInteger count, int occurrences) {
    assert occurrences >= 0;
    if (count.get() - occurrences >= 1) {
      count.getAndAdd(-occurrences);
      return true;
    }
    return false;
  }

  /*
   * Inheriting from AbstractMultiset: isEmpty, contains, containsAll, add,
   * add, addAll, removeAll, equals, hashCode, toString, toArray, toArray.
   *
   * TODO(kevinb): check our logic in deciding to inherit these
   */

  private static final long serialVersionUID = 8960755798254249671L;
}
