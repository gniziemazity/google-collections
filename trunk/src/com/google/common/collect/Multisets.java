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
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
public final class Multisets {
  private static final Multiset<?> EMPTY_MULTISET = new EmptyMultiset<Object>();

  private Multisets() {}

  /**
   * Constructs a new empty {@code HashMultiset} using the default initial
   * capacity (16 distinct elements) and load factor (0.75).
   */
  public static <E> HashMultiset<E> newHashMultiset() {
    return new HashMultiset<E>();
  }

  /**
   * Constructs a new empty {@code HashMultiset} containing the specified
   * elements, using the default initial capacity (16 distinct elements) and
   * load factor (0.75).
   *
   * @param elements the elements that the multiset should contain
   * @throws NullPointerException if {@code elements} is null
   */
  public static <E> HashMultiset<E> newHashMultiset(E... elements) {
    checkNotNull(elements);
    HashMultiset<E> multiset = new HashMultiset<E>();
    Collections.addAll(multiset, elements);
    return multiset;
  }

  /**
   * Constructs a new {@code HashMultiset} containing the specified elements. If
   * the specified elements is a {@code Multiset}, this method behaves
   * identically to {@link HashMultiset#HashMultiset(Multiset)}. Otherwise, the
   * {@code HashMultiset} is created with the default initial capacity (16
   * distinct elements) and load factor (0.75).
   *
   * @param elements the elements that the multiset should contain
   * @throws NullPointerException if {@code elements} is null
   */
  public static <E> HashMultiset<E> newHashMultiset(
      Iterable<? extends E> elements) {
    return new HashMultiset<E>(elements);
  }

  /**
   * Creates an empty {@link TreeMultiset} instance.
   *
   * <p>TODO(mbostock): change the type parameter to {@code <E extends
   * Comparable<? super E>}, or at least {@code <E extends Comparable<E>>}.
   *
   * @return a newly-created, initially-empty TreeMultiset
   */
  public static <E> TreeMultiset<E> newTreeMultiset() {
    return new TreeMultiset<E>();
  }

  /**
   * Creates an empty {@link TreeMultiset} instance, sorted according to the
   * specified comparator.
   *
   * @return a newly-created, initially-empty TreeMultiset
   */
  public static <E> TreeMultiset<E> newTreeMultiset(Comparator<? super E> c) {
    return new TreeMultiset<E>(c);
  }

  /**
   * Returns an unmodifiable view of the specified multiset. Query operations
   * on the returned multiset "read through" to the specified multiset, and
   * attempts to modify the returned multiset, whether direct or via its
   * element set or iterator, result in an UnsupportedOperationException.
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
   * @param multiset the multiset to be wrapped
   * @return a sychronized view of the specified multiset
   */
  public static <E> Multiset<E> synchronizedMultiset(Multiset<E> multiset) {
    return Synchronized.multiset(multiset, null);
  }

  /**
   * Returns a dynamically typesafe view of the specified multiset. Any attempt
   * to insert an element of the wrong type will result in an immediate {@code
   * ClassCastException}. Assuming the multiset contains no incorrectly typed
   * elements prior to the time a dynamically typesafe view is generated, and
   * that all subsequent access to the multiset takes place through the view, it
   * is <i>guaranteed</i> that the multiset cannot contain an incorrectly typed
   * element.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned multiset will be serializable if the specified multiset is
   * serializable.
   *
   * @param m the multiset for which a dynamically typesafe view is to be
   *          returned
   * @param type the type of element that {@code m} is permitted to hold
   * @return a dynamically typesafe view of the specified multiset
   */
  public static <E> Multiset<E> checkedMultiset(Multiset<E> m, Class<E> type) {
    return Constraints.constrainedMultiset(m,
        Constraints.classConstraint(type));
  }

  /** Returns the empty multiset (immutable). This multiset is serializable. */
  @SuppressWarnings("unchecked")
  public static <E> Multiset<E> emptyMultiset() {
    return (Multiset<E>) EMPTY_MULTISET;
  }

