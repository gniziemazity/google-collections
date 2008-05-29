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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Factory and utilities pertaining to the {@code MapConstraint} interface.
 *
 * <p>Constraints and collections returned by this class are serializable.
 *
 * @see Constraints
 * @author Mike Bostock
 */
public final class MapConstraints {
  private MapConstraints() {}

  /**
   * A constraint that verifies that neither the key nor the value is null. If
   * either is null, a {@link NullPointerException} is thrown.
   */
  public static final MapConstraint<Object, Object> NOT_NULL =
      NotNullMapConstraint.INSTANCE;

  // enum singleton pattern
  private enum NotNullMapConstraint implements MapConstraint<Object, Object> {
    INSTANCE;

    public void checkKeyValue(Object key, Object value) {
      checkNotNull(key);
      checkNotNull(value);
    }

    @Override public String toString() {
      return "Not null";
    }
  }
  
  /**
   * Returns a constraint that verifies that the key is an instance of {@code
   * keyType} and that the value is an instance of {@code valueType}. A {@link
   * ClassCastException} is thrown otherwise. In addition, the constraint throws
   * a {@link ClassCastException} is thrown when the key or value is
   * {@code null}.  
   *
   * @param keyType the required type for keys
   * @param valueType the required type for values
   * @return a constraint which verifies the type of keys and values
   */
  static MapConstraint<Object, Object> classConstraint(
      Class<?> keyType, Class<?> valueType) {
    return new ClassMapConstraint(keyType, valueType);
  }

  /** @see MapConstraints#classConstraint */
  private static class ClassMapConstraint
      implements MapConstraint<Object, Object>, Serializable {
    final Class<?> keyType;
    final Class<?> valueType;

    ClassMapConstraint(Class<?> keyType, Class<?> valueType) {
      this.keyType = checkNotNull(keyType);
      this.valueType = checkNotNull(valueType);
    }

    public void checkKeyValue(Object key, Object value) {
      keyType.cast(checkNotNull(key));
      valueType.cast(checkNotNull(value));
    }

    @Override public String toString() {
      return "key " + keyType + ", value " + valueType;
    }
    
    static final long serialVersionUID = 5170999662998754707L;
  }

  /**
   * Returns a constrained view of the specified map, using the specified
   * constraint. Any operations that add new mappings will call the provided
   * constraint. However, this method does not verify that existing mappings
   * satisfy the constraint.
   * 
   * @param map the map to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the specified map
   */
  public static <K, V> Map<K, V> constrainedMap(
      Map<K, V> map, MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedMap<K, V>(map, constraint);
  }

  /**
   * Returns a constrained view of the specified multimap, using the specified
   * constraint. Any operations that add new mappings will call the provided
   * constraint. However, this method does not verify that existing mappings
   * satisfy the constraint.
   * 
   * <p>Note that the generated multimap's {@link Multimap#removeAll} and
   * {@link Multimap#replaceValues} methods return collections that are not
   * constrained.
   * 
   * @param multimap the multimap to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the multimap
   */
  public static <K, V> Multimap<K, V> constrainedMultimap(
      Multimap<K, V> multimap, MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedMultimap<K, V>(multimap, constraint);
  }

  /**
   * Returns a constrained view of the specified list multimap, using the
   * specified constraint. Any operations that add new mappings will call the
   * provided constraint. However, this method does not verify that existing
   * mappings satisfy the constraint.
   * 
   * <p>Note that the generated multimap's {@link Multimap#removeAll} and
   * {@link Multimap#replaceValues} methods return collections that are not
   * constrained.
   * 
   * @param multimap the multimap to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the specified multimap
   */
  public static <K, V> ListMultimap<K, V> constrainedListMultimap(
      ListMultimap<K, V> multimap,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedListMultimap<K, V>(multimap, constraint);
  }

  /**
   * Returns a constrained view of the specified set multimap, using the
   * specified constraint. Any operations that add new mappings will call the
   * provided constraint. However, this method does not verify that existing
   * mappings satisfy the constraint.
   * 
   * <p>Note that the generated multimap's {@link Multimap#removeAll} and
   * {@link Multimap#replaceValues} methods return collections that are not
   * constrained.
   * 
   * @param multimap the multimap to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the specified multimap
   */
  public static <K, V> SetMultimap<K, V> constrainedSetMultimap(
      SetMultimap<K, V> multimap,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedSetMultimap<K, V>(multimap, constraint);
  }

