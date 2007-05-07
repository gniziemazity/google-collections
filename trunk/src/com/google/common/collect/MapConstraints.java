// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

/**
 * Factory and utilities for {@link MapConstraint}s.
 *
 * <p>{@link MapConstraint}, {@code Map} and {@code Multimap} instances returned
 * by this class are serializable.
 *
 * @see Constraints
 * @author mbostock@google.com (Mike Bostock)
 */
public final class MapConstraints {
  private MapConstraints() {}

  /**
   * A constraint that verifies that neither the key nor the value is {@code
   * null}. If either is {@code null}, a {@link NullPointerException} is thrown.
   */
  public static final MapConstraint<Object, Object> NOT_NULL
      = new NotNullMapConstraint();

  /** @see #NOT_NULL */
  static class NotNullMapConstraint
      implements MapConstraint<Object, Object>, Serializable {
    private static final long serialVersionUID = -8865373377796638411L;
    public void checkKeyValue(Object key, Object value) {
      checkNotNull(key);
      checkNotNull(value);
    }
    private Object readResolve() {
      return NOT_NULL; // preserve singleton property
    }
  }

  /**
   * Returns a constraint that verfies that the key is an instance of {@code
   * keyType} and that the value is an instance of {@code valueType}. A {@link
   * ClassCastException} is thrown otherwise.
   *
   * @param keyType the required type for keys
   * @param valueType the required type for values
   * @return a constraint which verifies the type of keys and values
   */
  static MapConstraint<Object, Object> classConstraint(Class<?> keyType,
      Class<?> valueType) {
    return new ClassMapConstraint(keyType, valueType);
  }

  /** @see #classConstraint */
  static class ClassMapConstraint
      implements MapConstraint<Object, Object>, Serializable {
    private static final long serialVersionUID = 5170999662998754707L;
    private final Class<?> keyType;
    private final Class<?> valueType;

    public ClassMapConstraint(Class<?> keyType, Class<?> valueType) {
      checkNotNull(keyType);
      checkNotNull(valueType);
      this.keyType = keyType;
      this.valueType = valueType;
    }

    public void checkKeyValue(Object key, Object value) {
      if (!keyType.isInstance(key)) {
        throw new ClassCastException("Attempt to insert " + key.getClass()
            + " key into collection with key type " + keyType);
      }
      if (!valueType.isInstance(value)) {
        throw new ClassCastException("Attempt to insert " + value.getClass()
            + " value into collection with value type " + valueType);
      }
    }
  }

  /**
   * Returns a constrained view of the specified map, using the specified
   * constraint. Any operations that modify the map will have the associated
   * keys and values verified with the constraint.
   *
   * @param map the map for which to return a constrained view
   * @param constraint the constraint for entries in the map
   * @return a constrained view of the specified map
   */
  public static <K,V> Map<K,V> constrainedMap(Map<K,V> map,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedMap<K,V>(map, constraint);
  }

  /**
   * Returns a constrained view of the specified multimap, using the specified
   * constraint. Any operations that modify the multimap will have the
   * associated keys and values verified with the constraint.
   *
   * @param multimap the multimap for which to return a constrained view
   * @param constraint the constraint for entries in the multimap
   * @return a constrained view of the multimap
   */
  public static <K,V> Multimap<K,V> constrainedMultimap(
      Multimap<K,V> multimap, MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedMultimap<K,V>(multimap, constraint);
  }

  /**
   * Returns a constrained view of the specified list multimap, using the
   * specified constraint. Any operations that modify the multimap will have the
   * associated keys and values verified with the constraint.
   *
   * @param multimap the multimap for which to return a constrained view
   * @param constraint the constraint for entries in the multimap
   * @return a constrained view of the specified multimap
   */
  public static <K,V> ListMultimap<K,V> constrainedListMultimap(
      ListMultimap<K,V> multimap,
      MapConstraint<? super K, ? super V> constraint) {
    return new Multimaps.ListDecorator<K,V>(
        new ConstrainedMultimap<K,V>(multimap, constraint));
  }

  /**
   * Returns a constrained view of the specified set multimap, using the
   * specified constraint. Any operations that modify the multimap will have the
   * associated keys and values verified with the constraint.
   *
   * @param multimap the multimap for which to return a constrained view
   * @param constraint the constraint for entries in the multimap
   * @return a constrained view of the specified multimap
   */
  public static <K,V> SetMultimap<K,V> constrainedSetMultimap(
      SetMultimap<K,V> multimap,
      MapConstraint<? super K, ? super V> constraint) {
    return new Multimaps.SetDecorator<K,V>(
        new ConstrainedMultimap<K,V>(multimap, constraint));
  }

