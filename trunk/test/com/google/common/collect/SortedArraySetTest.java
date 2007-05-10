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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import junit.framework.TestCase;

/**
 * Unit tests for {@code SortedArraySet}.
 *
 * @author mharris@google.com (Matthew Harris)
 */
public class SortedArraySetTest extends TestCase {
  private static String[] forwardElts_ = new String[] { "a", "b", "c" };
  private static String[] reverseElts_ = new String[] { "c", "b", "a" };

  private static Comparator<String> REVERSE_STRING_COMPARATOR =
      new ReverseStringComparator();

  public void testFactoryWithComparator() throws Exception {
    String[] elements = new String[] { "a", "c", "b" };

    SortedSet<String> s = Sets.newSortedArraySet(REVERSE_STRING_COMPARATOR);
    s.addAll(Arrays.asList(elements));
    checkIteration(reverseElts_, s);

    Comparator<String> nullComparator = null;
    try {
      new SortedArraySet<String>(nullComparator);
    } catch (NullPointerException e) {}
  }

  public void testFactoryWithIterable() throws Exception {
    String[] elements = new String[] { "a", "c", "b" };
    SortedSet<String> s = Sets.newSortedArraySet(elements);
    checkIteration(forwardElts_, s);
  }

  public void testFactoryWithIterableAndComparator() throws Exception {
    String[] elements = new String[] { "a", "c", "b" };

    SortedSet<String> src = new TreeSet<String>(REVERSE_STRING_COMPARATOR);
    src.addAll(Arrays.asList(elements));

    SortedSet<String> s = Sets.newSortedArraySet(src,
                                                 REVERSE_STRING_COMPARATOR);
    checkIteration(reverseElts_, s);
  }

  public void testCopyConstructor() throws Exception {
    SortedSet<String> src = new TreeSet<String>();
    src.addAll(Arrays.asList(reverseElts_));

    SortedSet<String> s = new SortedArraySet<String>(src);
    checkIteration(forwardElts_, s);
  }

  public void testEmptySet() throws Exception {
    SortedSet<String> s = Sets.newSortedArraySet();

    assertTrue(s.isEmpty());
    assertEquals(0, s.size());
    try {
      s.contains(null);
      fail();
    } catch (NullPointerException ignored) {}
    assertFalse(s.contains("a"));
    assertFalse(s.equals(null));
    assertTrue(s.equals(s));
    SortedSet<String> emptySet = Sets.newSortedArraySet();
    assertTrue(s.equals(emptySet));
    assertFalse(s.iterator().hasNext());
    try {
      s.remove(null);
      fail();
    } catch (NullPointerException ignored) {}
    assertFalse(s.remove("a"));
    try {
      s.first();
      fail();
    } catch (NoSuchElementException ignored) {}
    try {
      s.last();
      fail();
    } catch (NoSuchElementException ignored) {}
  }

  public void testModifiedEmptySet() throws Exception {
    SortedSet<String> s = Sets.newSortedArraySet();

    s.add("a");
    s.remove("a");

    try {
      s.first();
      fail();
    } catch (NoSuchElementException ignored) {}
    try {
      s.last();
      fail();
    } catch (NoSuchElementException ignored) {}

    s.clear();
    assertTrue(s.isEmpty());
  }

  public void testSetWithOneElement() throws Exception {
    SortedSet<String> s = Sets.newSortedArraySet();

    try {
      s.add(null);
      fail();
    } catch (NullPointerException ignored) {
    }
    s.add("a");
    assertFalse(s.isEmpty());
    assertEquals(1, s.size());
    assertTrue(s.contains("a"));
    assertFalse(s.add("a"));

    SortedSet<String> s2 = Sets.newSortedArraySet();
    assertFalse(s.equals(s2));
    s2.add("a");
    assertTrue(s.equals(s2));

    assertEquals("a", s.first());
    assertEquals("a", s.last());

    String[] expectedIteration = new String[] { "a" };
    checkIteration(expectedIteration, s);

    assertFalse(s.remove("b"));
    assertTrue(s.remove("a"));
    assertTrue(s.isEmpty());
  }

  public void testSetWithThreeElements() throws Exception {
    SortedSet<String> s = Sets.newSortedArraySet();
    String[] elements = new String[] { "a", "c", "b" };
    for (String e : elements) {
      s.add(e);
    }
    assertFalse(s.isEmpty());
    assertEquals(3, s.size());
    for (String e : elements) {
      assertTrue(s.contains(e));
    }
    assertFalse(s.contains("z"));

    assertTrue(s.equals(new HashSet<String>(Arrays.asList(reverseElts_))));

    SortedSet<String> s2 = Sets.newSortedArraySet();
    s2.add("a");
    assertFalse(s.equals(s2));
    s2.add("c");
    s2.add("b");
    assertTrue(s.equals((Object)s2));
    s2.remove("b");
    s2.add("d");
    assertFalse(s.equals(s2));

    assertEquals("a", s.first());
    assertEquals("c", s.last());

    checkIteration(forwardElts_, s);

    assertTrue(s.remove("b"));
    assertTrue(s.remove("a"));
    assertEquals(1, s.size());
    assertTrue(s.remove("c"));
    assertTrue(s.isEmpty());
  }