  /**
   * Returns a constrained view of the specified sorted-set multimap, using the
   * specified constraint. Any operations that add new mappings will call the
   * provided constraint. However, this method does not verify that existing
   * mappings satisfy the constraint.
   * 
   * <p>Note that the generated multimap's {@link Multimap#removeAll} and
   * {@link Multimap#replaceValues} methods return collections that are not
   * constrained.
   * 
   * @param multimap the multimap to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the specified multimap
   */
  public static <K, V> SortedSetMultimap<K, V> constrainedSortedSetMultimap(
      SortedSetMultimap<K, V> multimap,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedSortedSetMultimap<K, V>(multimap, constraint);
  }

  /**
   * Returns a constrained view of the specified entry, using the specified
   * constraint. The {@link Entry#setValue} operation will be verified with the
   * constraint.
   *
   * @param entry the entry to constrain
   * @param constraint the constraint for the entry
   * @return a constrained view of the specified entry
   */
  private static <K, V> Entry<K, V> constrainedEntry(
      Entry<K, V> entry, final MapConstraint<? super K, ? super V> constraint) {
    checkNotNull(entry);
    checkNotNull(constraint);
    return new ForwardingMapEntry<K, V>(entry) { // not Serializable
      @Override public V setValue(V value) {
        constraint.checkKeyValue(getKey(), value);
        return super.setValue(value);
      }
    };
  }

  /**
   * Returns a constrained view of the specified {@code asMap} entry, using the
   * specified constraint. The {@link Entry#setValue} operation will be verified
   * with the constraint, and the collection returned by {@link Entry#getValue}
   * will be similarly constrained.
   *
   * @param entry the {@code asMap} entry to constrain
   * @param constraint the constraint for the entry
   * @return a constrained view of the specified entry
   */
  private static <K, V> Entry<K, Collection<V>> constrainedAsMapEntry(
      Entry<K, Collection<V>> entry,
      final MapConstraint<? super K, ? super V> constraint) {
    checkNotNull(entry);
    checkNotNull(constraint);
    return new ForwardingMapEntry<K, Collection<V>>(entry) { // not Serializable
      @Override public Collection<V> getValue() {
        return Constraints.constrainedTypePreservingCollection(
            super.getValue(), new Constraint<V>() {
          public void checkElement(V value) {
            constraint.checkKeyValue(getKey(), value);
          }
        });
      }
    };
  }

  /**
   * Returns a constrained view of the specified set of {@code asMap} entries,
   * using the specified constraint. The {@link Entry#setValue} operation will
   * be verified with the constraint, and the collection returned by {@link
   * Entry#getValue} will be similarly constrained. The {@code add} and {@code
   * addAll} operations simply forward to the underlying set, which throws an
   * {@link UnsupportedOperationException} per the multimap specification.
   *
   * @param entries the entries to constrain
   * @param constraint the constraint for the entries
   * @return a constrained view of the entries
   */
  private static <K, V> Set<Entry<K, Collection<V>>> constrainedAsMapEntries(
      Set<Entry<K, Collection<V>>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedAsMapEntries<K, V>(entries, constraint);
  }

  /**
   * Returns a constrained view of the specified collection (or set) of entries,
   * using the specified constraint. The {@link Entry#setValue} operation will
   * be verified with the constraint, along with add operations on the returned
   * collection. The {@code add} and {@code addAll} operations simply forward to
   * the underlying collection, which throws an {@link
   * UnsupportedOperationException} per the map and multimap specification.
   *
   * @param entries the entries to constrain
   * @param constraint the constraint for the entries
   * @return a constrained view of the specified entries
   */
  @SuppressWarnings("unchecked")
  private static <K, V> Collection<Entry<K, V>> constrainedEntries(
      Collection<Entry<K, V>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    if (entries instanceof Set<?>) {
      return constrainedEntrySet((Set<Entry<K, V>>) entries, constraint);
    }
    return new ConstrainedEntries<K, V>(entries, constraint);
  }

  /**
   * Returns a constrained view of the specified set of entries, using the
   * specified constraint. The {@link Entry#setValue} operation will be verified
   * with the constraint, along with add operations on the returned set. The
   * {@code add} and {@code addAll} operations simply forward to the underlying
   * set, which throws an {@link UnsupportedOperationException} per the map and
   * multimap specification.
   *
   * @param entries the entries to constrain
   * @param constraint the constraint for the entries
   * @return a constrained view of the specified entries
   */
  private static <K, V> Set<Entry<K, V>> constrainedEntrySet(
      Set<Entry<K, V>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedEntrySet<K, V>(entries, constraint);
  }

