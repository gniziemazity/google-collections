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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;

/**
 * Synchronized collection views. This class is package-private because it is
 * intended for use only by other synchronized wrappers within this package. It
 * does not represent a complete set of synchronized wrappers. Also, it's easy
 * to misuse.
 *
 * <p>The returned synchronized collection views are serializable if the backing
 * collection and the lock are serializable.
 *
 * @see Multimaps#synchronizedMultimap
 * @see Multisets#synchronizedMultiset
 * @author Mike Bostock
 */
final class Synchronized {
  private Synchronized() {}

  /** Abstract base class for synchronized views. */
  static class SynchronizedObject implements Serializable {
    private final Object delegate;
    protected final Object lock;

    public SynchronizedObject(Object delegate, Object lock) {
      this.delegate = checkNotNull(delegate);
      this.lock = (lock == null) ? this : lock;
    }

    // No equals and hashCode; see ForwardingObject for details.

    @Override public String toString() {
      synchronized (lock) {
        return delegate.toString();
      }
    }

    private static final long serialVersionUID = -5880321047335989868L;
  }

  /**
   * Returns a synchronized (thread-safe) collection backed by the specified
   * collection using the specified lock (mutex). In order to guarantee serial
   * access, it is critical that <b>all</b> access to the backing collection is
   * accomplished through the returned collection.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned collection:
   *
   * <pre>Collection&lt;E&gt; s = Synchronized.collection(
   *      new HashSet&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param collection the collection to be wrapped in a synchronized view
   * @return a synchronized view of the specified collection
   */
  static <E> Collection<E> collection(Collection<E> collection, Object lock) {
    return new SynchronizedCollection<E>(collection, lock);
  }