  public void testSubset() throws Exception {
    String[] elements = new String[]{ "a", "b", "c", "d", "e", "f", "g" };
    SortedSet<String> set = Sets.newSortedArraySet(Arrays.asList(elements));

    assertEquals(0, set.headSet("a").size());
    assertEquals(0, set.tailSet("h").size());
    assertEquals(0, set.subSet("d", "d").size());

    SortedSet<String> firstThree = set.headSet("d");
    SortedSet<String> lastThree = set.tailSet("e");
    SortedSet<String> middleThree = set.subSet("c", "f");

    assertEquals(3, firstThree.size());
    assertEquals(3, lastThree.size());
    assertEquals(3, middleThree.size());

    checkIteration(subrange(elements, 0, 3), firstThree);
    checkIteration(subrange(elements, 4, 3), lastThree);
    checkIteration(subrange(elements, 2, 3), middleThree);

    assertFalse(middleThree.contains("b"));
    assertTrue(middleThree.contains("c"));
    try {
      middleThree.contains(null);
      fail();
    } catch (NullPointerException e) {}
    assertEquals("c", middleThree.first());
    assertEquals("e", middleThree.last());

    SortedSet<String> emptySubset = set.subSet("e", "e");
    assertEquals(0, emptySubset.size());
    try {
      emptySubset.first();
      fail();
    } catch (NoSuchElementException e) {}
    try {
      emptySubset.last();
      fail();
    } catch (NoSuchElementException e) {}
    try {
      set.subSet("ee", "dd");
      fail();
    } catch (IllegalArgumentException e) {}
    assertEquals("b", set.subSet("aa", "cc").first());
    assertEquals("c", set.subSet("aa", "cc").last());

    assertEquals("a", firstThree.first());
    assertEquals("g", lastThree.last());

    try {
      set.headSet("4").first();
      fail();
    } catch (NoSuchElementException e) {}
    try {
      set.headSet("4").last();
      fail();
    } catch (NoSuchElementException e) {}
    try {
      set.tailSet("j").first();
      fail();
    } catch (NoSuchElementException e) {}
    try {
      set.tailSet("j").last();
      fail();
    } catch (NoSuchElementException e) {}
  }

  public void testSubsetOfSubset() throws Exception {
    String[] elements = new String[]{ "a", "b", "c", "d", "e", "f", "g" };
    SortedSet<String> set = Sets.newSortedArraySet(Arrays.asList(elements));
    SortedSet<String> middleThree = set.subSet("c", "f");
    checkIteration(subrange(elements, 2, 2), middleThree.subSet("c", "e"));
    checkIteration(subrange(elements, 2, 2), middleThree.headSet("dd"));
    checkIteration(subrange(elements, 3, 2), middleThree.tailSet("cd"));

    try {
      middleThree.subSet("n", "j");
      fail();
    } catch (IllegalArgumentException e) {}

    try {
      middleThree.subSet("b", "d");
      fail();
    } catch (IllegalArgumentException e) {}
    try {
      middleThree.subSet("f", "g");
    } catch (IllegalArgumentException e) {}
  }

  public void testClone() {
    SortedArraySet<String> s = new SortedArraySet<String>();
    Collections.addAll(s, "a", "c", "b");
    assertContentsInOrder(s, "a", "b", "c");
    SortedArraySet<String> c = s.clone();
    assertEquals(s, c);
    assertEquals(c, s);
    assertContentsInOrder(c, "a", "b", "c");
    c.add("aa");
    assertFalse(s.equals(c));
    assertFalse(c.equals(s));
    assertContentsInOrder(s, "a", "b", "c");
    assertContentsInOrder(c, "a", "aa", "b", "c");
  }

  public void testEnsureCapacity() {
    /* No real way to test that it worked, other than no exceptions. */
    SortedArraySet<String> s = new SortedArraySet<String>();
    s.ensureCapacity(-10);
    s.ensureCapacity(0);
    s.ensureCapacity(100);
    s.ensureCapacity(0);
    s.ensureCapacity(-10);
  }

  public void testTrimToSize() {
    /* No real way to test that it worked, other than no exceptions. */
    SortedArraySet<String> s = new SortedArraySet<String>();
    s.trimToSize();
    Collections.addAll(s, "foo", "bar", "baz");
    s.trimToSize();
    s.ensureCapacity(100);
    Collections.addAll(s, "a", "b", "c", "d");
    s.ensureCapacity(100);
    s.trimToSize();
    s.ensureCapacity(10);
    s.ensureCapacity(0);
    s.ensureCapacity(-1);
  }

  private String[] subrange(String[] src, int firstPos, int length) {
    String[] result = new String[length];
    System.arraycopy(src, firstPos, result, 0, length);
    return result;
  }

  private void checkIteration(String[] expectedIteration,
                              SortedSet<String> s) {
    int i = 0;
    for (String e : s) {
      assertEquals(expectedIteration[i++], e);
    }
    assertEquals(expectedIteration.length, i);
  }

  private static class ReverseStringComparator implements Comparator<String> {
    public int compare(String s1, String s2) {
      return s2.compareTo(s1);
    }
  }
}
