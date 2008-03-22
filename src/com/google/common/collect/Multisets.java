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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides static utility methods for creating and working with {@link
 * Multiset} instances.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 */
public final class Multisets {
  private static final Multiset<?> EMPTY_MULTISET = new EmptyMultiset<Object>();
  private static final Multiset<?> IMMUTABLE_EMPTY_MULTISET =
      unmodifiableMultiset(new EmptyMultiset<Object>());

  private Multisets() {}

  /**
   * Creates a new empty {@code HashMultiset} using the default initial capacity
   * (16 distinct elements) and load factor (0.75).
   */
  public static <E> HashMultiset<E> newHashMultiset() {
    return new HashMultiset<E>();
  }

  /**
   * Creates a new {@code HashMultiset} containing the specified elements, using
   * the default initial capacity (16 distinct elements) and load factor
   * (0.75).
   *
   * @param elements the elements that the multiset should contain
   */
  public static <E> HashMultiset<E> newHashMultiset(E... elements) {
    HashMultiset<E> multiset = new HashMultiset<E>();
    Collections.addAll(multiset, elements);
    return multiset;
  }

  /**
   * Creates a new {@code HashMultiset} containing the specified elements.
   *
   * @param elements the elements that the multiset should contain
   */
  public static <E> HashMultiset<E> newHashMultiset(
      Iterable<? extends E> elements) {
    return new HashMultiset<E>(elements);
  }

  /**
   * Creates an empty {@code TreeMultiset} instance.
   *
   * @return a newly-created, initially-empty TreeMultiset
   */
  @SuppressWarnings("unchecked")  // allow ungenerified Comparable types
  public static <E extends Comparable> TreeMultiset<E> newTreeMultiset() {
    return new TreeMultiset<E>();
  }

  /**
   * Creates an empty {@code TreeMultiset} instance, sorted according to the
   * specified comparator.
   *
   * @return a newly-created, initially-empty TreeMultiset
   */
  public static <E> TreeMultiset<E> newTreeMultiset(Comparator<? super E> c) {
    return new TreeMultiset<E>(c);
  }

  /** Creates a new empty {@code EnumMultiset}. */
  public static <E extends Enum<E>> EnumMultiset<E> newEnumMultiset(
      Class<E> type) {
    return new EnumMultiset<E>(type);
  }

  /**
   * Creates a new {@code EnumMultiset} containing the specified elements.
   *
   * @throws IllegalArgumentException if {@code elements} is empty
   */
  public static <E extends Enum<E>> EnumMultiset<E> newEnumMultiset(
      Iterable<E> elements) {
    return new EnumMultiset<E>(elements);
  }

  /**
   * Creates a new {@code EnumMultiset} containing the specified elements.
   *
   * @throws IllegalArgumentException if {@code elements} is empty
   */
  public static <E extends Enum<E>> EnumMultiset<E> newEnumMultiset(
      E... elements) {
    checkArgument(elements.length > 0,
        "newEnumMultiset requires at least one element");
    EnumMultiset<E> multiset = newEnumMultiset(elements[0].getDeclaringClass());
    Collections.addAll(multiset, elements);
    return multiset;
  }