  /** @see Synchronized#collection */
  static class SynchronizedCollection<E> extends SynchronizedObject
      implements Collection<E> {
    @SuppressWarnings("hiding")
    private final Collection<E> delegate;

    public SynchronizedCollection(Collection<E> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    public boolean add(E o) {
      synchronized (lock) {
        return delegate.add(o);
      }
    }

    public boolean addAll(Collection<? extends E> c) {
      synchronized (lock) {
        return delegate.addAll(c);
      }
    }

    public void clear() {
      synchronized (lock) {
        delegate.clear();
      }
    }

    public boolean contains(Object o) {
      synchronized (lock) {
        return delegate.contains(o);
      }
    }

    public boolean containsAll(Collection<?> c) {
      synchronized (lock) {
        return delegate.containsAll(c);
      }
    }

    public boolean isEmpty() {
      synchronized (lock) {
        return delegate.isEmpty();
      }
    }

    public Iterator<E> iterator() {
      return delegate.iterator(); // manually synchronized
    }

    public boolean remove(Object o) {
      synchronized (lock) {
        return delegate.remove(o);
      }
    }

    public boolean removeAll(Collection<?> c) {
      synchronized (lock) {
        return delegate.removeAll(c);
      }
    }

    public boolean retainAll(Collection<?> c) {
      synchronized (lock) {
        return delegate.retainAll(c);
      }
    }

    public int size() {
      synchronized (lock) {
        return delegate.size();
      }
    }

    public Object[] toArray() {
      synchronized (lock) {
        return delegate.toArray();
      }
    }

    public <T> T[] toArray(T[] a) {
      synchronized (lock) {
        return delegate.toArray(a);
      }
    }

    private static final long serialVersionUID = 184628707078353613L;
  }

  /**
   * Returns a synchronized (thread-safe) set backed by the specified set using
   * the specified lock (mutex). In order to guarantee serial access, it is
   * critical that <b>all</b> access to the backing set is accomplished through
   * the returned set.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned set:
   *
   * <pre>Set&lt;E&gt; s = Synchronized.set(new HashSet&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param set the set to be wrapped in a synchronized view
   * @return a synchronized view of the specified set
   */
  public static <E> Set<E> set(Set<E> set, Object lock) {
    return new SynchronizedSet<E>(set, lock);
  }

  /** @see Synchronized#set */
  static class SynchronizedSet<E> extends SynchronizedCollection<E>
      implements Set<E> {
    @SuppressWarnings("hiding")
    private final Set<E> delegate;

    public SynchronizedSet(Set<E> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    @Override public boolean equals(Object o) {
      synchronized (lock) {
        return delegate.equals(o);
      }
    }

    @Override public int hashCode() {
      synchronized (lock) {
        return delegate.hashCode();
      }
    }

    private static final long serialVersionUID = -1182284868190508661L;
  }

  /**
   * Returns a synchronized (thread-safe) sorted set backed by the specified
   * sorted set using the specified lock (mutex). In order to guarantee serial
   * access, it is critical that <b>all</b> access to the backing sorted set is
   * accomplished through the returned sorted set.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned sorted set:
   *
   * <pre>SortedSet&lt;E&gt; s = Synchronized.sortedSet(
   *      new TreeSet&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param set the sorted set to be wrapped in a synchronized view
   * @return a synchronized view of the specified sorted set
   */
  static <E> SortedSet<E> sortedSet(SortedSet<E> set, Object lock) {
    return new SynchronizedSortedSet<E>(set, lock);
  }

  /** @see Synchronized#sortedSet */
  static class SynchronizedSortedSet<E> extends SynchronizedSet<E>
      implements SortedSet<E> {
    @SuppressWarnings("hiding")
    private final SortedSet<E> delegate;

    public SynchronizedSortedSet(SortedSet<E> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    public Comparator<? super E> comparator() {
      synchronized (lock) {
        return delegate.comparator();
      }
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      synchronized (lock) {
        return sortedSet(delegate.subSet(fromElement, toElement), lock);
      }
    }

    public SortedSet<E> headSet(E toElement) {
      synchronized (lock) {
        return sortedSet(delegate.headSet(toElement), lock);
      }
    }

    public SortedSet<E> tailSet(E fromElement) {
      synchronized (lock) {
        return sortedSet(delegate.tailSet(fromElement), lock);
      }
    }

    public E first() {
      synchronized (lock) {
        return delegate.first();
      }
    }

    public E last() {
      synchronized (lock) {
        return delegate.last();
      }
    }

    private static final long serialVersionUID = 257153630837525973L;
  }

  /**
   * Returns a synchronized (thread-safe) list backed by the specified list
   * using the specified lock (mutex). In order to guarantee serial access, it
   * is critical that <b>all</b> access to the backing list is accomplished
   * through the returned list.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned list:
   *
   * <pre>List&lt;E&gt; l = Synchronized.list(new ArrayList&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = l.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * <p>The returned list implements {@link RandomAccess} if the specified list
   * implements {@code RandomAccess}.
   *
   * @param list the list to be wrapped in a synchronized view
   * @return a synchronized view of the specified list
   */
  static <E> List<E> list(List<E> list, Object lock) {
    return (list instanceof RandomAccess)
        ? new SynchronizedRandomAccessList<E>(list, lock)
        : new SynchronizedList<E>(list, lock);
  }

  /** @see Synchronized#list */
  static class SynchronizedList<E> extends SynchronizedCollection<E>
      implements List<E> {
    @SuppressWarnings("hiding")
    private final List<E> delegate;

    public SynchronizedList(List<E> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    public void add(int index, E element) {
      synchronized (lock) {
        delegate.add(index, element);
      }
    }

    public boolean addAll(int index, Collection<? extends E> c) {
      synchronized (lock) {
        return delegate.addAll(index, c);
      }
    }

    public E get(int index) {
      synchronized (lock) {
        return delegate.get(index);
      }
    }

    public int indexOf(Object o) {
      synchronized (lock) {
        return delegate.indexOf(o);
      }
    }

    public int lastIndexOf(Object o) {
      synchronized (lock) {
        return delegate.lastIndexOf(o);
      }
    }

    public ListIterator<E> listIterator() {
      return delegate.listIterator(); // manually synchronized
    }

    public ListIterator<E> listIterator(int index) {
      return delegate.listIterator(index); // manually synchronized
    }

    public E remove(int index) {
      synchronized (lock) {
        return delegate.remove(index);
      }
    }

    public E set(int index, E element) {
      synchronized (lock) {
        return delegate.set(index, element);
      }
    }

    public List<E> subList(int fromIndex, int toIndex) {
      synchronized (lock) {
        return list(delegate.subList(fromIndex, toIndex), lock);
      }
    }

    @Override public boolean equals(Object o) {
      synchronized (lock) {
        return delegate.equals(o);
      }
    }

    @Override public int hashCode() {
      synchronized (lock) {
        return delegate.hashCode();
      }
    }

    private static final long serialVersionUID = -774310967040756161L;
  }

  /** @see Synchronized#list */
  static class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
      implements RandomAccess {
    public SynchronizedRandomAccessList(List<E> list, Object lock) {
      super(list, lock);
    }
    private static final long serialVersionUID = 700333540904833406L;
  }

  /**
   * Returns a synchronized (thread-safe) multiset backed by the specified
   * multiset using the specified lock (mutex). In order to guarantee serial
   * access, it is critical that <b>all</b> access to the backing multiset is
   * accomplished through the returned multiset.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned multiset:
   *
   * <pre>Multiset&lt;E&gt; s = Synchronized.multiset(
   *      new HashMultiset&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param multiset the multiset to be wrapped
   * @return a synchronized view of the specified multiset
   */
  public static <E> Multiset<E> multiset(Multiset<E> multiset, Object lock) {
    return new SynchronizedMultiset<E>(multiset, lock);
  }

  /** @see Synchronized#multiset */
  static class SynchronizedMultiset<E> extends SynchronizedCollection<E>
      implements Multiset<E> {
    @SuppressWarnings("hiding")
    private final Multiset<E> delegate;

    private transient volatile Set<E> elementSet;
    private transient volatile Set<Entry<E>> entrySet;

    public SynchronizedMultiset(Multiset<E> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    public int count(Object o) {
      synchronized (lock) {
        return delegate.count(o);
      }
    }

    public boolean add(E e, int n) {
      synchronized (lock) {
        return delegate.add(e, n);
      }
    }

    public int remove(Object o, int n) {
      synchronized (lock) {
        return delegate.remove(o, n);
      }
    }

    public int removeAllOccurrences(Object o) {
      synchronized (lock) {
        return delegate.removeAllOccurrences(o);
      }
    }

    public Set<E> elementSet() {
      synchronized (lock) {
        if (elementSet == null) {
          elementSet = typePreservingSet(delegate.elementSet(), lock);
        }
        return elementSet;
      }
    }

    public Set<Entry<E>> entrySet() {
      synchronized (lock) {
        if (entrySet == null) {
          entrySet = typePreservingSet(delegate.entrySet(), lock);
        }
        return entrySet;
      }
    }

    @Override public boolean equals(Object o) {
      synchronized (lock) {
        return delegate.equals(o);
      }
    }

    @Override public int hashCode() {
      synchronized (lock) {
        return delegate.hashCode();
      }
    }

    private static final long serialVersionUID = -1644906276741825553L;
  }

  /**
   * Returns a synchronized (thread-safe) multimap backed by the specified
   * multimap using the specified lock (mutex). In order to guarantee serial
   * access, it is critical that <b>all</b> access to the backing multimap is
   * accomplished through the returned multimap.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when accessing any of the return multimap's collection views:
   *
   * <pre>Multimap&lt;K,V> m = Synchronized.multimap(
   *      new HashMultimap&lt;K,V>(), lock);
   *   ...
   *  Set&lt;K> s = m.keySet();  // Needn't be in synchronized block
   *   ...
   *  synchronized (lock) {
   *    Iterator&lt;K> i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param multimap the multimap to be wrapped in a synchronized view
   * @return a synchronized view of the specified multimap
   */
  public static <K, V> Multimap<K, V> multimap(
      Multimap<K, V> multimap, Object lock) {
    return new SynchronizedMultimap<K, V>(multimap, lock);
  }

  /** @see Synchronized#multimap */
  private static class SynchronizedMultimap<K, V>
      implements Multimap<K, V>, Serializable {
    final Multimap<K, V> delegate;
    final Object lock;

    transient volatile Set<K> keySet;
    transient volatile Collection<V> values;
    transient volatile Collection<Map.Entry<K, V>> entries;
    transient volatile Map<K, Collection<V>> asMap;
    transient volatile Multiset<K> keys;

    SynchronizedMultimap(Multimap<K, V> delegate, Object lock) {
      this.delegate = checkNotNull(delegate);
      this.lock = (lock == null) ? this : lock;
    }

    public int size() {
      synchronized (lock) {
        return delegate.size();
      }
    }

    public boolean isEmpty() {
      synchronized (lock) {
        return delegate.isEmpty();
      }
    }

    public boolean containsKey(Object key) {
      synchronized (lock) {
        return delegate.containsKey(key);
      }
    }

    public boolean containsValue(Object value) {
      synchronized (lock) {
        return delegate.containsValue(value);
      }
    }

    public boolean containsEntry(Object key, Object value) {
      synchronized (lock) {
        return delegate.containsEntry(key, value);
      }
    }

    public Collection<V> get(K key) {
      synchronized (lock) {
        return typePreservingCollection(delegate.get(key), lock);
      }
    }

    public boolean put(K key, V value) {
      synchronized (lock) {
        return delegate.put(key, value);
      }
    }

    public void putAll(K key, Iterable<? extends V> values) {
      synchronized (lock) {
        delegate.putAll(key, values);
      }
    }

    public void putAll(Multimap<? extends K, ? extends V> multimap) {
      synchronized (lock) {
        delegate.putAll(multimap);
      }
    }

    public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
      synchronized (lock) {
        return delegate.replaceValues(key, values); // copy not synchronized
      }
    }

    public boolean remove(Object key, Object value) {
      synchronized (lock) {
        return delegate.remove(key, value);
      }
    }

    public Collection<V> removeAll(Object key) {
      synchronized (lock) {
        return delegate.removeAll(key); // copy not synchronized
      }
    }

    public void clear() {
      synchronized (lock) {
        delegate.clear();
      }
    }

    public Set<K> keySet() {
      synchronized (lock) {
        if (keySet == null) {
          keySet = typePreservingSet(delegate.keySet(), lock);
        }
        return keySet;
      }
    }

    public Collection<V> values() {
      synchronized (lock) {
        if (values == null) {
          values = collection(delegate.values(), lock);
        }
        return values;
      }
    }

    public Collection<Map.Entry<K, V>> entries() {
      synchronized (lock) {
        if (entries == null) {
          entries = typePreservingCollection(delegate.entries(), lock);
        }
        return entries;
      }
    }

    public Map<K, Collection<V>> asMap() {
      synchronized (lock) {
        if (asMap == null) {
          asMap = new SynchronizedAsMap<K, V>(delegate.asMap(), lock);
        }
        return asMap;
      }
    }

    public Multiset<K> keys() {
      synchronized (lock) {
        if (keys == null) {
          keys = multiset(delegate.keys(), lock);
        }
        return keys;
      }
    }

    @Override public boolean equals(Object other) {
      synchronized (lock) {
        return delegate.equals(other);
      }
    }

    @Override public int hashCode() {
      synchronized (lock) {
        return delegate.hashCode();
      }
    }

    private static final long serialVersionUID = 7083631791577112787L;
  }

  /**
   * Returns a synchronized (thread-safe) collection backed by the specified
   * collection using the specified lock (mutex). In order to guarantee serial
   * access, it is critical that <b>all</b> access to the backing collection is
   * accomplished through the returned collection.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned collection:
   *
   * <pre>Collection&lt;E&gt; s = Synchronized.typePreservingCollection(
   *      new HashSet&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * <p>If the specified collection is a {@code SortedSet}, {@code Set} or
   * {@code List}, this method will behave identically to {@link #sortedSet},
   * {@link #set} or {@link #list} respectively, in that order of specificity.
   *
   * @param collection the collection to be wrapped in a synchronized view
   * @return a synchronized view of the specified collection
   */
  private static <E> Collection<E> typePreservingCollection(
      Collection<E> collection, Object lock) {
    if (collection instanceof SortedSet<?>) {
      return sortedSet((SortedSet<E>) collection, lock);
    } else if (collection instanceof Set<?>) {
      return set((Set<E>) collection, lock);
    } else if (collection instanceof List<?>) {
      return list((List<E>) collection, lock);
    } else {
      return collection(collection, lock);
    }
  }

  /**
   * Returns a synchronized (thread-safe) set backed by the specified set using
   * the specified lock (mutex). In order to guarantee serial access, it is
   * critical that <b>all</b> access to the backing collection is accomplished
   * through the returned collection.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when iterating over the returned collection:
   *
   * <pre>Set&lt;E&gt; s = Synchronized.typePreservingSet(
   *      new HashSet&lt;E&gt;(), lock);
   *   ...
   * synchronized (lock) {
   *   Iterator&lt;E&gt; i = s.iterator(); // Must be in synchronized block
   *   while (i.hasNext()) {
   *     foo(i.next());
   *   }
   * }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * <p>If the specified collection is a {@code SortedSet} this method will
   * behave identically to {@link #sortedSet}.
   *
   * @param set the set to be wrapped in a synchronized view
   * @return a synchronized view of the specified set
   */
  public static <E> Set<E> typePreservingSet(Set<E> set, Object lock) {
    if (set instanceof SortedSet<?>) {
      return sortedSet((SortedSet<E>) set, lock);
    } else {
      return set(set, lock);
    }
  }

  /** @see Synchronized#multimap */
  static class SynchronizedAsMapEntries<K, V>
      extends SynchronizedSet<Map.Entry<K, Collection<V>>> {
    @SuppressWarnings("hiding")
    private final Set<Map.Entry<K, Collection<V>>> delegate;

    public SynchronizedAsMapEntries(
        Set<Map.Entry<K, Collection<V>>> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    @Override public Iterator<Map.Entry<K, Collection<V>>> iterator() {
      // The iterator and entry aren't synchronized, but the entry value is.
      return new ForwardingIterator<Map.Entry<K, Collection<V>>>(
          super.iterator()) {
        @Override public Map.Entry<K, Collection<V>> next() {
          return new ForwardingMapEntry<K, Collection<V>>(super.next()) {
            @Override public Collection<V> getValue() {
              return typePreservingCollection(super.getValue(), lock);
            }
          };
        }
      };
    }

    // See java.util.Collections.CheckedEntrySet for details on attacks.

    @Override public Object[] toArray() {
      synchronized (lock) {
        return ForwardingCollection.toArrayImpl(this);
      }
    }
    @Override public <T> T[] toArray(T[] array) {
      synchronized (lock) {
        return ForwardingCollection.toArrayImpl(this, array);
      }
    }
    @Override public boolean contains(Object o) {
      synchronized (lock) {
        return Maps.containsEntryImpl(delegate, o);
      }
    }
    @Override public boolean containsAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.containsAllImpl(this, c);
      }
    }
    @Override public boolean equals(Object o) {
      synchronized (lock) {
        return ForwardingSet.equalsImpl(this, o);
      }
    }
    @Override public boolean remove(Object o) {
      synchronized (lock) {
        return Maps.removeEntryImpl(delegate, o);
      }
    }
    @Override public boolean removeAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.removeAllImpl(this, c);
      }
    }
    @Override public boolean retainAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.retainAllImpl(this, c);
      }
    }

