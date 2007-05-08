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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@link Synchronized#multimap}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class SynchronizedMultimapTest extends AbstractSetMultimapTest {

  @Override protected Multimap<String, Integer> create() {
    TestMultimap<String, Integer> inner = new TestMultimap<String, Integer>();
    Multimap<String, Integer> outer = Synchronized.multimap(inner, inner.lock);
    return outer;
  }

  @Override protected Multimap<String, Integer> makeClone(
      Multimap<String, Integer> multimap) {
    return null;
  }

  private static final class TestMultimap<K,V> extends ForwardingMultimap<K,V> {
    public final Object lock = new Integer(1); // something Serializable

    public TestMultimap() {
      super(new HashMultimap<K,V>());
    }

    @Override public String toString() {
      assertTrue(Thread.holdsLock(lock));
      return super.toString();
    }

    @Override public boolean equals(@Nullable Object o) {
      assertTrue(Thread.holdsLock(lock));
      return super.equals(o);
    }

    @Override public int hashCode() {
      assertTrue(Thread.holdsLock(lock));
      return super.hashCode();
    }

    @Override public int size() {
      assertTrue(Thread.holdsLock(lock));
      return super.size();
    }

    @Override public boolean isEmpty() {
      assertTrue(Thread.holdsLock(lock));
      return super.isEmpty();
    }

    @Override public boolean containsKey(@Nullable Object key) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsKey(key);
    }

    @Override public boolean containsValue(@Nullable Object value) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsValue(value);
    }

    @Override public boolean containsEntry(@Nullable Object key,
        @Nullable Object value) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsEntry(key, value);
    }

    @Override public Collection<V> get(@Nullable K key) {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(mbostock): verify that the Collection is also synchronized? */
      return super.get(key);
    }

    @Override public boolean put(K key, V value) {
      assertTrue(Thread.holdsLock(lock));
      return super.put(key, value);
    }

    @Override public void putAll(@Nullable K key,
        Iterable<? extends V> values) {
      assertTrue(Thread.holdsLock(lock));
      super.putAll(key, values);
    }

    @Override public void putAll(Multimap<? extends K, ? extends V> map) {
      assertTrue(Thread.holdsLock(lock));
      super.putAll(map);
    }

    @Override public Collection<V> replaceValues(@Nullable K key,
        Iterable<? extends V> values) {
      assertTrue(Thread.holdsLock(lock));
      return super.replaceValues(key, values);
    }

    @Override public boolean remove(@Nullable Object key,
        @Nullable Object value) {
      assertTrue(Thread.holdsLock(lock));
      return super.remove(key, value);
    }

    @Override public Collection<V> removeAll(@Nullable Object key) {
      assertTrue(Thread.holdsLock(lock));
      return super.removeAll(key);
    }

    @Override public void clear() {
      assertTrue(Thread.holdsLock(lock));
      super.clear();
    }

    @Override public Set<K> keySet() {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(mbostock): verify that the Set is also synchronized? */
      return super.keySet();
    }

    @Override public Multiset<K> keys() {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(mbostock): verify that the Set is also synchronized? */
      return super.keys();
    }

    @Override public Collection<V> values() {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(mbostock): verify that the Collection is also synchronized? */
      return super.values();
    }

    @Override public Collection<Map.Entry<K, V>> entries() {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(mbostock): verify that the Collection is also synchronized? */
      return super.entries();
    }

    @Override public Map<K, Collection<V>> asMap() {
      assertTrue(Thread.holdsLock(lock));
      /* TODO(jlevy): verify that the Map is also synchronized? */
      return super.asMap();
    }
  }
}