  /**
   * Returns an unmodifiable view of the specified multiset. Query operations on
   * the returned multiset "read through" to the specified multiset, and
   * attempts to modify the returned multiset, whether direct or via its element
   * set or iterator, result in an UnsupportedOperationException.
   *
   * @param multiset the multiset for which an unmodifiable view is to be
   *     returned
   * @return an unmodifiable view of the specified set
   */
  public static <E> Multiset<E> unmodifiableMultiset(Multiset<E> multiset) {
    return new ForwardingMultiset<E>(multiset) {
      transient volatile Set<E> elementSet;

      @Override public Set<E> elementSet() {
        if (elementSet == null) {
          elementSet = Collections.unmodifiableSet(super.elementSet());
        }
        return elementSet;
      }

      transient volatile Set<Multiset.Entry<E>> entrySet;

      @Override public Set<Multiset.Entry<E>> entrySet() {
        if (entrySet == null) {
          entrySet = Collections.unmodifiableSet(super.entrySet());
        }
        return entrySet;
      }

      @Override public Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(super.iterator());
      }

      @Override public boolean add(E element) {
        throw up();
      }

      @Override public boolean add(E element, int occurences) {
        throw up();
      }

      @Override public boolean addAll(Collection<? extends E> elementsToAdd) {
        throw up();
      }

      @Override public boolean remove(Object element) {
        throw up();
      }

      @Override public int remove(Object element, int occurrences) {
        throw up();
      }

      @Override public int removeAllOccurrences(Object element) {
        throw up();
      }

      @Override public boolean removeAll(Collection<?> elementsToRemove) {
        throw up();
      }

      @Override public boolean retainAll(Collection<?> elementsToRetain) {
        throw up();
      }

      @Override public void clear() {
        throw up();
      }

      UnsupportedOperationException up() {
        return new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns a synchronized (thread-safe) multiset backed by the specified
   * multiset. In order to guarantee serial access, it is critical that
   * <b>all</b> access to the backing multiset is accomplished through the
   * returned multiset.
   *
   * <p>It is imperative that the user manually synchronize on the returned
   * multiset when iterating over any of its collection views:
   *
   * <pre>  Multiset&lt;E&gt; m = Multisets.synchronizedMultiset(
   *      new HashMultiset&lt;E&gt;());
   *   ...
   *  Set&lt;E&gt; s = m.elementSet(); // Needn't be in synchronized block
   *   ...
   *  synchronized (m) { // Synchronizing on m, not s!
   *    Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * <p>For a greater degree of concurrency, you may wish to use a {@link
   * ConcurrentMultiset}.
   *
   * @param multiset the multiset to be wrapped
   * @return a synchronized view of the specified multiset
   */
  public static <E> Multiset<E> synchronizedMultiset(Multiset<E> multiset) {
    return Synchronized.multiset(multiset, null);
  }

  /** Returns the empty multiset (immutable). This multiset is serializable. */
  @SuppressWarnings("unchecked")
  public static <E> Multiset<E> emptyMultiset() {
    return (Multiset<E>) EMPTY_MULTISET;
  }

  /** @see Multisets#emptyMultiset */
  private static class EmptyMultiset<E> extends AbstractCollection<E>
      implements Multiset<E>, Serializable {
    @Override public int size() {
      return 0;
    }

    @Override public Iterator<E> iterator() {
      return Iterators.emptyIterator();
    }

    public int count(Object element) {
      return 0;
    }

    public boolean add(E element, int occurrences) {
      throw new UnsupportedOperationException();
    }

    /*
     * The remove methods return 0, for consistency with
     * Collections.emptySet().remove().
     */

    public int remove(Object element, int occurrences) {
      return 0;
    }

    public int removeAllOccurrences(Object element) {
      return 0;
    }

    public Set<E> elementSet() {
      return Collections.emptySet();
    }

    public Set<Entry<E>> entrySet() {
      return Collections.emptySet();
    }

    @Override public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Multiset<?>)) {
        return false;
      }
      return ((Multiset<?>) obj).isEmpty();
    }

    @Override public int hashCode() {
      return 0;
    }

    @Override public String toString() {
      return "[]";
    }

    private Object readResolve() {
      return EMPTY_MULTISET; // preserve singleton property
    }
    private static final long serialVersionUID = -4387083049544049902L;
  }

  /**
   * Returns an immutable multiset entry with the specified element and count.
   *
   * @param e the element to be associated with the returned entry
   * @param n the count to be associated with the returned entry
   */
  public static <E> Multiset.Entry<E> immutableEntry(final E e, final int n) {
    return new AbstractMultisetEntry<E>() {
      public E getElement() {
        return e;
      }
      public int getCount() {
        return n;
      }
    };
  }

  /**
   * Returns a multiset view of the specified set. The multiset is backed by the
   * set, so changes to the set are reflected in the multiset, and vice versa.
   * If the set is modified while an iteration over the multiset is in progress
   * (except through the iterator's own {@code remove} operation) the results of
   * the iteration are undefined.
   *
   * <p>The multiset supports element removal, which removes the corresponding
   * element from the set. It does not support the {@code add} or {@code addAll}
   * operations.
   *
   * <p>The returned multiset will be serializable if the specified set is
   * serializable.
   *
   * @param set the backing set for the returned multiset view
   */
  public static <E> Multiset<E> forSet(Set<E> set) {
    return new SetMultiset<E>(set);
  }

