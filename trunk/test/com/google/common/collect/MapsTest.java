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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.helpers.NullPointerTester;
import java.io.StringBufferInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import junit.framework.TestCase;

/**
 * Unit test for {@link Maps}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
public class MapsTest extends TestCase {

  private static final Comparator<Integer> SOME_COMPARATOR =
      Collections.reverseOrder();

  public void testConstantMap0() throws Exception {
    Map<String,Integer> map = Maps.immutableMap();
    Map<String,Integer> expected = Maps.newHashMap();
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testConstantMap1() throws Exception {
    Map<String,Integer> map = Maps.immutableMap("a", 1);
    Map<String,Integer> expected = Maps.newHashMap();
    expected.put("a", 1);
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testConstantMap2() throws Exception {
    Map<String,Integer> map = Maps.immutableMap("a", 1, "b", 2);
    Map<String,Integer> expected = Maps.newHashMap();
    expected.put("a", 1);
    expected.put("b", 2);
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testConstantMap3() throws Exception {
    Map<String,Integer> map = Maps.immutableMap("a", 1, "b", 2, "c", 3);
    Map<String,Integer> expected = Maps.newHashMap();
    expected.put("a", 1);
    expected.put("b", 2);
    expected.put("c", 3);
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testConstantMap4() throws Exception {
    Map<String,Integer> map
        = Maps.immutableMap("a", 1, "b", 2, "c", 3, "d", 4);
    Map<String,Integer> expected = Maps.newHashMap();
    expected.put("a", 1);
    expected.put("b", 2);
    expected.put("c", 3);
    expected.put("d", 4);
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testConstantMap5() throws Exception {
    Map<String,Integer> map
        = Maps.immutableMap("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
    Map<String,Integer> expected = Maps.newHashMap();
    expected.put("a", 1);
    expected.put("b", 2);
    expected.put("c", 3);
    expected.put("d", 4);
    expected.put("e", 5);
    assertEquals(expected, map);
    assertEquals(expected.hashCode(), map.hashCode());
  }

  public void testHashMap() {
    HashMap<Integer, Integer> map = Maps.newHashMap();
    assertEquals(Collections.emptyMap(), map);
  }

  public void testCapacityForNegativeSizeFails() {
    try {
      Maps.capacity(-1);
      fail("Negative expected dize must result in IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
    }
  }

  public void testCapacityForNonNegativeSizeIsAtLeast16() {
    int[] nonNegativeExpectedSizes = new int[] {0, 1, 15, 16, 32};
    for (int expectedSize : nonNegativeExpectedSizes) {
      int capacity = Maps.capacity(expectedSize);
      assertTrue(capacity >= 16);
      assertTrue(capacity >= expectedSize * 4 / 3);
    }
  }

  public void testLinkedHashMap() {
    LinkedHashMap<Integer, Integer> map = Maps.newLinkedHashMap();
    assertEquals(Collections.emptyMap(), map);
  }

  public void testIdentityHashMap() {
    IdentityHashMap<Integer, Integer> map = Maps.newIdentityHashMap();
    assertEquals(Collections.emptyMap(), map);
  }

  public void testConcurrentHashMap() {
    ConcurrentHashMap<Integer, Integer> map = Maps.newConcurrentHashMap();
    assertEquals(Collections.emptyMap(), map);
  }

  public void testTreeMap() {
    TreeMap<Integer, Integer> map = Maps.newTreeMap();
    assertEquals(Collections.emptyMap(), map);
    assertNull(map.comparator());
  }

  public void testTreeMapWithComparator() {
    TreeMap<Integer, Integer> map = Maps.newTreeMap(SOME_COMPARATOR);
    assertEquals(Collections.emptyMap(), map);
    assertSame(SOME_COMPARATOR, map.comparator());
  }

  public enum SomeEnum { SOME_INSTANCE }

  public void testEnumMap() {
    EnumMap<SomeEnum, Integer> map = Maps.newEnumMap(SomeEnum.class);
    assertEquals(Collections.emptyMap(), map);
    map.put(SomeEnum.SOME_INSTANCE, 0);
    assertEquals(Collections.singletonMap(SomeEnum.SOME_INSTANCE, 0), map);
  }

  public void testEnumMapNull() {
    try {
      Maps.<SomeEnum, Long>newEnumMap(null);
      fail("no exception thrown");
    } catch (NullPointerException expected) {
    }
  }

  public void testContainsEntry1() throws Exception {
    Map<String,Integer> map = Maps.immutableMap("a", 1);
    assertTrue(Maps.containsEntry(map, "a", 1));
    assertFalse(Maps.containsEntry(map, "b", 1));
    assertFalse(Maps.containsEntry(map, "a", 2));
    assertFalse(Maps.containsEntry(map, "a", null));
    assertFalse(Maps.containsEntry(map, null, 1));
    assertFalse(Maps.containsEntry(map, null, null));
  }

  public void testContainsEntry2() throws Exception {
    Map<String,Integer> map = Maps.immutableMap("a", null);
    assertTrue(Maps.containsEntry(map, "a", null));
    assertFalse(Maps.containsEntry(map, "b", 1));
    assertFalse(Maps.containsEntry(map, "a", 2));
    assertFalse(Maps.containsEntry(map, "a", 1));
    assertFalse(Maps.containsEntry(map, null, 1));
    assertFalse(Maps.containsEntry(map, null, null));
  }

  public void testContainsEntry3() throws Exception {
    Map<String,Integer> map = Maps.immutableMap(null, null);
    assertTrue(Maps.containsEntry(map, null, null));
    assertFalse(Maps.containsEntry(map, "b", 1));
    assertFalse(Maps.containsEntry(map, "a", 2));
    assertFalse(Maps.containsEntry(map, "a", 1));
    assertFalse(Maps.containsEntry(map, null, 1));
    assertFalse(Maps.containsEntry(map, "a", null));
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.setDefault(BiMap.class, Maps.emptyBiMap());
    tester.testAllPublicStaticMethods(Maps.class);
  }

  private static SortedMap<String,Integer> newSortedMap() {
    SortedMap<String,Integer> map = Maps.newTreeMap();
    map.put("foo", 1);
    map.put("bar", 2);
    map.put("baz", 3);
    return map;
  }

  public void testSortedKeySet() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map);
    assertEquals(Sets.newTreeSet("foo", "bar", "baz"), set);
    assertEquals(map.comparator(), set.comparator());
    assertEquals("bar", set.first());
    assertEquals("foo", set.last());
    assertEquals("[bar, baz, foo]", set.toString());
  }

  public void testSortedKeySetHeadSet() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map).headSet("foo");
    assertEquals(Sets.newTreeSet("bar", "baz"), set);
    assertEquals(map.comparator(), set.comparator());
    assertEquals("bar", set.first());
    assertEquals("baz", set.last());
  }

  public void testSortedKeySetSubSet() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map).subSet("bar", "foo");
    assertEquals(Sets.newTreeSet("bar", "baz"), set);
    assertEquals(map.comparator(), set.comparator());
    assertEquals("bar", set.first());
    assertEquals("baz", set.last());
  }

  public void testSortedKeySetTailSet() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map).tailSet("baz");
    assertEquals(Sets.newTreeSet("baz", "foo"), set);
    assertEquals(map.comparator(), set.comparator());
    assertEquals("baz", set.first());
    assertEquals("foo", set.last());
  }

  public void testSortedKeySetView() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map);
    assertEquals(Sets.newTreeSet("foo", "bar", "baz"), set);
    map.put("qux", 4);
    assertEquals(Sets.newTreeSet("foo", "bar", "baz", "qux"), set);
  }

  public void testSortedKeySetIterator() {
    SortedMap<String,Integer> map = newSortedMap();
    SortedSet<String> set = Maps.sortedKeySet(map);
    Iterator<String> i = set.iterator();
    assertEquals("bar", i.next());
    assertEquals("baz", i.next());
    assertEquals("foo", i.next());
    assertFalse(i.hasNext());
    i.remove();
    assertEquals(Sets.newTreeSet("bar", "baz"), set);
    assertNull(map.get("foo"));
  }

  public void testUniqueIndex() {
    final BiMap<Integer, String> intToStringMap =
        new ImmutableBiMapBuilder<Integer,String>()
        .put(1, "one")
        .put(2, "two")
        .put(3, "three")
        .getBiMap();

    Map<Integer,String> outputMap =
        Maps.uniqueIndex(intToStringMap.values(),
                         Functions.forMap(intToStringMap.inverse()));
    assertEquals(intToStringMap, outputMap);

    // Make sure you can do the same thing by passing an Iterable
    Map<Integer,String> outputMap2 =
        Maps.uniqueIndex(new Iterable<String>() {
                           public Iterator<String> iterator() {
                             return intToStringMap.values().iterator();
                           }
                         },
                         Functions.forMap(intToStringMap.inverse()));
    assertEquals(intToStringMap, outputMap2);

    // And an Iterator
    Map<Integer,String> outputMap3 =
        Maps.uniqueIndex(intToStringMap.values().iterator(),
                         Functions.forMap(intToStringMap.inverse()));
    assertEquals(intToStringMap, outputMap3);

    // Can't create the map if more than one value maps to the same key
    Map<String,Integer> mapWithDups = new HashMap<String,Integer>();
    mapWithDups.put("one", 1);
    mapWithDups.put("uno", 1);
    try {
      Maps.uniqueIndex(mapWithDups.keySet(), Functions.forMap(mapWithDups));
      fail("Able to create an index map with duplicate keys");
    } catch (IllegalArgumentException expected) {
    }

    // Null keys are not allowed
    List<String> listWithNull = Lists.newArrayList((String) null);
    try {
      Maps.uniqueIndex(listWithNull, constFunction(1));
    } catch (NullPointerException expected) {
      // expected
    }

    // Null values aren't allowed either
    List<String> oneStringList = Lists.newArrayList("foo");
    try {
      Maps.uniqueIndex(oneStringList, constFunction(null));
    } catch (NullPointerException expected) {
      // expected
    }
  }

  /** Creates a Function that always returns the same value */
  private static <A,B> Function<A,B> constFunction(final B result) {
    return new Function<A,B>() {
      public B apply(A from) {
        return result;
      }
    };
  }

  public void testFromProperties() throws Exception {
    Properties testProp = new Properties();

    Map<String, String> result = Maps.fromProperties(testProp);
    assertTrue(result.isEmpty());
    testProp.setProperty("first", "true");

    result = Maps.fromProperties(testProp);
    assertTrue(Maps.containsEntry(result, "first", "true"));
    assertEquals(1, result.size());
    testProp.setProperty("second", "null");

    result = Maps.fromProperties(testProp);
    assertTrue(Maps.containsEntry(result, "first", "true"));
    assertTrue(Maps.containsEntry(result, "second", "null"));
    assertEquals(2, result.size());

    // Now test values loaded from a stream.
    String props = "test\n second = 2\n Third item :   a short  phrase   ";
    testProp.load(new StringBufferInputStream(props));

    result = Maps.fromProperties(testProp);
    assertEquals(4, result.size());
    assertTrue(Maps.containsEntry(result, "first", "true"));
    assertTrue(Maps.containsEntry(result, "test", ""));
    assertTrue(Maps.containsEntry(result, "second", "2"));
    assertEquals("item :   a short  phrase   ", result.get("Third"));
    assertFalse(result.containsKey("not here"));

    // Test loading system properties
    result = Maps.fromProperties(System.getProperties());
    assertTrue(result.containsKey("java.version"));

    // Test that defaults work, too.
    testProp = new Properties(System.getProperties());
    String override = "test\njava.version : hidden";
    testProp.load(new StringBufferInputStream(override));

    result = Maps.fromProperties(testProp);
    assertTrue(result.size() > 2);
    assertTrue(Maps.containsEntry(result, "test", ""));
    assertTrue(Maps.containsEntry(result, "java.version", "hidden"));
    assertNotSame(System.getProperty("java.version"),
                  result.get("java.version"));
  }

  /**
   * Constructs a "nefarious" map entry with the specified key and value,
   * meaning an entry that is suitable for testing that map entries cannot be
   * modified via a nefarious implementation of equals. This is used for testing
   * unmodifiable collections of map entries; for example, it should not be
   * possible to access the raw (modifiable) map entry via a nefarious equals
   * method.
   */
  public static <K, V> Map.Entry<K, V> nefariousEntry(
      final K key, final V value) {
    return new AbstractMapEntry<K, V>() {
        public K getKey() {
          return key;
        }
        public V getValue() {
          return value;
        }
        public V setValue(V value) {
          throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked")
        @Override public boolean equals(Object o) {
          if (o instanceof Map.Entry<?, ?>) {
            Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            e.setValue(value); // muhahaha!
          }
          return super.equals(o);
        }
      };
  }

  @SuppressWarnings("unchecked")
  public void testUnmodifiableBiMap() {
    BiMap<Integer, String> mod = Maps.newHashBiMap();
    mod.put(1, "one");
    mod.put(2, "two");
    mod.put(3, "three");

    BiMap<Integer, String> unmod = Maps.unmodifiableBiMap(mod);

    /* No aliasing on inverse operations. */
    assertSame(unmod.inverse(), unmod.inverse());
    assertSame(unmod, unmod.inverse().inverse());

    /* Unmodifiable is a view. */
    mod.put(4, "four");
    assertTrue(Maps.containsEntry(unmod, 4, "four"));
    assertTrue(Maps.containsEntry(unmod.inverse(), "four", 4));

    /* UnsupportedOperationException on direct modifications. */
    try {
      unmod.put(4, "four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      unmod.forcePut(4, "four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      unmod.putAll(Collections.singletonMap(4, "four"));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}

    /* UnsupportedOperationException on indirect modifications. */
    BiMap<String, Integer> inverse = unmod.inverse();
    try {
      inverse.put("four", 4);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      inverse.forcePut("four", 4);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      inverse.putAll(Collections.singletonMap("four", 4));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    Set<String> values = unmod.values();
    try {
      values.remove("four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    Set<Map.Entry<Integer, String>> entries = unmod.entrySet();
    Map.Entry<Integer, String> entry = entries.iterator().next();
    try {
      entry.setValue("four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    entry = (Map.Entry<Integer, String>) entries.toArray()[0];
    try {
      entry.setValue("four");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testBiMapEntrySetIteratorRemove() {
    BiMap<Integer, String> map = Maps.newHashBiMap();
    map.put(1, "one");
    Set<Map.Entry<Integer, String>> entries = map.entrySet();
    Iterator<Map.Entry<Integer, String>> iterator = entries.iterator();
    Map.Entry<Integer, String> entry = iterator.next();
    entry.setValue("two"); // changes the iterator's current entry value
    assertEquals("two", map.get(1));
    iterator.remove(); // removes the updated entry
    assertTrue(map.isEmpty());
  }

  public void testBiMapClone() {
    HashBiMap<Integer, String> map = Maps.newHashBiMap();
    map.put(1, "one");
    HashBiMap<Integer, String> clone = map.clone();
    assertEquals(map, clone);
    assertEquals(clone, map);
    map.put(2, "two");
    clone.put(3, "three");
    assertFalse(map.equals(clone));
    assertFalse(clone.equals(map));
    assertFalse(Maps.containsEntry(clone, 2, "two"));
    assertFalse(Maps.containsEntry(clone.inverse(), "two", 2));
    assertFalse(Maps.containsEntry(map, 3, "three"));
    assertFalse(Maps.containsEntry(map.inverse(), "three", 3));
    assertSame(clone.inverse(), clone.inverse());
    assertFalse(map.inverse().equals(clone.inverse()));
    assertFalse(clone.inverse().equals(map.inverse()));
  }

  public void testImmutableEntry() {
    Map.Entry<String, Integer> e = Maps.immutableEntry("foo", 1);
    assertEquals("foo", e.getKey());
    assertEquals(1, (int) e.getValue());
    try {
      e.setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertEquals("foo=1", e.toString());
    assertEquals(101575, e.hashCode());
  }

  public void testImmutableEntryNull() {
    Map.Entry<String, Integer> e
        = Maps.immutableEntry((String) null, (Integer) null);
    assertNull(e.getKey());
    assertNull(e.getValue());
    try {
      e.setValue(null);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertEquals("null=null", e.toString());
    assertEquals(0, e.hashCode());
  }

  public void testCheckedBiMap() {
    /* See MapConstraintsTest */
  }

  public void testEmptyBiMap() {
    BiMap<String, Integer> map = Maps.emptyBiMap();
    Map<String, Integer> control = Collections.emptyMap();
    assertEquals(control, map);
    assertEquals(control.toString(), map.toString());
    assertEquals(control.hashCode(), map.hashCode());
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    assertEquals(Collections.emptySet(), map.keySet());
    assertEquals(Collections.emptySet(), map.values());
    assertFalse(map.containsKey("foo"));
    assertFalse(map.containsValue(1));
    assertEquals(control.entrySet(), map.entrySet());
    try {
      map.put("bar", 2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.forcePut("bar", 2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.putAll(map);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.remove("foo");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.clear();
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testSingletonBiMap() {
    BiMap<String, Integer> map = Maps.singletonBiMap("foo", 1);
    Map<String, Integer> control = Collections.singletonMap("foo", 1);
    assertEquals(control, map);
    assertEquals(control.toString(), map.toString());
    assertEquals(control.hashCode(), map.hashCode());
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    assertEquals(Collections.singleton("foo"), map.keySet());
    assertEquals(Collections.singleton(1), map.values());
    assertTrue(map.containsKey("foo"));
    assertFalse(map.containsKey("bar"));
    assertTrue(map.containsValue(1));
    assertFalse(map.containsValue(2));
    assertEquals(control.entrySet(), map.entrySet());
    try {
      map.put("bar", 2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.forcePut("bar", 2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.putAll(map);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.remove("foo");
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.clear();
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testImmutableBiMapTest() {
    /* See ImmutableBiMapBuilderTest for more tests. */
    BiMap<String, Integer> bimap = new HashBiMap<String, Integer>();
    bimap.put("one", 1);
    bimap.put("two", 2);
    bimap.put("three", 3);
    BiMap<String, Integer> bimap2 = Maps.immutableBiMap(
        "one", 1, "two", 2, "three", 3);
    assertEquals(bimap, bimap2);
  }
}
