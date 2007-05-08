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
import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for {@link LinkedHashMultimap}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public class LinkedHashMultimapTest extends AbstractSetMultimapTest {

  @Override protected Multimap<String, Integer> create() {
    return Multimaps.newLinkedHashMultimap();
  }

  @SuppressWarnings("unchecked")
  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return ((LinkedHashMultimap<String,Integer>) multimap).clone();
  }

  private Multimap<String, Integer> initializeMultimap5() {
    Multimap<String, Integer> multimap = getMultimap();
    multimap.put("foo", 5);
    multimap.put("bar", 4);
    multimap.put("foo", 3);
    multimap.put("cow", 2);
    multimap.put("bar", 1);
    return multimap;
  }

  public void testToString() {
    assertEquals("{foo=[3, -1, 2, 4, 1], bar=[1, 2, 3]}",
        createSample().toString());
  }

  public void testOrderingReadOnly() {
    Multimap<String, Integer> multimap = initializeMultimap5();
    assertOrderingReadOnly(multimap);
  }

  public void testOrderingUnmodifiable() {
    Multimap<String, Integer> multimap = initializeMultimap5();
    assertOrderingReadOnly(Multimaps.unmodifiableMultimap(multimap));
  }

  public void testOrderingSynchronized() {
    Multimap<String, Integer> multimap = initializeMultimap5();
    assertOrderingReadOnly(Multimaps.synchronizedMultimap(multimap));
  }

  private void assertOrderingReadOnly(Multimap<String, Integer> multimap) {
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 5, 3);
    MoreAsserts.assertContentsInOrder(multimap.get("bar"), 4, 1);
    MoreAsserts.assertContentsInOrder(multimap.get("cow"), 2);

    MoreAsserts.assertContentsInOrder(multimap.keySet(), "foo", "bar", "cow");
    MoreAsserts.assertContentsInOrder(multimap.values(), 5, 4, 3, 2, 1);

    Iterator<Map.Entry<String, Integer>> entryIterator =
        multimap.entries().iterator();
    assertEquals(Maps.immutableEntry("foo", 5), entryIterator.next());
    assertEquals(Maps.immutableEntry("bar", 4), entryIterator.next());
    assertEquals(Maps.immutableEntry("foo", 3), entryIterator.next());
    assertEquals(Maps.immutableEntry("cow", 2), entryIterator.next());
    assertEquals(Maps.immutableEntry("bar", 1), entryIterator.next());

    Iterator<Map.Entry<String, Collection<Integer>>> collectionIterator =
        multimap.asMap().entrySet().iterator();
    Map.Entry<String, Collection<Integer>> entry = collectionIterator.next();
    assertEquals("foo", entry.getKey());
    MoreAsserts.assertContentsInOrder(entry.getValue(), 5, 3);
    entry = collectionIterator.next();
    assertEquals("bar", entry.getKey());
    MoreAsserts.assertContentsInOrder(entry.getValue(), 4, 1);
    entry = collectionIterator.next();
    assertEquals("cow", entry.getKey());
    MoreAsserts.assertContentsInOrder(entry.getValue(), 2);
  }

  public void testOrderingUpdates() {
    Multimap<String, Integer> multimap = initializeMultimap5();

    MoreAsserts.assertContentsInOrder(
        multimap.replaceValues("foo", Arrays.asList(6, 7)), 5, 3);
    MoreAsserts.assertContentsInOrder(multimap.keySet(), "bar", "cow", "foo");
    MoreAsserts.assertContentsInOrder(multimap.removeAll("foo"), 6, 7);
    MoreAsserts.assertContentsInOrder(multimap.keySet(), "bar", "cow");
    assertTrue(multimap.remove("bar", 4));
    MoreAsserts.assertContentsInOrder(multimap.keySet(), "bar", "cow");
    assertTrue(multimap.remove("bar", 1));
    MoreAsserts.assertContentsInOrder(multimap.keySet(), "cow");
    multimap.put("bar", 9);
    MoreAsserts.assertContentsInOrder(multimap.keySet(), "cow", "bar");
  }

  public void testToStringNullExact() {
    Multimap<String, Integer> multimap = getMultimap();

    multimap.put("foo", 3);
    multimap.put("foo", -1);
    multimap.put(null, null);
    multimap.put("bar", 1);
    multimap.put("foo", 2);
    multimap.put(null, 0);
    multimap.put("bar", 2);
    multimap.put("bar", null);
    multimap.put("foo", null);
    multimap.put("foo", 4);
    multimap.put(null, -1);
    multimap.put("bar", 3);
    multimap.put("bar", 1);
    multimap.put("foo", 1);

    assertEquals(
        "{foo=[3, -1, 2, null, 4, 1], null=[null, 0, -1], bar=[1, 2, null, 3]}",
        multimap.toString());
  }

  public void testPutMultimapOrdered() {
    Multimap<String, Integer> multimap = Multimaps.newLinkedHashMultimap();
    multimap.putAll(initializeMultimap5());
    assertOrderingReadOnly(multimap);
  }

  public void testKeysToString() {
    Multimap<String, Integer> multimap = initializeMultimap5();
    assertEquals("[foo x 2, bar x 2, cow]", multimap.keys().toString());
  }

  public void testMultimapConstructor() {
    Multimap<String, Integer> multimap = createSample();
    LinkedHashMultimap<String, Integer> copy =
        Multimaps.newLinkedHashMultimap(multimap);
    assertEquals(multimap, copy);
  }
}
