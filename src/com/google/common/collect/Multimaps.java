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
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides static methods acting on or generating a {@link Multimap}.
 *
 * @author jlevy@google.com (Jared Levy)
 * @author konigsberg@google.com (Robert Konigsberg)
 * @author mbostock@google.com (Mike Bostock)
 */
public final class Multimaps {
  private static final SetMultimap<?,?> EMPTY_MULTIMAP
      = new EmptyMultimap<Object, Object>();
  private static final ListMultimap<?,?> EMPTY_LIST_MULTIMAP
      = new ImmutableMultimapBuilder<Object, Object>().getMultimap();

  private Multimaps() {}

  /**
   * Creates an empty {@code HashMultimap} instance.
   *
   * @return a newly-created, initially-empty {@code HashMultimap}
   */
  public static <K,V> HashMultimap<K,V> newHashMultimap() {
    return new HashMultimap<K,V>();
  }

  /**
   * Creates a {@code HashMultimap} instance initialized with all elements
   * from the supplied {@link Multimap}. If the supplied multimap supports
   * duplicate key-value pairs, those duplicate pairs will only be stored once
   * in the new multimap.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code HashMultimap}
   */
  public static <K,V> HashMultimap<K,V> newHashMultimap(
      Multimap<? extends K, ? extends V> multimap) {
    return new HashMultimap<K,V>(multimap);
  }

  /**
   * Creates an empty {@code ArrayListMultimap} instance.
   *
   * @return a newly-created, initially-empty {@code ArrayListMultimap}
   */
  public static <K,V> ArrayListMultimap<K,V> newArrayListMultimap() {
    return new ArrayListMultimap<K,V>();
  }

  /**
   * Creates an {@code ArrayListMultimap} instance initialized with all elements
   * from the supplied {@link Multimap}.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code ArrayListMultimap}
   */
  public static <K,V> ArrayListMultimap<K,V> newArrayListMultimap(
      Multimap<? extends K, ? extends V> multimap) {
    return new ArrayListMultimap<K,V>(multimap);
  }

  /**
   * Creates an empty {@code LinkedHashMultimap} instance.
   *
   * @return a newly-created, initially-empty {@code LinkedHashMultimap}
   */
  public static <K,V> LinkedHashMultimap<K,V> newLinkedHashMultimap() {
    return new LinkedHashMultimap<K,V>();
  }

  /**
   * Creates a {@code LinkedHashMultimap} instance initialized with all elements
   * from the supplied {@link Multimap}. The ordering follows {@link
   * Multimap#entries()}.  If the supplied multimap supports duplicate key-value
   * those duplicate pairs will only be stored once in the new multimap.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code LinkedHashMultimap}
   */
  public static <K,V> LinkedHashMultimap<K,V> newLinkedHashMultimap(
      Multimap<? extends K, ? extends V> multimap) {
    return new LinkedHashMultimap<K,V>(multimap);
  }

  /**
   * Creates an empty {@code LinkedListMultimap} instance.
   *
   * @return a newly-created, initially-empty {@code LinkedListMultimap}
   */
  public static <K,V> LinkedListMultimap<K,V> newLinkedListMultimap() {
    return new LinkedListMultimap<K,V>();
  }

  /**
   * Creates a {@code LinkedListMultimap} instance initialized with all elements
   * from the supplied {@link Multimap}.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code LinkedListMultimap}
   */
  public static <K,V> LinkedListMultimap<K,V> newLinkedListMultimap(
      Multimap<? extends K, ? extends V> multimap) {
    return new LinkedListMultimap<K,V>(multimap);
  }

  /**
   * Creates an empty {@code TreeMultimap} instance using the natural ordering
   * of keys and values.  If the supplied multimap supports duplicate key-value
   * pairs, those duplicate pairs will only be stored once in the new multimap.
   *
   * <p>TODO(mbostock): change both type parameters to {@code <T extends
   * Comparable<? super T>}, or at least {@code <T extends Comparable<T>>}.
   *
   * @return a newly-created, initially-empty {@code TreeMultimap}
   */
  public static <K,V> TreeMultimap<K,V> newTreeMultimap() {
    return new TreeMultimap<K,V>();
  }

  /**
   * Constructs a {@link TreeMultimap} with the same mappings as the specified
   * {@code Multimap}.
   *
   * <p>If the supplied multimap is an instance of TreeMultimap, then the
   * then the supplied multimap's comparators are copied to the new
   * instance.
   *
   * <p>If the supplied multimap is not an instance of TreeMultimap, the new
   * multimap is ordered using the natural ordering of the key and value
   * classes. The key and value classes must satisfy the {@link Comparable}
   * interface.
   *
   * <p>TODO(mbostock): change both type parameters to {@code <T extends
   * Comparable<? super T>}, or at least {@code <T extends Comparable<T>>}.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code TreeMultimap}
   */
  public static <K,V> TreeMultimap<K,V> newTreeMultimap(
      Multimap<? extends K, ? extends V> multimap) {
    return new TreeMultimap<K,V>(multimap);
  }

  /**
   * Creates an empty {@code TreeMultimap} instance using explicit comparators.
   *
   * @param keyComparator the comparator that determines the key ordering. If
   * it's {@code null}, the natural ordering of the keys is used.
   * @param valueComparator the comparator that determines the value ordering.
   * If it's {@code null}, the natural ordering of the values is used.
   * @return a newly-created, initially-empty {@code TreeMultimap}
   */
  public static <K,V> TreeMultimap<K,V> newTreeMultimap(
      @Nullable Comparator<? super K> keyComparator,
      @Nullable Comparator<? super V> valueComparator) {
    return new TreeMultimap<K,V>(keyComparator, valueComparator);
  }

  /**
   * Creates a {@code TreeMultimap} instance using explicit comparators,
   * initialized with all elements from the supplied {@link
   * Multimap}.
   *
   * @param multimap the multimap whose contents are copied to this multimap.
   * @return a newly-created and initialized {@code TreeMultimap}
   */
  public static <K,V> TreeMultimap<K,V> newTreeMultimap(
      @Nullable Comparator<? super K> keyComparator,
      @Nullable Comparator<? super V> valueComparator,
      Multimap<? extends K, ? extends V> multimap) {
    return new TreeMultimap<K,V>(keyComparator, valueComparator, multimap);
  }

