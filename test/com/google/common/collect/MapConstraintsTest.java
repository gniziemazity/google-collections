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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import junit.framework.TestCase;

/**
 * Tests for {@link MapConstraints}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class MapConstraintsTest extends TestCase {

  private static final String TEST_KEY = "test";

  private static final Integer TEST_VALUE = 42;

  private static final class TestKeyException
      extends IllegalArgumentException {}

  private static final class TestValueException
      extends IllegalArgumentException {}

  private static final MapConstraint<String, Integer> TEST_CONSTRAINT
      = new MapConstraint<String, Integer>() {
          public void checkKeyValue(String key, Integer value) {
            if (TEST_KEY.equals(key)) {
              throw new TestKeyException();
            }
            if (TEST_VALUE.equals(value)) {
              throw new TestValueException();
            }
          }
        };

  public void testNotNull() {
    MapConstraint<? super String, ? super Integer> constraint
        = MapConstraints.NOT_NULL;
    constraint.checkKeyValue("foo", 1);
    try {
      constraint.checkKeyValue(null, 1);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkKeyValue("foo", null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkKeyValue(null, null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
  }

  public void testConstrainedMapLegal() {
    Map<String, Integer> map = Maps.newLinkedHashMap();
    Map<String, Integer> constrained = MapConstraints.constrainedMap(
        map, TEST_CONSTRAINT);
    map.put(TEST_KEY, TEST_VALUE);
    constrained.put("foo", 1);
    map.putAll(Maps.immutableMap("bar", 2));
    constrained.putAll(Maps.immutableMap("baz", 3));
    assertTrue(map.equals(constrained));
    assertTrue(constrained.equals(map));
    assertEquals(map.entrySet(), constrained.entrySet());
    assertEquals(map.keySet(), constrained.keySet());
    assertEquals(map.values(), constrained.values());
    assertEquals(map.toString(), constrained.toString());
    assertEquals(map.hashCode(), constrained.hashCode());
    assertContentsInOrder(map.entrySet(),
        Maps.immutableEntry(TEST_KEY, TEST_VALUE),
        Maps.immutableEntry("foo", 1),
        Maps.immutableEntry("bar", 2),
        Maps.immutableEntry("baz", 3));
  }

  public void testConstrainedMapIllegal() {
    Map<String, Integer> map = Maps.newLinkedHashMap();
    Map<String, Integer> constrained = MapConstraints.constrainedMap(
        map, TEST_CONSTRAINT);
    try {
      constrained.put(TEST_KEY, TEST_VALUE);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.put("baz", TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.put(TEST_KEY, 3);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll(Maps.immutableMap("baz", 3, TEST_KEY, 4));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    assertEquals(Collections.emptySet(), map.entrySet());
    assertEquals(Collections.emptySet(), constrained.entrySet());
  }

  public void testConstrainedBiMapLegal() {
    BiMap<String, Integer> map = Maps.newBiMap(
        Maps.<String, Integer>newLinkedHashMap(),
        Maps.<Integer, String>newLinkedHashMap());
    BiMap<String, Integer> constrained = MapConstraints.constrainedBiMap(
        map, TEST_CONSTRAINT);
    map.put(TEST_KEY, TEST_VALUE);
    constrained.put("foo", 1);
    map.putAll(Maps.immutableMap("bar", 2));
    constrained.putAll(Maps.immutableMap("baz", 3));
    assertTrue(map.equals(constrained));
    assertTrue(constrained.equals(map));
    assertEquals(map.entrySet(), constrained.entrySet());
    assertEquals(map.keySet(), constrained.keySet());
    assertEquals(map.values(), constrained.values());
    assertEquals(map.toString(), constrained.toString());
    assertEquals(map.hashCode(), constrained.hashCode());
    assertContentsInOrder(map.entrySet(),
        Maps.immutableEntry(TEST_KEY, TEST_VALUE),
        Maps.immutableEntry("foo", 1),
        Maps.immutableEntry("bar", 2),
        Maps.immutableEntry("baz", 3));
  }

  public void testConstrainedBiMapIllegal() {
    BiMap<String, Integer> map = Maps.newBiMap(
        Maps.<String, Integer>newLinkedHashMap(),
        Maps.<Integer, String>newLinkedHashMap());
    BiMap<String, Integer> constrained = MapConstraints.constrainedBiMap(
        map, TEST_CONSTRAINT);
    try {
      constrained.put(TEST_KEY, TEST_VALUE);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.put("baz", TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.put(TEST_KEY, 3);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll(Maps.immutableMap("baz", 3, TEST_KEY, 4));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.forcePut(TEST_KEY, 3);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.inverse().forcePut(TEST_VALUE, "baz");
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.inverse().forcePut(3, TEST_KEY);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    assertEquals(Collections.emptySet(), map.entrySet());
    assertEquals(Collections.emptySet(), constrained.entrySet());
  }

  public void testConstrainedMultimapLegal() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained = MapConstraints.constrainedMultimap(
        map, TEST_CONSTRAINT);
    map.put(TEST_KEY, TEST_VALUE);
    constrained.put("foo", 1);
    map.get("bar").add(2);
    constrained.get("baz").add(3);
    map.get("qux").addAll(Arrays.asList(4));
    constrained.get("zig").addAll(Arrays.asList(5));
    map.putAll("zag", Arrays.asList(6));
    constrained.putAll("bee", Arrays.asList(7));
    map.putAll(Multimaps.immutableMultimap("bim", 8));
    constrained.putAll(Multimaps.immutableMultimap("bop", 9));
    map.putAll(new ImmutableMultimapBuilder<String, Integer>()
        .put("dig", 10).getMultimap());
    constrained.putAll(new ImmutableMultimapBuilder<String, Integer>()
        .put("dag", 11).getMultimap());
    assertTrue(map.equals(constrained));
    assertTrue(constrained.equals(map));
    assertContentsInOrder(map.entries(), constrained.entries().toArray());
    assertContentsInOrder(constrained.asMap().get("foo"), 1);
    assertNull(constrained.asMap().get("missing"));
    assertEquals(map.asMap(), constrained.asMap());
    assertEquals(map.values(), constrained.values());
    assertEquals(map.keys(), constrained.keys());
    assertEquals(map.keySet(), constrained.keySet());
    assertEquals(map.toString(), constrained.toString());
    assertEquals(map.hashCode(), constrained.hashCode());
    assertContentsInOrder(map.entries(),
        Maps.immutableEntry(TEST_KEY, TEST_VALUE),
        Maps.immutableEntry("foo", 1),
        Maps.immutableEntry("bar", 2),
        Maps.immutableEntry("baz", 3),
        Maps.immutableEntry("qux", 4),
        Maps.immutableEntry("zig", 5),
        Maps.immutableEntry("zag", 6),
        Maps.immutableEntry("bee", 7),
        Maps.immutableEntry("bim", 8),
        Maps.immutableEntry("bop", 9),
        Maps.immutableEntry("dig", 10),
        Maps.immutableEntry("dag", 11));
    Iterator<Collection<Integer>> iterator =
        constrained.asMap().values().iterator();
    iterator.next();
    iterator.next().add(12);
    assertTrue(map.containsEntry("foo", 12));
  }

  public void testConstrainedTypePreservingList() {
    ListMultimap<String, Integer> map
        = MapConstraints.constrainedListMultimap(
            Multimaps.<String, Integer>newLinkedListMultimap(),
            TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Collection<Integer>> entry
        = map.asMap().entrySet().iterator().next();
    assertTrue(entry.getValue() instanceof List);
    assertFalse(map.entries() instanceof Set);
  }

  public void testConstrainedTypePreservingSet() {
    SetMultimap<String, Integer> map
        = MapConstraints.constrainedSetMultimap(
            Multimaps.<String, Integer>newLinkedHashMultimap(),
            TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Collection<Integer>> entry
        = map.asMap().entrySet().iterator().next();
    assertTrue(entry.getValue() instanceof Set);
  }

  public void testConstrainedTypePreservingSortedSet() {
    SortedSetMultimap<String, Integer> map
        = MapConstraints.constrainedSortedSetMultimap(
            Multimaps.<String, Integer>newTreeMultimap(),
            TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Collection<Integer>> entry
        = map.asMap().entrySet().iterator().next();
    assertTrue(entry.getValue() instanceof SortedSet);
  }

  @SuppressWarnings("unchecked")
  public void testConstrainedMultimapIllegal() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained = MapConstraints.constrainedMultimap(
        map, TEST_CONSTRAINT);
    try {
      constrained.put(TEST_KEY, 1);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.put("foo", TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.put(TEST_KEY, TEST_VALUE);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.get(TEST_KEY).add(1);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.get("foo").add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.get(TEST_KEY).add(TEST_VALUE);
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.get(TEST_KEY).addAll(Arrays.asList(1));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.get("foo").addAll(Arrays.asList(1, TEST_VALUE));
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.get(TEST_KEY).addAll(Arrays.asList(1, TEST_VALUE));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll(TEST_KEY, Arrays.asList(1));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll("foo", Arrays.asList(1, TEST_VALUE));
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.putAll(TEST_KEY, Arrays.asList(1, TEST_VALUE));
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll(new ImmutableMultimapBuilder<String, Integer>()
          .put("foo", 1).put(TEST_KEY, 2).getMultimap());
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.putAll(new ImmutableMultimapBuilder<String, Integer>()
          .put("foo", 1).put("bar", TEST_VALUE).getMultimap());
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.putAll(new ImmutableMultimapBuilder<String, Integer>()
          .put("foo", 1).put(TEST_KEY, TEST_VALUE).getMultimap());
      fail("TestKeyException expected");
    } catch (TestKeyException expected) {}
    try {
      constrained.entries().add(Maps.immutableEntry(TEST_KEY, 1));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      constrained.entries().addAll(Arrays.asList(
          Maps.immutableEntry("foo", 1),
          Maps.immutableEntry(TEST_KEY, 2)));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertTrue(map.isEmpty());
    assertTrue(constrained.isEmpty());
    constrained.put("foo", 1);
    try {
      constrained.asMap().get("foo").add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      constrained.asMap().values().iterator().next().add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    try {
      ((Collection<Integer>) constrained.asMap().values().toArray()[0])
          .add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    assertContentsInOrder(map.entries(), constrained.entries().toArray());
    assertEquals(map.asMap(), constrained.asMap());
    assertEquals(map.values(), constrained.values());
    assertEquals(map.keys(), constrained.keys());
    assertEquals(map.keySet(), constrained.keySet());
    assertEquals(map.toString(), constrained.toString());
    assertEquals(map.hashCode(), constrained.hashCode());
  }

  public void testClassConstraint() {
    MapConstraint<Object, Object> constraint
        = MapConstraints.classConstraint(String.class, Integer.class);
    constraint.checkKeyValue("foo", 1);
    try {
      constraint.checkKeyValue(null, 1);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkKeyValue("foo", null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkKeyValue(null, null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkKeyValue(new Object(), 1);
      fail("ClassCastException expected");
    } catch (ClassCastException expected) {}
    try {
      constraint.checkKeyValue("foo", new Object());
      fail("ClassCastException expected");
    } catch (ClassCastException expected) {}
    try {
      constraint.checkKeyValue(new Object(), new Object());
      fail("ClassCastException expected");
    } catch (ClassCastException expected) {}
  }

  @SuppressWarnings("unchecked")
  public void testMapEntrySetToArray() {
    Map<String, Integer> map = Maps.newLinkedHashMap();
    Map<String, Integer> constrained
        = MapConstraints.constrainedMap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Integer> entry
        = (Map.Entry) constrained.entrySet().toArray()[0];
    try {
      entry.setValue(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    assertFalse(map.containsValue(TEST_VALUE));
  }

  public void testMapEntrySetContainsNefariousEntry() {
    Map<String, Integer> map = Maps.newTreeMap();
    Map<String, Integer> constrained
        = MapConstraints.constrainedMap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Integer> nefariousEntry
        = MapsTest.nefariousEntry(TEST_KEY, TEST_VALUE);
    Set<Map.Entry<String, Integer>> entries = constrained.entrySet();
    assertFalse(entries.contains(nefariousEntry));
    assertFalse(map.containsValue(TEST_VALUE));
    assertFalse(entries.containsAll(Collections.singleton(nefariousEntry)));
    assertFalse(map.containsValue(TEST_VALUE));
  }

  @SuppressWarnings("unchecked")
  public void testMultimapAsMapEntriesToArray() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Collection<Integer>> entry
        = (Map.Entry) constrained.asMap().entrySet().toArray()[0];    
    try {
      entry.setValue(Collections.<Integer>emptySet());
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      entry.getValue().add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    assertFalse(map.containsValue(TEST_VALUE));    
  }

  @SuppressWarnings("unchecked")
  public void testMultimapAsMapValuesToArray() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Collection<Integer> collection
        = (Collection<Integer>) constrained.asMap().values().toArray()[0];
    try {
      collection.add(TEST_VALUE);
      fail("TestValueException expected");
    } catch (TestValueException expected) {}
    assertFalse(map.containsValue(TEST_VALUE));
  }
  
  public void testMultimapEntriesContainsNefariousEntry() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Integer> nefariousEntry
        = MapsTest.nefariousEntry(TEST_KEY, TEST_VALUE);
    Collection<Map.Entry<String, Integer>> entries = constrained.entries();
    assertFalse(entries.contains(nefariousEntry));
    assertFalse(map.containsValue(TEST_VALUE));
    assertFalse(entries.containsAll(Collections.singleton(nefariousEntry)));
    assertFalse(map.containsValue(TEST_VALUE));
  }

  public void testMultimapEntriesRemoveNefariousEntry() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, Integer> nefariousEntry
        = MapsTest.nefariousEntry(TEST_KEY, TEST_VALUE);
    Collection<Map.Entry<String, Integer>> entries = constrained.entries();
    assertFalse(entries.remove(nefariousEntry));
    assertFalse(map.containsValue(TEST_VALUE));
    assertFalse(entries.removeAll(Collections.singleton(nefariousEntry)));
    assertFalse(map.containsValue(TEST_VALUE));
  }

  public void testMultimapAsMapEntriesContainsNefariousEntry() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, ? extends Collection<Integer>> nefariousEntry
        = MapsTest.nefariousEntry(TEST_KEY, Collections.singleton(TEST_VALUE));
    Set<Map.Entry<String, Collection<Integer>>> entries
        = constrained.asMap().entrySet();
    assertFalse(entries.contains(nefariousEntry));
    assertFalse(map.containsValue(TEST_VALUE));
    assertFalse(entries.containsAll(Collections.singleton(nefariousEntry)));
    assertFalse(map.containsValue(TEST_VALUE));
  }

  public void testMultimapAsMapEntriesRemoveNefariousEntry() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained
        = MapConstraints.constrainedMultimap(map, TEST_CONSTRAINT);
    map.put("foo", 1);
    Map.Entry<String, ? extends Collection<Integer>> nefariousEntry
        = MapsTest.nefariousEntry(TEST_KEY, Collections.singleton(TEST_VALUE));
    Set<Map.Entry<String, Collection<Integer>>> entries
        = constrained.asMap().entrySet();
    assertFalse(entries.remove(nefariousEntry));
    assertFalse(map.containsValue(TEST_VALUE));
    assertFalse(entries.removeAll(Collections.singleton(nefariousEntry)));
    assertFalse(map.containsValue(TEST_VALUE));
  }

  public void testNefariousMapPutAll() {
    Map<String, Integer> map = Maps.newLinkedHashMap();
    Map<String, Integer> constrained = MapConstraints.constrainedMap(
        map, TEST_CONSTRAINT);
    Map<String, Integer> nefarious
        = nefariousMap("foo", TEST_VALUE);
    constrained.putAll(nefarious);
    assertNull(constrained.get("foo"));
    assertFalse(constrained.containsValue(TEST_VALUE));
  }

  public void testNefariousMultimapPutAllIterable() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained = MapConstraints.constrainedMultimap(
        map, TEST_CONSTRAINT);
    Collection<Integer> nefarious
        = ConstraintsTest.nefariousCollection(TEST_VALUE);
    constrained.putAll("foo", nefarious);
    assertTrue(constrained.get("foo").isEmpty());
    assertFalse(constrained.containsValue(TEST_VALUE));
  }

  public void testNefariousMultimapPutAllMultimap() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained = MapConstraints.constrainedMultimap(
        map, TEST_CONSTRAINT);
    Multimap<String, Integer> nefarious
        = Multimaps.forMap(nefariousMap("foo", TEST_VALUE));
    constrained.putAll(nefarious);
    assertTrue(constrained.get("foo").isEmpty());
    assertFalse(constrained.containsValue(TEST_VALUE));
  }

  public void testNefariousMultimapGetAddAll() {
    Multimap<String, Integer> map = Multimaps.newLinkedListMultimap();
    Multimap<String, Integer> constrained = MapConstraints.constrainedMultimap(
        map, TEST_CONSTRAINT);
    Collection<Integer> nefarious
        = ConstraintsTest.nefariousCollection(TEST_VALUE);
    constrained.get("foo").addAll(nefarious);
    assertTrue(constrained.get("foo").isEmpty());
    assertFalse(constrained.containsValue(TEST_VALUE));
  }

  /**
   * Returns a "nefarious" map, which initially appears to be empty, but on
   * subsequent iterations contains a single mapping with the specified key and
   * value. This verifies that the constrained collection uses a defensive copy.
   *
   * @param key the key to be contained in the nefarious map
   * @param value the value to be contained in the nefarious map
   */
  static <K,V> Map<K,V> nefariousMap(K key, V value) {
    final Map.Entry<K,V> entry = Maps.immutableEntry(key, value);
    return new AbstractMap<K,V>() {
        int i = 0;
        @Override public int size() {
          return i;
        }
        @Override public Set<Entry<K,V>> entrySet() {
          return (i++ > 0) // muhahaha!
              ? Collections.singleton(entry)
              : Collections.<Entry<K,V>>emptySet();
        }
      };
  }
}
