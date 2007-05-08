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

import static com.google.common.collect.Synchronized.SynchronizedBiMap;
import static com.google.common.collect.Synchronized.SynchronizedSet;
import java.util.Set;
import junit.framework.TestSuite;

/**
 * Tests for {@link Synchronized#biMap}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class SynchronizedBiMapTest extends SynchronizedMapTest {

  public static TestSuite suite() {
    TestSuite suite = new TestSuite(SynchronizedBiMapTest.class);
    suite.addTestSuite(AbstractBiMapTests.class);
    return suite;
  }

  @Override protected <K,V> BiMap<K,V> create() {
    TestBiMap<K,V> inner = new TestBiMap<K,V>(new HashBiMap<K,V>(), lock);
    BiMap<K,V> outer = Synchronized.biMap(inner, lock);
    return outer;
  }

  static class TestBiMap<K,V> extends TestMap<K,V> implements BiMap<K,V> {
    private final BiMap<K,V> delegate;

    public TestBiMap(BiMap<K,V> delegate, Object lock) {
      super(delegate, lock);
      this.delegate = delegate;
    }

    public V forcePut(K key, V value) {
      assertTrue(Thread.holdsLock(lock));
      return delegate.forcePut(key, value);
    }

    public BiMap<V,K> inverse() {
      assertTrue(Thread.holdsLock(lock));
      return delegate.inverse();
    }

    @Override public Set<V> values() {
      assertTrue(Thread.holdsLock(lock));
      return delegate.values();
    }
  }

  public void testForcePut() {
    create().forcePut(null, null);
  }

  @SuppressWarnings("unchecked")
  public void testInverse() {
    BiMap<String, Integer> bimap = create();
    BiMap<Integer, String> inverse = bimap.inverse();
    assertSame(bimap, inverse.inverse());
    assertTrue(inverse instanceof SynchronizedBiMap);
    assertSame(lock, ((SynchronizedBiMap<?,?>) inverse).lock);
  }

  @SuppressWarnings("unchecked")
  @Override public void testValues() {
    BiMap<String, Integer> map = create();
    Set<Integer> values = map.values();
    assertTrue(values instanceof SynchronizedSet);
    assertSame(lock, ((SynchronizedSet<?>) values).lock);
  }

  public static class AbstractBiMapTests extends AbstractBiMapTest {
    private final Object lock = new Object();

    @Override protected BiMap<Integer, String> create() {
      TestBiMap<Integer, String> inner = new TestBiMap<Integer, String>(
          new HashBiMap<Integer, String>(), lock);
      BiMap<Integer, String> outer = Synchronized.biMap(inner, lock);
      return outer;
    }
  }
}