  /**
   * Creates a {@code HashMultimap} that's the inverse of the provided map. If
   * the input map includes the mapping from a key to a value, the returned
   * multimap contains a mapping from the value to the key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa.
   *
   * @param map the map to invert
   * @return the inverse of the input map
   */
  public static <K,V> HashMultimap<V,K> inverseHashMultimap(Map<K,V> map) {
    HashMultimap<V,K> inverse = new HashMultimap<V,K>();
    addInverse(inverse, map.entrySet());
    return inverse;
  }

  /**
   * Creates a {@code HashMultimap} that's the inverse of the provided multimap.
   * If the input multimap includes the mapping from a key to a value, the
   * returned multimap contains a mapping from the value to the key. <p>
   *
   * If the input multimap has duplicate key-value mappings, the returned
   * multimap includes the inverse mapping once. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * multimap, and visa-versa.
   *
   * @param multimap the multimap to invert
   * @return the inverse of the input multimap
   */
  public static <K,V> HashMultimap<V,K> inverseHashMultimap(
      Multimap<K,V> multimap) {
    HashMultimap<V,K> inverse = new HashMultimap<V,K>();
    addInverse(inverse, multimap.entries());
    return inverse;
  }

  /**
   * Creates an {@code ArrayListMultimap} that's the inverse of the provided
   * map. If the input map includes the mapping from a key to a value, the
   * returned multimap contains a mapping from the value to the key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa.
   *
   * @param map the map to invert
   * @return the inverse of the input map
   */
  public static <K,V> ArrayListMultimap<V,K> inverseArrayListMultimap(
      Map<K,V> map) {
    ArrayListMultimap<V,K> inverse = new ArrayListMultimap<V,K>();
    addInverse(inverse, map.entrySet());
    return inverse;
  }

  /**
   * Creates an {@code ArrayListMultimap} that's the inverse of the provided
   * multimap. If the input multimap includes the mapping from a key to a value,
   * the returned multimap contains a mapping from the value to the key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * multimap, and visa-versa.
   *
   * @param multimap the multimap to invert
   * @return the inverse of the input multimap
   */
  public static <K,V> ArrayListMultimap<V,K> inverseArrayListMultimap(
      Multimap<K,V> multimap) {
    ArrayListMultimap<V,K> inverse = new ArrayListMultimap<V,K>();
    addInverse(inverse, multimap.entries());
    return inverse;
  }

  /**
   * Creates a {@code LinkedHashMultimap} that's the inverse of the provided
   * map. If the input map includes the mapping from a key to a value, the
   * returned multimap contains a mapping from the value to the key. <p>
   *
   * The iteration order of the input map determines the sequence in which data
   * is added to the returned multimap. See {@link LinkedHashMultimap} for
   * information about the resulting iteration ordering. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa.
   *
   * @param map the map to invert
   * @return the inverse of the input map
   */
  public static <K,V> LinkedHashMultimap<V,K> inverseLinkedHashMultimap(
      Map<K,V> map) {
    LinkedHashMultimap<V,K> inverse = new LinkedHashMultimap<V,K>();
    addInverse(inverse, map.entrySet());
    return inverse;
  }

  /**
   * Creates a {@code LinkedHashMultimap} that's the inverse of the provided
   * multimap. If the input multimap includes the mapping from a key to a value,
   * the returned multimap contains a mapping from the value to the key. <p>
   *
   * The iteration order of the input multi map determines the sequence in which
   * data is added to the returned multimap. See {@link LinkedHashMultimap} for
   * information about the resulting iteration ordering. <p>
   *
   * If the input multimap has duplicate key-value mappings, the returned
   * multimap includes the inverse mapping once. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * multimap, and visa-versa.
   *
   * @param multimap the multimap to invert
   * @return the inverse of the input multimap
   */
  public static <K,V> LinkedHashMultimap<V,K> inverseLinkedHashMultimap(
      Multimap<K,V> multimap) {
    LinkedHashMultimap<V,K> inverse = new LinkedHashMultimap<V,K>();
    addInverse(inverse, multimap.entries());
    return inverse;
  }

  /**
   * Creates a {@code TreeMultimap} that's the inverse of the provided map. If
   * the input map includes the mapping from a key to a value, the returned
   * multimap contains a mapping from the value to the key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa. The returned multimap orders the keys and values
   * according to their natural ordering.
   *
   * @param map the map to invert
   * @return the inverse of the input map
   */
  public static <K,V> TreeMultimap<V,K> inverseTreeMultimap(Map<K,V> map) {
    TreeMultimap<V,K> inverse = new TreeMultimap<V,K>();
    addInverse(inverse, map.entrySet());
    return inverse;
  }

  /**
   * Creates a {@code TreeMultimap} that's the inverse of the provided multimap.
   * If the input multimap includes the mapping from a key to a value, the
   * returned multimap contains a mapping from the value to the key. <p>
   *
   * If the input multimap has duplicate key-value mappings, the returned
   * multimap includes the inverse mapping once. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa. The returned multimap orders the keys and values
   * according to their natural ordering.
   *
   * @param multimap the multimap to invert
   * @return the inverse of the input multimap
   */
  public static <K,V> TreeMultimap<V,K> inverseTreeMultimap(
      Multimap<K,V> multimap) {
    TreeMultimap<V,K> inverse = new TreeMultimap<V,K>();
    addInverse(inverse, multimap.entries());
    return inverse;
  }

  /**
   * Creates a {@code LinkedListMultimap} that's the inverse of the provided
   * map. If the input map includes the mapping from a key to a value, the
   * returned multimap contains a mapping from the value to the key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * map, and visa-versa.
   *
   * @param map the map to invert
   * @return the inverse of the input map
   */
  public static <K,V> LinkedListMultimap<V,K> inverseLinkedListMultimap(
      Map<K,V> map) {
    LinkedListMultimap<V,K> inverse = new LinkedListMultimap<V,K>();
    addInverse(inverse, map.entrySet());
    return inverse;
  }

