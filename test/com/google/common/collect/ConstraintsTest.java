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

import static com.google.common.collect.helpers.MoreAsserts.assertContentsAnyOrder;
import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import junit.framework.TestCase;

/**
 * Tests for {@link Constraints}.
 *
 * @author mbostock@google.com (Mike Bostock)
 */
public class ConstraintsTest extends TestCase {

  private static final String TEST_ELEMENT = "test";

  private static final class TestElementException
      extends IllegalArgumentException {}

  private static final Constraint<String> TEST_CONSTRAINT
      = new Constraint<String>() {
          public void checkElement(String element) {
            if (TEST_ELEMENT.equals(element)) {
              throw new TestElementException();
            }
          }
        };

  public void testNotNull() {
    Constraint<? super String> constraint = Constraints.NOT_NULL;
    constraint.checkElement("foo");
    try {
      constraint.checkElement(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
  }

  public void testConstrainedCollectionLegal() {
    Collection<String> collection = Lists.newArrayList("foo", "bar");
    Collection<String> constrained = Constraints.constrainedCollection(
        collection, TEST_CONSTRAINT);
    collection.add(TEST_ELEMENT);
    constrained.add("qux");
    /* equals and hashCode aren't defined for Collection */
    assertContentsInOrder(collection, "foo", "bar", TEST_ELEMENT, "qux");
    assertContentsInOrder(constrained, "foo", "bar", TEST_ELEMENT, "qux");
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
      constrained.addAll(Arrays.asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(collection, "foo", "bar");
  }

  public void testConstrainedSetLegal() {
    Set<String> set = Sets.newLinkedHashSet("foo", "bar");
    Set<String> constrained = Constraints.constrainedSet(set, TEST_CONSTRAINT);
    set.add(TEST_ELEMENT);
    constrained.add("qux");
    assertTrue(set.equals(constrained));
    assertTrue(constrained.equals(set));
    assertEquals(set.toString(), constrained.toString());
    assertEquals(set.hashCode(), constrained.hashCode());
    assertContentsInOrder(set, "foo", "bar", TEST_ELEMENT, "qux");
    assertContentsInOrder(constrained, "foo", "bar", TEST_ELEMENT, "qux");
  }

  public void testConstrainedSetIllegal() {
    Set<String> set = Sets.newLinkedHashSet("foo", "bar");
    Set<String> constrained = Constraints.constrainedSet(set, TEST_CONSTRAINT);
    try {
      constrained.add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(Arrays.asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(set, "foo", "bar");
  }

  public void testConstrainedSortedSetLegal() {
    SortedSet<String> sortedSet = Sets.newTreeSet("foo", "bar");
    SortedSet<String> constrained = Constraints.constrainedSortedSet(
        sortedSet, TEST_CONSTRAINT);
    sortedSet.add(TEST_ELEMENT);
    constrained.add("qux");
    assertTrue(sortedSet.equals(constrained));
    assertTrue(constrained.equals(sortedSet));
    assertEquals(sortedSet.toString(), constrained.toString());
    assertEquals(sortedSet.hashCode(), constrained.hashCode());
    assertContentsInOrder(sortedSet, "bar", "foo", "qux", TEST_ELEMENT);
    assertContentsInOrder(constrained, "bar", "foo", "qux", TEST_ELEMENT);
  }

  public void testConstrainedSortedSetIllegal() {
    SortedSet<String> sortedSet = Sets.newTreeSet("foo", "bar");
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
      constrained.addAll(Arrays.asList("baz", TEST_ELEMENT));
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
    assertTrue(list.equals(constrained));
    assertTrue(constrained.equals(list));
    assertEquals(list.toString(), constrained.toString());
    assertEquals(list.hashCode(), constrained.hashCode());
    assertContentsInOrder(list, "foo", "bar", TEST_ELEMENT, "qux");
    assertContentsInOrder(constrained, "foo", "bar", TEST_ELEMENT, "qux");
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
      constrained.subList(0, 1).add(TEST_ELEMENT);
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    try {
      constrained.addAll(Arrays.asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsInOrder(constrained, "foo", "bar");
    assertContentsInOrder(list, "foo", "bar");
  }

  public void testConstrainedMultisetLegal() {
    Multiset<String> multiset = Multisets.newHashMultiset("foo", "bar");
    Multiset<String> constrained = Constraints.constrainedMultiset(
        multiset, TEST_CONSTRAINT);
    multiset.add(TEST_ELEMENT);
    constrained.add("qux");
    assertTrue(multiset.equals(constrained));
    assertTrue(constrained.equals(multiset));
    assertEquals(multiset.toString(), constrained.toString());
    assertEquals(multiset.hashCode(), constrained.hashCode());
    assertContentsAnyOrder(multiset, "foo", "bar", TEST_ELEMENT, "qux");
    assertContentsAnyOrder(constrained, "foo", "bar", TEST_ELEMENT, "qux");
  }

  public void testConstrainedMultisetIllegal() {
    Multiset<String> multiset = Multisets.newHashMultiset("foo", "bar");
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
      constrained.addAll(Arrays.asList("baz", TEST_ELEMENT));
      fail("TestElementException expected");
    } catch (TestElementException expected) {}
    assertContentsAnyOrder(constrained, "foo", "bar");
    assertContentsAnyOrder(multiset, "foo", "bar");
  }

  public void testClassConstraint() {
    Constraint<Object> constraint = Constraints.classConstraint(String.class);
    constraint.checkElement("foo");
    try {
      constraint.checkElement(null);
      fail("NullPointerException expected");
    } catch (NullPointerException expected) {}
    try {
      constraint.checkElement(new Object());
      fail("ClassCastException expected");
    } catch (ClassCastException expected) {}
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
}
