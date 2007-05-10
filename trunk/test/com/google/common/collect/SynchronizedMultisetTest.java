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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Tests for {@code Multisets#synchronizedMultiset}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class SynchronizedMultisetTest extends AbstractMultisetTest {

  @Override protected <E> Multiset<E> create() {
    TestMultiset<E> inner = new TestMultiset<E>();
    Multiset<E> outer = Multisets.synchronizedMultiset(inner);
    inner.lock = outer;
    return outer;
  }

  private static HashMultiset<String> createInnerSample() {
    HashMultiset<String> multiset = new HashMultiset<String>();
    multiset.addAll(Arrays.asList("a", "b", "b", "c", "d", "d", "d"));
    return multiset;
  }

  @Override public void testNullPointerExceptions() throws Exception {
    /* Skip this test, as SynchronizedMultiset is not a public class. */
  }

  public void testIteratorBashing() throws Exception {
    final HashMultiset<String> inner = createInnerSample();
    final Multiset<String> outer
        = Multisets.synchronizedMultiset(createInnerSample());
    IteratorTester tester = new IteratorTester(9) {
      @Override protected Iterator<?> newReferenceIterator() {
        return inner.iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return outer.iterator();
      }
    };
    tester.test();
  }

  public void testElementSetIteratorBashing() throws Exception {
    final HashMultiset<String> inner = createInnerSample();
    final Multiset<String> outer
        = Multisets.synchronizedMultiset(createInnerSample());
    IteratorTester tester = new IteratorTester(9) {
      @Override protected Iterator<?> newReferenceIterator() {
        return inner.elementSet().iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return outer.elementSet().iterator();
      }
    };
    tester.test();
  }

  public void testToString() {
    Multiset<String> inner = createInnerSample();
    Multiset<String> outer = Multisets.synchronizedMultiset(inner);
    assertEquals(inner.toString(), outer.toString());
  }

  public void testIteration() throws Exception {
    ms.add("a", 1);
    ms.add("b", 2);
    int a = 0;
    int b = 0;
    synchronized (ms) {
      for (String s : ms) {
        if ("a".equals(s)) {
          a++;
        } else if ("b".equals(s)) {
          b++;
        } else {
          fail("unknown element: " + s);
        }
      }
    }
    assertEquals(1, a);
    assertEquals(2, b);
  }

  public void testIterationViaElementSet() throws Exception {
    ms.add("a", 1);
    ms.add("b", 2);
    int a = 0;
    int b = 0;
    synchronized (ms) {
      for (String s : ms.elementSet()) {
        if ("a".equals(s)) {
          a++;
        } else if ("b".equals(s)) {
          b++;
        } else {
          fail("unknown element: " + s);
        }
      }
    }
    assertEquals(1, a);
    assertEquals(1, b);
  }

  private static final class TestMultiset<E> extends AbstractMultiset<E> {
    public Object lock;

    public TestMultiset() {
      super(new HashMap<E,Frequency>());
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

    @Override public boolean add(@Nullable E o) {
      assertTrue(Thread.holdsLock(lock));
      return super.add(o);
    }

    @Override public boolean addAll(Collection<? extends E> c) {
      assertTrue(Thread.holdsLock(lock));
      return super.addAll(c);
    }

    @Override public void clear() {
      assertTrue(Thread.holdsLock(lock));
      super.clear();
    }

    @Override public boolean contains(@Nullable Object o) {
      assertTrue(Thread.holdsLock(lock));
      return super.contains(o);
    }

    @Override public boolean containsAll(Collection<?> c) {
      assertTrue(Thread.holdsLock(lock));
      return super.containsAll(c);
    }

    @Override public boolean isEmpty() {
      assertTrue(Thread.holdsLock(lock));
      return super.isEmpty();
    }

    /* Don't test iterator(); it may or may not hold the lock. */

    @Override public boolean remove(@Nullable Object o) {
      assertTrue(Thread.holdsLock(lock));
      return super.remove(o);
    }

    @Override public boolean removeAll(Collection<?> c) {
      assertTrue(Thread.holdsLock(lock));
      return super.removeAll(c);
    }

    @Override public boolean retainAll(Collection<?> c) {
      assertTrue(Thread.holdsLock(lock));
      return super.retainAll(c);
    }

    @Override public int size() {
      assertTrue(Thread.holdsLock(lock));
      return super.size();
    }

    @Override public Object[] toArray() {
      assertTrue(Thread.holdsLock(lock));
      return super.toArray();
    }

    @Override public <T> T[] toArray(T[] a) {
      assertTrue(Thread.holdsLock(lock));
      return super.toArray(a);
    }

    @Override public int count(@Nullable Object o) {
      assertTrue(Thread.holdsLock(lock));
      return super.count(o);
    }

    @Override public boolean add(@Nullable E o, int occurrences) {
      assertTrue(Thread.holdsLock(lock));
      return super.add(o, occurrences);
    }

    @Override public int remove(@Nullable Object o, int occurrences) {
      assertTrue(Thread.holdsLock(lock));
      return super.remove(o, occurrences);
    }

    @Override public int removeAllOccurrences(@Nullable Object o) {
      assertTrue(Thread.holdsLock(lock));
      return super.removeAllOccurrences(o);
    }

    @Override public Set<E> elementSet() {
      assertTrue(Thread.holdsLock(lock));
      return super.elementSet();
    }

    @Override public Set<Multiset.Entry<E>> entrySet() {
      assertTrue(Thread.holdsLock(lock));
      return super.entrySet();
    }
  }
}