  /**
   * Returns a constrained view of the specified sorted-set multimap,
   * using the specified constraint. Any operations that modify the multimap
   * will have the associated keys and values verified with the constraint.
   *
   * @param multimap the multimap for which to return a constrained view
   * @param constraint the constraint for entries in the multimap
   * @return a constrained view of the specified multimap
   */
  public static <K,V> SortedSetMultimap<K,V>
      constrainedSortedSetMultimap(SortedSetMultimap<K,V> multimap,
          MapConstraint<? super K, ? super V> constraint) {
    return new Multimaps.SortedSetDecorator<K,V>(
        new ConstrainedMultimap<K,V>(multimap, constraint));
  }

  /**
   * Returns a constrained view of the specified entry, using the specified
   * constraint. The {@link Entry#setValue} operation will be verified with the
   * constraint.
   *
   * @param entry the entry for which to return a constrained view
   * @param constraint the constraint for the entry
   * @return a constrained view of the specified entry
   */
  static <K,V> Entry<K,V> constrainedEntry(Entry<K,V> entry,
      final MapConstraint<? super K, ? super V> constraint) {
    checkNotNull(entry);
    checkNotNull(constraint);
    return new ForwardingMapEntry<K,V>(entry) { // not Serializable
        @Override public V setValue(V value) {
          constraint.checkKeyValue(getKey(), value);
          return super.setValue(value);
        }
      };
  }