  /** @see #emptyMultiset */
  private static class EmptyMultiset<E> extends AbstractCollection<E>
      implements Multiset<E>, Serializable {
    private static final long serialVersionUID = -4387083049544049902L;
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
    public int remove(Object element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int removeAllOccurrences(Object element) {
      throw new UnsupportedOperationException();
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
   * set, so changes to the set are reflected in the multiset, and vice-versa.
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

  /** @see #forSet */
  static class SetMultiset<E> extends ForwardingCollection<E> // not a Set<E>!
      implements Multiset<E>, Serializable {
    private static final long serialVersionUID = 7787490547740866319L;
    private transient volatile Set<E> elementSet;
    private transient volatile Set<Entry<E>> entrySet;

    SetMultiset(Set<E> set) {
      super(set);
    }

    @SuppressWarnings("unchecked")
    @Override protected Set<E> delegate() {
      return (Set<E>) super.delegate();
    }

    /* Multiset methods */
    public int count(Object element) {
      return delegate().contains(element) ? 1 : 0;
    }
    public boolean add(E element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int remove(Object element, int occurrences) {
      checkArgument(occurrences >= 0);
      return (occurrences > 0) ? removeAllOccurrences(element) : 0;
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

    /* Collection methods */
    public boolean add(E o) {
      throw new UnsupportedOperationException();
    }
    public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    /* Object methods */
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Multiset<?>)) {
        return false;
      }
      Multiset<?> m = (Multiset<?>) o;
      if (m.size() != size()) { // m may have duplicate elements
        return false;
      }
      return delegate().equals(m.elementSet());
    }
    public int hashCode() {
      int sum = 0;
      for (E e : this) {
        sum += ((e == null) ? 0 : e.hashCode()) ^ 1;
      }
      return sum;
    }

    /** @see #elementSet */
    class ElementSet extends ForwardingSet<E> {
      ElementSet() {
        super(SetMultiset.this.delegate());
      }
      public boolean add(E o) {
        throw new UnsupportedOperationException();
      }
      public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
      }
    }

    /** @see #entrySet */
    class EntrySet extends AbstractSet<Entry<E>> {
      public int size() {
        return delegate().size();
      }
      public Iterator<Entry<E>> iterator() {
        return new Iterator<Entry<E>>() {
            Iterator<E> elements = delegate().iterator();
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
      /* TODO(mbostock): faster contains, remove? */
    }
  }

  /**
   * Returns an immutable multiset containing only the specified object. The
   * returned multiset is serializable if the specified object is serializable.
   *
   * @param element the sole object to be stored in the returned multiset
   */
  public static <E> Multiset<E> singletonMultiset(@Nullable E element) {
    return new SingletonMultiset<E>(element);
  }

  /** @see Multisets#singletonMultiset */
  static class SingletonMultiset<E> implements Multiset<E>, Serializable {
    private final E element;
    private transient volatile Set<E> elementSet;
    private transient volatile Set<Entry<E>> entrySet;

    public SingletonMultiset(E element) {
      this.element = element;
    }

    /* Multiset methods */
    public int count(Object element) {
      return Objects.equal(this.element, element) ? 1 : 0;
    }
    public boolean add(E element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int remove(Object element, int occurrences) {
      throw new UnsupportedOperationException();
    }
    public int removeAllOccurrences(Object element) {
      throw new UnsupportedOperationException();
    }
    public Set<E> elementSet() {
      if (elementSet == null) {
        elementSet = Collections.singleton(element);
      }
      return elementSet;
    }
    public Set<Entry<E>> entrySet() {
      if (entrySet == null) {
        entrySet = Collections.singleton(immutableEntry(element, 1));
      }
      return entrySet;
    }

    /* Collection methods */
    public boolean add(E o) {
      throw new UnsupportedOperationException();
    }
    public boolean addAll(Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public boolean contains(Object o) {
      return Objects.equal(element, o);
    }
    public boolean containsAll(Collection<?> c) {
      return ForwardingCollection.containsAllImpl(this, c);
    }
    public boolean isEmpty() {
      return false;
    }
    public Iterator<E> iterator() {
      return elementSet().iterator();
    }
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }
    public int size() {
      return 1;
    }
    public Object[] toArray() {
      return ForwardingCollection.toArrayImpl(this);
    }
    public <T> T[] toArray(T[] a) {
      return ForwardingCollection.toArrayImpl(this, a);
    }

    /* Object methods */
    @Override public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof Multiset<?>)) {
        return false;
      }
      Multiset<?> m = (Multiset<?>) o;
      return (m.size() == 1) && Objects.equal(m.iterator().next(), element);
    }
    @Override public int hashCode() {
      return (element == null) ? 1 : (element.hashCode() ^ 1);
    }
    @Override public String toString() {
      return "[" + element + "]";
    }
  }

  /**
   * Returns an immutable empty {@code Multiset}. Equivalent to {@link
   * Multisets#emptyMultiset}.
   */
  public static <E> Multiset<E> immutableMultiset() {
    return emptyMultiset();
  }

  /**
   * Returns an immutable {@code Multiset} containing the specified element.
   * Equivalent to {@link #singletonMultiset}.
   *
   * @param element the element that the returned multiset should contain
   */
  public static <E> Multiset<E> immutableMultiset(@Nullable E element) {
    return singletonMultiset(element);
  }

  /**
   * Returns an immutable {@code Multiset} containing the specified elements.
   *
   * <p>Unlike an <i>unmodifiable</i> multimap such as that returned by {@link
   * Multimaps#unmodifiableMultimap}, which provides a read-only view of an
   * underlying multimap which may itself be mutable, an <i>immutable</i>
   * multimap makes a copy of the original mappings, so that the returned
   * multimap is <i>guaranteed</i> never to change. This is critical, for
   * example, if the multimap is an element of a {@code HashSet} or a key in a
   * {@code HashMap}.
   *
   * @param elements the elements that the returned multiset should contain
   */
  public static <E> Multiset<E> immutableMultiset(E... elements) {
    switch (elements.length) {
      case 0: return emptyMultiset();
      case 1: return singletonMultiset(elements[0]);
    }
    return unmodifiableMultiset(newHashMultiset(elements));
  }
}
