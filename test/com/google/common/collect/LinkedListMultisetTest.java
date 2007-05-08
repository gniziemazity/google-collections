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

import com.google.common.collect.helpers.MoreAsserts;
import java.util.Collections;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Tests for {@link LinkedListMultiset}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class LinkedListMultisetTest extends AbstractMultisetTest {

  @Override protected <E> LinkedListMultiset<E> create() {
    return new LinkedListMultiset<E>();
  }

  public void testToString() {
    Multiset<String> m = create();
    m.add("a");
    m.add("b");
    m.add("a");
    assertEquals("[a, b, a]", m.toString());
    m.remove("a");
    assertEquals("[b, a]", m.toString());
  }

  public void testToStringConsecutive() {
    Multiset<String> m = create();
    m.add("a", 3);
    m.add("b", 2);
    m.add("c");
    assertEquals("[a x 3, b x 2, c]", m.toString());
  }

  public void testToStringConsecutiveNulls() {
    Multiset<String> m = create();
    m.add(null);
    m.add("a");
    m.add(null, 4);
    m.add("a", 3);
    m.add("b", 10);
    assertEquals("[null, a, null x 4, a x 3, b x 10]", m.toString());
  }

  public void testToArray() {
    Multiset<String> m = create();
    m.add("a");
    m.add("b");
    m.add("a");
    MoreAsserts.assertEquals(new String[] { "a", "b", "a" }, m.toArray());
    m.remove("a");
    MoreAsserts.assertEquals(new String[] { "b", "a" }, m.toArray());
  }

  public void testClone() {
    LinkedListMultiset<String> m = create();
    HashMultiset<String> h = new HashMultiset<String>();
    Collections.addAll(m, "a", "b", "a");
    Collections.addAll(h, "a", "b", "a");
    assertEquals(m, h);
    LinkedListMultiset<String> c = m.clone();
    assertEquals(m, c);
    assertEquals(h, c);
    c.add("c");
    assertEquals("[a, b, a, c]", c.toString());
    assertEquals("[a, b, a]", m.toString());
    assertFalse(m.equals(c));
    assertFalse(c.equals(m));
  }

  @Override public void testAddTooMany() {
    /* Disable this test, which isn't reasonable for LinkedListMultiset. */
  }

  @Override public void testReallyBig() {
    /* Disable this test, which isn't reasonable for LinkedListMultiset. */
  }

  public void testFirst() {
    LinkedListMultiset<String> m = create();
    Collections.addAll(m, "a", "b", "b", "c", "b");
    assertEquals("a", m.first());
    m.remove("a");
    assertEquals("b", m.first());
    m.clear();
    try {
      m.first();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
  }

  public void testLast() {
    LinkedListMultiset<String> m = create();
    Collections.addAll(m, "a", "b", "b", "c", "b");
    assertEquals("b", m.last());
    ListIterator<String> i = m.listIterator();
    while (i.hasNext()) {
      i.next();
    }
    i.remove(); // removes last element, a "b"
    assertEquals("c", m.last());
    m.clear();
    try {
      m.last();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
  }

  public void testIteratorNotListIterator() {
    LinkedListMultiset<String> m = create();
    assertFalse(m.iterator() instanceof ListIterator);
  }

  public void testListIterator() {
    LinkedListMultiset<String> m = create();
    Collections.addAll(m, "a", "b", "b", "c", "b");
    ListIterator<String> i = m.listIterator();
    ListIteratorTester<String> tester = ListIteratorTester.of(i);
    tester.checkIndex(0, 5);
    tester.checkNext("a", "b", "b", "c", "b");
    tester.checkIndex(5, 5);
    tester.checkPrevious("b", "c", "b", "b", "a");
    tester.checkIndex(0, 5);
    i.remove();
    tester.checkIndex(0, 4);
    MoreAsserts.assertContentsInOrder(m, "b", "b", "c", "b");
    MoreAsserts.assertContentsInOrder(m.elementSet(), "b", "c");
    tester.checkNext("b", "b");
    tester.checkIndex(2, 4);
    i.remove();
    tester.checkIndex(1, 3);
    MoreAsserts.assertContentsInOrder(m, "b", "c", "b");
    MoreAsserts.assertContentsInOrder(m.elementSet(), "b", "c");
    tester.checkNext("c");
    tester.checkIndex(2, 3);
    i.remove();
    tester.checkIndex(1, 2);
    MoreAsserts.assertContentsInOrder(m, "b", "b");
    MoreAsserts.assertContentsInOrder(m.elementSet(), "b");
    tester.checkPrevious("b");
    tester.checkIndex(0, 2);
    i.remove();
    tester.checkIndex(0, 1);
    tester.checkNext("b");
    tester.checkIndex(1, 1);
    i.remove();
    tester.checkIndex(0, 0);
    assertTrue(m.isEmpty());
    assertTrue(m.elementSet().isEmpty());
  }
}
