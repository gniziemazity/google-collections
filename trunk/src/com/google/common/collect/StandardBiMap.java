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

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkArgument;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A general-purpose bimap implementation using any two backing {@code Map}
 * instances.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 */
class StandardBiMap<K, V> extends ForwardingMap<K, V> implements BiMap<K, V> {
  /** Package-private constructor for creating a map-backed bimap. */
  StandardBiMap(Map<K, V> forward, Map<V, K> backward) {
    super(forward);
    checkArgument(forward.isEmpty());
    checkArgument(backward.isEmpty());
    inverse = new StandardBiMap<V, K>(backward, this);
  }

  /** Private constructor for inverse bimap. */
  private StandardBiMap(Map<K, V> backward, StandardBiMap<V, K> forward) {
    super(backward);
    inverse = forward;
  }

  // Query Operations (optimizations)

  @Override public boolean containsValue(Object value) {
    return inverse.containsKey(value);
  }

  // Modification Operations

  @Override public V put(K key, V value) {
    return putInBothMaps(key, value, false);
  }

  public V forcePut(K key, V value) {
    return putInBothMaps(key, value, true);
  }

  private V putInBothMaps(K key, V value, boolean force) {
    boolean containedKey = containsKey(key);
    if (containedKey && Objects.equal(value, get(key))) {
      return value;
    }
    if (force) {
      inverse().remove(value);
    } else {
      checkArgument(!containsValue(value), "value already present: %s", value);
    }
    V oldValue = super.put(key, value);
    updateInverseMap(key, containedKey, oldValue, value);
    return oldValue;
  }

  private void updateInverseMap(
      K key, boolean containedKey, V oldValue, V newValue) {
    if (containedKey) {
      removeFromInverseMap(oldValue);
    }
    inverse.delegate().put(newValue, key);
  }

  @Override public V remove(Object key) {
    return containsKey(key) ? removeFromBothMaps(key) : null;
  }

  private V removeFromBothMaps(Object key) {
    V oldValue = super.remove(key);
    removeFromInverseMap(oldValue);
    return oldValue;
  }

  private void removeFromInverseMap(V oldValue) {
    inverse.delegate().remove(oldValue);
  }

  // Bulk Operations

  @Override public void putAll(Map<? extends K, ? extends V> map) {
    for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override public void clear() {
    super.clear();
    inverse.delegate().clear();
  }

  // Views

  // TODO: make this transient?
  private StandardBiMap<V, K> inverse;

  public BiMap<V, K> inverse() {
    return inverse;
  }

  private transient volatile Set<K> keySet;

  @Override public Set<K> keySet() {
    if (keySet == null) {
      keySet = new KeySet(super.keySet());
    }
    return keySet;
  }

  private class KeySet extends ForwardingSet<K> {
    KeySet(Set<K> keySet) {
      super(keySet);
    }

    @Override public void clear() {
      StandardBiMap.this.clear();
    }

    @Override public boolean remove(Object key) {
      if (!contains(key)) {
        return false;
      }
      removeFromBothMaps(key);
      return true;
    }

    @Override public boolean removeAll(Collection<?> keysToRemove) {
      return removeAllImpl(this, keysToRemove);
    }

    @Override public boolean retainAll(Collection<?> keysToRetain) {
      return retainAllImpl(this, keysToRetain);
    }

    @Override public Iterator<K> iterator() {
      final Iterator<Entry<K, V>> iterator
          = StandardBiMap.super.entrySet().iterator();
      return new Iterator<K>() {
        Entry<K, V> entry;

        public boolean hasNext() {
          return iterator.hasNext();
        }
        public K next() {
          entry = iterator.next();
          return entry.getKey();
        }
        public void remove() {
          iterator.remove();
          removeFromInverseMap(entry.getValue());
        }
      };
    }
  }

  @Override public Set<V> values() {
    if (valueSet == null) {
      /*
       * We can almost reuse the inverse's keySet, except we have to fix the
       * iteration order so that it is consistent with the forward map.
       */
      valueSet = new ValueSet(inverse.keySet());
    }
    return valueSet;
  }

  private transient volatile Set<V> valueSet;

  private class ValueSet extends ForwardingSet<V> {
    ValueSet(Set<V> values) {
      super(values);
    }

    @Override public Iterator<V> iterator() {
      Iterator<V> iterator
          = StandardBiMap.super.values().iterator();
      return new ForwardingIterator<V>(iterator) {
        V valueToRemove;

        @Override public V next() {
          return valueToRemove = super.next();
        }

        @Override public void remove() {
          super.remove();
          removeFromInverseMap(valueToRemove);
        }
      };
    }

    @Override public Object[] toArray() {
      return toArrayImpl(this);
    }

    @Override public <T> T[] toArray(T[] array) {
      return toArrayImpl(this, array);
    }

    @Override public String toString() {
      return toStringImpl(this);
    }
  }

  private transient volatile Set<Entry<K, V>> entrySet;

  @Override public Set<Entry<K, V>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet(super.entrySet());
    }
    return entrySet;
  }

  private class EntrySet extends ForwardingSet<Entry<K, V>> {
    EntrySet(Set<Entry<K, V>> entrySet) {
      super(entrySet);
    }

    @Override public void clear() {
      StandardBiMap.this.clear();
    }

    @Override public boolean remove(Object object) {
      if (!super.remove(object)) {
        return false;
      }
      Entry<?, ?> entry = (Entry<?, ?>) object;
      inverse.delegate().remove(entry.getValue());
      return true;
    }

    @Override public Iterator<Entry<K, V>> iterator() {
      return new ForwardingIterator<Entry<K, V>>(super.iterator()) {
        Entry<K, V> entry;

        @Override public Entry<K, V> next() {
          entry = super.next();
          return new ForwardingMapEntry<K, V>(entry) {
            @Override public V setValue(V value) {
              // similar to putInBothMaps, but set via entry
              if (Objects.equal(value, getValue())) {
                return value;
              }
              checkArgument(!containsValue(value),
                  "value already present: %s", value);
              V oldValue = super.setValue(value);
              updateInverseMap(getKey(), true, oldValue, value);
              return oldValue;
            }
          };
        }
        @Override public void remove() {
          super.remove();
          removeFromInverseMap(entry.getValue());
        }
      };
    }

    // See java.util.Collections.CheckedEntrySet for details on attacks.

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
    @Override public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return retainAllImpl(this, c);
    }
  }

  private static final long serialVersionUID = 0x3EE04EBA918F30AFL;
}
