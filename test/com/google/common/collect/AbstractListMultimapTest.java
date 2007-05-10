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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Tests for {@code ListMultimap} implementations.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public abstract class AbstractListMultimapTest extends AbstractMultimapTest {

  @Override protected abstract ListMultimap<String, Integer> create();

  /**
   * Test adding duplicate key-value pairs to multimap.
   */
  public void testDuplicates() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("foo", 1);
    assertEquals(4, multimap.size());
    assertTrue(multimap.containsEntry("foo", 1));
    multimap.remove("foo", 1);
    assertEquals(3, multimap.size());
    assertTrue(multimap.containsEntry("foo", 1));
  }

  /**
   * Test returned boolean when adding duplicate key-value pairs to multimap.
   */
  public void testPutReturn() {
    Multimap<String, Integer> multimap = create();
    assertTrue(multimap.put("foo", 1));
    assertTrue(multimap.put("foo", 1));
    assertTrue(multimap.put("foo", 3));
    assertTrue(multimap.put("bar", 5));
  }

  /**
   * Confirm that get() returns a collection equal to a List.
   */
  public void testGetEquals() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    assertEquals(Lists.immutableList(1, 3), multimap.get("foo"));
  }

  public void testAsMapEquals() {
    Multimap<String, Integer> multimap = getMultimap();
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Map<String, Collection<Integer>> map = multimap.asMap();

    Map<String, Collection<Integer>> equalMap = Maps.newHashMap();
    equalMap.put("foo", Arrays.asList(1, nullValue()));
    equalMap.put(nullKey(), Arrays.asList(3));
    assertEquals(map, equalMap);
    assertEquals(equalMap, map);
    assertEquals(equalMap.hashCode(), multimap.hashCode());

    Map<String, Collection<Integer>> unequalMap = Maps.newHashMap();
    equalMap.put("foo", Arrays.asList(3, nullValue()));
    equalMap.put(nullKey(), Arrays.asList(1));
    assertFalse(map.equals(unequalMap));
    assertFalse(unequalMap.equals(map));
  }

  /**
   * Confirm that asMap().entrySet() returns values equal to a List.
   */
  public void testAsMapEntriesEquals() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Iterator<Map.Entry<String,Collection<Integer>>> i =
        multimap.asMap().entrySet().iterator();
    Map.Entry<String,Collection<Integer>> entry = i.next();
    assertEquals("foo", entry.getKey());
    assertEquals(Lists.immutableList(1, 3), entry.getValue());
    assertFalse(i.hasNext());
  }

  /**
   * Test multimap.equals() for multimaps with different insertion orderings.
   */
  public void testEqualsOrdering() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    Multimap<String, Integer> multimap2 = create();
    multimap2.put("foo", 3);
    multimap2.put("foo", 1);
    multimap2.put("bar", 3);
    assertFalse(multimap.equals(multimap2));
  }

  /**
   * Test the ordering of the values returned by multimap.get().
   */
  public void testPutGetOrdering() {
    Multimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    Iterator<Integer> values = multimap.get("foo").iterator();
    assertEquals(Integer.valueOf(1), values.next());
    assertEquals(Integer.valueOf(3), values.next());
  }

  /**
   * Test List-specific methods on List returned by get().
   */
  public void testListMethods() {
    ListMultimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("foo", 5);
    List<Integer> list = multimap.get("foo");

    list.add(1, 2);
    assertEquals(4, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 5);

    list.addAll(3, Arrays.asList(4, 8));
    assertEquals(6, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 8, 5);

    assertEquals(8, list.get(4).intValue());
    assertEquals(4, list.indexOf(8));
    assertEquals(4, list.lastIndexOf(8));

    list.remove(4);
    assertEquals(5, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);

    list.set(4, 10);
    assertEquals(5, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 10);

    List sublist = list.subList(1, 4);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 4);
    list.set(3, 6);
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 6, 10);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 6);
  }

  /**
   * Test ListIterator methods that don't change the multimap.
   */
  public void testListIteratorNavigate() {
    ListMultimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    List<Integer> list = multimap.get("foo");
    ListIterator<Integer> iterator = list.listIterator();

    assertFalse(iterator.hasPrevious());
    assertTrue(iterator.hasNext());
    assertEquals(0, iterator.nextIndex());
    assertEquals(-1, iterator.previousIndex());

    assertEquals(1, iterator.next().intValue());
    assertEquals(3, iterator.next().intValue());
    assertTrue(iterator.hasPrevious());
    assertFalse(iterator.hasNext());

    assertEquals(3, iterator.previous().intValue());
    assertEquals(1, iterator.previous().intValue());
  }

  /**
   * Test ListIterator methods that change the multimap.
   */
  public void testListIteratorUpdate() {
    ListMultimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("foo", 5);
    List<Integer> list = multimap.get("foo");
    ListIterator<Integer> iterator = list.listIterator();

    assertEquals(1, iterator.next().intValue());
    iterator.set(2);
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 2, 3, 5);

    assertEquals(3, iterator.next().intValue());
    iterator.remove();
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 2, 5);
  }

  /**
   * Test calling setValue() on an entry returned by multimap.entries().
   */
  public void testEntrySetValue() {
    ListMultimap<String, Integer> multimap = create();
    multimap.put("foo", 1);
    Collection<Map.Entry<String, Integer>> entries = multimap.entries();
    int oldValue = entries.iterator().next().setValue(2);
    assertEquals(1, oldValue);
    assertFalse(multimap.containsEntry("foo", 1));
    assertTrue(multimap.containsEntry("foo", 2));
  }

  /**
   * Test calling toString() on the multimap, which does not have a
   * deterministic iteration order for keys but does for values.
   */
  public void testToString() {
    String s = createSample().toString();
    assertTrue(s.equals("{foo=[3, -1, 2, 4, 1], bar=[1, 2, 3, 1]}")
        || s.equals("{bar=[1, 2, 3, 1], foo=[3, -1, 2, 4, 1]}"));
  }

  /**
   * Test calling set() on a sublist.
   */
  public void testSublistSet() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(1, 4);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 4);

    sublist.set(1, 6);
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 6, 4, 5);
  }

  /**
   * Test removing elements from a sublist.
   */
  public void testSublistRemove() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(1, 4);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 4);

    sublist.remove(1);
    assertEquals(4, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 4, 5);

    sublist.removeAll(Collections.singleton(4));
    assertEquals(3, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 5);

    sublist.remove(0);
    assertEquals(2, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 5);

  }

  /**
   * Test adding elements to a sublist.
   */
  public void testSublistAdd() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(1, 4);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 4);

    sublist.add(6);
    assertEquals(6, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 6, 5);

    sublist.add(0, 7);
    assertEquals(7, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 7, 2, 3, 4, 6, 5);
  }

  /**
   * Test clearing a sublist.
   */
  public void testSublistClear() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(1, 4);
    MoreAsserts.assertContentsInOrder(sublist, 2, 3, 4);

    sublist.clear();
    assertEquals(2, multimap.size());
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 5);
  }

  /**
   * Test adding elements to an empty sublist with an empty ancestor.
   */
  public void testSubListAddToEmpty() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    List<Integer> list = multimap.get("foo");
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 2, 3, 4, 5);
    List<Integer> sublist = list.subList(0, 5);
    MoreAsserts.assertContentsInOrder(sublist, 1, 2, 3, 4, 5);

    sublist.retainAll(Collections.EMPTY_LIST);
    assertTrue(multimap.isEmpty());

    sublist.add(6);
    assertEquals(1, multimap.size());
    assertTrue(multimap.containsEntry("foo", 6));
  }

  /**
   * Test updates through a list iterator retrieved by
   * multimap.get(key).listIterator(index).
   */
  public void testListIteratorIndexUpdate() {
    ListMultimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(1, 2, 3, 4, 5));
    ListIterator<Integer> iterator = multimap.get("foo").listIterator(1);

    assertEquals(2, iterator.next().intValue());
    iterator.set(6);
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 6, 3, 4, 5);

    assertTrue(iterator.hasNext());
    assertEquals(3, iterator.next().intValue());
    iterator.remove();
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 6, 4, 5);
    assertEquals(4, multimap.size());
  }

}
