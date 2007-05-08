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
import com.google.common.base.Join;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.helpers.MoreAsserts;
import com.google.common.collect.helpers.NullPointerTester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import junit.framework.TestCase;

/**
 * Unit test for {@link Iterables}.
 *
 * @author kevinb
 */
public class IterablesTest extends TestCase {

  public void testEmptyIterable() {
    Iterable<String> iterable = Iterables.emptyIterable();
    for (String uhoh : iterable) {
      fail();
    }
  }

  public void testGetLoneItemValid() {
    Iterable<String> iterable = Lists.newArrayList("foo");
    assertEquals("foo", Iterables.getLoneItem(iterable));
  }

  public void testGetLoneItemFromEmpty() {
    Iterable<String> iterable = Collections.emptyList();
    try {
      Iterables.getLoneItem(iterable);
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
  }

  public void testGetLoneItemFromMultiple() {
    Iterable<String> iterable = Lists.newArrayList("foo", "bar");
    try {
      Iterables.getLoneItem(iterable);
      fail("no exception thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testGetOptionalItemForEmptyCollectionYieldsNull() {
    assertNull(Iterables.getOptionalItem(Collections.emptyList()));
  }

  public void testGetOptionalItemForOneSizedCollectionYieldsLoneItem() {
    Object lonelyItem = new Object();
    Collection<Object> collection = Arrays.asList(lonelyItem);
    assertSame(lonelyItem, Iterables.getOptionalItem(collection));
  }

  public void testGetOptionalItemRejectsCollectionsWithMultipleElements() {
    Object first = new Object();
    Object second = new Object();
    Collection<Object> collection = Arrays.asList(first, second);
    try {
      Iterables.getOptionalItem(collection);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("too many elements", e.getMessage());
    }
  }

  public void testNewArrayEmpty() {
    Iterable<String> iterable = Collections.emptyList();
    String[] array = Iterables.newArray(iterable, String.class);
    assertTrue(Arrays.equals(new String[0], array));
  }

  public void testNewArraySingleton() {
    Iterable<String> iterable = Collections.singletonList("a");
    String[] array = Iterables.newArray(iterable, String.class);
    assertTrue(Arrays.equals(new String[] { "a" }, array));
  }

  public void testNewArray() {
    String[] sourceArray = new String[] {"a", "b", "c"};
    Iterable<String> iterable = Arrays.asList(sourceArray);
    String[] newArray = Iterables.newArray(iterable, String.class);
    assertTrue(Arrays.equals(sourceArray, newArray));
  }

  public void testFilter() {
    Iterable<String> unfiltered = Lists.newArrayList("foo", "bar");
    Iterable<String> filtered = Iterables.filter(unfiltered,
                                                 Predicates.isEqualTo("foo"));

    List<String> expected = Collections.singletonList("foo");
    List<String> actual = Lists.newArrayList(filtered);
    assertEquals(expected, actual);
    assertCanIterateAgain(filtered);
  }

  public void testAny() {
    List<String> list = Lists.newArrayList();
    Predicate<String> predicate = Predicates.isEqualTo("pants");

    assertFalse(Iterables.any(list, predicate));
    list.add("cool");
    assertFalse(Iterables.any(list, predicate));
    list.add("pants");
    assertTrue(Iterables.any(list, predicate));
  }

  public void testAll() {
    List<String> list = Lists.newArrayList();
    Predicate<String> predicate = Predicates.isEqualTo("cool");

    assertTrue(Iterables.all(list, predicate));
    list.add("cool");
    assertTrue(Iterables.all(list, predicate));
    list.add("pants");
    assertFalse(Iterables.all(list, predicate));
  }

  public void testFind() {
    Iterable<String> list = Lists.newArrayList("cool", "pants");
    assertEquals("cool", Iterables.find(list, Predicates.isEqualTo("cool")));
    assertEquals("pants", Iterables.find(list, Predicates.isEqualTo("pants")));
    try {
      Iterables.find(list, Predicates.alwaysFalse());
      fail();
    } catch (NoSuchElementException e) {
    }
    assertEquals("cool", Iterables.find(list, Predicates.alwaysTrue()));
    assertCanIterateAgain(list);
  }

  private static class TypeA {}
  private interface TypeB {}
  private static class HasBoth extends TypeA implements TypeB {}

  public void testFilterByType() throws Exception {
    HasBoth hasBoth = new HasBoth();
    Iterable<TypeA> alist = Lists
        .newArrayList(new TypeA(), new TypeA(), hasBoth, new TypeA());
    Iterable<TypeB> blist = Iterables.filter(alist, TypeB.class);
    MoreAsserts.assertContentsInOrder(blist, hasBoth);
  }

  public void testTransform() {
    List<String> input = Arrays.asList("1", "2", "3");
    Iterable<Integer> result = Iterables.transform(input,
        new Function<String, Integer>() {
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    List<Integer> actual = Lists.newArrayList(result);
    List<Integer> expected = Arrays.asList(1, 2, 3);
    assertEquals(expected, actual);
    assertCanIterateAgain(result);
  }

  public void testPoorlyBehavedTransform() {
    List<String> input = Arrays.asList("1", null, "3");
    Iterable<Integer> result = Iterables.transform(input,
        new Function<String, Integer>() {
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    Iterator<Integer> resultIterator = result.iterator();
    resultIterator.next();

    try {
      resultIterator.next();
      fail("Expected NFE");
    } catch (NumberFormatException nfe) {
      // Expected to fail.
    }
  }

  public void testNullFriendlyTransform() {
    List<Integer> input = Arrays.asList(1, 2, null, 3);
    Iterable<String> result = Iterables.transform(input,
        new Function<Integer, String>() {
          public String apply(Integer from) {
            return String.valueOf(from);
          }
        });

    List<String> actual = Lists.newArrayList(result);
    List<String> expected = Arrays.asList("1", "2", "null", "3");
    assertEquals(expected, actual);
  }

  // Far less exhaustive than the tests in IteratorsTest
  public void testCycle() {
    Iterable<String> cycle = Iterables.cycle("a", "b");

    int howManyChecked = 0;
    for (String string : cycle) {
      String expected = (howManyChecked % 2 == 0) ? "a" : "b";
      assertEquals(expected, string);
      if (howManyChecked++ == 5) {
        break;
      }
    }

    // We left the last iterator pointing to "b". But a new iterator should
    // always point to "a".
    for (String string : cycle) {
      assertEquals("a", string);
      break;
    }
  }

  // Again, the exhaustive tests are in IteratorsTest
  public void testConcat() {
    List<Integer> list1 = Lists.newArrayList(1);
    List<Integer> list2 = Lists.newArrayList(4);

    List<List<Integer>> input = Lists.newArrayList(list1, list2);

    Iterable<Integer> result = Iterables.concat(input);
    assertEquals(Arrays.asList(1, 4), Lists.newArrayList(result));

    // Now change the inputs and see result dynamically change as well

    list1.add(2);
    List<Integer> list3 = Lists.newArrayList(3);
    input.add(1, list3);

    assertEquals(Arrays.asList(1, 2, 3, 4), Lists.newArrayList(result));
  }

  // More tests in IteratorsTest
  public void testAddAllToList() {
    List<String> alreadyThere = Lists.newArrayList("already", "there");
    List<String> freshlyAdded = Lists.newArrayList("freshly", "added");
    
    boolean changed = Iterables.addAll(alreadyThere, freshlyAdded);
    
    String expected = "already there freshly added";
    assertEquals(expected, Join.join(" ", alreadyThere));
    assertTrue(changed);
  }
  
  private static void assertCanIterateAgain(Iterable<?> iterable) {
    for (Object obj : iterable) {
      ;
    }
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Iterables.class);
  }

  // More exhaustive tests are in IteratorsTest.
  public void testElementsEqual() throws Exception {
    Iterable<?> a;
    Iterable<?> b;

    // A few elements.
    a = Arrays.asList(4, 8, 15, 16, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertTrue(Iterables.elementsEqual(a, b));

    // An element differs.
    a = Arrays.asList(4, 8, 15, 12, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterables.elementsEqual(a, b));

    // null versus non-null.
    a = Arrays.asList(4, 8, 15, null, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterables.elementsEqual(a, b));
    assertFalse(Iterables.elementsEqual(b, a));

    // Different lengths.
    a = Arrays.asList(4, 8, 15, 16, 23);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterables.elementsEqual(a, b));
    assertFalse(Iterables.elementsEqual(b, a));
  }

  public void testRotateNullList() {
    try {
      Iterables.rotate(null, 33);
      fail("should have thrown an exception");
    } catch (NullPointerException expected) {
    }
  }

  public void testRotateEmptyAndSingletonList() {
    final Iterator<String> returnMe = new Iterator() {
      public boolean hasNext() {
        throw new UnsupportedOperationException();
      }

      public Object next() {
        throw new UnsupportedOperationException();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };

    List<String> nonIterableList = new ArrayList<String>() {
      @Override
      public Iterator<String> iterator() {
        return returnMe;
      }
    };

    /*
     * Prove that our optimization works with an empty list.
     * We're asserting that we get back a specific Iterator because
     * we know the iterator() method is only called on the list
     * in cases where we don't actually need to do any rotating.
     * This is very much a white-box test.
     */
    assertSame(returnMe, Iterables.rotate(nonIterableList, 0).iterator());
    assertSame(returnMe, Iterables.rotate(nonIterableList, -33).iterator());
    assertSame(returnMe, Iterables.rotate(nonIterableList, 33).iterator());

    nonIterableList.add("yam");

    // prove that our optimization works with a singleton list
    assertSame(returnMe, Iterables.rotate(nonIterableList, 0).iterator());
    assertSame(returnMe, Iterables.rotate(nonIterableList, -33).iterator());
    assertSame(returnMe, Iterables.rotate(nonIterableList, 33).iterator());
  }

  private void testRotations(List<String> list, String[][] rotationResults) {
    /*
     * This is supposed to be a read-only operation.  Performing it on an
     * unmodifiable list will show that it is.
     */
    List<String> unmodifiableList = Collections.unmodifiableList(list);
    /*
     * Establish the range of rotation distances by multiplying the size of the
     * list by 4.  We're going to rotate by distances ranging from the negation
     * of that result to the result itself.
     */
    int endDistance = unmodifiableList.size() * 4;
    int startDistance = -endDistance;
    int listSize = unmodifiableList.size();
    for(int distance = startDistance; distance <= endDistance; distance++) {
      Iterable<String> rotated = Iterables.rotate(unmodifiableList, distance);
      // determine the index of the results we'll be comparing against
      int rotationResultIndex = (endDistance - distance) % listSize;
      int index = 0;
      for(String str : rotated) {
        assertEquals(rotationResults[rotationResultIndex][index++], str);
      }
    }
  }

  public void testRotateTwoElementList() {
    List<String> list = Lists.newArrayList("yam", "bam");
    String[][] rotations = {
        {"yam", "bam"},
        {"bam", "yam"}
    };
    testRotations(list, rotations);
  }

  public void testRotateThreeElementList() {
    List<String> list = Lists.newArrayList("yam", "bam", "jam");
    String[][] rotations = {
        {"yam", "bam", "jam"},
        {"jam", "yam", "bam"},
        {"bam", "jam", "yam"}
    };
    testRotations(list, rotations);
  }

  public void testRotateFourElementList() {
    List<String> list = Lists.newArrayList("yam", "bam", "jam", "ham");
    String[][] rotations = {
        {"yam", "bam", "jam", "ham"},
        {"ham", "yam", "bam", "jam"},
        {"jam", "ham", "yam", "bam"},
        {"bam", "jam", "ham", "yam"},
    };
    testRotations(list, rotations);
  }

 public void testPartition() {
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
    Iterator<Iterable<Integer>> iter, iter2;
    Iterator<Integer> innerIter1, innerIter2;

    // test without padding
    iter = Iterables.partition(list, 3, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3);
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5);
    assertFalse(iter.hasNext());

    // test with padding
    iter = Iterables.partition(list, 3, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3);
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5, null);
    assertFalse(iter.hasNext());

    // test without examining first row
    iter = Iterables.partition(list, 3, false).iterator();
    iter.next();
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5);
    assertFalse(iter.hasNext());

    // test row size == list size
    iter = Iterables.partition(list, 5, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size == list size with padding
    iter = Iterables.partition(list, 5, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size longer than list
    iter = Iterables.partition(list, 6, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size longer than list with padding
    iter = Iterables.partition(list, 6, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3, 4, 5, null);
    assertFalse(iter.hasNext());

    // test partition size 1
    iter = Iterables.partition(list, 1, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1);
    MoreAsserts.assertContentsInOrder(iter.next(), 2);
    MoreAsserts.assertContentsInOrder(iter.next(), 3);
    MoreAsserts.assertContentsInOrder(iter.next(), 4);
    MoreAsserts.assertContentsInOrder(iter.next(), 5);
    assertFalse(iter.hasNext());

    // test partition size 1 with padding
    iter = Iterables.partition(list, 1, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1);
    MoreAsserts.assertContentsInOrder(iter.next(), 2);
    MoreAsserts.assertContentsInOrder(iter.next(), 3);
    MoreAsserts.assertContentsInOrder(iter.next(), 4);
    MoreAsserts.assertContentsInOrder(iter.next(), 5);
    assertFalse(iter.hasNext());

    // test grabbing multiple iterators from the iterable
    Iterable<Iterable<Integer>> iterable = Iterables.partition(list, 3, false);
    iter = iterable.iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, 3);
    iter2 = iterable.iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5);
    assertFalse(iter.hasNext());
    MoreAsserts.assertContentsInOrder(iter2.next(), 1, 2, 3);
    MoreAsserts.assertContentsInOrder(iter2.next(), 4, 5);
    assertFalse(iter2.hasNext());

    // test grabbing multiple iterators from the inner iterable
    iter = Iterables.partition(list, 3, false).iterator();

    Iterable<Integer> innerIterable1 = iter.next();
    MoreAsserts.assertContentsInOrder(innerIterable1, 1, 2, 3);
    MoreAsserts.assertContentsInOrder(innerIterable1, 1, 2, 3);

    Iterable<Integer> innerIterable2 = iter.next();
    MoreAsserts.assertContentsInOrder(innerIterable2, 4, 5);
    MoreAsserts.assertContentsInOrder(innerIterable2, 4, 5);

    // test list size = multiple of partition size
    list = Arrays.asList(1, 2, 3, 4);
    iter = Iterables.partition(list, 2, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2);
    MoreAsserts.assertContentsInOrder(iter.next(), 3, 4);
    assertFalse(iter.hasNext());

    // test list size = multiple of partition size with padding
    iter = Iterables.partition(list, 2, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2);
    MoreAsserts.assertContentsInOrder(iter.next(), 3, 4);
    assertFalse(iter.hasNext());

    // test empty list
    list = Lists.newArrayList();
    iter = Iterables.partition(list, 1, false).iterator();
    assertFalse(iter.hasNext());

    // test empty list with padding
    list = Lists.newArrayList();
    iter = Iterables.partition(list, 1, true).iterator();
    assertFalse(iter.hasNext());

    // test with a null in the list
    list = Arrays.asList(1, 2, null, 4, 5);
    iter = Iterables.partition(list, 3, false).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, null);
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5);
    assertFalse(iter.hasNext());

    // test with a null in the list with padding
    list = Arrays.asList(1, 2, null, 4, 5);
    iter = Iterables.partition(list, 3, true).iterator();
    MoreAsserts.assertContentsInOrder(iter.next(), 1, 2, null);
    MoreAsserts.assertContentsInOrder(iter.next(), 4, 5, null);
    assertFalse(iter.hasNext());
  }

  public void testReversePassesIteratorsTester() throws Exception {
    new IteratorTester(7) {
      protected Iterator<?> newReferenceIterator() {
        return Arrays.asList(2, 4, 6, 8).iterator();
      }
      protected Iterator<?> newTargetIterator() {
        return Iterables.reverse(Arrays.asList(8, 6, 4, 2)).iterator();
      }
    }.test();
  }

  public void testReverseWorksAsExpected() {
    String[] testStrs = new String[] {"foo", "bar", "baz"};
    Object[] expected = new Object[] {"baz", "bar", "foo"};

    List<String> stuff = Lists.immutableList(testStrs);

    Iterable<String> reversed = Iterables.reverse(stuff);
    MoreAsserts.assertContentsInOrder(reversed, expected);

    List<String> removable = Lists.newArrayList("foo", "bar", "bad", "baz");

    reversed = Iterables.reverse(removable);
    MoreAsserts.assertContentsInOrder(reversed, "baz", "bad", "bar", "foo");

    Iterator<String> reverseIter = reversed.iterator();
    assertEquals("baz", reverseIter.next());
    assertEquals("bad", reverseIter.next());
    reverseIter.remove();

    MoreAsserts.assertContentsInOrder(reversed, expected);
    MoreAsserts.assertContentsInOrder(reversed, expected);
  }

  public void testToString() {
    List<String> list = Collections.emptyList();
    assertEquals("[]", Iterables.toString(list));

    list = Lists.newArrayList("yam", "bam", "jam", "ham");
    assertEquals("[yam, bam, jam, ham]", Iterables.toString(list));     
  }
}