  /** @see MapConstraints#constrainedMap */
  static class ConstrainedMap<K, V> extends ForwardingMap<K, V> {
    final MapConstraint<? super K, ? super V> constraint;
    private transient volatile Set<Entry<K, V>> entrySet;

    ConstrainedMap(
        Map<K, V> delegate, MapConstraint<? super K, ? super V> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }
    @Override public Set<Entry<K, V>> entrySet() {
      if (entrySet == null) {
        entrySet = constrainedEntrySet(super.entrySet(), constraint);
      }
      return entrySet;
    }
    @Override public V put(K key, V value) {
      constraint.checkKeyValue(key, value);
      return super.put(key, value);
    }
    @Override public void putAll(Map<? extends K, ? extends V> map) {
      super.putAll(checkMap(map, constraint));
    }
    private static final long serialVersionUID = 2187468218878274045L;
  }

  /**
   * Returns a constrained view of the specified bimap, using the specified
   * constraint. Any operations that modify the bimap will have the associated
   * keys and values verified with the constraint.
   *
   * @param map the bimap to constrain
   * @param constraint the constraint that validates added entries
   * @return a constrained view of the specified bimap
   */
  public static <K, V> BiMap<K, V> constrainedBiMap(
      BiMap<K, V> map, MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedBiMap<K, V>(map, null, constraint);
  }

  /** @see MapConstraints#constrainedBiMap */
  private static class ConstrainedBiMap<K, V> extends ConstrainedMap<K, V>
      implements BiMap<K, V> {
    transient volatile BiMap<V, K> inverse;

    ConstrainedBiMap(BiMap<K, V> delegate, BiMap<V, K> inverse,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate, constraint);
      this.inverse = inverse;
    }

    @SuppressWarnings("unchecked")
    @Override protected BiMap<K, V> delegate() {
      return (BiMap<K, V>) super.delegate();
    }

    public V forcePut(K key, V value) {
      constraint.checkKeyValue(key, value);
      return delegate().forcePut(key, value);
    }

    public BiMap<V, K> inverse() {
      if (inverse == null) {
        inverse = new ConstrainedBiMap<V, K>(delegate().inverse(), this,
            new InverseConstraint<V, K>(constraint));
      }
      return inverse;
    }

    @Override public Set<V> values() {
      return delegate().values();
    }

