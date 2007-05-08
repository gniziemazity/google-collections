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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Unit tests for {@link TreeMultimap} with natural ordering.
 *
 * @author jlevy@google.com (Jared Levy)
 */
public class TreeMultimapNaturalTest<E> extends AbstractSetMultimapTest {
  @Override protected Multimap<String, Integer> create() {
    return Multimaps.newTreeMultimap();
  }

  @SuppressWarnings("unchecked")
  @Override protected Multimap<String, Integer>
      makeClone(Multimap<String, Integer> multimap) {
    return ((TreeMultimap<String, Integer>) multimap).clone();
  }

  /* Null keys and values aren't supported. */
  @Override protected String nullKey() {
    return "null";
  }

  @Override protected Integer nullValue() {
    return 42;
  }

  /**
   * Create and populate a {@link TreeMultimap} with the natural ordering of
   * keys and values.
   */
  private TreeMultimap<String, Integer> createPopulate() {
    TreeMultimap<String, Integer> multimap = Multimaps.newTreeMultimap();
    multimap.put("google", 2);
    multimap.put("google", 6);
    multimap.put("foo", 3);
    multimap.put("foo", 1);
    multimap.put("foo", 7);
    multimap.put("tree", 4);
    multimap.put("tree", 0);
    return multimap;
  }

  public void testToString() {
    assertEquals("{bar=[1, 2, 3], foo=[-1, 1, 2, 3, 4]}",
        createSample().toString());
  }

  public void testGetComparator() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    assertNull(multimap.keyComparator());
    assertNull(multimap.valueComparator());
  }

  public void testOrderedGet() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    MoreAsserts.assertContentsInOrder(multimap.get("foo"), 1, 3, 7);
    MoreAsserts.assertContentsInOrder(multimap.get("google"), 2, 6);
    MoreAsserts.assertContentsInOrder(multimap.get("tree"), 0, 4);
  }

  public void testOrderedKeySet() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    MoreAsserts.assertContentsInOrder(
        multimap.keySet(), "foo", "google", "tree");
  }

  public void testOrderedAsMapEntries() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    Iterator<Map.Entry<String, Collection<Integer>>> iterator =
        multimap.asMap().entrySet().iterator();
    Map.Entry<String, Collection<Integer>> entry = iterator.next();
    assertEquals("foo", entry.getKey());
    MoreAsserts.assertContentsAnyOrder(entry.getValue(), 1, 3, 7);
    entry = iterator.next();
    assertEquals("google", entry.getKey());
    MoreAsserts.assertContentsAnyOrder(entry.getValue(), 2, 6);
    entry = iterator.next();
    assertEquals("tree", entry.getKey());
    MoreAsserts.assertContentsAnyOrder(entry.getValue(), 0, 4);
  }

  public void testOrderedEntries() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    MoreAsserts.assertContentsInOrder(multimap.entries(),
        Maps.immutableEntry("foo", 1),
        Maps.immutableEntry("foo", 3),
        Maps.immutableEntry("foo", 7),
        Maps.immutableEntry("google", 2),
        Maps.immutableEntry("google", 6),
        Maps.immutableEntry("tree", 0),
        Maps.immutableEntry("tree", 4));
  }

  public void testOrderedValues() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    MoreAsserts.assertContentsInOrder(multimap.values(),
        1, 3, 7, 2, 6, 0, 4);
  }

  public void testFirst() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    assertEquals(Integer.valueOf(1), multimap.get("foo").first());
    try {
      multimap.get("missing").first();
      fail("Expected NoSuchElementException");
    } catch (NoSuchElementException expected) {}
  }

  public void testLast() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    assertEquals(Integer.valueOf(7), multimap.get("foo").last());
    try {
      multimap.get("missing").last();
      fail("Expected NoSuchElementException");
    } catch (NoSuchElementException expected) {}
  }

  public void testComparator() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    assertNull(multimap.get("foo").comparator());
    assertNull(multimap.get("missing").comparator());
  }

  public void testHeadSet() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    Set<Integer> fooSet = multimap.get("foo").headSet(4);
    assertEquals(Sets.newHashSet(1, 3), fooSet);
    Set<Integer> missingSet = multimap.get("missing").headSet(4);
    assertEquals(Sets.newHashSet(), missingSet);

    multimap.put("foo", 0);
    assertEquals(Sets.newHashSet(0, 1, 3), fooSet);

    missingSet.add(2);
    assertEquals(Sets.newHashSet(2), multimap.get("missing"));
  }

  public void testTailSet() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    Set<Integer> fooSet = multimap.get("foo").tailSet(2);
    assertEquals(Sets.newHashSet(3, 7), fooSet);
    Set<Integer> missingSet = multimap.get("missing").tailSet(4);
    assertEquals(Sets.newHashSet(), missingSet);

    multimap.put("foo", 6);
    assertEquals(Sets.newHashSet(3, 6, 7), fooSet);

    missingSet.add(9);
    assertEquals(Sets.newHashSet(9), multimap.get("missing"));
  }

  public void testSubSet() {
    TreeMultimap<String, Integer> multimap = createPopulate();
    Set<Integer> fooSet = multimap.get("foo").subSet(2, 6);
    assertEquals(Sets.newHashSet(3), fooSet);

    multimap.put("foo", 5);
    assertEquals(Sets.newHashSet(3, 5), fooSet);

    fooSet.add(4);
    assertEquals(Sets.newHashSet(1, 3, 4, 5, 7), multimap.get("foo"));
  }

  public void testMultimapConstructor() {
    Multimap<String, Integer> multimap = createSample();
    TreeMultimap<String, Integer> copy = Multimaps.newTreeMultimap(multimap);
    assertEquals(multimap, copy);
  }

  private static final Comparator<Double> KEY_COMPARATOR =
      Comparators.<Double>naturalOrder();

  private static final Comparator<Double> VALUE_COMPARATOR =
      Collections.reverseOrder();

  /**
   * Test that creating one TreeMultimap from another copies the comparators
   * from the source TreeMultimap
   */
  public void testMultimapConstructorPreservesComparators() {
    Multimap<Double, Double> tree = Multimaps.newTreeMultimap(
        KEY_COMPARATOR, VALUE_COMPARATOR);
    tree.put(1.0, 2.0);
    tree.put(2.0, 3.0);
    tree.put(3.0, 4.0);
    tree.put(4.0, 5.0);

    TreeMultimap<Double, Double> copyFromTree = Multimaps.newTreeMultimap(tree);
    assertEquals(tree, copyFromTree);
    assertEquals(KEY_COMPARATOR, copyFromTree.keyComparator());
    assertEquals(VALUE_COMPARATOR, copyFromTree.valueComparator());
  }

  /**
   * Test that creating one TreeMultimap from a non-TreeMultimap
   * results in natural ordering.
   */
  public void testMultimapConstructorFromNonTreeMultimapUsesNaturalOrdering() {
    Multimap<Double, Double> tree = Multimaps.newTreeMultimap(
        KEY_COMPARATOR, VALUE_COMPARATOR);
    tree.put(1.0, 2.0);
    tree.put(2.0, 3.0);
    tree.put(3.0, 4.0);
    tree.put(4.0, 5.0);

    Multimap<Double, Double> hash = Multimaps.newHashMultimap(tree);

    TreeMultimap<Double, Double> copyFromHash = Multimaps.newTreeMultimap(hash);
    assertEquals(hash, copyFromHash);
    assertNull(copyFromHash.keyComparator());
    assertNull(copyFromHash.valueComparator());
  }
}
