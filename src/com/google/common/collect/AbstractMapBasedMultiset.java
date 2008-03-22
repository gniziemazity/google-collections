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
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic implementation of {@code Multiset<E>} backed by an instance of {@code
 * Map<E, AtomicInteger>}.
 *
 * @author Kevin Bourrillion
 */
abstract class AbstractMapBasedMultiset<E> extends AbstractMultiset<E>
    implements Serializable {
  private final Map<E, AtomicInteger> backingMap;

  /*
   * Cache the size for efficiency. Using a long lets us avoid the need for
   * overflow checking and ensures that size() will function correctly even if
   * the multiset had once been larger than Integer.MAX_VALUE.
   */
  private long size;

  protected AbstractMapBasedMultiset(Map<E, AtomicInteger> backingMap) {
    this.backingMap = checkNotNull(backingMap);
    this.size = super.size();
  }

  protected Map<E, AtomicInteger> backingMap() {
    return backingMap;
  }

  // Required Implementations

  private transient volatile EntrySet entrySet;

  /**
   * {@inheritDoc}
   * 
   * <p>Invoking {@link Multiset.Entry#getCount} on an entry in the returned
   * set always returns the current count of that element in the multiset, as
   * opposed to the count at the time the entry was retrieved.
   */
  @Override public Set<Multiset.Entry<E>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  private class EntrySet extends AbstractSet<Multiset.Entry<E>> {
    @Override public Iterator<Multiset.Entry<E>> iterator() {
      final Iterator<Map.Entry<E, AtomicInteger>> backingEntries
          = backingMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
        Map.Entry<E, AtomicInteger> toRemove;

        public boolean hasNext() {
          return backingEntries.hasNext();
        }

        public Multiset.Entry<E> next() {
          final Map.Entry<E, AtomicInteger> mapEntry = backingEntries.next();
          toRemove = mapEntry;
          return new AbstractMultisetEntry<E>() {
            public E getElement() {
              return mapEntry.getKey();
            }
            public int getCount() {
              int count = mapEntry.getValue().get();
              if (count == 0) {
                AtomicInteger frequency = backingMap.get(getElement());
                if (frequency != null) {
                  count = frequency.get();
                }
              }
              return count;
            }
          };
        }

        public void remove() {
          checkState(toRemove != null,
              "no calls to next() since the last call to remove()");
          size -= toRemove.getValue().getAndSet(0);
          backingEntries.remove();
          toRemove = null;
        }
      };
    }

    @Override public int size() {
      return backingMap.size();
    }

    // This seems to be the only method worth overriding for optimization
    @Override public void clear() {
      for (AtomicInteger frequency : backingMap.values()) {
        frequency.set(0);
      }
      backingMap.clear();
      size = 0L;
    }
  }

  // Optimizations - Query Operations

  @Override public int size() {
    return (int) Math.min(this.size, Integer.MAX_VALUE);
  }

  @Override public Iterator<E> iterator() {
    return new MapBasedMultisetIterator();
  }

  /* 
   * Not subclassing AbstractMultiset$MultisetIterator because next() needs to
   * retrieve the Map.Entry<E, AtomicInteger> entry, which can then be used for
   * a more efficient remove() call.
   */
  private class MapBasedMultisetIterator implements Iterator<E> {
    final Iterator<Map.Entry<E, AtomicInteger>> entryIterator;
    Map.Entry<E, AtomicInteger> currentEntry;
    int occurrencesLeft;
    boolean canRemove;

    MapBasedMultisetIterator() {
      this.entryIterator = backingMap.entrySet().iterator();
    }

    public boolean hasNext() {
      return occurrencesLeft > 0 || entryIterator.hasNext();
    }

    public E next() {
      if (occurrencesLeft == 0) {
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
      int frequency = currentEntry.getValue().get();
      if (frequency <= 0) { 
        throw new ConcurrentModificationException();
      }
      if (currentEntry.getValue().addAndGet(-1) == 0) {
        entryIterator.remove();
      }
      size--;
      canRemove = false;
    }
  }

  @Override public int count(@Nullable Object element) {
    AtomicInteger frequency = backingMap.get(element);
    return (frequency == null) ? 0 : frequency.get();
  }

  // Optional Operations - Modification Operations

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if the call would result in more than
   *     {@link Integer#MAX_VALUE} occurrences of {@code element} in this
   *     multiset. 
   */
  @Override public boolean add(@Nullable E element, int occurrences) {
    if (occurrences == 0) {
      return false;
    }
    checkArgument(
        occurrences > 0, "occurrences cannot be negative: %s", occurrences);
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      backingMap.put(element, new AtomicInteger(occurrences));
    } else {
      long newCount = (long) frequency.get() + (long) occurrences;
      checkArgument(newCount <= Integer.MAX_VALUE,
          "too many occurrences: %s", newCount);
      frequency.getAndAdd(occurrences);
    }
    size += occurrences;
    return true;
  }

  @Override public int remove(@Nullable Object element, int occurrences) {
    if (occurrences == 0) {
      return 0;
    }
    checkArgument(
        occurrences > 0, "occurrences cannot be negative: %s", occurrences);
    AtomicInteger frequency = backingMap.get(element);
    if (frequency == null) {
      return 0;
    }

    int numberRemoved;
    if (frequency.get() > occurrences) {
      numberRemoved = occurrences;
    } else {
      numberRemoved = frequency.get();
      backingMap.remove(element);
    }
    
    frequency.addAndGet(-numberRemoved);
    size -= numberRemoved;
    return numberRemoved;
  }

  @Override public int removeAllOccurrences(@Nullable Object element) {
    return removeAllOccurrences(element, backingMap);
  }
  
  private int removeAllOccurrences(@Nullable Object element,
      Map<E, AtomicInteger> map) {
    AtomicInteger frequency = map.remove(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = frequency.getAndSet(0);
    size -= numberRemoved;    
    return numberRemoved;
  }

  // Views

  @Override protected Set<E> createElementSet() {
    return new MapBasedElementSet(backingMap);      
  }

  class MapBasedElementSet extends ForwardingSet<E> {
    /**
     * This mapping is the usually the same as {@code backingMap}, but can
     * be a submap in some implementations.
     */
    private final Map<E, AtomicInteger> map;
    
    MapBasedElementSet(Map<E, AtomicInteger> map) {
      super(map.keySet());
      this.map = map;
    }

    // TODO: a way to not have to write this much code?

    @Override public Iterator<E> iterator() {
      final Iterator<Map.Entry<E, AtomicInteger>> entries
          = map.entrySet().iterator();
      return new Iterator<E>() {
        Map.Entry<E, AtomicInteger> toRemove;

        public boolean hasNext() {
          return entries.hasNext();
        }

        public E next() {
          toRemove = entries.next();
          return toRemove.getKey();
        }

        public void remove() {
          checkState(toRemove != null,
              "no calls to next() since the last call to remove()");
          size -= toRemove.getValue().getAndSet(0);
          entries.remove();
          toRemove = null;
        }
      };
    }

    @Override public boolean remove(Object element) {
      return removeAllOccurrences(element, map) != 0;
    }

    @Override public boolean removeAll(Collection<?> elementsToRemove) {
      return removeAllImpl(this, elementsToRemove);
    }

    @Override public boolean retainAll(Collection<?> elementsToRetain) {
      return retainAllImpl(this, elementsToRetain);
    }

    @Override public void clear() {
      if (map == backingMap) {
        AbstractMapBasedMultiset.this.clear();
      } else {
        Iterator<E> i = iterator();
        while (i.hasNext()) {
          i.next();
          i.remove();
        }
      }
    }

    public Map<E, AtomicInteger> getMap() {
      return map;
    }    
  }
  
  private static final long serialVersionUID = 8960755798254249671L;
}
