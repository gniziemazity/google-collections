/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.common.collect.Serialization.FieldSetter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A base class for immutable {@link Multimap} implementations.
 *
 * @author Jared Levy
 * @author Mike Ward
 */
abstract class AbstractImmutableMultimap<K, V, C extends ImmutableCollection<V>>
    implements Multimap<K, V>, Serializable {

  final transient ImmutableMap<K, C> map;
  final transient int size;

  // These constants allow the deserialization code to set final fields. This
  // holder class makes sure they are not initialized unless an instance is
  // deserialized.
  static class FieldSettersHolder {
    // Eclipse doesn't like the raw AbstractImmutableMultimap
    @SuppressWarnings("unchecked")
    static final FieldSetter<AbstractImmutableMultimap>
        MAP_FIELD_SETTER = Serialization.getFieldSetter(
            AbstractImmutableMultimap.class, "map");
    // Eclipse doesn't like the raw AbstractImmutableMultimap
    @SuppressWarnings("unchecked")
    static final FieldSetter<AbstractImmutableMultimap>
        SIZE_FIELD_SETTER = Serialization.getFieldSetter(
            AbstractImmutableMultimap.class, "size");
  }

  AbstractImmutableMultimap(ImmutableMap<K, C> map, int size) {
    this.map = map;
    this.size = size;
  }

  // mutators (not supported)

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public C removeAll(Object key) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public C replaceValues(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public boolean put(K key, V value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public boolean putAll(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public boolean putAll(Multimap<? extends K, ? extends V> multimap) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public boolean remove(Object key, Object value) {
    throw new UnsupportedOperationException();
  }

  // accessors

  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    Collection<V> values = map.get(key);
    return values != null && values.contains(value);
  }

  public boolean containsKey(@Nullable Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(@Nullable Object value) {
    for (Collection<V> valueCollection : map.values()) {
      if (valueCollection.contains(value)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  @Override public boolean equals(@Nullable Object object) {
    if (object instanceof Multimap) {
      Multimap<?, ?> that = (Multimap<?, ?>) object;
      return this.map.equals(that.asMap());
    }
    return false;
  }

  @Override public int hashCode() {
    return map.hashCode();
  }

  @Override public String toString() {
    return map.toString();
  }

  // views

  /**
   * Returns an immutable set of the distinct keys in this multimap. These keys
   * are ordered according to when they first appeared during the construction
   * of this multimap.
   */
  public ImmutableSet<K> keySet() {
    return map.keySet();
  }

  /**
   * Returns an immutable map that associates each key with its corresponding
   * values in the multimap.
   */
  @SuppressWarnings("unchecked") // a widening cast
  public ImmutableMap<K, Collection<V>> asMap() {
    return (ImmutableMap) map;
  }

  private transient ImmutableCollection<Map.Entry<K, V>> entries;

  /**
   * Returns an immutable collection of all key-value pairs in the multimap. Its
   * iterator traverses the values for the first key, the values for the second
   * key, and so on.
   */
  public ImmutableCollection<Map.Entry<K, V>> entries() {
    ImmutableCollection<Map.Entry<K, V>> result = entries;
    return (result == null)
        ? (entries = new EntryCollection<K, V, C>(this)) : result;
  }

  private static class EntryCollection<K, V, C extends ImmutableCollection<V>>
      extends ImmutableCollection<Map.Entry<K, V>> {
    final AbstractImmutableMultimap<K, V, C> multimap;

    EntryCollection(AbstractImmutableMultimap<K, V, C> multimap) {
      this.multimap = multimap;
    }

    @Override public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
      final Iterator<Map.Entry<K, C>> mapIterator
          = multimap.map.entrySet().iterator();

      return new UnmodifiableIterator<Map.Entry<K, V>>() {
        K key;
        Iterator<V> valueIterator;

        public boolean hasNext() {
          return (key != null && valueIterator.hasNext())
              || mapIterator.hasNext();
        }

        public Map.Entry<K, V> next() {
          if (key == null || !valueIterator.hasNext()) {
            Map.Entry<K, C> entry = mapIterator.next();
            key = entry.getKey();
            valueIterator = entry.getValue().iterator();
          }
          return Maps.immutableEntry(key, valueIterator.next());
        }
      };
    }

    public int size() {
      return multimap.size();
    }

    @Override public boolean contains(Object object) {
      if (object instanceof Map.Entry) {
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
        return multimap.containsEntry(entry.getKey(), entry.getValue());
      }
      return false;
    }

    private static final long serialVersionUID = 0;
  }

  private transient ImmutableMultiset<K> keys;

  /**
   * Returns a collection, which may contain duplicates, of all keys. The number
   * of times a key appears in the returned multiset equals the number of
   * mappings the key has in the multimap. Duplicate keys appear consecutively
   * in the multiset's iteration order.
   */
  public ImmutableMultiset<K> keys() {
    ImmutableMultiset<K> result = keys;
    return (result == null)
        ? (keys = new ImmutableMultiset<K>(new CountMap<K, V, C>(this), size))
        : result;
  }

  // TODO: Consider creating a multiset with a copy of the information, which
  // would be much simpler.

  /*
   * Map from key to value count, used to create the keys() multiset.
   * Methods that ImmutableMultiset doesn't require are unsupported.
   */
  private static class CountMap<K, V, C extends ImmutableCollection<V>>
      extends ImmutableMap<K, Integer> {
    final AbstractImmutableMultimap<K, V, C> multimap;

    CountMap(AbstractImmutableMultimap<K, V, C> multimap) {
      this.multimap = multimap;
    }

    @Override public boolean containsKey(Object key) {
      return multimap.containsKey(key);
    }

    @Override public boolean containsValue(Object value) {
      throw new AssertionError(); // not supposed to be reachable
    }

    @Override public Integer get(Object key) {
      Collection<?> valueCollection = multimap.map.get(key);
      return (valueCollection == null) ? 0 : valueCollection.size();
    }

    @Override public ImmutableSet<K> keySet() {
      return multimap.keySet();
    }

    @Override public ImmutableCollection<Integer> values() {
      throw new AssertionError(); // not supposed to be reachable
    }

    public boolean isEmpty() {
      return multimap.isEmpty();
    }

    public int size() {
      return multimap.map.size();
    }

    transient ImmutableSet<Entry<K, Integer>> entrySet;

    @Override public ImmutableSet<Entry<K, Integer>> entrySet() {
      ImmutableSet<Entry<K, Integer>> result = entrySet;
      return (result == null)
          ? (entrySet = new EntrySet<K, V, C>(multimap))
          : result;
    }

    private static class EntrySet<K, V, C extends ImmutableCollection<V>>
        extends ImmutableSet<Entry<K, Integer>> {
      final AbstractImmutableMultimap<K, V, C> multimap;

      EntrySet(AbstractImmutableMultimap<K, V, C> multimap) {
        this.multimap = multimap;
      }

      @Override public UnmodifiableIterator<Entry<K, Integer>> iterator() {
        final Iterator<Entry<K, C>> mapIterator
            = multimap.map.entrySet().iterator();
        return new UnmodifiableIterator<Entry<K, Integer>>() {
          public boolean hasNext() {
            return mapIterator.hasNext();
          }
          public Entry<K, Integer> next() {
            Entry<K, C> entry = mapIterator.next();
            return Maps.immutableEntry(entry.getKey(), entry.getValue().size());
          }
        };
      }

      public int size() {
        return multimap.map.size();
      }

      private static final long serialVersionUID = 0;
    }

    private static final long serialVersionUID = 0;
  }

  private transient ImmutableCollection<V> values;

  /**
   * Returns an immutable collection of the values in this multimap. Its
   * iterator traverses the values for the first key, the values for the second
   * key, and so on.
   */
  public ImmutableCollection<V> values() {
    ImmutableCollection<V> result = values;
    return (result == null) ? (values = new Values<V>(this)) : result;
  }

  private static class Values<V> extends ImmutableCollection<V>  {
    final Multimap<?, V> multimap;

    Values(Multimap<?, V> multimap) {
      this.multimap = multimap;
    }

    @Override public UnmodifiableIterator<V> iterator() {
      final Iterator<? extends Map.Entry<?, V>> entryIterator
          = multimap.entries().iterator();
      return new UnmodifiableIterator<V>() {
        public boolean hasNext() {
          return entryIterator.hasNext();
        }
        public V next() {
          return entryIterator.next().getValue();
        }
      };
    }

    public int size() {
      return multimap.size();
    }

    private static final long serialVersionUID = 0;
  }
}