  /**
   * Creates a {@code LinkedListMultimap} that's the inverse of the provided
   * multimap.  If the input multimap includes the mapping from a key to a
   * value, the returned multimap contains a mapping from the value to the
   * key. <p>
   *
   * The returned multimap is modifiable. Updating it will not affect the input
   * multimap, and visa-versa.
   *
   * @param multimap the multimap to invert
   * @return the inverse of the input multimap
   */
  public static <K,V> LinkedListMultimap<V,K> inverseLinkedListMultimap(
      Multimap<K,V> multimap) {
    LinkedListMultimap<V,K> inverse = new LinkedListMultimap<V,K>();
    addInverse(inverse, multimap.entries());
    return inverse;
  }

  /**
   * Inverts all map entries and add them to the invert multimap.
   */
  private static <K,V> void addInverse(Multimap<V,K> inverse,
      Collection<Map.Entry<K,V>> entries) {
    for (Map.Entry<K,V> entry : entries) {
      inverse.put(entry.getValue(), entry.getKey());
    }
  }

  /**
   * Returns a synchronized (thread-safe) multimap backed by the specified
   * multimap. In order to guarantee serial access, it is critical that
   * <b>all</b> access to the backing multimap is accomplished through the
   * returned multimap.
   *
   * <p>It is imperative that the user manually synchronize on the returned map
   * when accessing any of its collection views:
   *
   * <pre>  Multimap&lt;K,V> m = Multimaps.synchronizedMultimap(
   *      new HashMultimap&lt;K,V>());
   *   ...
   *  Set&lt;K> s = m.keySet();  // Needn't be in synchronized block
   *   ...
   *  synchronized (m) {  // Synchronizing on m, not s!
   *    Iterator&lt;K> i = s.iterator(); // Must be in synchronized block
   *    while (i.hasNext()) {
   *      foo(i.next());
   *    }
   *  }</pre>
   *
   * Failure to follow this advice may result in non-deterministic behavior.
   *
   * @param multimap the multimap to be wrapped in a synchronized view
   * @return a sychronized view of the specified multimap
   */
  public static <K,V> Multimap<K,V> synchronizedMultimap(
      Multimap<K,V> multimap) {
    return Synchronized.multimap(multimap, null);
  }

  /**
   * Returns an unmodifiable view of the specified multimap. Query operations
   * on the returned multimap "read through" to the specified multimap, and
   * attempts to modify the returned multimap, whether direct or via the various
   * collections returned through operations, result in an
   * {@code UnsupportedOperationException}.
   *
   * @param delegate the multimap for which an unmodifiable view is to be
   * returned
   * @return an unmodifiable view of the specified multimap
   */
  public static <K,V> Multimap<K,V> unmodifiableMultimap(
      Multimap<K,V> delegate) {
    return new UnmodifiableMultimap<K,V>(delegate);
  }

  private static final class UnmodifiableMultimap<K,V>
      extends ForwardingMultimap<K,V> {

    private volatile transient Set<Entry<K, Collection<V>>> collectionEntries
        = null;
    private volatile transient Collection<Entry<K,V>> entries = null;
    private volatile transient Multiset<K> keys = null;
    private volatile transient Set<K> keySet = null;
    private volatile transient Collection<V> values = null;
    private volatile transient Map<K, Collection<V>> map = null;

    public UnmodifiableMultimap(final Multimap<K,V> delegate) {
      super(delegate);
      checkNotNull(delegate);
    }

    @Override public void clear() {
      throw new UnsupportedOperationException();
    }

    @Override public Set<Entry<K, Collection<V>>> collectionEntries() {
      if (collectionEntries == null) {
        collectionEntries = unmodifiableCollectionEntries(
            super.collectionEntries());
      }
      return collectionEntries;
    }

    @Override public Map<K, Collection<V>> asMap() {
      if (map == null) {
        final Map<K, Collection<V>> unmodifiableMap =
            Collections.unmodifiableMap(delegate().asMap());
        map = new ForwardingMap<K, Collection<V>>(unmodifiableMap) {
            Collection<Collection<V>> values = null;
            @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
              return collectionEntries();
            }
            @Override public Collection<V> get(Object key) {
              Collection<V> collection = super.get(key);
              return (collection == null)
                  ? null : unmodifiableValueCollection(collection);
            }
            @Override public Collection<Collection<V>> values() {
              if (values == null) {
                values = new UnmodifiableAsMapValues<V>(super.values());
              }
              return values;
            }
            @Override public boolean containsValue(Object o) {
              return values().contains(o);
            }
        };
      }
      return map;
    }

    @Override public Collection<Entry<K,V>> entries() {
      if (entries == null) {
        entries = unmodifiableEntries(super.entries());
      }
      return entries;
    }

    @Override public Collection<V> get(K key) {
      return unmodifiableValueCollection(super.get(key));
    }

    @Override public Multiset<K> keys() {
      if (keys == null) {
        keys = Multisets.unmodifiableMultiset(super.keys());
      }
      return keys;
    }

    @Override public Set<K> keySet() {
      if (keySet == null) {
        keySet = Collections.unmodifiableSet(super.keySet());
      }
      return keySet;
    }

