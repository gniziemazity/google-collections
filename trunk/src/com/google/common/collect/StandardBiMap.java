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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A general-purpose abstract {@link BiMap} implementation using any two backing
 * {@link Map} instances. Instances of this class are not thread-safe. All
 * methods throw {@link NullPointerException} in response to any null parameter,
 * except as noted.
 *
 * <p>Instances of {@code StandardBiMap} will be serializable if the backing
 * maps are serializable.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
class StandardBiMap<K,V> extends ForwardingMap<K,V> implements BiMap<K,V> {
  private static final long serialVersionUID = 0x3EE04EBA918F30AFL;
  private StandardBiMap<V,K> inverse; // not final to allow for clone

  private transient volatile Set<K> keySet;
  private transient volatile Set<V> valueSet;
  private transient volatile Set<Entry<K,V>> entrySet;

  /** Package-private constructor for creating a map-backed bimap. */
  StandardBiMap(Map<K,V> forward, Map<V,K> backward) {
    super(forward);
    if (!forward.isEmpty()) {
      throw new IllegalArgumentException("forward map must be empty");
    }
    if (!backward.isEmpty()) {
      throw new IllegalArgumentException("backward map must be empty");
    }
    inverse = new StandardBiMap<V,K>(backward, this);
  }

  /** Private constructor for inverse bimap. */
  private StandardBiMap(Map<K,V> backward, StandardBiMap<V,K> forward) {
    super(backward);
    inverse = forward;
  }

  public BiMap<V,K> inverse() {
    return inverse;
  }

  @Override public V put(K key, V value) {
    return putInBothMaps(key, value, false);
  }

  public V forcePut(K key, V value) {
    return putInBothMaps(key, value, true);
  }

  @Override public void putAll(Map<? extends K, ? extends V> map) {
    for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override public boolean containsValue(Object value) {
    return inverse.containsKey(value);
  }

  @Override public V remove(Object key) {
    return containsKey(key) ? removeFromBothMaps(key) : null;
  }

  @Override public void clear() {
    super.clear();
    inverse.delegate().clear();
  }

  @SuppressWarnings("unchecked")
  @Override protected StandardBiMap<K,V> clone()
      throws CloneNotSupportedException {
    StandardBiMap<K,V> clone;
    try {
      clone = (StandardBiMap<K,V>) super.clone();
    } catch (CloneDelegateNotSupportedException e) {
      throw new CloneNotSupportedException();
    }
    Map<V,K> backwardClone = Objects.clone(inverse.delegate());
    clone.inverse = new StandardBiMap<V,K>(backwardClone, clone);
    clone.keySet = null;
    clone.valueSet = null;
    clone.entrySet = null;
    return clone;
  }

  /** @see #cloneDelegate */
  private static class CloneDelegateNotSupportedException
      extends RuntimeException {}

  @SuppressWarnings("unchecked")
  @Override protected Map<K,V> cloneDelegate() {
    try {
      return (Map<K,V>) Objects.clone(delegate());
    } catch (CloneNotSupportedException e) {
      throw new CloneDelegateNotSupportedException();
    }
  }

  /*
   * No need to override read-only operations size, isEmpty, get, containsKey,
   * equals, hashCode, or toString. All that's left are the "view collections".
   */

  @Override public Set<K> keySet() {
    if (keySet == null) {
      keySet = new KeySet(super.keySet());
    }
    return keySet;
  }

  /** @see #keySet() */
  private class KeySet extends ForwardingSet<K> {
    public KeySet(Set<K> keySet) {
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
      final Iterator<Entry<K,V>> iterator
          = StandardBiMap.super.entrySet().iterator();
      return new Iterator<K>() {
          Entry<K,V> entry;
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

    /*
     * No need to override read-only or unsupported operations size, isEmpty,
     * add, addAll, contains, containsAll, toArray, toArray, equals, hashCode,
     * or toString.
     */
  }

  @Override public Set<V> values() {
    if (valueSet == null) {
      /*
       * We can almost reuse the inverse's keySet, except we have to fix the
       * iteration order so that it is consistent with the forward map.
       */
      valueSet = new Values(inverse.keySet());
    }
    return valueSet;
  }

  /** @see #values() */
  private class Values extends ForwardingSet<V> {
    Values(Set<V> values) {
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

    /*
     * No need to override remove operations clear, remove, removeAll,
     * retainAll, because the backing set is from the inverse bimap.
     */

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

  @Override public Set<Entry<K,V>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet(super.entrySet());
    }
    return entrySet;
  }

  /** @see #entrySet() */
  private class EntrySet extends ForwardingSet<Entry<K,V>> {
    EntrySet(Set<Entry<K,V>> entrySet) {
      super(entrySet);
    }

    @Override public void clear() {
      StandardBiMap.this.clear();
    }

    @Override public boolean remove(Object object) {
      if (!(object instanceof Entry)) {
        return false;
      }
      Entry<?,?> entry = (Entry<?,?>) object;
      if (!containsEntry(entry.getKey(), entry.getValue())) {
        return false;
      }
      super.remove(entry.getKey());
      inverse.delegate().remove(entry.getValue());
      return true;
    }

    @Override public Iterator<Entry<K,V>> iterator() {
      return new ForwardingIterator<Entry<K,V>>(super.iterator()) {
        Entry<K,V> entry;
        @Override public Entry<K,V> next() {
          entry = super.next();
          return new ForwardingMapEntry<K,V>(entry) {
            @Override public V setValue(V value) {
                /* similar to putInBothMaps, but set via entry */
                if (Objects.equal(value, getValue())) {
                  return value;
                }
                if (containsValue(value)) {
                  throw new IllegalArgumentException(
                      "value already present: " + value);
                }
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
    @Override public boolean removeAll(Collection<?> c) {
      return removeAllImpl(this, c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return retainAllImpl(this, c);
    }

    /*
     * No need to override read-only or unsupported operations size, isEmpty,
     * add, addAll, equals, hashCode, or toString.
     */
  }

  private V putInBothMaps(K key, V value, boolean force) {
    boolean containedKey = containsKey(key);
    if (containedKey && Objects.equal(value, get(key))) {
      return value;
    }
    if (force) {
      inverse().remove(value);
    } else if (containsValue(value)) {
      throw new IllegalArgumentException(
          "value already present: " + value);
    }
    V oldValue = super.put(key, value);
    updateInverseMap(key, containedKey, oldValue, value);
    return oldValue;
  }

  private void updateInverseMap(K key, boolean containedKey, V oldValue,
      V newValue) {
    if (containedKey) {
      removeFromInverseMap(oldValue);
    }
    inverse.delegate().put(newValue, key);
  }

  private V removeFromBothMaps(Object key) {
    V oldValue = super.remove(key);
    removeFromInverseMap(oldValue);
    return oldValue;
  }

  private void removeFromInverseMap(V oldValue) {
    inverse.delegate().remove(oldValue);
  }

  private boolean containsEntry(Object key, Object value) {
    Object valueForKey = get(key);
    return (valueForKey == null)
        ? ((value == null) && containsKey(key))
        : valueForKey.equals(value);
  }
}
