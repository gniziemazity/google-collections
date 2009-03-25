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

import static com.google.common.collect.testing.Helpers.assertContentsAnyOrder;
import static com.google.common.testing.junit3.JUnitAsserts.assertContentsInOrder;
import com.google.common.testutils.SerializableTester;

import junit.framework.TestCase;

import java.util.AbstractCollection;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;

/**
 * Tests for {@code Constraints}.
 *
 * @author Mike Bostock
 * @author Jared Levy
 */
public class ConstraintsTest extends TestCase {

  private static final String TEST_ELEMENT = "test";

  private static final class TestElementException
      extends IllegalArgumentException {
    private static final long serialVersionUID = 0;
  }

  private static final Constraint<String> TEST_CONSTRAINT
      = new Constraint<String>() {
          public String checkElement(String element) {
            if (TEST_ELEMENT.equals(element)) {
              throw new TestElementException();
            }
            return element;
          }
        };

  public void testNotNull() {
    Constraint<? super String> constraint = Constraints.notNull();
    assertSame(TEST_ELEMENT, constraint.checkElement(TEST_ELEMENT));
    try {
      constraint.checkElement(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    assertEquals("Not null", constraint.toString());
  }

  public void testConstrainedCollectionLegal() {
    Collection<String> collection = Lists.newArrayList("foo", "bar");
    Collection<String> constrained = Constraints.constrainedCollection(
        collection, TEST_CONSTRAINT);
    collection.add(TEST_ELEMENT);
    constrained.add("qux");
    constrained.addAll(asList("cat", "dog"));
    /* equals and hashCode aren't defined for Collection */
    assertContentsInOrder(
        collection, "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog");
    assertContentsInOrder(
        constrained, "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog");
  }

  public void testConstrainedCollectionIllegal() {
    Collection<String> collection = Lists.newArrayList("foo", "bar");
    Collection<String> constrained = Constraints.constrainedCollection(
        collection, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(collection, "foo", "bar");
  }

  public void testConstrainedSetLegal() {
    Set<String> set = Sets.newLinkedHashSet(asList("foo", "bar"));
    Set<String> constrained = Constraints.constrainedSet(set, TEST_CONSTRAINT);
    set.add(TEST_ELEMENT);
    constrained.add("qux");
    constrained.addAll(asList("cat", "dog"));
    assertTrue(set.equals(constrained));
    assertTrue(constrained.equals(set));
    assertEquals(set.toString(), constrained.toString());
    assertEquals(set.hashCode(), constrained.hashCode());
    assertContentsInOrder(set, "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog");
    assertContentsInOrder(
        constrained, "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog");
  }

  public void testConstrainedSetIllegal() {
    Set<String> set = Sets.newLinkedHashSet(asList("foo", "bar"));
    Set<String> constrained = Constraints.constrainedSet(set, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(set, "foo", "bar");
  }

  public void testConstrainedSortedSetLegal() {
    SortedSet<String> sortedSet = Sets.newTreeSet(asList("foo", "bar"));
    SortedSet<String> constrained = Constraints.constrainedSortedSet(
        sortedSet, TEST_CONSTRAINT);
    sortedSet.add(TEST_ELEMENT);
    constrained.add("qux");
    constrained.addAll(asList("cat", "dog"));
    assertTrue(sortedSet.equals(constrained));
    assertTrue(constrained.equals(sortedSet));
    assertEquals(sortedSet.toString(), constrained.toString());
    assertEquals(sortedSet.hashCode(), constrained.hashCode());
    assertContentsInOrder(
        sortedSet, "bar", "cat", "dog", "foo", "qux", TEST_ELEMENT);
    assertContentsInOrder(
        constrained, "bar", "cat", "dog", "foo", "qux", TEST_ELEMENT);
    assertNull(constrained.comparator());
    assertEquals("bar", constrained.first());
    assertEquals(TEST_ELEMENT, constrained.last());
  }

  public void testConstrainedSortedSetIllegal() {
    SortedSet<String> sortedSet = Sets.newTreeSet(asList("foo", "bar"));
    SortedSet<String> constrained = Constraints.constrainedSortedSet(
        sortedSet, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.subSet("bar", "foo").add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.headSet("bar").add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.tailSet("foo").add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "bar", "foo");
    assertContentsInOrder(sortedSet, "bar", "foo");
  }

  public void testConstrainedListLegal() {
    List<String> list = Lists.newArrayList("foo", "bar");
    List<String> constrained = Constraints.constrainedList(
        list, TEST_CONSTRAINT);
    list.add(TEST_ELEMENT);
    constrained.add("qux");
    constrained.addAll(asList("cat", "dog"));
    constrained.add(1, "cow");
    constrained.addAll(4, asList("box", "fan"));
    constrained.set(2, "baz");
    assertTrue(list.equals(constrained));
    assertTrue(constrained.equals(list));
    assertEquals(list.toString(), constrained.toString());
    assertEquals(list.hashCode(), constrained.hashCode());
    assertContentsInOrder(list, "foo", "cow", "baz", TEST_ELEMENT, "box",
        "fan", "qux", "cat", "dog");
    assertContentsInOrder(constrained, "foo", "cow", "baz", TEST_ELEMENT, "box",
        "fan", "qux", "cat", "dog");
    ListIterator<String> iterator = constrained.listIterator();
    iterator.next();
    iterator.set("sun");
    constrained.listIterator(2).add("sky");
    assertContentsInOrder(list, "sun", "cow", "sky", "baz", TEST_ELEMENT, "box",
        "fan", "qux", "cat", "dog");
    assertContentsInOrder(constrained, "sun", "cow", "sky", "baz", TEST_ELEMENT,
        "box", "fan", "qux", "cat", "dog");
  }

  public void testConstrainedListIllegal() {
    List<String> list = Lists.newArrayList("foo", "bar");
    List<String> constrained = Constraints.constrainedList(
        list, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.listIterator().add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.listIterator(1).add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.listIterator().set(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.listIterator(1).set(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.subList(0, 1).add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.add(1, TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.set(1, TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(1, asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(list, "foo", "bar");
  }

  public void testConstrainedMultisetLegal() {
    Multiset<String> multiset = HashMultiset.create(asList("foo", "bar"));
    Multiset<String> constrained = Constraints.constrainedMultiset(
        multiset, TEST_CONSTRAINT);
    multiset.add(TEST_ELEMENT);
    constrained.add("qux");
    constrained.addAll(asList("cat", "dog"));
    constrained.add("cow", 2);
    assertTrue(multiset.equals(constrained));
    assertTrue(constrained.equals(multiset));
    assertEquals(multiset.toString(), constrained.toString());
    assertEquals(multiset.hashCode(), constrained.hashCode());
    assertContentsAnyOrder(multiset,
        "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog", "cow", "cow");
    assertContentsAnyOrder(constrained,
        "foo", "bar", TEST_ELEMENT, "qux", "cat", "dog", "cow", "cow");
    assertEquals(1, constrained.count("foo"));
    assertEquals(1, constrained.remove("foo", 3));
    assertEquals(2, constrained.setCount("cow", 0));
    assertContentsAnyOrder(multiset,
        "bar", TEST_ELEMENT, "qux", "cat", "dog");
    assertContentsAnyOrder(constrained,
        "bar", TEST_ELEMENT, "qux", "cat", "dog");
  }

  public void testConstrainedMultisetIllegal() {
    Multiset<String> multiset = HashMultiset.create(asList("foo", "bar"));
    Multiset<String> constrained = Constraints.constrainedMultiset(
        multiset, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.add(TEST_ELEMENT, 2);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsAnyOrder(constrained, "foo", "bar");
    assertContentsAnyOrder(multiset, "foo", "bar");
  }

  public void testNefariousAddAll() {
    List<String> list = Lists.newArrayList("foo", "bar");
    List<String> constrained = Constraints.constrainedList(
        list, TEST_CONSTRAINT);
    Collection<String> nefarious = nefariousCollection(TEST_ELEMENT);
    constrained.addAll(nefarious);
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(list, "foo", "bar");
  }

  /**
   * Returns a "nefarious" collection, which initially appears to be empty, but
   * on subsequent iterations contains the single specified element. This
   * verifies that the constrained collection uses a defensive copy.
   *
   * @param element the element to be contained in the nefarious collection
   */
  static <E> Collection<E> nefariousCollection(final E element) {
    return new AbstractCollection<E>() {
        int i = 0;
        @Override public int size() {
          return i;
        }
        @Override public Iterator<E> iterator() {
          return Collections.nCopies(i++, element).iterator(); // muhahaha!
        }
      };
  }

  public void testSerialization() {
    // TODO: Test serialization of constrained collections.
    assertSame(Constraints.notNull(),
        SerializableTester.reserialize(Constraints.notNull()));
  }
}