    private static final long serialVersionUID = 0;  
  }

  /** @see MapConstraints#constrainedBiMap */
  private static class InverseConstraint<K, V>
      implements MapConstraint<K, V>, Serializable {
    final MapConstraint<? super V, ? super K> constraint;

    public InverseConstraint(MapConstraint<? super V, ? super K> constraint) {
      this.constraint = checkNotNull(constraint);
    }
    public void checkKeyValue(K key, V value) {
      constraint.checkKeyValue(value, key);
    }
    private static final long serialVersionUID = 4461050635804577699L;
  }

  /** @see MapConstraints#constrainedMultimap */
  private static class ConstrainedMultimap<K, V>
      extends ForwardingMultimap<K, V> {
    final MapConstraint<? super K, ? super V> constraint;
    transient volatile Collection<Entry<K, V>> entries;
    transient volatile Map<K, Collection<V>> asMap;

    public ConstrainedMultimap(Multimap<K, V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate);
      this.constraint = constraint;
    }

    @Override public Map<K, Collection<V>> asMap() {
      if (asMap == null) {
        asMap = new ForwardingMap<K, Collection<V>>(delegate().asMap()) {
          volatile Set<Entry<K, Collection<V>>> entrySet;
          volatile Collection<Collection<V>> values;

          @Override public Set<Entry<K, Collection<V>>> entrySet() {
            if (entrySet == null) {
              entrySet = constrainedAsMapEntries(
                  super.entrySet(), constraint);
            }
            return entrySet;
          }

          @SuppressWarnings("unchecked")
          @Override public Collection<V> get(Object key) {
            Collection<V> collection = ConstrainedMultimap.this.get((K) key);
            return collection.isEmpty() ? null : collection;
          }

          @Override public Collection<Collection<V>> values() {
            if (values == null) {
              values = new ConstrainedAsMapValues<K, V>(
                  delegate().values(), entrySet());
            }
            return values;
          }

          @Override public boolean containsValue(Object o) {
            return values().contains(o);
          }
        };
      }
      return asMap;
    }

    @Override public Collection<Entry<K, V>> entries() {
      if (entries == null) {
        entries = constrainedEntries(super.entries(), constraint);
      }
      return entries;
    }

    @Override public Collection<V> get(final K key) {
      return Constraints.constrainedTypePreservingCollection(
          super.get(key), new Constraint<V>() {
        public void checkElement(V value) {
          constraint.checkKeyValue(key, value);
        }
      });
    }

    @Override public boolean put(K key, V value) {
      constraint.checkKeyValue(key, value);
      return super.put(key, value);
    }

    @Override public void putAll(K key, Iterable<? extends V> values) {
      super.putAll(key, checkValues(key, values, constraint));
    }

    @Override public void putAll(Multimap<? extends K, ? extends V> multimap) {
      super.putAll(checkMultimap(multimap, constraint));
    }

    @Override public Collection<V> replaceValues(
        K key, Iterable<? extends V> values) {
      return super.replaceValues(key, checkValues(key, values, constraint));
    }
    private static final long serialVersionUID = 1022236989881570422L;
  }

  /** @see ConstrainedMultimap#asMap */
  private static class ConstrainedAsMapValues<K, V>
      extends NonSerializableForwardingCollection<Collection<V>> {
    final Set<Entry<K, Collection<V>>> entrySet;

    /**
     * @param entrySet map entries, linking each key with its corresponding
     *     values, that already enforce the constraint
     */
    ConstrainedAsMapValues(Collection<Collection<V>> delegate,
        Set<Entry<K, Collection<V>>> entrySet) {
      super(delegate);
      this.entrySet = entrySet;
    }

   @Override public Iterator<Collection<V>> iterator() {
      final Iterator<Entry<K, Collection<V>>> iterator = entrySet.iterator();
      return new Iterator<Collection<V>>() {
        public boolean hasNext() {
          return iterator.hasNext();
        }
        public Collection<V> next() {
          return iterator.next().getValue();
        }
        public void remove() {
          iterator.remove();
        }
      };
    }

    @Override public Object[] toArray() {
      return ForwardingCollection.toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return ForwardingCollection.toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return ForwardingCollection.containsImpl(this, o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return ForwardingCollection.containsAllImpl(this, c);
    }
    @Override public boolean remove(Object o) {
      return ForwardingCollection.removeImpl(this, o);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return ForwardingCollection.removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return ForwardingCollection.retainAllImpl(this, c);
    }
  }

  /** @see MapConstraints#constrainedEntries */
  private static class ConstrainedEntries<K, V> // not Serializable
      extends NonSerializableForwardingCollection<Entry<K, V>> {
    final MapConstraint<? super K, ? super V> constraint;

    ConstrainedEntries(Collection<Entry<K, V>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries);
      this.constraint = constraint;
    }

    @Override public Iterator<Entry<K, V>> iterator() {
      return new ForwardingIterator<Entry<K, V>>(super.iterator()) {
        @Override public Entry<K, V> next() {
          return constrainedEntry(super.next(), constraint);
        }
      };
    }

    // See Collections.CheckedMap.CheckedEntrySet for details on attacks.

    @Override public Object[] toArray() {
      return ForwardingCollection.toArrayImpl(this);
    }
    @Override public <T> T[] toArray(T[] array) {
      return ForwardingCollection.toArrayImpl(this, array);
    }
    @Override public boolean contains(Object o) {
      return Maps.containsEntryImpl(delegate(), o);
    }
    @Override public boolean containsAll(Collection<?> c) {
      return ForwardingCollection.containsAllImpl(this, c);
    }
    @Override public boolean remove(Object o) {
      return Maps.removeEntryImpl(delegate(), o);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return ForwardingCollection.removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return ForwardingCollection.retainAllImpl(this, c);
    }
  }

  /** @see MapConstraints#constrainedEntrySet */
  static class ConstrainedEntrySet<K, V> // not Serializable
      extends ConstrainedEntries<K, V> implements Set<Entry<K, V>> {
    ConstrainedEntrySet(Set<Entry<K, V>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries, constraint);
    }

    // See Collections.CheckedMap.CheckedEntrySet for details on attacks.

    @Override public boolean equals(Object o) {
      return ForwardingSet.equalsImpl(this, o);
    }

    @Override public int hashCode() {
      return ForwardingSet.hashCodeImpl(this);
    }
  }

  /** @see MapConstraints#constrainedAsMapEntries */
  static class ConstrainedAsMapEntries<K, V> // not Serializable
      extends NonSerializableForwardingSet<Entry<K, Collection<V>>> {
    private final MapConstraint<? super K, ? super V> constraint;

    ConstrainedAsMapEntries(Set<Entry<K, Collection<V>>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries);
      this.constraint = constraint;
    }

    @Override public Iterator<Entry<K, Collection<V>>> iterator() {
      return new ForwardingIterator<Entry<K, Collection<V>>>(super.iterator()) {
        @Override public Entry<K, Collection<V>> next() {
          return constrainedAsMapEntry(super.next(), constraint);
        }
      };
    }

    // See Collections.CheckedMap.CheckedEntrySet for details on attacks.
    
    @Override public Object[] toArray() {
      return ForwardingSet.toArrayImpl(this);
    }

    @Override public <T> T[] toArray(T[] array) {
      return ForwardingSet.toArrayImpl(this, array);
    }

    @Override public boolean contains(Object o) {
      return Maps.containsEntryImpl(delegate(), o);
    }

    @Override public boolean containsAll(Collection<?> c) {
      return ForwardingSet.containsAllImpl(this, c);
    }

    @Override public boolean equals(Object o) {
      return ForwardingSet.equalsImpl(this, o);
    }

    @Override public int hashCode() {
      return ForwardingSet.hashCodeImpl(this);
    }
    
    @Override public boolean remove(Object o) {
      return Maps.removeEntryImpl(delegate(), o);
    }

    @Override public boolean removeAll(Collection<?> c) {
      return ForwardingSet.removeAllImpl(this, c);
    }

    @Override public boolean retainAll(Collection<?> c) {
      return ForwardingSet.retainAllImpl(this, c);
    }
  }

  private static class ConstrainedListMultimap<K, V>
      extends ConstrainedMultimap<K, V> implements ListMultimap<K, V> {
    ConstrainedListMultimap(ListMultimap<K, V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate, constraint);
    }
    @Override public List<V> get(K key) {
      return (List<V>) super.get(key);
    }
    @Override public List<V> removeAll(Object key) {
      return (List<V>) super.removeAll(key);
    }
    @Override public List<V> replaceValues(
        K key, Iterable<? extends V> values) {
      return (List<V>) super.replaceValues(key, values);
    }
    private static final long serialVersionUID = 0;    
  }
  
  private static class ConstrainedSetMultimap<K, V>
      extends ConstrainedMultimap<K, V> implements SetMultimap<K, V> {
    ConstrainedSetMultimap(SetMultimap<K, V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate, constraint);
    }
    @Override public Set<V> get(K key) {
      return (Set<V>) super.get(key);
    }
    @Override public Set<Map.Entry<K, V>> entries() {
      return (Set<Map.Entry<K, V>>) super.entries();
    }
    @Override public Set<V> removeAll(Object key) {
      return (Set<V>) super.removeAll(key);
    }
    @Override public Set<V> replaceValues(
        K key, Iterable<? extends V> values) {
      return (Set<V>) super.replaceValues(key, values);
    }
    private static final long serialVersionUID = 0;    
  }

  private static class ConstrainedSortedSetMultimap<K, V>
      extends ConstrainedSetMultimap<K, V> implements SortedSetMultimap<K, V> {
    ConstrainedSortedSetMultimap(SortedSetMultimap<K, V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate, constraint);
    }
    @Override public SortedSet<V> get(K key) {
      return (SortedSet<V>) super.get(key);
    }
    @Override public SortedSet<V> removeAll(Object key) {
      return (SortedSet<V>) super.removeAll(key);
    }
    @Override public SortedSet<V> replaceValues(
        K key, Iterable<? extends V> values) {
      return (SortedSet<V>) super.replaceValues(key, values);
    }
    public Comparator<? super V> valueComparator() {
      return ((SortedSetMultimap<K, V>) delegate()).valueComparator();
    }
    private static final long serialVersionUID = 0;    
  }

  private static <K, V> Collection<V> checkValues(K key,
      Iterable<? extends V> values,
      MapConstraint<? super K, ? super V> constraint) {
    Collection<V> copy = Lists.newArrayList(values);
    for (V value : copy) {
      constraint.checkKeyValue(key, value);
    }
    return copy;
  }

  private static <K, V> Map<K, V> checkMap(Map<? extends K, ? extends V> map,
      MapConstraint<? super K, ? super V> constraint) {
    Map<K, V> copy = new LinkedHashMap<K, V>(map);
    for (Entry<K, V> entry : copy.entrySet()) {
      constraint.checkKeyValue(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  private static <K, V> Multimap<K, V> checkMultimap(
      Multimap<? extends K, ? extends V> map,
      MapConstraint<? super K, ? super V> constraint) {
    Multimap<K, V> copy = new LinkedListMultimap<K, V>(map);
    for (Entry<K, V> entry : copy.entries()) {
      constraint.checkKeyValue(entry.getKey(), entry.getValue());
    }
    return copy;
  }
}
