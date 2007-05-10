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

import static com.google.common.collect.Maps.immutableMap;
import com.google.common.collect.helpers.MoreAsserts;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Common tests for any {@code BiMap}.
 *
 * @author kevinb
 */
public abstract class AbstractBiMapTest extends TestCase {

  protected abstract BiMap<Integer,String> create();

  protected BiMap<Integer,String> bimap;
  protected Set<Entry<Integer,String>> entrySet;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bimap = create();
    entrySet = bimap.entrySet();
  }

  public void testClear() throws Exception {
    bimap.clear();
    assertTrue(bimap.isEmpty());
    putOneTwoThree();
    bimap.clear();
    assertTrue(bimap.isEmpty());
  }

  public void testContainsKey() throws Exception {
    assertFalse(bimap.containsKey(null));
    assertFalse(bimap.containsKey(1));
    assertFalse(bimap.containsKey("one"));

    bimap.put(1, "one");
    assertTrue(bimap.containsKey(1));

    bimap.put(null, null);
    assertTrue(bimap.containsKey(null));
  }

  public void testContainsValue() throws Exception {
    assertFalse(bimap.containsValue(null));
    assertFalse(bimap.containsValue(1));
    assertFalse(bimap.containsValue("one"));

    bimap.put(1, "one");
    assertTrue(bimap.containsValue("one"));

    bimap.put(null, null);
    assertTrue(bimap.containsValue(null));
  }

  public void testEquals() throws Exception {
    BiMap<Integer, String> biMap = create();
    assertEquals(biMap, biMap);
    assertEquals(create(), biMap);
    biMap.put(1, null);
    MoreAsserts.assertNotEqual(create(), biMap);
  }

  public void testGet() throws Exception {
    assertNull(bimap.get(1));
    assertNull(bimap.get(null));
    assertNull(bimap.get("bad"));

    bimap.put(1, "one");
    bimap.put(0, null);
    bimap.put(null, "nothing");
    assertEquals("one", bimap.get(1));
    assertNull(bimap.get(0));
    assertEquals("nothing", bimap.get(null));
    assertNull(bimap.get("bad"));

    bimap.forcePut(null, null);
    assertNull(bimap.get(null));
    bimap.remove(null);
    assertNull(bimap.get(null));
  }

  public void testInverseSimple() throws Exception {
    BiMap<String, Integer> inverse = bimap.inverse();
    bimap.put(1, "one");
    bimap.put(2, "two");
    assertEquals(immutableMap("one", 1, "two", 2), inverse);
    // see InverseBiMapTest

    assertSame(bimap, inverse.inverse());
  }

  public void testIsEmpty() throws Exception {
    assertTrue(bimap.isEmpty());
    bimap.put(1, "one");
    assertFalse(bimap.isEmpty());
    bimap.remove(1);
    assertTrue(bimap.isEmpty());
  }

  public void testPut() throws Exception {
    bimap.put(1, "one");
    assertEquals(immutableMap(1, "one"), bimap);

    bimap.put(2, "two");
    assertEquals(immutableMap(1, "one", 2, "two"), bimap);

    bimap.put(2, "two");
    assertEquals(immutableMap(1, "one", 2, "two"), bimap);

    bimap.put(1, "ONE");
    assertEquals(immutableMap(1, "ONE", 2, "two"), bimap);

    try {
      bimap.put(3, "two");
      fail();
    } catch (IllegalArgumentException e) {
    }
    assertEquals(immutableMap(1, "ONE", 2, "two"), bimap);

    bimap.put(-1, null);
    bimap.put(null, "null");
    assertEquals(immutableMap(1, "ONE", 2, "two", -1, null, null, "null"), bimap);

    bimap.remove(-1);
    bimap.put(null, null);
    assertEquals(immutableMap(1, "ONE", 2, "two", null, null), bimap);
  }

  public void testPutNull() throws Exception {
    bimap.put(-1, null);
    assertTrue(bimap.containsValue(null));
    bimap.put(1, "one");
    assertTrue(bimap.containsValue(null));
  }

  public void testPutAll() throws Exception {
    bimap.put(1, "one");
    Map<Integer,String> newEntries = immutableMap(2, "two", 3, "three");
    bimap.putAll(newEntries);
    assertEquals(immutableMap(1, "one", 2, "two", 3, "three"), bimap);
  }

  public void testForcePut() throws Exception {
    assertNull(bimap.forcePut(1, "one"));
    assertEquals(immutableMap(1, "one"), bimap);
    assertEquals("one", bimap.forcePut(1, "one"));
    assertEquals(immutableMap(1, "one"), bimap);
    assertEquals("one", bimap.forcePut(1, "ONE"));
    assertEquals(immutableMap(1, "ONE"), bimap);
    assertNull(bimap.forcePut(-1, "ONE")); // key 1 disappears without a trace
    assertEquals(immutableMap(-1, "ONE"), bimap);
    assertNull(bimap.forcePut(2, "two"));
    assertEquals(immutableMap(-1, "ONE", 2, "two"), bimap);
    assertEquals("two", bimap.forcePut(2, "ONE"));
    assertEquals(immutableMap(2, "ONE"), bimap);
  }

  public void testRemove() throws Exception {
    bimap.putAll(immutableMap(0, null, 1, "one", null, "null"));
    assertNull(bimap.remove(0));
    assertEquals(immutableMap(1, "one", null, "null"), bimap);
    assertEquals("null", bimap.remove(null));
    assertEquals(immutableMap(1, "one"), bimap);

    assertNull(bimap.remove(15));

    assertEquals("one", bimap.remove(1));
    assertTrue(bimap.isEmpty());
  }

  public void testSize() throws Exception {
    assertEquals(0, bimap.size());
    bimap.put(1, "one");
    assertEquals(1, bimap.size());
    bimap.put(1, "ONE");
    assertEquals(1, bimap.size());
    bimap.put(2, "two");
    assertEquals(2, bimap.size());
    bimap.forcePut(1, "two");
    assertEquals(1, bimap.size());
  }

  public void testToString() throws Exception {
    bimap.put(1, "one");
    bimap.put(2, "two");

    String string = bimap.toString();
    String expected = string.startsWith("{1")
        ? "{1=one, 2=two}"
        : "{2=two, 1=one}";
    assertEquals(expected, bimap.toString());
  }

  // Entry Set

  public void testEntrySetAdd() throws Exception {
    try {
      entrySet.add(Maps.immutableEntry(1, "one"));
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testEntrySetAddAll() throws Exception {
    try {
      entrySet.addAll(Arrays.asList(Maps.immutableEntry(1, "one")));
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testEntrySetClear() throws Exception {
    entrySet.clear();
    assertTrue(entrySet.isEmpty());
    assertTrue(bimap.isEmpty());
    putOneTwoThree();
    entrySet.clear();
    assertTrue(entrySet.isEmpty());
    assertTrue(bimap.isEmpty());
  }

  public void testEntrySetContains() throws Exception {
    assertFalse(entrySet.contains(Maps.immutableEntry(1, "one")));
    bimap.put(1, "one");
    assertTrue(entrySet.contains(Maps.immutableEntry(1, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, "")));
    assertFalse(entrySet.contains(Maps.immutableEntry(0, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, null)));
    assertFalse(entrySet.contains(Maps.immutableEntry(null, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(null, null)));

    bimap.put(null, null);
    assertTrue(entrySet.contains(Maps.immutableEntry(1, "one")));
    assertTrue(entrySet.contains(Maps.immutableEntry(null, null)));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, "")));
    assertFalse(entrySet.contains(Maps.immutableEntry(0, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, null)));
    assertFalse(entrySet.contains(Maps.immutableEntry(null, "one")));

    bimap.put(null, "null");
    bimap.put(0, null);
    assertTrue(entrySet.contains(Maps.immutableEntry(1, "one")));
    assertTrue(entrySet.contains(Maps.immutableEntry(null, "null")));
    assertTrue(entrySet.contains(Maps.immutableEntry(0, null)));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, "")));
    assertFalse(entrySet.contains(Maps.immutableEntry(0, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(1, null)));
    assertFalse(entrySet.contains(Maps.immutableEntry(null, "one")));
    assertFalse(entrySet.contains(Maps.immutableEntry(null, null)));
  }

  public void testEntrySetIsEmpty() throws Exception {
    assertTrue(entrySet.isEmpty());
    bimap.put(1, "one");
    assertFalse(entrySet.isEmpty());
    bimap.remove(1);
    assertTrue(entrySet.isEmpty());
  }

  public void testKeySetIteratorRemove() throws Exception {
    putOneTwoThree();
    Iterator<Integer> iterator = bimap.keySet().iterator();
    iterator.next();
    iterator.remove();
    assertEquals(2, bimap.size());
    assertEquals(2, bimap.inverse().size());
  }

  public void testKeySetRemoveAll() throws Exception {
    putOneTwoThree();
    Set<Integer> keySet = bimap.keySet();
    assertTrue(keySet.removeAll(Arrays.asList(1, 3)));
    assertEquals(1, bimap.size());
    assertTrue(keySet.contains(2));
  }

  public void testKeySetRetainAll() throws Exception {
    putOneTwoThree();
    Set<Integer> keySet = bimap.keySet();
    assertTrue(keySet.retainAll(Collections.singleton(2)));
    assertEquals(1, bimap.size());
    assertTrue(keySet.contains(2));
  }

  public void testEntriesIteratorRemove() throws Exception {
    putOneTwoThree();
    Iterator<Entry<Integer, String>> iterator = bimap.entrySet().iterator();
    iterator.next();
    iterator.remove();
    assertEquals(2, bimap.size());
    assertEquals(2, bimap.inverse().size());
  }

  public void testEntriesRetainAll() throws Exception {
    putOneTwoThree();
    Set<Map.Entry<Integer, String>> entries = bimap.entrySet();
    Map.Entry<Integer, String> entry = Maps.immutableEntry(2, "two");
    assertTrue(entries.retainAll(Collections.singleton(entry)));
    assertEquals(1, bimap.size());
    assertTrue(bimap.containsKey(2));
  }

  public void testValuesIteratorRemove() throws Exception {
    putOneTwoThree();
    Iterator<String> iterator = bimap.values().iterator();
    iterator.next();
    iterator.remove();
    assertEquals(2, bimap.size());
    assertEquals(2, bimap.inverse().size());
  }

  /*
   *
   *  I apologize that this unit test is still woefully inadequate.
   *
   */

  private void putOneTwoThree() {
    bimap.put(1, "one");
    bimap.put(2, "two");
    bimap.put(3, "three");
  }
}
