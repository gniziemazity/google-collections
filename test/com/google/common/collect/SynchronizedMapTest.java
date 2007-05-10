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
import static com.google.common.collect.Synchronized.SynchronizedCollection;
import static com.google.common.collect.Synchronized.SynchronizedSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests for {@code Synchronized#map}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class SynchronizedMapTest extends TestCase {
  protected final Object lock = new Object();

  protected <K,V> Map<K,V> create() {
    TestMap<K,V> inner = new TestMap<K,V>(new HashMap<K,V>(), lock);
    Map<K,V> outer = Synchronized.map(inner, lock);
    return outer;
  }

  static class TestMap<K,V> extends ForwardingMap<K,V> {
    public final Object lock;

    public TestMap(Map<K,V> delegate, Object lock) {
      super(delegate);
      checkNotNull(lock);
      this.lock = lock;
    }

    @Override public int size() {
      assertTrue(Thread.holdsLock(lock));
      return super.size();
    }

    @Override public boolean isEmpty() {
      assertTrue(Thread.holdsLock(lock));
      return super.isEmpty();
    }

    @Override public V remove(Object object) {
      assertTrue(Thread.holdsLock(lock));
      return super.remove(object);
    }

    @Override public void clear() {
      assertTrue(Thread.holdsLock(lock));
      super.clear();
    }

    @Override public boolean containsKey(Object key) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsKey(key);
    }

    @Override public boolean containsValue(Object value) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsValue(value);
    }

    @Override public V get(Object key) {
      assertTrue(Thread.holdsLock(lock));
      return super.get(key);
    }

    @Override public V put(K key, V value) {
      assertTrue(Thread.holdsLock(lock));
      return super.put(key, value);
    }

    @Override public void putAll(Map<? extends K, ? extends V> map) {
      assertTrue(Thread.holdsLock(lock));
      super.putAll(map);
    }

    @Override public Set<K> keySet() {
      assertTrue(Thread.holdsLock(lock));
      return super.keySet();
    }

    @Override public Collection<V> values() {
      assertTrue(Thread.holdsLock(lock));
      return super.values();
    }

    @Override public Set<Entry<K,V>> entrySet() {
      assertTrue(Thread.holdsLock(lock));
      return super.entrySet();
    }

    @Override public boolean equals(Object obj) {
      assertTrue(Thread.holdsLock(lock));
      return super.equals(obj);
    }

    @Override public int hashCode() {
      assertTrue(Thread.holdsLock(lock));
      return super.hashCode();
    }

    @Override public String toString() {
      assertTrue(Thread.holdsLock(lock));
      return super.toString();
    }
  }

  /*
   * This is somewhat of a weak test; we verify that all of the methods are
   * correct, but not that they're actually forwarding correctly. We also rely
   * on the other tests (e.g., SynchronizedSetTest) to verify that the
   * collection views are synchronized correctly.
   */

  public void testSize() {
    create().size();
  }

  public void testIsEmpty() {
    create().isEmpty();
  }

  public void testRemove() {
    create().remove(null);
  }

  public void testClear() {
    create().clear();
  }

  public void testContainsKey() {
    create().containsKey(null);
  }

  public void testContainsValue() {
    create().containsValue(null);
  }

  public void testGet() {
    create().get(null);
  }

  public void testPut() {
    create().put(null, null);
  }

  public void testPutAll() {
    create().putAll(new HashMap<String, Integer>());
  }

  @SuppressWarnings("unchecked")
  public void testKeySet() {
    Map<String, Integer> map = create();
    Set<String> keySet = map.keySet();
    assertTrue(keySet instanceof SynchronizedSet);
    assertSame(lock, ((SynchronizedSet<?>) keySet).lock);
  }

  @SuppressWarnings("unchecked")
  public void testValues() {
    Map<String, Integer> map = create();
    Collection<Integer> values = map.values();
    assertTrue(values instanceof SynchronizedCollection);
    assertSame(lock, ((SynchronizedCollection<?>) values).lock);
  }

  @SuppressWarnings("unchecked")
  public void testEntrySet() {
    Map<String, Integer> map = create();
    Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
    assertTrue(entrySet instanceof SynchronizedSet);
    assertSame(lock, ((SynchronizedSet<?>) entrySet).lock);
  }

  public void testEquals() {
    create().equals(new HashMap<String, Integer>());
  }

  public void testHashCode() {
    create().hashCode();
  }

  public void testToString() {
    create().toString();
  }
}