  /**
   * Returns a constrained view of the specified collection entry, using the
   * specified constraint. The {@link Entry#setValue} operation will be
   * verified with the constraint, and the collection returned by {@link
   * Entry#getValue} will be similarly constrained.
   *
   * @param entry the collection entry for which to return a constrained view
   * @param constraint the constraint for the collection entry
   * @return a constrained view of the specified collection entry
   */
  static <K,V> Entry<K, Collection<V>> constrainedCollectionEntry(
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
   * Returns a constrained view of the specified set of collection entries,
   * using the specified constraint. The {@link Entry#setValue} operation will
   * be verified with the constraint, and the collection returned by {@link
   * Entry#getValue} will be similarly constrained. The {@code add} and {@code
   * addAll} operations simply forward to the underlying set, which throws an
   * {@link UnsupportedOperationException} per the multimap specification.
   *
   * @param entries the entries for which to return a constrained view
   * @param constraint the constraint for the entries
   * @return a constrained view of the entries
   */
  static <K,V> Set<Entry<K, Collection<V>>> constrainedCollectionEntries(
      Set<Entry<K, Collection<V>>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedCollectionEntries<K,V>(entries, constraint);
  }

  /**
   * Returns a constrained view of the specified collection (or set) of entries,
   * using the specified constraint. The {@link Entry#setValue} operation will
   * be verified with the constraint, along with add operations on the returned
   * collection. The {@code add} and {@code addAll} operations simply forward to
   * the underlying collection, which throws an {@link
   * UnsupportedOperationException} per the map and multimap specification.
   *
   * @param entries the entries for which to return a constrained view
   * @param constraint the constraint for the entries
   * @return a constrained view of the specified entries
   */
  @SuppressWarnings("unchecked")
  static <K,V> Collection<Entry<K,V>> constrainedEntries(
      Collection<Entry<K,V>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    if (entries instanceof Set) {
      return constrainedEntrySet((Set<Entry<K,V>>) entries, constraint);
    }
    return new ConstrainedEntries<K,V>(entries, constraint);
  }

  /**
   * Returns a constrained view of the specified set of entries, using the
   * specified constraint. The {@link Entry#setValue} operation will be verified
   * with the constraint, along with add operations on the returned set. The
   * {@code add} and {@code addAll} operations simply forward to the underlying
   * set, which throws an {@link UnsupportedOperationException} per the map and
   * multimap specification.
   *
   * @param entries the entries for which to return a constrained view
   * @param constraint the constraint for the entries
   * @return a constrained view of the specified entries
   */
  static <K,V> Set<Entry<K,V>> constrainedEntrySet(
      Set<Entry<K,V>> entries,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedEntrySet<K,V>(entries, constraint);
  }

  /** @see #constrainedMap */
  private static class ConstrainedMap<K,V> extends ForwardingMap<K,V> {
    private static final long serialVersionUID = 2187468218878274045L;
    protected final MapConstraint<? super K, ? super V> constraint;
    private volatile transient Set<Entry<K,V>> entrySet;

    public ConstrainedMap(Map<K,V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
    }

    @Override public Set<Entry<K,V>> entrySet() {
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
  }

  /**
   * Returns a constrained view of the specified bimap, using the specified
   * constraint. Any operations that modify the bimap will have the associated
   * keys and values verified with the constraint.
   *
   * @param map the bimap for which to return a constrained view
   * @param constraint the constraint for entries in the bimap
   * @return a constrained view of the specified map
   */
  public static <K,V> BiMap<K,V> constrainedBiMap(BiMap<K,V> map,
      MapConstraint<? super K, ? super V> constraint) {
    return new ConstrainedBiMap<K,V>(map, null, constraint);
  }

  /** @see #constrainedBiMap */
  private static class ConstrainedBiMap<K,V> extends ConstrainedMap<K,V>
      implements BiMap<K,V> {
    private transient volatile BiMap<V,K> inverse;

    public ConstrainedBiMap(BiMap<K,V> delegate, BiMap<V,K> inverse,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate, constraint);
      this.inverse = inverse;
    }

    @SuppressWarnings("unchecked")
    @Override protected BiMap<K,V> delegate() {
      return (BiMap<K,V>) super.delegate();
    }

    public V forcePut(K key, V value) {
      constraint.checkKeyValue(key, value);
      return delegate().forcePut(key, value);
    }
    public BiMap<V,K> inverse() {
      if (inverse == null) {
        inverse = new ConstrainedBiMap<V,K>(delegate().inverse(), this,
            new InverseConstraint<V,K>(constraint));
      }
      return inverse;
    }
    @Override public Set<V> values() {
      return delegate().values();
    }
  }

  /** @see #constrainedBiMap */
  static class InverseConstraint<K,V>
      implements MapConstraint<K,V>, Serializable {
    private static final long serialVersionUID = 4461050635804577699L;
    private final MapConstraint<? super V, ? super K> constraint;

    public InverseConstraint(MapConstraint<? super V, ? super K> constraint) {
      checkNotNull(constraint);
      this.constraint = constraint;
    }

    public void checkKeyValue(K key, V value) {
      constraint.checkKeyValue(value, key);
    }
  }

  /** @see #constrainedMultimap */
  static class ConstrainedMultimap<K,V> extends ForwardingMultimap<K,V> {
    private static final long serialVersionUID = 1022236989881570422L;
    private final MapConstraint<? super K, ? super V> constraint;
    private volatile transient Set<Entry<K, Collection<V>>> collectionEntries;
    private volatile transient Collection<Entry<K,V>> entries;
    private volatile transient Map<K, Collection<V>> asMap;

    public ConstrainedMultimap(Multimap<K,V> delegate,
        MapConstraint<? super K, ? super V> constraint) {
      super(delegate);
      this.constraint = constraint;
    }

    @Override public Set<Entry<K, Collection<V>>> collectionEntries() {
      if (collectionEntries == null) {
        collectionEntries = constrainedCollectionEntries(
            super.collectionEntries(), constraint);
      }
      return collectionEntries;
    }

    @Override public Map<K, Collection<V>> asMap() {
      if (asMap == null) {
        asMap = new ForwardingMap<K, Collection<V>>(delegate().asMap()) {
            volatile Collection<Collection<V>> values;
            @Override public Set<Entry<K, Collection<V>>> entrySet() {
              return collectionEntries();
            }
            @SuppressWarnings("unchecked")
            @Override public Collection<V> get(Object key) {
              Collection<V> collection = ConstrainedMultimap.this.get((K) key);
              return collection.isEmpty() ? null : collection;
            }
            @Override public Collection<Collection<V>> values() {
              if (values == null) {
                values = new ConstrainedAsMapValues<K,V>(
                    delegate().values(), collectionEntries());
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

    @Override public Collection<Entry<K,V>> entries() {
      if (entries == null) {
        entries = constrainedEntries(super.entries(), constraint);
      }
      return entries;
    }
    @Override public Collection<V> get(final K key) {
      return Constraints.constrainedTypePreservingCollection(
          super.get(key), new Constraint<V>() {
              @SuppressWarnings("unchecked") public void checkElement(V value) {
                constraint.checkKeyValue((K) key, value);
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
    @Override public Collection<V> replaceValues(K key,
        Iterable<? extends V> values) {
      return super.replaceValues(key, checkValues(key, values, constraint));
    }
  }

  /** @see ConstrainedMultimap#asMap */
  static class ConstrainedAsMapValues<K,V>
      extends ForwardingCollection<Collection<V>> {
    private final Set<Entry<K, Collection<V>>> collectionEntries;

    /**
     * @param collectionEntries map entries, linking each key with its
     * corresponding values, that already enforce the constraint
     */
    protected ConstrainedAsMapValues(Collection<Collection<V>> delegate,
        Set<Entry<K, Collection<V>>> collectionEntries) {
      super(delegate);
      this.collectionEntries = collectionEntries;
    }

    public Iterator<Collection<V>> iterator() {
      final Iterator<Entry<K, Collection<V>>> iterator =
          collectionEntries.iterator();
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
    @Override public boolean remove(Object o) {
      return removeImpl(this, o);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return retainAllImpl(this, c);
    }
  }

  /** @see #constrainedEntries */
  static class ConstrainedEntries<K,V> // not Serializable
      extends ForwardingCollection<Entry<K,V>> {
    private final MapConstraint<? super K, ? super V> constraint;

    ConstrainedEntries(Collection<Entry<K,V>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries);
      this.constraint = constraint;
    }

    @Override public Iterator<Entry<K,V>> iterator() {
      return new ForwardingIterator<Entry<K,V>>(super.iterator()) {
          @Override public Entry<K,V> next() {
            return constrainedEntry(super.next(), constraint);
          }
        };
    }

    /* See java.util.Collections.CheckedEntrySet for details on attacks. */
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
    @Override public boolean remove(Object o) {
      return Maps.removeEntryImpl(delegate(), o);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return retainAllImpl(this, c);
    }
  }

  /** @see #constrainedEntrySet */
  static class ConstrainedEntrySet<K,V> // not Serializable
      extends ConstrainedEntries<K,V> implements Set<Entry<K,V>> {
    ConstrainedEntrySet(Set<Entry<K,V>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries, constraint);
    }

    /* See java.util.Collections.CheckedEntrySet for details on attacks. */
    @Override public boolean equals(Object o) {
      return ForwardingSet.equalsImpl(this, o);
    }
  }

  /** @see #constrainedCollectionEntries */
  static class ConstrainedCollectionEntries<K,V> // not Serializable
      extends ForwardingSet<Entry<K, Collection<V>>> {
    private final MapConstraint<? super K, ? super V> constraint;

    ConstrainedCollectionEntries(Set<Entry<K, Collection<V>>> entries,
        MapConstraint<? super K, ? super V> constraint) {
      super(entries);
      this.constraint = constraint;
    }

    @Override public Iterator<Entry<K, Collection<V>>> iterator() {
      return new ForwardingIterator<Entry<K, Collection<V>>>(super.iterator()) {
          @Override public Entry<K, Collection<V>> next() {
            return constrainedCollectionEntry(super.next(), constraint);
          }
        };
    }

    /* See java.util.Collections.CheckedEntrySet for details on attacks. */
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
    @Override public boolean remove(Object o) {
      return Maps.removeEntryImpl(delegate(), o);
    }
    @Override public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return retainAllImpl(this, c);
    }
  }

  private static <K,V> Collection<V> checkValues(K key,
      Iterable<? extends V> values,
      MapConstraint<? super K, ? super V> constraint) {
    Collection<V> copy = Lists.newArrayList(values);
    for (V value : copy) {
      constraint.checkKeyValue(key, value);
    }
    return copy;
  }

  private static <K,V> Map<K,V> checkMap(Map<? extends K, ? extends V> map,
      MapConstraint<? super K, ? super V> constraint) {
    Map<K,V> copy = new LinkedHashMap<K,V>(map);
    for (Entry<K,V> entry : copy.entrySet()) {
      constraint.checkKeyValue(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  private static <K,V> Multimap<K,V> checkMultimap(
      Multimap<? extends K, ? extends V> map,
      MapConstraint<? super K, ? super V> constraint) {
    Multimap<K,V> copy = new LinkedListMultimap<K,V>(map);
    for (Entry<K,V> entry : copy.entries()) {
      constraint.checkKeyValue(entry.getKey(), entry.getValue());
    }
    return copy;
  }
}
