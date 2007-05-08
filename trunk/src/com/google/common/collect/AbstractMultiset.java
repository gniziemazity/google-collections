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
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Basic implementation of {@code Multiset<E>} backed by an instance of
 * {@code Map<E, Frequency>}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
@SuppressWarnings("SuspiciousMethodCalls")
public abstract class AbstractMultiset<E> extends AbstractCollection<E>
    implements Multiset<E>, Serializable {
  private static final long serialVersionUID = 8960755798254249671L;

  private Map<E, Frequency> backingMap; // final, except for clone

  /*
   * Cache the size for efficiency. Using a long lets us avoid the need for
   * overflow checking and ensures that size() will function correctly even if
   * the multiset had once been larger than Integer.MAX_VALUE.
   */
  private long size;

  protected AbstractMultiset(Map<E, Frequency> backingMap) {
    this.backingMap = backingMap;
    this.size = computeSize();
  }

  protected Map<E, Frequency> backingMap() {
    return backingMap;
  }

  public int count(@Nullable Object element) {
    Frequency frequency = backingMap.get(element);
    return (frequency == null) ? 0 : frequency.get();
  }

  public boolean add(@Nullable E element, int occurrences) {
    checkOccurrences(occurrences);
    if (occurrences == 0) {
      return false;
    }
    doAdd(element, occurrences);
    return true;
  }

  private static void checkOccurrences(int occurrences) {
    checkArgument(occurrences >= 0,
        "occurrences cannot be negative: " + occurrences);
  }

  private void doAdd(E element, int occurrences) {
    Frequency frequency = backingMap.get(element);
    if (frequency == null) {
      backingMap.put(element, new Frequency(occurrences));
    } else {
      frequency.increment(occurrences);
    }
    size += occurrences;
    assert sizeInvariant();
  }

  @Override public boolean addAll(Collection<? extends E> elementsToAdd) {
    if (elementsToAdd.isEmpty()) {
      return false;
    }
    if (elementsToAdd instanceof Multiset<?>) {
      Multiset<? extends E> that = (Multiset<? extends E>) elementsToAdd;
      for (Multiset.Entry<? extends E> entry : that.entrySet()) {
        doAdd(entry.getElement(), entry.getCount());
      }
    } else {
      for (E element : elementsToAdd) {
        add(element);
      }
    }
    return true;
  }

  public int remove(@Nullable Object element, int occurrences) {
    checkOccurrences(occurrences);
    if (occurrences == 0) {
      return 0;
    }
    Frequency frequency = backingMap.get(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = frequency.decrement(occurrences)
        ? occurrences
        : backingMap.remove(element).get();
    size -= numberRemoved;
    assert sizeInvariant();
    return numberRemoved;
  }

  public int removeAllOccurrences(@Nullable Object element) {
    Frequency frequency = backingMap.remove(element);
    if (frequency == null) {
      return 0;
    }
    int numberRemoved = frequency.get();
    size -= numberRemoved;
    assert sizeInvariant();
    return numberRemoved;
  }

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Override public int size() {
    assert sizeInvariant();
    return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) size;
  }

  @Override public boolean contains(@Nullable Object element) {
    return backingMap.containsKey(element);
  }

  @Override public boolean add(@Nullable E element) {
    doAdd(element, 1);
    return true;
  }

  @Override public boolean remove(@Nullable Object element) {
    Frequency frequency = backingMap.get(element);
    if (frequency == null) {
      return false;
    }
    if (!frequency.decrement(1)) {
      backingMap.remove(element);
    }
    size--;
    assert sizeInvariant();
    return true;
  }

  @Override public boolean containsAll(Collection<?> elements) {
    checkNotNull(elements);
    return backingMap.keySet().containsAll(elements);
  }

  @Override public boolean removeAll(Collection<?> elementsToRemove) {
    checkNotNull(elementsToRemove);
    boolean modified = false;
    for (Object element : elementsToRemove) {
      modified |= (removeAllOccurrences(element) != 0);
    }
    return modified;
  }

  @Override public boolean retainAll(Collection<?> elementsToRetain) {
    checkNotNull(elementsToRetain);
    Iterator<Map.Entry<E, Frequency>> entries
        = backingMap.entrySet().iterator();
    boolean modified = false;
    while (entries.hasNext()) {
      Map.Entry<E, Frequency> entry = entries.next();
      if (!elementsToRetain.contains(entry.getKey())) {
        size -= entry.getValue().get();
        entries.remove();
        modified = true;
      }
    }
    assert sizeInvariant();
    return modified;
  }

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
          final Iterator<Map.Entry<E, Frequency>> entries
              = backingMap.entrySet().iterator();
          return new Iterator<E>() {
            Map.Entry<E, Frequency> toRemove = null;

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
              size -= toRemove.getValue().get();
              entries.remove();
              toRemove = null;
              assert sizeInvariant();
            }
          };
        }

        @Override public boolean remove(Object element) {
          return AbstractMultiset.this.removeAllOccurrences(element) != 0;
        }
        @Override public boolean removeAll(Collection<?> elementsToRemove) {
          return AbstractMultiset.this.removeAll(elementsToRemove);
        }
        @Override public boolean retainAll(Collection<?> elementsToRetain) {
          return AbstractMultiset.this.retainAll(elementsToRetain);
        }
        @Override public void clear() {
          AbstractMultiset.this.clear();
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

  private class EntrySet extends AbstractSet<Multiset.Entry<E>> {

    @Override
    public int size() {
      return backingMap.size();
    }

    @Override
    public Iterator<Multiset.Entry<E>> iterator() {
      final Iterator<Map.Entry<E, Frequency>> backingEntries
          = backingMap.entrySet().iterator();
      return new Iterator<Multiset.Entry<E>>() {
        Map.Entry<E, Frequency> toRemove = null;

        public boolean hasNext() {
          return backingEntries.hasNext();
        }

        public Multiset.Entry<E> next() {
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
      AbstractMultiset.this.clear();
    }
  }

  private static class EntryImpl<E> extends AbstractMultisetEntry<E> {
    final Map.Entry<E, Frequency> mapEntry;

    EntryImpl(Map.Entry<E, Frequency> mapEntry) {
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
    return new MultisetIterator(backingMap.entrySet());
  }

  protected class MultisetIterator implements Iterator<E> {
    private final Iterator<Map.Entry<E, Frequency>> entryIterator;
    private Map.Entry<E, Frequency> currentEntry;
    private int occurrencesLeft;
    private boolean canRemove;

    MultisetIterator(Set<Map.Entry<E, Frequency>> entrySet) {
      this.entryIterator = entrySet.iterator();
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
      if (!currentEntry.getValue().decrement(1)) {
        entryIterator.remove();
      }
      size--;
      canRemove = false;
    }
  }

  @Override public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Multiset<?>)) {
      return false;
    }
    Multiset<?> that = (Multiset<?>) obj;
    if (this.size() != that.size()) {
      return false;
    }
    for (Map.Entry<E, Frequency> entry : backingMap.entrySet()) {
      E element = entry.getKey();
      if (that.count(element) != entry.getValue().get()) {
        return false;
      }
    }
    return true;
  }

  @Override public int hashCode() {
    return backingMap.hashCode();
  }

  /**
   * Returns a string representation of this multiset.  The string
   * representation consists of a list of element-frequency pairs in the order
   * returned by {@link Multiset#iterator}, enclosed in brackets ({@code "[]"}).
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
  @Override public String toString() {
    return entrySet().toString();
  }

  /**
   * {@inheritDoc}
   *
   * <p>To implement {@link Cloneable}, override this method to make it public,
   * drop the {@code throws} clause, and specify the correct return type. For
   * example:
   *
   * <pre>  @{@literal @}uppressWarnings("unchecked")
   *  {@literal @}Override public FooMultiset clone() {
   *    try {
   *      return (FooMultiset) super.clone();
   *    } catch (CloneNotSupportedException e) {
   *      throw new AssertionError(e);
   *    }
   *  }</pre>
   *
   * Since the default ({@code super}) implementation of {@code clone} performs
   * only a shallow copy, you should typically also override {@link
   * AbstractMultiset#cloneBackingMap} to specify how the backing map is cloned.
   *
   * @see AbstractMultiset#cloneBackingMap
   */
  @SuppressWarnings("unchecked")
  @Override protected AbstractMultiset<E> clone()
      throws CloneNotSupportedException {
    AbstractMultiset<E> clone = (AbstractMultiset<E>) super.clone();
    clone.backingMap = clone.cloneBackingMap();
    clone.elementSet = null;
    clone.entrySet = null;
    return clone;
  }

  /**
   * Creates and returns a clone of the backing map. This method has the same
   * semantics as {@link Object#clone}, but in regards to the backing map rather
   * than the multiset. The default behavior is just to return a reference to
   * the backing map.
   *
   * <p>Override this method, <i>leaving it protected</i>, and specify the
   * correct return type. For example:
   *
   * <pre>  {@literal @}SuppressWarnings("unchecked")
   *  {@literal @}Override protected Map&lt;E, Frequency> cloneBackingMap() {
   *    HashMap&lt;E, Frequency> map = (HashMap&lt;E, Frequency>) backingMap();
   *    return (Map&lt;E, Frequency>) map.clone();
   *  }</pre>
   */
  protected Map<E, Frequency> cloneBackingMap() {
    return backingMap;
  }

  private long computeSize() {
    long sum = 0L;
    for (Frequency frequency : backingMap.values()) {
      sum += frequency.get();
    }
    return sum;
  }

  private boolean sizeInvariant() {
    return size == computeSize();
  }

  static class Frequency implements Serializable {
    private static final long serialVersionUID = -6533185044256949176L;

    int currentValue;

    Frequency(int initialValue) {
      assert initialValue >= 1;
      this.currentValue = initialValue;
    }

    void increment(int occurrences) {
      assert occurrences >= 0;
      checkArgument(occurrences <= Integer.MAX_VALUE - currentValue,
          "overflow: cannot add " + occurrences + " to " + currentValue);
      currentValue += occurrences;
    }

    boolean decrement(int occurrences) {
      assert occurrences >= 0;
      if (currentValue - occurrences >= 1) {
        currentValue -= occurrences;
        return true;
      }
      return false;
    }

    int get() {
      return currentValue;
    }

    /*
     * We implement hashCode() only so we can mooch off of HashMap.hashCode()
     * to hash our multiset.
     */
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override public int hashCode() {
      return currentValue;
    }
  }
}