    @Override @SuppressWarnings("unused")
    public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }

    @Override @SuppressWarnings("unused")
    public void putAll(
        K key,
        @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }

    @Override @SuppressWarnings("unused")
    public void putAll(Multimap<? extends K, ? extends V> multimap) {
      throw new UnsupportedOperationException();
    }

    @Override @SuppressWarnings("unused")
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override @SuppressWarnings("unused")
    public Collection<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }

    @Override @SuppressWarnings("unused")
    public Collection<V> replaceValues(
        K key,
        @SuppressWarnings("hiding") Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }

    @Override public Collection<V> values() {
      if (values == null) {
        values = Collections.unmodifiableCollection(super.values());
      }
      return values;
    }
  }

  static class UnmodifiableAsMapValues<V>
      extends ForwardingCollection<Collection<V>> {
    public UnmodifiableAsMapValues(Collection<Collection<V>> delegate) {
      super(Collections.unmodifiableCollection(delegate));
    }

    @Override public Iterator<Collection<V>> iterator() {
      final Iterator<Collection<V>> iterator = super.iterator();
      return new Iterator<Collection<V>>() {
          public boolean hasNext() {
            return iterator.hasNext();
          }
          public Collection<V> next() {
            return unmodifiableValueCollection(iterator.next());
          }
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }

    @Override public Object[] toArray() {
      return toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return containsImpl(this, o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return containsAllImpl(this, c);
    }
  }

  /**
   * Returns a synchronized (thread-safe) {@link SetMultimap} backed by
   * the specified multimap. <p>
   *
   * You must follow the warning described for {@link #synchronizedMultimap}.
   *
   * @param multimap the multimap to be wrapped
   * @return a sychronized view of the specified multimap
   */
  public static <K,V> SetMultimap<K,V> synchronizedSetMultimap(
      SetMultimap<K,V> multimap) {
    return new SetDecorator<K,V>(synchronizedMultimap(multimap));
  }

  /**
   * {@link SetMultimap} generated by casting the collections returned by
   * the provided multimap.
   */
  static class SetDecorator<K,V> extends ForwardingMultimap<K,V>
      implements SetMultimap<K,V> {
    private static final long serialVersionUID = -6403585629821552555L;

    public SetDecorator(Multimap<K,V> delegate) {
      super(delegate);
    }

    @Override public Set<V> get(@Nullable K key) {
      return (Set<V>) super.get(key);
    }
    @Override public Set<Map.Entry<K,V>> entries() {
      return (Set<Map.Entry<K,V>>) super.entries();
    }
    @Override public Set<V> removeAll(@Nullable Object key) {
      return (Set<V>) super.removeAll(key);
    }
    @Override public Set<V> replaceValues(
        @Nullable K key, Iterable<? extends V> values) {
      return (Set<V>) super.replaceValues(key, values);
    }
  }

  /**
   * Returns an unmodifiable view of the specified {@link SetMultimap}.
   * Query operations on the returned multimap "read through" to the specified
   * multimap, and attempts to modify the returned multimap, whether direct or
   * via the various collections returned through operations, result in an
   * {@code UnsupportedOperationException}.
   *
   * @param delegate the multimap for which an unmodifiable view is to be
   *          returned
   * @return an unmodifiable view of the specified multimap
   */
  public static <K,V> SetMultimap<K,V> unmodifiableSetMultimap(
      SetMultimap<K,V> delegate) {
    return new SetDecorator<K,V>(unmodifiableMultimap(delegate));
  }


  /**
   * Returns a synchronized (thread-safe) {@link SortedSetMultimap} backed
   * by the specified multimap.
   * <p>
   *
   * You must follow the warning described for {@link #synchronizedMultimap}.
   *
   * @param multimap the multimap to be wrapped
   * @return a sychronized view of the specified multimap
   */
  public static <K,V> SortedSetMultimap<K,V>
      synchronizedSortedSetMultimap(SortedSetMultimap<K,V> multimap) {
    return new SortedSetDecorator<K,V>(synchronizedMultimap(multimap));
  }

  /**
   * {@link SortedSetMultimap} generated by casting the collections
   * returned by the provided multimap.
   */
  static class SortedSetDecorator<K,V> extends
      ForwardingMultimap<K,V> implements SortedSetMultimap<K,V> {
    private static final long serialVersionUID = 7463737759006994232L;

    public SortedSetDecorator(Multimap<K,V> delegate) {
      super(delegate);
    }

    @Override public SortedSet<V> get(@Nullable K key) {
      return (SortedSet<V>) super.get(key);
    }
    @Override public Set<Map.Entry<K,V>> entries() {
      return (Set<Map.Entry<K,V>>) super.entries();
    }
    @Override public SortedSet<V> removeAll(@Nullable Object key) {
      return (SortedSet<V>) super.removeAll(key);
    }
    @Override public SortedSet<V> replaceValues(@Nullable K key,
        Iterable<? extends V> values) {
      return (SortedSet<V>) super.replaceValues(key, values);
    }
  }

  /**
   * Returns an unmodifiable view of the specified
   * {@link SortedSetMultimap}. Query operations on the returned multimap
   * "read through" to the specified multimap, and attempts to modify the
   * returned multimap, whether direct or via the various collections returned
   * through operations, result in an {@code UnsupportedOperationException}.
   *
   * @param delegate the multimap for which an unmodifiable view is to be
   *          returned
   * @return an unmodifiable view of the specified multimap
   */
  public static <K,V> SortedSetMultimap<K,V>
      unmodifiableSortedSetMultimap(SortedSetMultimap<K,V> delegate) {
    return new SortedSetDecorator<K,V>(unmodifiableMultimap(delegate));
  }

  /**
   * Returns a synchronized (thread-safe) {@link ListMultimap} backed by
   * the specified multimap. <p>
   *
   * You must follow the warning described for {@link #synchronizedMultimap}.
   *
   * @param multimap the multimap to be wrapped
   * @return a sychronized view of the specified multimap
   */
  public static <K,V> ListMultimap<K,V> synchronizedListMultimap(
      ListMultimap<K,V> multimap) {
    return new ListDecorator<K,V>(synchronizedMultimap(multimap));
  }

  /**
   * {@link ListMultimap} generated by casting the collections returned by
   * the provided multimap.
   */
  static class ListDecorator<K,V> extends ForwardingMultimap<K,V>
      implements ListMultimap<K,V> {
    private static final long serialVersionUID = -8065289457161999256L;

    public ListDecorator(Multimap<K,V> delegate) {
      super(delegate);
    }

    @Override public List<V> get(@Nullable K key) {
      return (List<V>) super.get(key);
    }
    @Override public List<V> removeAll(@Nullable Object key) {
      return (List<V>) super.removeAll(key);
    }
    @Override public List<V> replaceValues(
        @Nullable K key, Iterable<? extends V> values) {
      return (List<V>) super.replaceValues(key, values);
    }
  }

  /**
   * Returns an unmodifiable view of the specified {@link ListMultimap}.
   * Query operations on the returned multimap "read through" to the specified
   * multimap, and attempts to modify the returned multimap, whether direct or
   * via the various collections returned through operations, result in an
   * {@code UnsupportedOperationException}.
   *
   * @param delegate the multimap for which an unmodifiable view is to be
   *          returned
   * @return an unmodifiable view of the specified multimap
   */
  public static <K,V> ListMultimap<K,V> unmodifiableListMultimap(
      ListMultimap<K,V> delegate) {
    return new ListDecorator<K,V>(unmodifiableMultimap(delegate));
  }

  /**
   * Returns a dynamically typesafe view of the specified multimap. Any attempt
   * to insert a mapping whose key or value have the wrong type will result in
   * an immediate {@code ClassCastException}. Similarly, any attempt to modify
   * the value currently associated with a key will result in an immediate
   * {@code ClassCastException} if the value has the wrong type, whether the
   * modification is attempted directly through the multimap itself, or through
   * one of its collection views.
   *
   * <p>Assuming the multimap contains no incorrectly typed keys or values prior
   * to the time a dynamically typesafe view is generated, and that all
   * subsequent access to the multimap takes place through the view (or one of
   * its collection views), it is <i>guaranteed</i> that the multimap cannot
   * contain an incorrectly typed key or value.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned multimap will be serializable if the specified multimap is
   * serializable.
   *
   * @param m the multimap for which a dynamically typesafe view is to be
   *          returned
   * @param keyType the type of key that {@code m} is permitted to hold
   * @param valueType the type of value that {@code m} is permitted to
   *          hold
   * @return a dynamically typesafe view of the specified multimap
   */
  public static <K,V> Multimap<K,V> checkedMultimap(Multimap<K,V> m,
      Class<K> keyType, Class<V> valueType) {
    return MapConstraints.constrainedMultimap(m,
        MapConstraints.classConstraint(keyType, valueType));
  }

  /**
   * Returns a dynamically typesafe view of the specified list multimap.
   * Any attempt to insert a mapping whose key or value have the wrong
   * type will result in an immediate {@code ClassCastException}. Similarly, any
   * attempt to modify the value currently associated with a key will result in
   * an immediate {@code ClassCastException}, whether the modification is
   * attempted directly through the multimap itself, or through one of its
   * collection views.
   *
   * <p>Assuming the multimap contains no incorrectly typed keys or values prior
   * to the time a dynamically typesafe view is generated, and that all
   * subsequent access to the multimap takes place through the view (or one of
   * its collection views), it is <i>guaranteed</i> that the multimap cannot
   * contain an incorrectly typed key or value.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned multimap will be serializable if the specified multimap is
   * serializable.
   *
   * @param m the multimap for which a dynamically typesafe view is to be
   *          returned
   * @param keyType the type of key that {@code m} is permitted to hold
   * @param valueType the type of value that {@code m} is permitted to
   *          hold
   * @return a dynamically typesafe view of the specified multimap
   */
  public static <K,V> ListMultimap<K,V> checkedListMultimap(
      ListMultimap<K,V> m, Class<K> keyType, Class<V> valueType) {
    return MapConstraints.constrainedListMultimap(m,
        MapConstraints.classConstraint(keyType, valueType));
  }

  /**
   * Returns a dynamically typesafe view of the specified set multimap.
   * Any attempt to insert a mapping whose key or value have the wrong
   * type will result in an immediate {@code ClassCastException}. Similarly, any
   * attempt to modify the value currently associated with a key will result in
   * an immediate {@code ClassCastException}, whether the modification is
   * attempted directly through the multimap itself, or through one of its
   * collection views.
   *
   * <p>Assuming the multimap contains no incorrectly typed keys or values prior
   * to the time a dynamically typesafe view is generated, and that all
   * subsequent access to the multimap takes place through the view (or one of
   * its collection views), it is <i>guaranteed</i> that the multimap cannot
   * contain an incorrectly typed key or value.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned multimap will be serializable if the specified multimap is
   * serializable.
   *
   * @param m the multimap for which a dynamically typesafe view is to be
   *          returned
   * @param keyType the type of key that {@code m} is permitted to hold
   * @param valueType the type of value that {@code m} is permitted to
   *          hold
   * @return a dynamically typesafe view of the specified multimap
   */
  public static <K,V> SetMultimap<K,V> checkedSetMultimap(
      SetMultimap<K,V> m, Class<K> keyType, Class<V> valueType) {
    return MapConstraints.constrainedSetMultimap(m,
        MapConstraints.classConstraint(keyType, valueType));
  }

  /**
   * Returns a dynamically typesafe view of the specified sorted-set
   * multimap. Any attempt to insert a mapping whose key or value have the wrong
   * type will result in an immediate {@code ClassCastException}. Similarly, any
   * attempt to modify the value currently associated with a key will result in
   * an immediate {@code ClassCastException}, whether the modification is
   * attempted directly through the multimap itself, or through one of its
   * collection views.
   *
   * <p>Assuming the multimap contains no incorrectly typed keys or values prior
   * to the time a dynamically typesafe view is generated, and that all
   * subsequent access to the multimap takes place through the view (or one of
   * its collection views), it is <i>guaranteed</i> that the multimap cannot
   * contain an incorrectly typed key or value.
   *
   * <p>A discussion of the use of dynamically typesafe views may be found in
   * the documentation for the {@link Collections#checkedCollection
   * checkedCollection} method.
   *
   * <p>The returned multimap will be serializable if the specified multimap is
   * serializable.
   *
   * @param m the multimap for which a dynamically typesafe view is to be
   *          returned
   * @param keyType the type of key that {@code m} is permitted to hold
   * @param valueType the type of value that {@code m} is permitted to
   *          hold
   * @return a dynamically typesafe view of the specified multimap
   */
  public static <K,V> SortedSetMultimap<K,V> checkedSortedSetMultimap(
      SortedSetMultimap<K,V> m, Class<K> keyType, Class<V> valueType) {
    return MapConstraints.constrainedSortedSetMultimap(m,
        MapConstraints.classConstraint(keyType, valueType));
  }

  /** Returns the empty multimap (immutable). This multimap is serializable. */
  @SuppressWarnings("unchecked")
  public static <K,V> SetMultimap<K,V> emptyMultimap() {
    return (SetMultimap<K,V>) EMPTY_MULTIMAP;
  }

  /** @see #emptyMultimap */
  static class EmptyMultimap<K,V>
      implements SetMultimap<K,V>, Serializable {
    private static final long serialVersionUID = -3460258070136750256L;
    public int size() {
      return 0;
    }
    public boolean isEmpty() {
      return true;
    }
    public boolean containsKey(Object key) {
      return false;
    }
    public boolean containsValue(Object value) {
      return false;
    }
    public boolean containsEntry(Object key, Object value) {
      return false;
    }
    public Set<V> get(K key) {
      return Collections.emptySet();
    }
    public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Multimap<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
    }
    public Set<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }
    public Set<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public Set<K> keySet() {
      return Collections.emptySet();
    }
    public Multiset<K> keys() {
      return Multisets.emptyMultiset();
    }
    public Collection<V> values() {
      return Collections.emptySet();
    }
    public Set<Map.Entry<K,V>> entries() {
      return Collections.emptySet();
    }
    public Map<K, Collection<V>> asMap() {
      return Collections.emptyMap();
    }
    public Set<Map.Entry<K, Collection<V>>> collectionEntries() {
      return Collections.emptySet();
    }
    @Override public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Multimap<?,?>)) {
        return false;
      }
      return ((Multimap<?,?>) obj).isEmpty();
    }
    @Override public int hashCode() {
      return 0;
    }
    @Override public String toString() {
      return "{}";
    }
    private Object readResolve() {
      return EMPTY_MULTIMAP; // preserve singleton property
    }
  }

  /**
   * Returns an unmodifiable view of the specified collection, preserving the
   * interface for instances of {@code SortedSet}, {@code Set}, {@code List} and
   * {@code Collection}, in that order of preference.
   *
   * @param collection the collection for which to return an unmodifiable view
   * @return an unmodifiable view of the collection
   * @throws NullPointerException if {@code collection} is null
   */
  static <V> Collection<V> unmodifiableValueCollection(
      Collection<V> collection) {
    if (collection instanceof SortedSet<?>) {
      return Collections.unmodifiableSortedSet((SortedSet<V>)collection);
    } else if (collection instanceof Set<?>) {
      return Collections.unmodifiableSet((Set<V>) collection);
    } else if (collection instanceof List<?>) {
      return Collections.unmodifiableList((List<V>) collection);
    }
    return Collections.unmodifiableCollection(collection);
  }

  /**
   * Returns an unmodifiable view of the specified multimap collection
   * entry. The {@link Entry#setValue} operation throws an {@link
   * UnsupportedOperationException}, and the collection returned by {@code
   * getValue} is also an unmodifiable (type-preserving) view. This also has the
   * side-effect of redefining equals to comply with the Map.Entry contract, and
   * to avoid a possible nefarious implementation of equals.
   *
   * @param collectionEntry the entry for which to return an unmodifiable view
   * @return an unmodifiable view of the entry
   * @throws NullPointerException if {@code collectionEntry} is null
   */
  static <K,V> Map.Entry<K, Collection<V>> unmodifiableCollectionEntry(
      final Map.Entry<K, Collection<V>> collectionEntry) {
    checkNotNull(collectionEntry);
    return new AbstractMapEntry<K, Collection<V>>() {
        public K getKey() {
          return collectionEntry.getKey();
        }
        public Collection<V> getValue() {
          return unmodifiableValueCollection(collectionEntry.getValue());
        }
      };
  }

  /**
   * Returns an unmodifiable view of the specified collection of entries. The
   * {@link Entry#setValue} operation throws an {@link
   * UnsupportedOperationException}. If the specified collection is a {@code
   * Set}, the returned collection is also a {@code Set}.
   *
   * @param entries the entries for which to return an unmodifiable view
   * @return an unmodifiable view of the entries
   * @throws NullPointerException if {@code entries} argument is null
   */
  static <K,V> Collection<Entry<K,V>> unmodifiableEntries(
      Collection<Entry<K,V>> entries) {
    if (entries instanceof Set<?>) {
      return Maps.unmodifiableEntrySet((Set<Entry<K,V>>) entries);
    }
    return new Maps.UnmodifiableEntries<K,V>(
        Collections.unmodifiableCollection(entries));
  }

  /**
   * Returns an unmodifiable view of the specified set of collection
   * entries. The {@link Entry#setValue} operation throws an {@link
   * UnsupportedOperationException}, as do any operations that attempt to modify
   * the returned collection. The type of the value collection is preserved for
   * instances of {@code SortedSet}, {@code Set}, and {@code List}, in that
   * order of preference.
   *
   * @param collectionEntries the collection entries for which to return an
   * unmodifiable view
   * @return an unmodifiable view of the collection entries
   * @throws NullPointerException if {@code entries} argument is null
   */
  static <K,V> Set<Entry<K, Collection<V>>> unmodifiableCollectionEntries(
      Set<Entry<K, Collection<V>>> collectionEntries) {
    return new UnmodifiableCollectionEntries<K,V>(
        Collections.unmodifiableSet(collectionEntries));
  }

  /** @see #unmodifiableCollectionEntries */
  static class UnmodifiableCollectionEntries<K,V>
      extends ForwardingSet<Entry<K, Collection<V>>> {
    UnmodifiableCollectionEntries(
        Set<Entry<K, Collection<V>>> collectionEntries) {
      super(collectionEntries);
    }
    @Override public Iterator<Entry<K, Collection<V>>> iterator() {
      return new ForwardingIterator<Entry<K, Collection<V>>>(super.iterator()) {
          public Entry<K, Collection<V>> next() {
            return unmodifiableCollectionEntry(super.next());
          }
        };
    }
    @Override public Object[] toArray() {
      return toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return Maps.containsEntryImpl(delegate(), o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return containsAllImpl(this, c);
    }
    @Override public boolean equals(Object o) {
      return equalsImpl(this, o);
    }
  }

  /**
   * Returns a multimap view of the specified map. The multimap is backed by the
   * map, so changes to the map are reflected in the multimap, and vice-versa.
   * If the map is modified while an iteration over one of the multimap's
   * collection views is in progress (except through the iterator's own {@code
   * remove} operation, or through the {@code setValue} operation on a map entry
   * returned by the iterator), the results of the iteration are undefined.
   *
   * <p>The multimap supports mapping removal, which removes the corresponding
   * mapping from the map. It does not support any operations which might add
   * mappings, such as {@code put}, {@code putAll} or {@code replaceValues}.
   *
   * <p>The returned multimap will be serializable if the specified map is
   * serializable.
   *
   * @param map the backing map for the returned multimap view
   */
  public static <K,V> SetMultimap<K,V> forMap(Map<K,V> map) {
    return new MapMultimap<K,V>(map);
  }

  /** @see #forMap */
  static class MapMultimap<K,V> implements SetMultimap<K,V>, Serializable {
    private static final long serialVersionUID = 7845222491160860175L;
    private final Map<K,V> map;
    private transient volatile Set<Entry<K, Collection<V>>> collectionEntries;
    private transient volatile Map<K, Collection<V>> asMap;

    MapMultimap(Map<K,V> map) {
      checkNotNull(map);
      this.map = map;
    }

    /* Multimap methods */
    public int size() {
      return map.size();
    }
    public boolean isEmpty() {
      return map.isEmpty();
    }
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }
    public boolean containsEntry(Object key, Object value) {
      return map.entrySet().contains(Maps.immutableEntry(key, value));
    }
    public Set<V> get(final K key) {
      return new AbstractSet<V>() {
          public Iterator<V> iterator() {
            return new Iterator<V>() {
                int i = 0;
                public boolean hasNext() {
                  return (i == 0) && map.containsKey(key);
                }
                public V next() {
                  if (!hasNext()) {
                    throw new NoSuchElementException();
                  }
                  i++;
                  return map.get(key);
                }
                public void remove() {
                  if (i != 1) {
                    throw new IllegalStateException();
                  }
                  i = -1;
                  map.remove(key);
                }
              };
          }
          public int size() {
            return map.containsKey(key) ? 1 : 0;
          }
        };
    }
    public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Multimap<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
    }
    public Set<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public boolean remove(Object key, Object value) {
      return map.entrySet().remove(Maps.immutableEntry(key, value));
    }
    public Set<V> removeAll(Object key) {
      Set<V> values = new HashSet<V>(2, 0.75f);
      if (!map.containsKey(key)) {
        return values;
      }
      values.add(map.remove(key));
      return values;
    }
    public void clear() {
      map.clear();
    }
    public Set<K> keySet() {
      return map.keySet();
    }
    public Multiset<K> keys() {
      return Multisets.forSet(map.keySet());
    }
    public Collection<V> values() {
      return map.values();
    }
    public Set<Entry<K,V>> entries() {
      return map.entrySet();
    }
    public Map<K, Collection<V>> asMap() {
      if (asMap == null) {
        asMap = new AsMap();
      }
      return asMap;
    }
    public Set<Entry<K, Collection<V>>> collectionEntries() {
      if (collectionEntries == null) {
        collectionEntries = new CollectionEntries();
      }
      return collectionEntries;
    }

    /* Object methods */
    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Multimap<?,?>)) {
        return false;
      }
      Multimap<?,?> m = (Multimap<?,?>) o;
      if (map.size() != m.size()) {
        return false;
      }
      for (Entry<K,V> e : map.entrySet()) {
        if (!m.containsEntry(e.getKey(), e.getValue())) {
          return false;
        }
      }
      return true;
    }
    @Override public int hashCode() {
      return map.hashCode();
    }
    @Override public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append('{');
      for (Entry<K,V> e : map.entrySet()) {
        buf.append(e.getKey()).append("=[").append(e.getValue()).append("], ");
      }
      if (buf.length() > 1) {
        buf.setLength(buf.length() - 2); // delete last comma and space
      }
      buf.append('}');
      return buf.toString();
    }

    /** @see #collectionEntries */
    class CollectionEntries extends AbstractSet<Entry<K, Collection<V>>> {
      public int size() {
        return map.size();
      }
      public Iterator<Entry<K, Collection<V>>> iterator() {
        return new Iterator<Entry<K, Collection<V>>>() {
            Iterator<K> keys = map.keySet().iterator();
            public boolean hasNext() {
              return keys.hasNext();
            }
            public Entry<K, Collection<V>> next() {
              final K key = keys.next();
              return new AbstractMapEntry<K, Collection<V>>() {
                  public K getKey() {
                    return key;
                  }
                  public Collection<V> getValue() {
                    return get(key);
                  }
                };
            }
            public void remove() {
              keys.remove();
            }
          };
      }
      /* TODO(mbostock): faster contains, remove? */
    }

    /** @see #asMap */
    class AsMap extends AbstractMap<K, Collection<V>> {
      public Set<Map.Entry<K, Collection<V>>> entrySet() {
        return collectionEntries();
      }

      /* The following methods are included for performance. */

      @Override public boolean containsKey(Object key) {
        return map.containsKey(key);
      }
      @SuppressWarnings("unchecked")
      @Override public Collection<V> get(Object key) {
        Collection<V> collection = MapMultimap.this.get((K) key);
        return collection.isEmpty() ? null : collection;
      }
      @Override public Collection<V> remove(Object key) {
        Collection<V> collection = removeAll(key);
        return collection.isEmpty() ? null : collection;
      }
    }
  }

  /**
   * Returns an immutable multimap, mapping only the specified key to the
   * specified value. The returned multimap is serializable if the specified key
   * and value are serializable.
   *
   * @param key the sole key to be stored in the returned multimap
   * @param value the sole value to be stored in the returned multimap
   */
  public static <K,V> SetMultimap<K,V> singletonMultimap(@Nullable K key,
      @Nullable V value) {
    return new SingletonMultimap<K,V>(key, value);
  }

  /** @see #singletonMultimap */
  static class SingletonMultimap<K,V>
      implements SetMultimap<K,V>, Serializable {
    private final K key;
    private final V value;
    private transient volatile Set<V> values;
    private transient volatile Set<K> keySet;
    private transient volatile Multiset<K> keys;
    private transient volatile Set<Entry<K,V>> entries;
    private transient volatile Map<K, Collection<V>> asMap;
    private transient volatile Set<Entry<K, Collection<V>>> collectionEntries;

    public SingletonMultimap(K key, V value) {
      this.key = key;
      this.value = value;
    }

    /* Multimap methods */
    public int size() {
      return 1;
    }
    public boolean isEmpty() {
      return false;
    }
    public boolean containsKey(Object key) {
      return Objects.equal(this.key, key);
    }
    public boolean containsValue(Object value) {
      return Objects.equal(this.value, value);
    }
    public boolean containsEntry(Object key, Object value) {
      return containsKey(key) && containsValue(value);
    }
    public Set<V> get(K key) {
      return containsKey(key) ? values() : Collections.<V>emptySet();
    }
    public boolean put(K key, V value) {
      throw new UnsupportedOperationException();
    }
    public void putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public void putAll(Multimap<? extends K, ? extends V> map) {
      throw new UnsupportedOperationException();
    }
    public Set<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
    }
    public boolean remove(Object key, Object value) {
      throw new UnsupportedOperationException();
    }
    public Set<V> removeAll(Object key) {
      throw new UnsupportedOperationException();
    }
    public void clear() {
      throw new UnsupportedOperationException();
    }
    public Set<K> keySet() {
      if (keySet == null) {
        keySet = Collections.singleton(key);
      }
      return keySet;
    }
    public Multiset<K> keys() {
      if (keys == null) {
        keys = Multisets.singletonMultiset(key);
      }
      return keys;
    }
    public Set<V> values() {
      if (values == null) {
        values = Collections.singleton(value);
      }
      return values;
    }
    public Set<Entry<K,V>> entries() {
      if (entries == null) {
        entries = Collections.singleton(Maps.immutableEntry(key, value));
      }
      return entries;
    }
    public Map<K, Collection<V>> asMap() {
      if (asMap == null) {
        asMap = Collections.<K, Collection<V>>singletonMap(key, values());
      }
      return asMap;
    }
    public Set<Entry<K, Collection<V>>> collectionEntries() {
      if (collectionEntries == null) {
        collectionEntries = Collections.singleton(
            Maps.<K, Collection<V>>immutableEntry(
                key, Collections.singleton(value)));
      }
      return collectionEntries;
    }

    /* Object methods */
    @Override public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Multimap<?,?>)) {
        return false;
      }
      Multimap<?,?> m = (Multimap<?,?>) o;
      return (m.size() == 1) && m.containsEntry(key, value);
    }
    @Override public int hashCode() {
      return ((key == null) ? 0 : key.hashCode())
          ^ ((value == null) ? 0 : value.hashCode());
    }
    @Override public String toString() {
      return "{" + key + "=[" + value + "]}";
    }
  }

  /**
   * Returns the immutable empty multimap. Similar to {@link #emptyMultimap},
   * but uses a {@code ListMultimap} for consistency with the overloaded
   * {@code immutableMultimap} methods.
   */
  @SuppressWarnings("unchecked")
  public static <K,V> ListMultimap<K,V> immutableMultimap() {
    return (ListMultimap<K,V>) EMPTY_LIST_MULTIMAP;
  }

  /**
   * Returns a new immutable multimap containing the specified key-value pair.
   * Similar to {@link #singletonMultimap}, but uses a {@code ListMultimap}
   * for consistency with the overloaded {@code immutableMultimap} methods.
   */
  public static <K,V> ListMultimap<K,V> immutableMultimap(
      @Nullable K k1, @Nullable V v1) {
    return new ImmutableMultimapBuilder<K,V>().put(k1, v1).getMultimap();
  }

  /**
   * Returns a new immutable multimap containing the specified key-value pairs.
   *
   * <p>Unlike an <i>unmodifiable</i> multimap such as that returned by {@link
   * #unmodifiableMultimap}, which provides a read-only view of an underlying
   * multimap which may itself be mutable, an <i>immutable</i> multimap makes a
   * copy of the original mappings, so that the returned multimap is
   * <i>guaranteed</i> never to change. This is critical, for example, if the
   * multimap is an element of a {@code HashSet} or a key in a {@code HashMap}.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> ListMultimap<K,V> immutableMultimap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2) {
    return new ImmutableMultimapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .getMultimap();
  }

  /**
   * Returns a new immutable multimap containing the specified key-value pairs.
   *
   * <p>Unlike an <i>unmodifiable</i> multimap such as that returned by {@link
   * #unmodifiableMultimap}, which provides a read-only view of an underlying
   * multimap which may itself be mutable, an <i>immutable</i> multimap makes a
   * copy of the original mappings, so that the returned multimap is
   * <i>guaranteed</i> never to change. This is critical, for example, if the
   * multimap is an element of a {@code HashSet} or a key in a {@code HashMap}.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> ListMultimap<K,V> immutableMultimap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3) {
    return new ImmutableMultimapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .getMultimap();
  }

  /**
   * Returns a new immutable multimap containing the specified key-value pairs.
   *
   * <p>Unlike an <i>unmodifiable</i> multimap such as that returned by {@link
   * #unmodifiableMultimap}, which provides a read-only view of an underlying
   * multimap which may itself be mutable, an <i>immutable</i> multimap makes a
   * copy of the original mappings, so that the returned multimap is
   * <i>guaranteed</i> never to change. This is critical, for example, if the
   * multimap is an element of a {@code HashSet} or a key in a {@code HashMap}.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> ListMultimap<K,V> immutableMultimap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4) {
    return new ImmutableMultimapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .getMultimap();
  }

  /**
   * Returns a new immutable multimap containing the specified key-value pairs.
   *
   * <p>Unlike an <i>unmodifiable</i> multimap such as that returned by {@link
   * #unmodifiableMultimap}, which provides a read-only view of an underlying
   * multimap which may itself be mutable, an <i>immutable</i> multimap makes a
   * copy of the original mappings, so that the returned multimap is
   * <i>guaranteed</i> never to change. This is critical, for example, if the
   * multimap is an element of a {@code HashSet} or a key in a {@code HashMap}.
   *
   * @see ImmutableMapBuilder
   */
  public static <K,V> ListMultimap<K,V> immutableMultimap(
      @Nullable K k1, @Nullable V v1,
      @Nullable K k2, @Nullable V v2,
      @Nullable K k3, @Nullable V v3,
      @Nullable K k4, @Nullable V v4,
      @Nullable K k5, @Nullable V v5) {
    return new ImmutableMultimapBuilder<K,V>()
        .put(k1, v1)
        .put(k2, v2)
        .put(k3, v3)
        .put(k4, v4)
        .put(k5, v5)
        .getMultimap();
  }

  /*
   * Please use ImmutableMultimapBuilder directly if you are looking for
   * overloads for 6 or more key-value pairs.
   */
}
