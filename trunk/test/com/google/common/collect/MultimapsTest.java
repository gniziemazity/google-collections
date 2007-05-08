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
import static com.google.common.collect.helpers.MoreAsserts.assertContentsAnyOrder;
import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import com.google.common.collect.helpers.SerializableTester;
import java.util.Collections;
import java.util.Map;

/**
 * Unit test for {@link Multimaps}.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public class MultimapsTest extends AbstractMultimapTest {

  @Override protected Multimap<String, Integer> create() {
    return Multimaps.synchronizedSetMultimap(
        new HashMultimap<String, Integer>());
  }

  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return null;
  }

  public void testUnmodifiableArrayListMultimap() {
    testUnmodifiableMultimap(new ArrayListMultimap<String, Integer>(), true);
  }

  public void testUnmodifiableHashMultimap() {
    testUnmodifiableMultimap(new HashMultimap<String, Integer>(), false);
  }

  public void testUnmodifiableTreeMultimap() {
    testUnmodifiableMultimap(
        new TreeMultimap<String, Integer>(), false, "null", 42);
  }

  public void testUnmodifiableSynchronizedArrayListMultimap() {
    testUnmodifiableMultimap(Multimaps.synchronizedListMultimap(
      new ArrayListMultimap<String, Integer>()), true);
  }

  public void testUnmodifiableSynchronizedHashMultimap() {
    testUnmodifiableMultimap(Multimaps.synchronizedSetMultimap(
      new HashMultimap<String, Integer>()), false);
  }

  public void testUnmodifiableSynchronizedTreeMultimap() {
    testUnmodifiableMultimap(Multimaps.synchronizedSortedSetMultimap(
      new TreeMultimap<String, Integer>()), false, "null", 42);
  }

  public void testUnmodifiableMultimapIsView() {
    Multimap<String, Integer> mod = Multimaps.newHashMultimap();
    Multimap<String, Integer> unmod = Multimaps.unmodifiableMultimap(mod);
    assertEquals(mod, unmod);
    mod.put("foo", 1);
    assertTrue(unmod.containsEntry("foo", 1));
    assertEquals(mod, unmod);
  }

  @SuppressWarnings("unchecked")
  public void testUnmodifiableMultimapEntries() {
    Multimap<String, Integer> mod = Multimaps.newHashMultimap();
    Multimap<String, Integer> unmod = Multimaps.unmodifiableMultimap(mod);
    mod.put("foo", 1);
    Map.Entry<String, Integer> entry = unmod.entries().iterator().next();
    try {
      entry.setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    entry = (Map.Entry<String, Integer>) unmod.entries().toArray()[0];
    try {
      entry.setValue(2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    assertFalse(unmod.entries().contains(MapsTest.nefariousEntry("pwnd", 2)));
    assertFalse(unmod.keys().contains("pwnd"));
  }

  /**
   * The supplied multimap will be mutated and an unmodifiable instance used
   * in its stead. The multimap must support null keys and values.
   */
  private static void testUnmodifiableMultimap(
      Multimap<String, Integer> multimap, boolean permitsDuplicates) {
    testUnmodifiableMultimap(multimap, permitsDuplicates, null, null);
  }

  /**
   * The supplied multimap will be mutated and an unmodifiable instance used
   * in its stead. If the multimap does not support null keys or values,
   * alternatives may be specified for tests involving nulls.
   */
  private static void testUnmodifiableMultimap(
      Multimap<String, Integer> multimap, boolean permitsDuplicates,
      @Nullable String nullKey, @Nullable Integer nullValue) {
    multimap.clear();
    multimap.put("foo", 1);
    multimap.put("foo", 2);
    multimap.put("foo", 3);
    multimap.put("bar", 5);
    multimap.put("bar", -1);
    multimap.put(nullKey, nullValue);
    multimap.put("foo", nullValue);
    multimap.put(nullKey, 5);
    multimap.put("foo", 2);

    if (permitsDuplicates) {
      assertEquals(9, multimap.size());
    } else {
      assertEquals(8, multimap.size());
    }

    Multimap<String, Integer> unmodifiable;
    if (multimap instanceof SortedSetMultimap) {
      unmodifiable = Multimaps.unmodifiableSortedSetMultimap(
          (SortedSetMultimap<String, Integer>) multimap);
    } else if (multimap instanceof SetMultimap) {
      unmodifiable = Multimaps.unmodifiableSetMultimap(
          (SetMultimap<String, Integer>) multimap);
    } else if (multimap instanceof ListMultimap) {
      unmodifiable = Multimaps.unmodifiableListMultimap(
          (ListMultimap<String, Integer>) multimap);
    } else {
      unmodifiable = Multimaps.unmodifiableMultimap(multimap);
    }

    UnmodifiableCollectionTests.assertMultimapIsUnmodifiable(
      unmodifiable, "test", 123);

    assertUnmodifiableIterableInTandem(
      unmodifiable.keys(), multimap.keys());

    assertUnmodifiableIterableInTandem(
      unmodifiable.keySet(), multimap.keySet());

    assertUnmodifiableIterableInTandem(
      unmodifiable.entries(), multimap.entries());

    assertUnmodifiableIterableInTandem(
      unmodifiable.asMap().entrySet(), multimap.asMap().entrySet());

    assertEquals(multimap.toString(), unmodifiable.toString());
    assertEquals(multimap.hashCode(), unmodifiable.hashCode());
    assertEquals(multimap, unmodifiable);
    
    assertContentsAnyOrder(unmodifiable.asMap().get("bar"), 5, -1);
    assertNull(unmodifiable.asMap().get("missing"));
  }

  private static <T> void assertUnmodifiableIterableInTandem(
      Iterable<T> unmodifiable, Iterable<T> modifiable) {

    UnmodifiableCollectionTests.assertIteratorIsUnmodifiable(
      unmodifiable.iterator());

    UnmodifiableCollectionTests.assertIteratorsInOrder(
      unmodifiable.iterator(), modifiable.iterator());
  }

  public void testInverseHashMultimapFromMap() {
    Map<String,Integer> map = Maps.immutableMap("foo", 3, "bar", 1, "cow", 3);

    HashMultimap<Integer,String> inverse = Multimaps.inverseHashMultimap(map);
    assertEquals(3, inverse.size());
    assertContentsAnyOrder(inverse.keySet(), 1, 3);
    assertContentsAnyOrder(inverse.get(1), "bar");
    assertContentsAnyOrder(inverse.get(3), "foo", "cow");
  }

  public void testInverseHashMultimapFromMultimap() {
    Multimap<String,Integer> multimap = Multimaps.newHashMultimap();
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("bar", 1);

    HashMultimap<Integer,String> inverse =
        Multimaps.inverseHashMultimap(multimap);
    assertEquals(3, inverse.size());
    assertContentsAnyOrder(inverse.keySet(), 1, 3);
    assertContentsAnyOrder(inverse.get(1), "bar");
    assertContentsAnyOrder(inverse.get(3), "foo", "bar");
  }

  public void testInverseArrayListMultimapFromMap() {
    Map<String,Integer> map = Maps.immutableMap("foo", 3, "bar", 1, "cow", 3);

    ArrayListMultimap<Integer,String> inverse =
        Multimaps.inverseArrayListMultimap(map);
    assertEquals(3, inverse.size());
    assertContentsAnyOrder(inverse.keySet(), 1, 3);
    assertContentsAnyOrder(inverse.get(1), "bar");
    assertContentsAnyOrder(inverse.get(3), "foo", "cow");
  }

  public void testInverseArrayListMultimapFromMultimap() {
    Multimap<String,Integer> multimap = Multimaps.newHashMultimap();
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("bar", 1);

    ArrayListMultimap<Integer,String> inverse =
        Multimaps.inverseArrayListMultimap(multimap);
    assertEquals(3, inverse.size());
    assertContentsAnyOrder(inverse.keySet(), 1, 3);
    assertContentsAnyOrder(inverse.get(1), "bar");
    assertContentsAnyOrder(inverse.get(3), "foo", "bar");
  }

  public void testInverseLinkedHashMultimapFromMap() {
    Map<String,Integer> map = Maps.newLinkedHashMap();
    map.put("foo", 3);
    map.put("bar", 1);
    map.put("cow", 3);

    LinkedHashMultimap<Integer,String> inverse =
        Multimaps.inverseLinkedHashMultimap(map);
    assertEquals(3, inverse.size());
    assertContentsInOrder(inverse.keySet(), 3, 1);
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "foo", "cow");
  }

  public void testInverseLinkedHashMultimapFromMultimap() {
    Multimap<String,Integer> multimap = Multimaps.newLinkedHashMultimap();
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("bar", 1);

    LinkedHashMultimap<Integer,String> inverse =
        Multimaps.inverseLinkedHashMultimap(multimap);
    assertEquals(3, inverse.size());
    assertContentsInOrder(inverse.keySet(), 3, 1);
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "foo", "bar");
  }

  public void testInverseTreeMultimapFromMap() {
    Map<String,Integer> map = Maps.immutableMap("foo", 3, "bar", 1, "cow", 3);

    TreeMultimap<Integer,String> inverse = Multimaps.inverseTreeMultimap(map);
    assertEquals(3, inverse.size());
    assertContentsInOrder(inverse.keySet(), 1, 3);
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "cow", "foo");
  }

  public void testInverseTreeMultimapFromMultimap() {
    Multimap<String,Integer> multimap = Multimaps.newHashMultimap();
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("bar", 1);

    TreeMultimap<Integer,String> inverse =
        Multimaps.inverseTreeMultimap(multimap);
    assertEquals(3, inverse.size());
    assertContentsInOrder(inverse.keySet(), 1, 3);
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "bar", "foo");
  }

  public void testInverseLinkedListMultimapFromMap() {
    Map<String,Integer> map = Maps.newLinkedHashMap();
    map.put("foo", 3);
    map.put("bar", 1);
    map.put("cow", 3);

    LinkedListMultimap<Integer,String> inverse =
        Multimaps.inverseLinkedListMultimap(map);
    assertEquals(3, inverse.size());
    assertContentsInOrder(inverse.keys(), 3, 1, 3);
    assertContentsInOrder(inverse.keySet(), 3, 1);
    assertContentsInOrder(inverse.values(), "foo", "bar", "cow");
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "foo", "cow");
  }

  public void testInverseLinkedListMultimapFromMultimap() {
    Multimap<String,Integer> multimap = Multimaps.newLinkedListMultimap();
    multimap.put("foo", 3);
    multimap.put("bar", 3);
    multimap.put("bar", 1);
    multimap.put("foo", 3); // duplicate key-value pair

    LinkedListMultimap<Integer,String> inverse =
        Multimaps.inverseLinkedListMultimap(multimap);
    assertEquals(4, inverse.size());
    assertContentsInOrder(inverse.keys(), 3, 3, 1, 3);
    assertContentsInOrder(inverse.keySet(), 3, 1);
    assertContentsInOrder(inverse.values(), "foo", "bar", "bar", "foo");
    assertContentsInOrder(inverse.get(1), "bar");
    assertContentsInOrder(inverse.get(3), "foo", "bar", "foo");
  }

  public void testEmptyMultimap() {
    SetMultimap<String, Integer> empty = Multimaps.emptyMultimap();
    SetMultimap<String, Integer> empty2 = Multimaps.newHashMultimap();
    assertTrue(empty.equals(empty2));
    assertTrue(empty2.equals(empty));
    assertFalse(empty.equals(null));
    assertFalse(empty.equals(new Object()));
    assertTrue(empty.isEmpty());
    assertEquals(0, empty.size());
    assertEquals("{}", empty.toString());
    assertEquals("[]", empty.keys().toString());
    try {
      empty.put("foo", 1);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testEmptyMultimapSingleton()  {
    SetMultimap<String, Integer> empty = Multimaps.emptyMultimap();
    assertSame(empty, Multimaps.emptyMultimap());
    assertSame(empty, SerializableTester.reserialize(empty));
  }

  public void testForMap() {
    Map<String, Integer> map = Maps.newHashMap();
    map.put("foo", 1);
    map.put("bar", 2);
    Multimap<String, Integer> multimap = new HashMultimap<String, Integer>();
    multimap.put("foo", 1);
    multimap.put("bar", 2);
    Multimap<String, Integer> multimapView = Multimaps.forMap(map);
    assertTrue(multimap.equals(multimapView));
    assertTrue(multimapView.equals(multimap));
    assertEquals(multimap.toString(), multimapView.toString());
    assertEquals(multimap.hashCode(), multimapView.hashCode());
    assertEquals(multimap.size(), multimapView.size());
    assertTrue(multimapView.containsKey("foo"));
    assertTrue(multimapView.containsValue(1));
    assertTrue(multimapView.containsEntry("bar", 2));
    assertEquals(Collections.singleton(1), multimapView.get("foo"));
    assertEquals(Collections.singleton(2), multimapView.get("bar"));
    assertEquals(Collections.singleton(1), multimapView.asMap().get("foo"));
    assertNull(multimapView.asMap().get("cow"));
    assertTrue(multimapView.asMap().containsKey("foo"));
    assertFalse(multimapView.asMap().containsKey("cow"));
    try {
      multimapView.put("baz", 3);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.putAll("baz", Collections.singleton(3));
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.putAll(multimap);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      multimapView.replaceValues("foo", Collections.<Integer>emptySet());
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    multimapView.remove("bar", 2);
    assertFalse(multimapView.containsKey("bar"));
    assertFalse(map.containsKey("bar"));
    assertEquals(map.keySet(), multimapView.keySet());
    assertEquals(map.keySet(), multimapView.keys().elementSet());
    assertContentsAnyOrder(multimapView.keys(), "foo");
    assertContentsAnyOrder(multimapView.values(), 1);
    assertContentsAnyOrder(multimapView.entries(),
        Maps.immutableEntry("foo", 1));
    assertContentsAnyOrder(multimapView.asMap().entrySet(),
        Maps.immutableEntry("foo", Collections.singleton(1)));
    multimapView.clear();
    assertFalse(multimapView.containsKey("foo"));
    assertFalse(map.containsKey("foo"));
    assertTrue(map.isEmpty());
    assertTrue(multimapView.isEmpty());
    multimap.clear();
    assertEquals(multimap.toString(), multimapView.toString());
    assertEquals(multimap.hashCode(), multimapView.hashCode());
    assertEquals(multimap.size(), multimapView.size());
  }

  public void testSingletonMultimap() {
    Multimap<String, Integer> map = Multimaps.singletonMultimap("foo", 1);
    Multimap<String, Integer> control = new HashMultimap<String, Integer>();
    control.put("foo", 1);
    assertEquals(control, map);
    assertEquals(control.toString(), map.toString());
    assertEquals(control.hashCode(), map.hashCode());
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    assertEquals(Multisets.singletonMultiset("foo"), map.keys());
    assertEquals(Collections.singleton(1), map.values());
    assertTrue(map.containsKey("foo"));
    assertFalse(map.containsKey("bar"));
    assertTrue(map.containsValue(1));
    assertFalse(map.containsValue(2));
    assertEquals(control.entries(), map.entries());
    assertEquals(control.asMap(), map.asMap());
    assertEquals(control.asMap().entrySet(), map.asMap().entrySet());
    try {
      map.put("bar", 2);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.putAll(map);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.remove("foo", 1);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
    try {
      map.clear();
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException expected) {}
  }
}