  /** @see Multisets#forSet */
  private static class SetMultiset<E> extends ForwardingCollection<E>
      implements Multiset<E>, Serializable {
    transient volatile Set<E> elementSet;
    transient volatile Set<Entry<E>> entrySet;

    SetMultiset(Set<E> set) {
      super(set);
    }

    @SuppressWarnings("unchecked")
    @Override protected Set<E> delegate() {
      return (Set<E>) super.delegate();
    }

    public int count(Object element) {
      return delegate().contains(element) ? 1 : 0;
    }

    public boolean add(E element, int occurrences) {
      throw new UnsupportedOperationException();
    }

    public int remove(Object element, int occurrences) {
      if (occurrences == 0) {
        return 0;
      }
      checkArgument(occurrences > 0);
      return removeAllOccurrences(element);
    }

    public int removeAllOccurrences(Object element) {
      return delegate().remove(element) ? 1 : 0;
    }

    public Set<E> elementSet() {
      if (elementSet == null) {
        elementSet = new ElementSet();
      }
      return elementSet;
    }

    public Set<Entry<E>> entrySet() {
      if (entrySet == null) {
        entrySet = new EntrySet();
      }
      return entrySet;
    }

    @Override public boolean add(E o) {
      throw new UnsupportedOperationException();
    }

    @Override public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Multiset<?>)) {
        return false;
      }
      Multiset<?> m = (Multiset<?>) o;
      return size() == m.size() && delegate().equals(m.elementSet());
    }

    @Override public int hashCode() {
      int sum = 0;
      for (E e : this) {
        sum += ((e == null) ? 0 : e.hashCode()) ^ 1;
      }
      return sum;
    }

    /** @see SetMultiset#elementSet */
    class ElementSet extends ForwardingSet<E> {
      ElementSet() {
        super(SetMultiset.this.delegate());
      }

      @Override public boolean add(E o) {
        throw new UnsupportedOperationException();
      }

      @Override public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
      }
    }

    /** @see SetMultiset#entrySet */
    class EntrySet extends AbstractSet<Entry<E>> {
      @Override public int size() {
        return delegate().size();
      }
      @Override public Iterator<Entry<E>> iterator() {
        return new Iterator<Entry<E>>() {
          final Iterator<E> elements = delegate().iterator();

          public boolean hasNext() {
            return elements.hasNext();
          }
          public Entry<E> next() {
            return immutableEntry(elements.next(), 1);
          }
          public void remove() {
            elements.remove();
          }
        };
      }
      // TODO: faster contains, remove?
    }
    private static final long serialVersionUID = 7787490547740866319L;
  }

  /**
   * Returns an immutable empty {@code Multiset}. Equivalent to {@link
   * Multisets#emptyMultiset}, except that the returned multiset's
   * {@code remove} methods throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unchecked")
  public static <E> Multiset<E> immutableMultiset() {
    return (Multiset<E>) IMMUTABLE_EMPTY_MULTISET;
  }

  /**
   * Returns an immutable {@code Multiset} containing the specified elements.
   *
   * <p>Unlike an <i>unmodifiable</i> multiset such as that returned by {@link
   * Multisets#unmodifiableMultiset}, which provides a read-only view of an
   * underlying multiset which may itself be mutable, an <i>immutable</i>
   * multiset makes a copy of the original mappings, so that the returned
   * multiset is <i>guaranteed</i> never to change. This is critical, for
   * example, if the multiset is an element of a {@code HashSet} or a key in a
   * {@code HashMap}.
   *
   * @param elements the elements that the returned multiset should contain
   */
  public static <E> Multiset<E> immutableMultiset(E... elements) {
    return (elements.length == 0)
        ? Multisets.<E>immutableMultiset()
        : unmodifiableMultiset(newHashMultiset(elements));
  }

  /**
   * Returns the expected number of distinct elements given the specified
   * elements. The number of distinct elements is only computed if {@code
   * elements} is an instance of {@code Multiset}; otherwise the default value
   * of 11 is returned.
   */
  static int inferDistinctElements(Iterable<?> elements) {
    if (elements instanceof Multiset<?>) {
      return ((Multiset<?>) elements).elementSet().size();
    }
    return 11; // initial capacity will be rounded up to 16
  }

  /**
   * Returns a comparator that imposes ascending frequency ordering on a
   * collection of objects, using {@code multiset} to determine the frequency of
   * each object. This enables a simple idiom for sorting (or maintaining)
   * collections (or arrays) of objects that are sorted by ascending frequency.
   * For example, suppose {@code multiset} is a multiset of strings. Then:
   *
   * <pre>  Collections.max(m.elementSet(), frequencyOrder(m));</pre>
   *
   * returns a string that occurs most frequently in {@code multiset}.
   *
   * <p>The returned comparator is a view into the backing multiset, so the
   * comparator's behavior will change if the backing multiset changes. This can
   * be dangerous; for example, if the comparator is used by a {@code TreeSet}
   * and the backing multiset changes, the behavior of the {@code TreeSet}
   * becomes undefined. Use a copy of the multiset to isolate against such
   * changes when necessary.
   *
   * @param multiset the multiset specifying the frequency of objects to compare
   */
  public static <T> Comparator<T> frequencyOrder(Multiset<?> multiset) {
    return new FrequencyOrder<T>(multiset);
  }

  /** @see Multisets#frequencyOrder(Multiset) */
  private static class FrequencyOrder<T> implements SerializableComparator<T> {
    final Multiset<?> multiset;

    FrequencyOrder(Multiset<?> multiset) {
      this.multiset = checkNotNull(multiset);
    }

    public int compare(T left, T right) {
      int leftCount = multiset.count(left);
      int rightCount = multiset.count(right);
      return Comparators.compare(leftCount, rightCount);
    }

    @Override public boolean equals(Object object) {
      if (object instanceof FrequencyOrder<?>) {
        FrequencyOrder<?> that = (FrequencyOrder<?>) object;
        return (this.multiset).equals(that.multiset);
      }
      return false;
    }

    @Override public int hashCode() {
      return multiset.hashCode();
    }

    private static final long serialVersionUID = -6424503578659119387L;
  }
}
