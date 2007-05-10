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
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@code Synchronized#set}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class SynchronizedSetTest extends AbstractCollectionTest {

  protected <E> Set<E> create() {
    TestSet<E> inner = new TestSet<E>(new HashSet<E>(), new Object());
    Set<E> outer = Synchronized.set(inner, inner.lock);
    return outer;
  }

  @Override public void testNullPointerExceptions() throws Exception {
    /* Skip this test, as SynchronizedSet is not a public class. */
  }

  static class TestSet<E> extends ForwardingSet<E> {
    public final Object lock;

    public TestSet(Set<E> delegate, Object lock) {
      super(delegate);
      checkNotNull(lock);
      this.lock = lock;
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
  }
}
