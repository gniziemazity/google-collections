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
import com.google.common.collect.helpers.SerializableTester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests for {@link AbstractMultimap}. Caution: when subclassing avoid
 * accidental naming collisions with tests in this class!
 */
public abstract class AbstractMultimapTest extends TestCase {

  private Multimap<String, Integer> multimap;

  protected abstract Multimap<String, Integer> create();

  protected Multimap<String, Integer> createSample() {
    Multimap<String, Integer> multimap = create();
    multimap.putAll("foo", Arrays.asList(3, -1, 2, 4, 1));
    multimap.putAll("bar", Arrays.asList(1, 2, 3, 1));
    return multimap;
  }

  /** Clone the multimap, or return {@code null} if it's not cloneable. */
  protected abstract Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap);

  @Override protected void setUp() throws Exception {
    super.setUp();
    multimap = create();
  }

  protected Multimap<String, Integer> getMultimap() {
    return multimap;
  }

  /**
   * Returns the key to use as a null placeholder in tests. The default
   * implementation returns {@code null}, but tests for multimaps that don't
   * support null keys should override it.
   */
  protected String nullKey() {
    return null;
  }

  /**
   * Returns the value to use as a null placeholder in tests. The default
   * implementation returns {@code null}, but tests for multimaps that don't
   * support null values should override it.
   */
  protected Integer nullValue() {
    return null;
  }

  /**
   * Validate multimap size by calling {@code size()} and also by iterating
   * through the entries. This tests cases where the {@code entries()} list is
   * stored separately, such as the {@link LinkedHashMultimap}. It also
   * verifies that the multimap contains every multimap entry.
   */
  protected void assertSize(int expectedSize) {
    assertEquals(expectedSize, multimap.size());

    int size = 0;
    for (Map.Entry<String, Integer> entry : multimap.entries()) {
      assertTrue(multimap.containsEntry(entry.getKey(), entry.getValue()));
      size++;
    }
    assertEquals(expectedSize, size);

    int size2 = 0;
    for (Map.Entry<String, Collection<Integer>> entry2 :
        multimap.asMap().entrySet()) {
      size2 += entry2.getValue().size();
    }
    assertEquals(expectedSize, size2);
  }

  public void testSize0() {
    assertSize(0);
  }

  public void testSize1() {
    multimap.put("foo", 1);
    assertSize(1);
  }

  public void testSize2Keys() {
    multimap.put("foo", 1);
    multimap.put("bar", 5);
    assertSize(2);
  }

  public void testSize2Values() {
    multimap.put("foo", 1);
    multimap.put("foo", 7);
    assertSize(2);
  }

  public void testSizeNull() {
    multimap.put("foo", 1);
    multimap.put("bar", 5);
    multimap.put(nullKey(), nullValue());
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 5);
    assertSize(5);
  }

  public void testIsEmptyYes() {
    assertTrue(multimap.isEmpty());
  }

  public void testIsEmptyNo() {
    multimap.put("foo", 1);
    assertFalse(multimap.isEmpty());
  }

  public void testIsEmptyNull() {
    multimap.put(nullKey(), nullValue());
    assertFalse(multimap.isEmpty());
  }

  public void testIsEmptyRemoved() {
    multimap.put("foo", 1);
    multimap.remove("foo", 1);
    assertTrue(multimap.isEmpty());
  }

  public void testContainsKeyTrue() {
    multimap.put("foo", 1);
    assertTrue(multimap.containsKey("foo"));
  }

  public void testContainsKeyFalse() {
    multimap.put("foo", 1);
    assertFalse(multimap.containsKey("bar"));
    assertFalse(multimap.containsKey(nullKey()));
  }

  public void testContainsKeyNull() {
    multimap.put(nullKey(), 1);
    assertTrue(multimap.containsKey(nullKey()));
  }

  public void testContainsValueTrue() {
    multimap.put("foo", 1);
    assertTrue(multimap.containsValue(1));
  }

  public void testContainsValueFalse() {
    multimap.put("foo", 1);
    assertFalse(multimap.containsValue(2));
    assertFalse(multimap.containsValue(nullValue()));
  }

  public void testContainsValueNull() {
    multimap.put("foo", nullValue());
    assertTrue(multimap.containsValue(nullValue()));
  }

  public void testContainsKeyValueTrue() {
    multimap.put("foo", 1);
    assertTrue(multimap.containsEntry("foo", 1));
  }

  public void testContainsKeyValueRemoved() {
    multimap.put("foo", 1);
    multimap.remove("foo", 1);
    assertFalse(multimap.containsEntry("foo", 1));
  }

  public void testGet0() {
    multimap.put("foo", 1);
    Collection<Integer> values = multimap.get("bar");
    assertEquals(0, values.size());
  }

  public void testGet1() {
    multimap.put("foo", 1);
    multimap.put("bar", 3);
    Collection<Integer> values = multimap.get("bar");
    assertEquals(1, values.size());
    assertTrue(values.contains(3));
  }

  public void testGet2() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> values = multimap.get("foo");
    assertEquals(2, values.size());
    assertTrue(values.contains(1));
    assertTrue(values.contains(3));
  }

  public void testGetNull() {
    multimap.put(nullKey(), nullValue());
    multimap.put(nullKey(), 3);
    Collection<Integer> values = multimap.get(nullKey());
    assertEquals(2, values.size());
    assertTrue(values.contains(nullValue()));
    assertTrue(values.contains(3));
  }

  public void testPutAllIterable() {
    Iterable<Integer> iterable = new Iterable<Integer>() {
      public Iterator<Integer> iterator() {
        return Lists.newArrayList(1, 3).iterator();
      }
    };
    multimap.putAll("foo", iterable);
    assertTrue(multimap.containsEntry("foo", 1));
    assertTrue(multimap.containsEntry("foo", 3));
    assertSize(2);
  }

  public void testPutAllCollection() {
    Collection<Integer> collection = Lists.newArrayList(1, 3);
    multimap.putAll("foo", collection);
    assertTrue(multimap.containsEntry("foo", 1));
    assertTrue(multimap.containsEntry("foo", 3));
    assertSize(2);
  }

  public void testPutAllCollectionNull() {
    Collection<Integer> collection = Lists.newArrayList(1, nullValue());
    multimap.putAll(nullKey(), collection);
    assertTrue(multimap.containsEntry(nullKey(), 1));
    assertTrue(multimap.containsEntry(nullKey(), nullValue()));
    assertSize(2);
  }

  public void testPutAllEmptyCollection() {
    Collection<Integer> collection = Lists.newArrayList();
    multimap.putAll("foo", collection);
    assertSize(0);
    assertTrue(multimap.isEmpty());
  }

  public void testPutAllMultimap() {
    multimap.put("foo", 2);
    multimap.put("cow", 5);
    multimap.put(nullKey(), 2);
    Multimap<String, Integer> multimap2 = create();
    multimap2.put("foo", 1);
    multimap2.put("bar", 3);
    multimap2.put(nullKey(), nullValue());
    multimap.putAll(multimap2);
    assertTrue(multimap.containsEntry("foo", 2));
    assertTrue(multimap.containsEntry("cow", 5));
    assertTrue(multimap.containsEntry("foo", 1));
    assertTrue(multimap.containsEntry("bar", 3));
    assertTrue(multimap.containsEntry(nullKey(), nullValue()));
    assertTrue(multimap.containsEntry(nullKey(), 2));
    assertSize(6);
  }

  public void testReplaceValues() {
    multimap.put("foo", 1);
    multimap.put("bar", 3);
    Collection<Integer> values = Arrays.asList(2, nullValue());
    Collection<Integer> oldValues = multimap.replaceValues("foo", values);
    assertTrue(multimap.containsEntry("foo", 2));
    assertTrue(multimap.containsEntry("foo", nullValue()));
    assertTrue(multimap.containsEntry("bar", 3));
    assertSize(3);
    assertTrue(oldValues.contains(1));
    assertEquals(1, oldValues.size());
  }

  public void testReplaceValuesNull() {
    multimap.put(nullKey(), 1);
    multimap.put("bar", 3);
    Collection<Integer> values = Arrays.asList(2, nullValue());
    Collection<Integer> oldValues = multimap.replaceValues(nullKey(), values);
    assertTrue(multimap.containsEntry(nullKey(), 2));
    assertTrue(multimap.containsEntry(nullKey(), nullValue()));
    assertTrue(multimap.containsEntry("bar", 3));
    assertSize(3);
    assertTrue(oldValues.contains(1));
    assertEquals(1, oldValues.size());
  }

  public void testReplaceValuesNotPresent() {
    multimap.put("bar", 3);
    Collection<Integer> values = Arrays.asList(2, 4);
    Collection<Integer> oldValues = multimap.replaceValues("foo", values);
    assertTrue(multimap.containsEntry("foo", 2));
    assertTrue(multimap.containsEntry("foo", 4));
    assertTrue(multimap.containsEntry("bar", 3));
    assertSize(3);
    assertNotNull(oldValues);
    assertTrue(oldValues.isEmpty());
  }

  public void testRemove() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);

    assertTrue(multimap.remove("foo", 1));
    assertFalse(multimap.containsEntry("foo", 1));
    assertTrue(multimap.containsEntry("foo", 3));
    assertSize(1);

    assertFalse(multimap.remove("bar", 3));
    assertTrue(multimap.containsEntry("foo", 3));
    assertSize(1);

    assertFalse(multimap.remove("foo", 2));
    assertTrue(multimap.containsEntry("foo", 3));
    assertSize(1);

    assertTrue(multimap.remove("foo", 3));
    assertFalse(multimap.containsKey("foo"));
    assertSize(0);
  }

  public void testRemoveNull() {
    multimap.put(nullKey(), 1);
    multimap.put(nullKey(), 3);
    multimap.put(nullKey(), nullValue());

    assertTrue(multimap.remove(nullKey(), 1));
    assertFalse(multimap.containsEntry(nullKey(), 1));
    assertTrue(multimap.containsEntry(nullKey(), 3));
    assertTrue(multimap.containsEntry(nullKey(), nullValue()));
    assertSize(2);

    assertTrue(multimap.remove(nullKey(), nullValue()));
    assertFalse(multimap.containsEntry(nullKey(), 1));
    assertTrue(multimap.containsEntry(nullKey(), 3));
    assertFalse(multimap.containsEntry(nullKey(), nullValue()));
    assertSize(1);
  }

  public void testRemoveAll() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> removed = multimap.removeAll("foo");
    assertFalse(multimap.containsKey("foo"));
    assertSize(0);
    assertTrue(removed.contains(1));
    assertTrue(removed.contains(3));
    assertEquals(2, removed.size());
  }

  public void testRemoveAllNull() {
    multimap.put(nullKey(), 1);
    multimap.put(nullKey(), nullValue());
    Collection<Integer> removed = multimap.removeAll(nullKey());
    assertFalse(multimap.containsKey(nullKey()));
    assertSize(0);
    assertTrue(removed.contains(1));
    assertTrue(removed.contains(nullValue()));
    assertEquals(2, removed.size());
  }

  public void testRemoveAllNotPresent() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> removed = multimap.removeAll("bar");
    assertSize(2);
    assertNotNull(removed);
    assertTrue(removed.isEmpty());
  }

  public void testClear() {
    multimap.put("foo", 1);
    multimap.put("bar", 3);
    multimap.clear();
    assertEquals(0, multimap.keySet().size());
    assertSize(0);
  }

  public void testKeySet() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Set<String> keys = multimap.keySet();
    assertEquals(2, keys.size());
    assertTrue(keys.contains("foo"));
    assertTrue(keys.contains(nullKey()));
    assertTrue(keys.containsAll(Lists.newArrayList("foo", nullKey())));
    assertFalse(keys.containsAll(Lists.newArrayList("foo", "bar")));
  }

  public void testValues() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Collection<Integer> values = multimap.values();
    assertEquals(3, values.size());
    assertTrue(values.contains(1));
    assertTrue(values.contains(3));
    assertTrue(values.contains(nullValue()));
    assertFalse(values.contains(5));
  }