    private static final long serialVersionUID = 794109514199117015L;
  }

  /**
   * Returns a synchronized (thread-safe) map backed by the specified map using
   * the specified lock (mutex). In order to guarantee serial access, it is
   * critical that <b>all</b> access to the backing map is accomplished through
   * the returned map.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when accessing any of the return map's collection views:
   *
   * <pre>Map&lt;K,V> m = Synchronized.map(
   *      new HashMap&lt;K,V>(), lock);
   *   ...
   *  Set&lt;K> s = m.keySet();  // Needn't be in synchronized block
   *   ...
   *  synchronized (lock) {
   *    Iterator&lt;K> i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param map the map to be wrapped in a synchronized view
   * @return a synchronized view of the specified map
   */
  public static <K, V> Map<K, V> map(Map<K, V> map, Object lock) {
    return new SynchronizedMap<K, V>(map, lock);
  }

  /** @see Synchronized#map */
  static class SynchronizedMap<K, V> implements Map<K, V>, Serializable {
    private final Map<K, V> delegate;
    protected final Object lock;

    private transient volatile Set<K> keySet;
    private transient volatile Collection<V> values;
    private transient volatile Set<Map.Entry<K, V>> entrySet;

    public SynchronizedMap(Map<K, V> delegate, Object lock) {
      this.delegate = checkNotNull(delegate);
      this.lock = (lock == null) ? this : lock;
    }

    public void clear() {
      synchronized (lock) {
        delegate.clear();
      }
    }

    public boolean containsKey(Object key) {
      synchronized (lock) {
        return delegate.containsKey(key);
      }
    }

    public boolean containsValue(Object value) {
      synchronized (lock) {
        return delegate.containsValue(value);
      }
    }

    public Set<Map.Entry<K, V>> entrySet() {
      synchronized (lock) {
        if (entrySet == null) {
          entrySet = set(delegate.entrySet(), lock);
        }
        return entrySet;
      }
    }

    public V get(Object key) {
      synchronized (lock) {
        return delegate.get(key);
      }
    }

    public boolean isEmpty() {
      synchronized (lock) {
        return delegate.isEmpty();
      }
    }

    public Set<K> keySet() {
      synchronized (lock) {
        if (keySet == null) {
          keySet = set(delegate.keySet(), lock);
        }
        return keySet;
      }
    }

    public V put(K key, V value) {
      synchronized (lock) {
        return delegate.put(key, value);
      }
    }

    public void putAll(Map<? extends K, ? extends V> t) {
      synchronized (lock) {
        delegate.putAll(t);
      }
    }

    public V remove(Object key) {
      synchronized (lock) {
        return delegate.remove(key);
      }
    }

    public int size() {
      synchronized (lock) {
        return delegate.size();
      }
    }

    public Collection<V> values() {
      synchronized (lock) {
        if (values == null) {
          values = collection(delegate.values(), lock);
        }
        return values;
      }
    }

    @Override public String toString() {
      synchronized (lock) {
        return delegate.toString();
      }
    }

    @Override public boolean equals(Object other) {
      synchronized (lock) {
        return delegate.equals(other);
      }
    }

    @Override public int hashCode() {
      synchronized (lock) {
        return delegate.hashCode();
      }
    }

    private static final long serialVersionUID = -2739593476673006162L;
  }

  /**
   * Returns a synchronized (thread-safe) bimap backed by the specified bimap
   * using the specified lock (mutex). In order to guarantee serial access, it
   * is critical that <b>all</b> access to the backing bimap is accomplished
   * through the returned bimap.
   *
   * <p>It is imperative that the user manually synchronize on the specified
   * lock when accessing any of the return bimap's collection views:
   *
   * <pre>BiMap&lt;K,V> m = Synchronized.biMap(
   *      new HashBiMap&lt;K,V>(), lock);
   *   ...
   *  Set&lt;K> s = m.keySet();  // Needn't be in synchronized block
   *   ...
   *  synchronized (lock) {
   *    Iterator&lt;K> i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param bimap the bimap to be wrapped in a synchronized view
   * @return a synchronized view of the specified bimap
   */
  public static <K, V> BiMap<K, V> biMap(BiMap<K, V> bimap, Object lock) {
    return new SynchronizedBiMap<K, V>(bimap, lock, null);
  }

  /** @see Synchronized#biMap */
  static class SynchronizedBiMap<K, V> extends SynchronizedMap<K, V>
      implements BiMap<K, V>, Serializable {
    @SuppressWarnings("hiding")
    private final BiMap<K, V> delegate;

    @SuppressWarnings("hiding")
    private transient volatile Set<V> values;
    private transient volatile BiMap<V, K> inverse;

    public SynchronizedBiMap(
        BiMap<K, V> delegate, Object lock, BiMap<V, K> inverse) {
      super(delegate, lock);
      this.delegate = delegate;
      this.inverse = inverse;
    }

    @Override public Set<V> values() {
      synchronized (lock) {
        if (values == null) {
          values = set(delegate.values(), lock);
        }
        return values;
      }
    }

    public V forcePut(K key, V value) {
      synchronized (lock) {
        return delegate.forcePut(key, value);
      }
    }

    public BiMap<V, K> inverse() {
      synchronized (lock) {
        if (inverse == null) {
          inverse = new SynchronizedBiMap<V, K>(delegate.inverse(), lock, this);
        }
        return inverse;
      }
    }

    private static final long serialVersionUID = -8892589047022295017L;
  }

  /** @see SynchronizedMultimap#asMap */
  static class SynchronizedAsMap<K, V>
      extends SynchronizedMap<K, Collection<V>> {
    @SuppressWarnings("hiding")
    private final Map<K, Collection<V>> delegate;

    @SuppressWarnings("hiding")
    private transient volatile Set<Map.Entry<K, Collection<V>>> entrySet;
    @SuppressWarnings("hiding")
    private transient volatile Collection<Collection<V>> values;

    public SynchronizedAsMap(Map<K, Collection<V>> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    @Override public Collection<V> get(Object key) {
      synchronized (lock) {
        Collection<V> collection = super.get(key);
        return (collection == null) ? null :
            typePreservingCollection(collection, lock);
      }
    }

    @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
      if (entrySet == null) {
        entrySet = new SynchronizedAsMapEntries<K, V>(
            delegate.entrySet(), lock);
      }
      return entrySet;
    }

    @Override public Collection<Collection<V>> values() {
      if (values == null) {
        values = new SynchronizedAsMapValues<V>(delegate.values(), lock);
      }
      return values;
    }

    @Override public boolean containsValue(Object o) {
      return values().contains(o);
    }

    private static final long serialVersionUID = 794109514199117015L;
  }

  /** @see SynchronizedMultimap#asMap */
  static class SynchronizedAsMapValues<V>
      extends SynchronizedCollection<Collection<V>> {
    SynchronizedAsMapValues(Collection<Collection<V>> delegate, Object lock) {
      super(delegate, lock);
    }

    @Override public Iterator<Collection<V>> iterator() {
      // The iterator isn't synchronized, but its value is.
      return new ForwardingIterator<Collection<V>>(super.iterator()) {
        @Override public Collection<V> next() {
          return typePreservingCollection(super.next(), lock);
        }
      };
    }

    // See java.util.Collections.CheckedEntrySet for details on attacks.
    
    @Override public Object[] toArray() {
      synchronized (lock) {
        return ForwardingCollection.toArrayImpl(this);
      }
    }
    @Override public <T> T[] toArray(T[] array) {
      synchronized (lock) {
        return ForwardingCollection.toArrayImpl(this, array);
      }
    }
    @Override public boolean contains(Object o) {
      synchronized (lock) {
        return ForwardingCollection.containsImpl(this, o);
      }
    }
    @Override public boolean containsAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.containsAllImpl(this, c);
      }
    }
    @Override public boolean remove(Object o) {
      synchronized (lock) {
        return ForwardingCollection.removeImpl(this, o);
      }
    }
    @Override public boolean removeAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.removeAllImpl(this, c);
      }
    }
    @Override public boolean retainAll(Collection<?> c) {
      synchronized (lock) {
        return ForwardingCollection.retainAllImpl(this, c);
      }
    }

    private static final long serialVersionUID = 794109514199117015L;
  }
}
