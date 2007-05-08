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

import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tests for {@link LinkedListMultimap}.
 *
 * @author mbostock
 */
public class LinkedListMultimapTest extends AbstractListMultimapTest {

  @Override protected LinkedListMultimap<String, Integer> create() {
    return new LinkedListMultimap<String, Integer>();
  }

  @SuppressWarnings("unchecked")
  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return ((LinkedListMultimap<String, Integer>) multimap).clone();
  }

  /* "Linked" prefix avoids collision with AbstractMultimapTest. */

  public void testLinkedToString() {
    assertEquals("{foo=[3, -1, 2, 4, 1], bar=[1, 2, 3, 1]}",
        createSample().toString());
  }

  public void testLinkedEmpty() {
    Multimap<String, Integer> map = create();
    assertEquals("{}", map.toString());
    assertEquals("[]", map.entries().toString());
  }

  public void testLinkedEmptyGet() {
    Multimap<String, Integer> map = create();
    map.get("foo"); // shouldn't have any side-effect
    assertEquals("{}", map.toString());
    assertEquals("[]", map.entries().toString());
  }

  public void testLinkedGetAdd() {
    LinkedListMultimap<String, Integer> map = create();
    map.put("bar", 1);
    Collection<Integer> foos = map.get("foo");
    foos.add(2);
    foos.add(3);
    map.put("bar", 4);
    map.put("foo", 5);
    assertEquals("{bar=[1, 4], foo=[2, 3, 5]}", map.toString());
    assertEquals("[bar=1, foo=2, foo=3, bar=4, foo=5]",
        map.entries().toString());
  }

  public void testLinkedGetInsert() {
    ListMultimap<String, Integer> map = create();
    map.put("bar", 1);
    List<Integer> foos = map.get("foo");
    foos.add(2);
    foos.add(0, 3);
    map.put("bar", 4);
    map.put("foo", 5);
    assertEquals("{bar=[1, 4], foo=[3, 2, 5]}", map.toString());
    assertEquals("[bar=1, foo=3, foo=2, bar=4, foo=5]",
        map.entries().toString());
  }

  public void testLinkedGetSet() {
    LinkedListMultimap<String, Integer> map = create();
    map.put("bar", 1);
    map.get("bar").set(0, 2);
    assertEquals("{bar=[2]}", map.toString());
    assertEquals("[bar=2]", map.entries().toString());
  }

  public void testLinkedGetRemove() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.get("foo").remove(2);
    map.get("bar").remove(1);
    assertEquals("{bar=[3]}", map.toString());
    assertEquals("[bar=3]", map.entries().toString());
  }

  public void testLinkedPutInOrder() {
    Multimap<String, Integer> map = create();
    map.put("foo", 1);
    map.put("bar", 2);
    map.put("bar", 3);
    assertEquals("{foo=[1], bar=[2, 3]}", map.toString());
    assertEquals("[foo=1, bar=2, bar=3]", map.entries().toString());
  }

  public void testLinkedPutOutOfOrder() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    assertEquals("{bar=[1, 3], foo=[2]}", map.toString());
    assertEquals("[bar=1, foo=2, bar=3]", map.entries().toString());
  }

  public void testLinkedPutAllIterable() {
    Multimap<String, Integer> map = create();
    map.putAll("foo", Arrays.asList(1, 2));
    assertEquals("{foo=[1, 2]}", map.toString());
    assertEquals("[foo=1, foo=2]", map.entries().toString());
  }

  public void testLinkedPutAllMultimap() {
    Multimap<String, Integer> src = create();
    src.put("bar", 1);
    src.put("foo", 2);
    src.put("bar", 3);
    Multimap<String, Integer> dst = create();
    dst.putAll(src);
    assertEquals("{bar=[1, 3], foo=[2]}", dst.toString());
    assertEquals("[bar=1, foo=2, bar=3]", src.entries().toString());
  }

  public void testLinkedReplaceValues() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.put("bar", 4);
    assertEquals("{bar=[1, 3, 4], foo=[2]}", map.toString());
    map.replaceValues("bar", Arrays.asList(1, 2));
    assertEquals("[bar=1, foo=2, bar=2]", map.entries().toString());
    assertEquals("{bar=[1, 2], foo=[2]}", map.toString());
  }

  public void testLinkedRemove() {
    Multimap<String, Integer> map = create();
    map.put("foo", 1);
    map.put("foo", 2);
    map.remove("foo", 1);
    assertEquals("[foo=2]", map.entries().toString());
  }

  public void testLinkedRemoveAll() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.put("bar", 4);
    map.removeAll("foo");
    assertEquals("[bar=1, bar=3, bar=4]", map.entries().toString());
    assertEquals("{bar=[1, 3, 4]}", map.toString());
    map.removeAll("bar");
    assertEquals("[]", map.entries().toString());
    assertEquals("{}", map.toString());
  }

  public void testLinkedClear() {
    ListMultimap<String, Integer> map = create();
    map.put("foo", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    List<Integer> foos = map.get("foo");
    Collection<Integer> values = map.values();
    assertEquals(Arrays.asList(1, 2), foos);
    assertContentsInOrder(values, 1, 2, 3);
    map.clear();
    assertEquals(Collections.emptyList(), foos);
    assertContentsInOrder(values);
    assertEquals("[]", map.entries().toString());
    assertEquals("{}", map.toString());
  }

  public void testLinkedKeySet() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.put("bar", 4);
    assertEquals("[bar, foo]", map.keySet().toString());
    map.keySet().remove("bar");
    assertEquals("{foo=[2]}", map.toString());
  }

  public void testLinkedKeys() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.put("bar", 4);
    assertEquals("[bar=1, foo=2, bar=3, bar=4]",
        map.entries().toString());
    assertContentsInOrder(map.keys(), "bar", "foo", "bar", "bar");
    map.keys().remove("bar"); // bar is no longer the first key!
    assertEquals("{foo=[2], bar=[3, 4]}", map.toString());
  }

  public void testLinkedValues() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    map.put("bar", 4);
    assertEquals("[1, 2, 3, 4]", map.values().toString());
    map.values().remove(2);
    assertEquals("{bar=[1, 3, 4]}", map.toString());
  }

  public void testLinkedEntries() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    Iterator<Map.Entry<String, Integer>> entries = map.entries().iterator();
    Map.Entry<String, Integer> entry = entries.next();
    assertEquals("bar", entry.getKey());
    assertEquals(1, (int) entry.getValue());
    entry = entries.next();
    assertEquals("foo", entry.getKey());
    assertEquals(2, (int) entry.getValue());
    entry.setValue(4);
    entry = entries.next();
    assertEquals("bar", entry.getKey());
    assertEquals(3, (int) entry.getValue());
    assertFalse(entries.hasNext());
    entries.remove();
    assertEquals("{bar=[1], foo=[4]}", map.toString());
  }

  public void testLinkedAsMapEntries() {
    Multimap<String, Integer> map = create();
    map.put("bar", 1);
    map.put("foo", 2);
    map.put("bar", 3);
    Iterator<Map.Entry<String, Collection<Integer>>> entries
        = map.asMap().entrySet().iterator();
    Map.Entry<String, Collection<Integer>> entry = entries.next();
    assertEquals("bar", entry.getKey());
    assertContentsInOrder(entry.getValue(), 1, 3);
    try {
      entry.setValue(Arrays.<Integer>asList());
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    entries.remove(); // clear
    entry = entries.next();
    assertEquals("foo", entry.getKey());
    assertContentsInOrder(entry.getValue(), 2);
    assertFalse(entries.hasNext());
    assertEquals("{foo=[2]}", map.toString());
  }

  public void testLinkedEquals() {
    Multimap<String, Integer> map1 = create();
    map1.put("bar", 1);
    map1.put("foo", 2);
    map1.put("bar", 3);
    Multimap<String, Integer> map2 = new ArrayListMultimap<String, Integer>();
    map2.putAll(map1);
    assertTrue(map1.equals(map2));
    assertTrue(map2.equals(map1));
    assertFalse(map1.equals(null));
    assertFalse(map1.equals(new Object()));
  }

  public void testLinkedHashCode() {
    Multimap<String, Integer> map1 = create();
    map1.put("bar", 1);
    map1.put("foo", 2);
    map1.put("bar", 3);
    Multimap<String, Integer> map2 = new ArrayListMultimap<String, Integer>();
    map2.putAll(map1);
    assertEquals(map1.hashCode(), map2.hashCode());
  }

  public void testLinkedIteration() {
    ListMultimap<String, Integer> map = create();
    map.put("bar", 1);
    map.putAll("foo", Arrays.asList(2, 3, 4));
    map.putAll("bar", Arrays.asList(5, 6));
    map.putAll("foo", Arrays.asList(7, 8));
    ListIterator<Integer> values = map.get("foo").listIterator(5);
    try {
      values.set(5);
      fail("IllegalStateException expected");
    } catch (IllegalStateException expected) {}
    try {
      values.remove();
      fail("IllegalStateException expected");
    } catch (IllegalStateException expected) {}
    values.add(9);
    ListIteratorTester<Integer> tester = ListIteratorTester.of(values);
    tester.checkPrevious(9, 8, 7, 4, 3, 2);
    tester.checkIndex(0, 6);
    tester.checkNext(2, 3, 4, 7, 8, 9);
    tester.checkIndex(6, 6);
    tester.checkPrevious(9, 8, 7);
    tester.checkNext(7, 8);
  }

  public void testLinkedValueListIterator() {
    ListMultimap<String, Integer> map = create();
    map.putAll("foo", Arrays.asList(3, -1, 2, 4, 1));
    map.putAll("bar", Arrays.asList(1, 2, 3, 1));

    try {
      map.get("foo").listIterator(-1);
      fail("IndexOutOfBoundsException expected");
    } catch (IndexOutOfBoundsException expected) {}

    ListIteratorTester<Integer> tester;

    tester = ListIteratorTester.of(map.get("foo").listIterator(0));
    tester.checkIndex(0, 5);
    tester.checkNext(3, -1, 2, 4, 1);
    tester = ListIteratorTester.of(map.get("foo").listIterator(0));
    tester.checkPrevious();

    tester = ListIteratorTester.of(map.get("foo").listIterator(1));
    tester.checkIndex(1, 5);
    tester.checkNext(-1, 2, 4, 1);
    tester = ListIteratorTester.of(map.get("foo").listIterator(1));
    tester.checkPrevious(3);

    tester = ListIteratorTester.of(map.get("foo").listIterator(4));
    tester.checkIndex(4, 5);
    tester.checkNext(1);
    tester = ListIteratorTester.of(map.get("foo").listIterator(4));
    tester.checkPrevious(4, 2, -1, 3);

    tester = ListIteratorTester.of(map.get("foo").listIterator(5));
    tester.checkIndex(5, 5);
    tester.checkNext();
    tester = ListIteratorTester.of(map.get("foo").listIterator(5));
    tester.checkPrevious(1, 4, 2, -1, 3);

    try {
      map.get("foo").listIterator(6);
      fail("IndexOutOfBoundsException expected");
    } catch (IndexOutOfBoundsException expected) {}
  }

  public void testLinkedIterationRemove() {
    ListMultimap<String, Integer> map = create();
    map.put("bar", 1);
    map.putAll("foo", Arrays.asList(2, 3, 4));
    map.putAll("bar", Arrays.asList(5, 6));
    map.putAll("foo", Arrays.asList(7, 8, 9));
    ListIterator<Integer> values = map.get("foo").listIterator(3);
    ListIteratorTester<Integer> tester = ListIteratorTester.of(values);
    tester.checkPrevious(4);
    values.remove(); // removes 4
    assertEquals(Arrays.asList(2, 3, 7, 8, 9), map.get("foo"));
    tester.checkIndex(2, 5);
    tester.checkNext(7, 8);
    values.remove(); // removes 8
    assertEquals(Arrays.asList(2, 3, 7, 9), map.get("foo"));
    tester.checkIndex(3, 4);
    tester.checkPrevious(7, 3, 2);
    tester.checkNext(2, 3);
    values.remove(); // removes 3
    assertEquals(Arrays.asList(2, 7, 9), map.get("foo"));
    tester.checkIndex(1, 3);
    tester.checkNext(7, 9);
    values.remove(); // removes 9
    assertEquals(Arrays.asList(2, 7), map.get("foo"));
    tester.checkIndex(2, 2);
    tester.checkPrevious(7);
    values.remove(); // removes 7
    assertEquals(Arrays.asList(2), map.get("foo"));
    tester.checkIndex(1, 1);
    tester.checkPrevious(2);
    values.remove(); // removes 2
    tester.checkIndex(0, 0);
    try {
      values.previous();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
    try {
      values.next();
      fail("NoSuchElementException expected");
    } catch (NoSuchElementException expected) {}
    assertEquals(Arrays.<Integer>asList(), map.get("foo"));
    assertEquals(Arrays.asList(1, 5, 6), map.get("bar"));
    assertContentsInOrder(map.values(), 1, 5, 6);
    assertContentsInOrder(map.keys(), "bar", "bar", "bar");
    assertContentsInOrder(map.keySet(), "bar");
  }

  public void testLinkedClone() {
    LinkedListMultimap<String, Integer> map = create();
    LinkedListMultimap<String, Integer> clone = map.clone();
    assertTrue(map.equals(clone));
    assertTrue(clone.equals(map));
    map.put("foo", 10);
    assertFalse(map.equals(clone));
    assertFalse(clone.equals(map));
  }

  public void testMultimapConstructor() {
    Multimap<String, Integer> multimap = createSample();
    LinkedListMultimap<String, Integer> copy =
        Multimaps.newLinkedListMultimap(multimap);
    assertEquals(multimap, copy);
  }
}