/*
 
  TODO(kevinb): don't use Pair


  @SuppressWarnings("unchecked")
  public void testEntries() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Collection<Map.Entry<String, Integer>> entries = multimap.entries();
    assertEquals(3, entries.size());
    Set<Pair<String, Integer>> pairs = new HashSet<Pair<String, Integer>>();
    for (Map.Entry<String, Integer> entry : entries) {
      pairs.add(new Pair<String,Integer>(entry.getKey(), entry.getValue()));
    }
    Set<Pair<String, Integer>> expected = Sets.newHashSet(
        new Pair<String,Integer>("foo", 1),
        new Pair<String,Integer>("foo", nullValue()),
        new Pair<String,Integer>(nullKey(), 3));
    assertEquals(expected, pairs);
    assertTrue(entries.contains(Maps.immutableEntry("foo", 1)));
    assertTrue(entries.contains(Maps.immutableEntry("foo", nullValue())));
    assertTrue(entries.contains(Maps.immutableEntry(nullKey(), 3)));
    assertFalse(entries.contains(Maps.immutableEntry("foo", 3)));
  }
*/

  public void testNoSuchElementException() {
    Iterator<Map.Entry<String, Integer>> entries =
        multimap.entries().iterator();
    try {
      entries.next();
      fail();
    } catch (NoSuchElementException expected) {}
  }

  public void testAsMap() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Map<String, Collection<Integer>> map = multimap.asMap();

    assertEquals(2, map.size());
    MoreAsserts.assertContentsAnyOrder(map.get("foo"), 1, nullValue());
    MoreAsserts.assertContentsAnyOrder(map.get(nullKey()), 3);
    assertNull(map.get("bar"));
    assertTrue(map.containsKey("foo"));
    assertTrue(map.containsKey(nullKey()));
    assertFalse(multimap.containsKey("bar"));
    
    MoreAsserts.assertContentsAnyOrder(map.remove("foo"), 1, nullValue());
    assertFalse(multimap.containsKey("foo"));
    assertEquals(1, multimap.size());
    assertNull(map.remove("bar"));
    multimap.get(nullKey()).add(5);
    assertTrue(multimap.containsEntry(nullKey(), 5));
    assertEquals(2, multimap.size());
    multimap.get(nullKey()).clear();
    assertTrue(multimap.isEmpty());
    assertEquals(0, multimap.size());
    
    try {
      map.put("bar", Arrays.asList(4, 8));
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {}
  }
  
  public void testAsMapEntries() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Collection<Map.Entry<String, Collection<Integer>>> entries =
        multimap.asMap().entrySet();
    assertEquals(2, entries.size());
    Iterator<Map.Entry<String, Collection<Integer>>> iterator =
        entries.iterator();
    for (int i = 0; i < 2; i++) {
      assertTrue(iterator.hasNext());
      Map.Entry<String, Collection<Integer>> entry = iterator.next();
      if ("foo".equals(entry.getKey())) {
        assertEquals(2, entry.getValue().size());
        assertTrue(entry.getValue().contains(1));
        assertTrue(entry.getValue().contains(nullValue()));
      } else {
        assertEquals(nullKey(), entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertTrue(entry.getValue().contains(3));
      }
    }
    assertFalse(iterator.hasNext());
  }

  public void testKeys() {
    multimap.put("foo", 1);
    multimap.put("foo", 5);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Multiset<String> multiset = multimap.keys();
    assertEquals(3, multiset.count("foo"));
    assertEquals(1, multiset.count(nullKey()));
    MoreAsserts.assertContentsAnyOrder(multiset.elementSet(), "foo", nullKey());
    assertEquals(2, multiset.entrySet().size());
    assertEquals(4, multiset.size());

    Multiset<String> foo3null1 =
        Multisets.newHashMultiset("foo", "foo", nullKey(), "foo");
    assertEquals(foo3null1, multiset);
    assertEquals(multiset, foo3null1);
    assertFalse(multiset.equals(
        Multisets.newHashMultiset("foo", "foo", nullKey(), nullKey())));
    assertEquals(foo3null1.hashCode(), multiset.hashCode());

    assertEquals(0, multiset.remove("bar", 1));
    assertEquals(1, multiset.remove(nullKey(), 4));
    assertFalse(multimap.containsKey(nullKey()));
    assertSize(3);
    assertEquals("foo", multiset.entrySet().iterator().next().getElement());

    assertEquals(1, multiset.remove("foo", 1));
    assertTrue(multimap.containsKey("foo"));
    assertSize(2);
    assertEquals(2, multiset.removeAllOccurrences("foo"));
    assertEquals(0, multiset.removeAllOccurrences("bar"));
  }

  public void testEqualsTrue() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    assertEquals(multimap, multimap);

    Multimap<String, Integer> multimap2 = create();
    multimap2.put(nullKey(), 3);
    multimap2.put("foo", 1);
    multimap2.put("foo", nullValue());

    assertEquals(multimap, multimap2);
    assertEquals(multimap.hashCode(), multimap2.hashCode());
  }

  public void testEqualsFalse() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 3);

    Multimap<String, Integer> multimap2 = create();
    multimap2.put("bar", 3);
    multimap2.put("bar", 1);
    assertFalse(multimap.equals(multimap2));

    multimap2.put("foo", 3);
    assertFalse(multimap.equals(multimap2));

    assertFalse(multimap.equals(nullValue()));
    assertFalse(multimap.equals("foo"));
  }

  public void testValuesIterator() {
    multimap.put("foo", 1);
    multimap.put("foo", 2);
    multimap.put(nullKey(), 4);
    int sum = 0;
    for (int i : multimap.values()) {
      sum += i;
    }
    assertEquals(7, sum);
  }

  public void testValuesIteratorEmpty() {
    int sum = 0;
    for (int i : multimap.values()) {
      sum += i;
    }
    assertEquals(0, sum);
  }

  public void testGetAddQuery() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 4);
    Collection<Integer> values = multimap.get("foo");
    multimap.put("foo", 5);
    multimap.put("bar", 6);

    /* Verify that values includes effect of put. */
    assertEquals(3, values.size());
    assertTrue(values.contains(1));
    assertTrue(values.contains(5));
    assertFalse(values.contains(6));
    MoreAsserts.assertContentsAnyOrder(values, 1, 3, 5);
    assertTrue(values.containsAll(Arrays.asList(3, 5)));
    assertFalse(values.isEmpty());
    assertEquals(multimap.get("foo"), values);
    assertEquals(multimap.get("foo").hashCode(), values.hashCode());
    assertEquals(multimap.get("foo").toString(), values.toString());
  }

  public void testGetRemoveAddQuery() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 4);
    Collection<Integer> values = multimap.get("foo");
    Iterator<Integer> iterator = values.iterator();
    multimap.remove("foo", 1);
    multimap.remove("foo", 3);

    /* Verify that values includes effect of remove */
    assertEquals(0, values.size());
    assertFalse(values.contains(1));
    assertFalse(values.contains(6));
    assertTrue(values.isEmpty());
    assertEquals(multimap.get("foo"), values);
    assertEquals(multimap.get("foo").hashCode(), values.hashCode());
    assertEquals(multimap.get("foo").toString(), values.toString());

    multimap.put("foo", 5);

    /* Verify that values includes effect of put. */
    assertEquals(1, values.size());
    assertFalse(values.contains(1));
    assertTrue(values.contains(5));
    assertFalse(values.contains(6));
    assertEquals(5, values.iterator().next().intValue());
    assertFalse(values.isEmpty());
    assertEquals(multimap.get("foo"), values);
    assertEquals(multimap.get("foo").hashCode(), values.hashCode());
    assertEquals(multimap.get("foo").toString(), values.toString());

    try {
      iterator.hasNext();
    } catch (ConcurrentModificationException expected) {}
  }

  public void testModifyCollectionFromGet() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("bar", 4);
    Collection<Integer> values = multimap.get("foo");

    values.add(5);
    assertSize(4);
    assertEquals(3, multimap.get("foo").size());
    assertTrue(multimap.containsEntry("foo", 5));

    values.clear();
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));

    values.addAll(Arrays.asList(7, 9));
    assertSize(3);
    assertEquals(2, multimap.get("foo").size());
    assertTrue(multimap.containsEntry("foo", 7));
    assertTrue(multimap.containsEntry("foo", 9));

    values.remove(7);
    assertSize(2);
    assertEquals(1, multimap.get("foo").size());
    assertFalse(multimap.containsEntry("foo", 7));
    assertTrue(multimap.containsEntry("foo", 9));

    values.add(11);
    values.add(13);
    values.add(15);
    values.add(17);

    values.removeAll(Arrays.asList(11, 15));
    assertSize(4);
    MoreAsserts.assertContentsAnyOrder(multimap.get("foo"), 9, 13, 17);

    values.retainAll(Arrays.asList(13, 17, 19));
    assertSize(3);
    MoreAsserts.assertContentsAnyOrder(multimap.get("foo"), 13, 17);

    values.remove(13);
    values.remove(17);
    assertTrue(multimap.get("foo").isEmpty());
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));
  }

  public void testGetIterator() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    multimap.put("foo", 5);
    multimap.put("bar", 4);
    Collection<Integer> values = multimap.get("foo");

    Iterator<Integer> iterator = values.iterator();
    assertTrue(iterator.hasNext());
    Integer v1 = iterator.next();
    assertTrue(iterator.hasNext());
    Integer v2 = iterator.next();
    iterator.remove();
    assertTrue(iterator.hasNext());
    Integer v3 = iterator.next();
    assertFalse(iterator.hasNext());

    MoreAsserts.assertContentsAnyOrder(Arrays.asList(v1, v2, v3), 1, 3, 5);
    assertSize(3);
    assertTrue(multimap.containsEntry("foo", v1));
    assertFalse(multimap.containsEntry("foo", v2));
    assertTrue(multimap.containsEntry("foo", v3));

    iterator = values.iterator();
    assertTrue(iterator.hasNext());
    Integer n1 = iterator.next();
    iterator.remove();
    assertTrue(iterator.hasNext());
    Integer n3 = iterator.next();
    iterator.remove();
    assertFalse(iterator.hasNext());

    MoreAsserts.assertContentsAnyOrder(Arrays.asList(n1, n3), v1, v3);
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));
  }

  public void testGetClear() {
    multimap.put("foo", 1);
    multimap.put("bar", 3);
    Collection<Integer> values = multimap.get("foo");
    multimap.clear();
    assertTrue(values.isEmpty());
  }

  public void testGetPutAllCollection() {
    Collection<Integer> values = multimap.get("foo");
    Collection<Integer> collection = Lists.newArrayList(1, 3);
    multimap.putAll("foo", collection);
    MoreAsserts.assertContentsAnyOrder(values, 1, 3);
  }

  public void testGetPutAllMultimap() {
    multimap.put("foo", 2);
    multimap.put("cow", 5);
    multimap.put(nullKey(), 2);
    Collection<Integer> valuesFoo = multimap.get("foo");
    Collection<Integer> valuesBar = multimap.get("bar");
    Collection<Integer> valuesCow = multimap.get("cow");
    Collection<Integer> valuesNull = multimap.get(nullKey());
    Multimap<String, Integer> multimap2 = create();
    multimap2.put("foo", 1);
    multimap2.put("bar", 3);
    multimap2.put(nullKey(), nullValue());
    multimap.putAll(multimap2);

    MoreAsserts.assertContentsAnyOrder(valuesFoo, 1, 2);
    MoreAsserts.assertContentsAnyOrder(valuesBar, 3);
    MoreAsserts.assertContentsAnyOrder(valuesCow, 5);
    MoreAsserts.assertContentsAnyOrder(valuesNull, nullValue(), 2);
  }

  public void testGetRemove() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> values = multimap.get("foo");
    multimap.remove("foo", 1);
    MoreAsserts.assertContentsAnyOrder(values, 3);
  }

  public void testGetRemoveAll() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> values = multimap.get("foo");
    multimap.removeAll("foo");
    assertTrue(values.isEmpty());
  }

  public void testGetReplaceValues() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Integer> values = multimap.get("foo");
    multimap.replaceValues("foo", Arrays.asList(1, 5));
    MoreAsserts.assertContentsAnyOrder(values, 1, 5);

    multimap.replaceValues("foo", new ArrayList<Integer>());
    assertTrue(multimap.isEmpty());
    assertSize(0);
    assertTrue(values.isEmpty());
  }

  public void testEntriesUpdate() {
    multimap.put("foo", 1);
    Collection<Map.Entry<String, Integer>> entries = multimap.entries();
    Iterator<Map.Entry<String, Integer>> iterator = entries.iterator();

    assertTrue(iterator.hasNext());
    Map.Entry<String, Integer> entry = iterator.next();
    assertEquals("foo", entry.getKey());
    assertEquals(1, entry.getValue().intValue());
    iterator.remove();
    assertFalse(iterator.hasNext());
    assertTrue(multimap.isEmpty());
    assertSize(0);

    try {
      entries.add(Maps.immutableEntry("bar", 2));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertSize(0);
    assertFalse(multimap.containsEntry("bar", 2));

    multimap.put("bar", 2);
    assertSize(1);
    assertTrue(entries.contains(Maps.immutableEntry("bar", 2)));

    entries.clear();
    assertTrue(multimap.isEmpty());
    assertSize(0);
  }

  public void testKeySetRemove() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);
    Set<String> keys = multimap.keySet();
    assertTrue(keys.remove("foo"));
    assertFalse(keys.remove("bar"));
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));
    assertTrue(multimap.containsEntry(nullKey(), 3));
  }

  public void testKeySetIterator() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);

    Iterator<String> iterator = multimap.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      if ("foo".equals(key)) {
        iterator.remove();
      }
    }
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));
    assertTrue(multimap.containsEntry(nullKey(), 3));

    iterator = multimap.keySet().iterator();
    assertEquals(nullKey(), iterator.next());
    iterator.remove();
    assertTrue(multimap.isEmpty());
    assertSize(0);
  }

  public void testKeySetClear() {
    multimap.put("foo", 1);
    multimap.put("foo", nullValue());
    multimap.put(nullKey(), 3);

    multimap.keySet().clear();
    assertTrue(multimap.isEmpty());
    assertSize(0);
  }

  public void testValuesIteratorRemove() {
    multimap.put("foo", 1);
    multimap.put("foo", 2);
    multimap.put(nullKey(), 4);

    Iterator<Integer> iterator = multimap.values().iterator();
    while (iterator.hasNext()) {
      int value = iterator.next();
      if ((value % 2) == 0) {
        iterator.remove();
      }
    }

    assertSize(1);
    assertTrue(multimap.containsEntry("foo", 1));
  }

  public void testAsMapEntriesUpdate() {
    multimap.put("foo", 1);
    multimap.put("foo", 3);
    Collection<Map.Entry<String, Collection<Integer>>> entries =
        multimap.asMap().entrySet();
    Map.Entry<String, Collection<Integer>> entry = entries.iterator().next();
    Collection<Integer> values = entry.getValue();

    multimap.put("foo", 5);
    assertEquals(3, values.size());
    assertTrue(values.contains(5));

    values.add(7);
    assertSize(4);
    assertTrue(multimap.containsValue(7));

    multimap.put("bar", 4);
    assertEquals(2, entries.size());
    assertSize(5);

    entries.remove(entry);
    assertSize(1);
    assertFalse(multimap.containsKey("foo"));
    assertTrue(multimap.containsKey("bar"));

    Iterator<Map.Entry<String, Collection<Integer>>> iterator =
        entries.iterator();
    assertTrue(iterator.hasNext());
    iterator.next();
    iterator.remove();
    assertFalse(iterator.hasNext());
    assertSize(0);
    assertTrue(multimap.isEmpty());
  }

  public void testToStringNull() {
    multimap.put("foo", 3);
    multimap.put("foo", -1);
    multimap.put(nullKey(), nullValue());
    multimap.put("bar", 1);
    multimap.put("foo", 2);
    multimap.put(nullKey(), 0);
    multimap.put("bar", 2);
    multimap.put("bar", nullValue());
    multimap.put("foo", nullValue());
    multimap.put("foo", 4);
    multimap.put(nullKey(), -1);
    multimap.put("bar", 3);
    multimap.put("bar", 1);
    multimap.put("foo", 1);

    // This test is brittle. The original test was meant to validate the
    // contents of the string itself, but key and value ordering tend
    // to change under unpredictable circumstances. Instead, we're just ensuring
    // that the string not return null, and implicitly, not throw an exception.
    assertNotNull(multimap.toString());
  }

  public void testClone() {
    multimap.put("foo", 3);
    multimap.put("foo", -1);
    multimap.put(nullKey(), nullValue());
    multimap.put("bar", 1);

    assertFalse(multimap.keySet().contains("cow"));
    assertFalse(multimap.values().contains(4));

    Multimap<String, Integer> clone = makeClone(multimap);
    if (clone == null) {
      return;
    }

    assertEquals(multimap, clone);
    assertFalse(multimap == clone);

    clone.put("foo", 4);
    clone.put("cow", 2);

    assertFalse(multimap.get("foo").contains(4));
    assertFalse(multimap.keySet().contains("cow"));
    assertFalse(multimap.values().contains(4));
    assertSize(4);

    assertTrue(clone.get("foo").contains(4));
    assertTrue(clone.keySet().contains("cow"));
    assertTrue(clone.values().contains(4));
    assertEquals(6, clone.size());
  }

  public void testSerializable() {
    multimap = createSample();
    assertEquals(multimap, SerializableTester.reserialize(multimap));
  }
}
