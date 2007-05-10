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
import static com.google.common.collect.Lists.newArrayList;
import com.google.common.collect.helpers.MoreAsserts;
import com.google.common.collect.helpers.NullPointerTester;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;
import junit.framework.TestCase;

/**
 * Unit test for {@code Iterators}.
 *
 * @author kevinb
 */
public class IteratorsTest extends TestCase {

  public void testEmptyIterator() {
    Iterator<String> iterator = Iterators.emptyIterator();
    assertFalse(iterator.hasNext());
    try {
      iterator.next();
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
  }

  public void testGetLoneItemValid() {
    Iterator<String> iterator = Lists.newArrayList("foo").iterator();
    assertEquals("foo", Iterators.getLoneItem(iterator));
  }

  public void testGetLoneItemFromEmpty() {
    Iterator<String> iterator = Collections.<String>emptyList().iterator();
    try {
      Iterators.getLoneItem(iterator);
      fail("no exception thrown");
    } catch (NoSuchElementException expected) {
    }
  }

  public void testGetLoneItemFromMultiple() {
    Iterator<String> iterator = Lists.newArrayList("foo", "bar").iterator();
    try {
      Iterators.getLoneItem(iterator);
      fail("no exception thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testOptionalItemValid() {
    Iterator<String> iterator = Lists.newArrayList("foo").iterator();
    assertEquals("foo", Iterators.getOptionalItem(iterator));
  }

  public void testOptionalItemFromMultiple() {
    Iterator<String> iterator = Lists.newArrayList("foo", "bar").iterator();
    try {
      Iterators.getOptionalItem(iterator);
      fail("no exception thrown");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testGetOptionalItemFromEmtpy() {
    Iterator<String> iterator = Collections.<String>emptyList().iterator();
    assertNull(Iterators.getOptionalItem(iterator));
  }

  public void testNewArrayEmpty() {
    Iterator<String> iterator = Collections.<String>emptyList().iterator();
    String[] array = Iterators.newArray(iterator, String.class);
    assertTrue(Arrays.equals(new String[0], array));
  }

  public void testNewArraySingleton() {
    Iterator<String> iterator = Collections.singletonList("a").iterator();
    String[] array = Iterators.newArray(iterator, String.class);
    assertTrue(Arrays.equals(new String[] { "a" }, array));
  }

  public void testNewArray() {
    String[] sourceArray = new String[] {"a", "b", "c"};
    Iterator<String> iterator = Arrays.asList(sourceArray).iterator();
    String[] newArray = Iterators.newArray(iterator, String.class);
    assertTrue(Arrays.equals(sourceArray, newArray));
  }

  public void testFilterSimple() {
    Iterator<String> unfiltered = Lists.newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
                                                 Predicates.isEqualTo("foo"));
    List<String> expected = Collections.singletonList("foo");
    List<String> actual = Lists.newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterNoMatch() {
    Iterator<String> unfiltered = Lists.newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
                                                 Predicates.alwaysFalse());
    List<String> expected = Collections.emptyList();
    List<String> actual = Lists.newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterMatchAll() {
    Iterator<String> unfiltered = Lists.newArrayList("foo", "bar").iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
                                                 Predicates.alwaysTrue());
    List<String> expected = Lists.newArrayList("foo", "bar");
    List<String> actual = Lists.newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterNothing() {
    Iterator<String> unfiltered = Collections.<String>emptyList().iterator();
    Iterator<String> filtered = Iterators.filter(unfiltered,
        new Predicate<String>() {
          public boolean apply(String s) {
            fail("Should never be evaluated");
            return false;
          }
        });

    List<String> expected = Collections.emptyList();
    List<String> actual = Lists.newArrayList(filtered);
    assertEquals(expected, actual);
  }

  public void testFilterUsingIteratorTester() throws Exception {
    final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
    final Predicate<Integer> isEven = new Predicate<Integer>() {
      public boolean apply(Integer integer) {
        return integer % 2 == 0;
      }
    };
    new IteratorTester(7) {
      protected Iterator<?> newReferenceIterator() {
        return Collections.unmodifiableList(Arrays.asList(2, 4)).iterator();
      }
      protected Iterator<?> newTargetIterator() {
        return Iterators.filter(list.iterator(), isEven);
      }
    }.test();
  }

  public void testAny() {
    List<String> list = Lists.newArrayList();
    Predicate<String> predicate = Predicates.isEqualTo("pants");

    assertFalse(Iterators.any(list.iterator(), predicate));
    list.add("cool");
    assertFalse(Iterators.any(list.iterator(), predicate));
    list.add("pants");
    assertTrue(Iterators.any(list.iterator(), predicate));
  }

  public void testAll() {
    List<String> list = Lists.newArrayList();
    Predicate<String> predicate = Predicates.isEqualTo("cool");

    assertTrue(Iterators.all(list.iterator(), predicate));
    list.add("cool");
    assertTrue(Iterators.all(list.iterator(), predicate));
    list.add("pants");
    assertFalse(Iterators.all(list.iterator(), predicate));
  }

  public void testFind() {
    Iterable<String> list = Lists.newArrayList("cool", "pants");
    assertEquals("cool",
        Iterators.find(list.iterator(), Predicates.isEqualTo("cool")));
    assertEquals("pants",
        Iterators.find(list.iterator(), Predicates.isEqualTo("pants")));
    try {
      Iterators.find(list.iterator(), Predicates.alwaysFalse());
      fail();
    } catch (NoSuchElementException e) {
    }
    assertEquals("cool",
        Iterators.find(list.iterator(), Predicates.alwaysTrue()));
  }

  public void testTransform() {
    Iterator<String> input = Arrays.asList("1", "2", "3").iterator();
    Iterator<Integer> result = Iterators.transform(input,
        new Function<String, Integer>() {
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    List<Integer> actual = Lists.newArrayList(result);
    List<Integer> expected = Arrays.asList(1, 2, 3);
    assertEquals(expected, actual);
  }

  public void testPoorlyBehavedTransform() {
    Iterator<String> input = Arrays.asList("1", null, "3").iterator();
    Iterator<Integer> result = Iterators.transform(input,
        new Function<String, Integer>() {
          public Integer apply(String from) {
            return Integer.valueOf(from);
          }
        });

    result.next();
    try {
      result.next();
      fail("Expected NFE");
    } catch (NumberFormatException nfe) {
      // Expected to fail.
    }
  }

  public void testNullFriendlyTransform() {
    Iterator<Integer> input = Arrays.asList(1, 2, null, 3).iterator();
    Iterator<String> result = Iterators.transform(input,
        new Function<Integer, String>() {
          public String apply(Integer from) {
            return String.valueOf(from);
          }
        });

    List<String> actual = Lists.newArrayList(result);
    List<String> expected = Arrays.asList("1", "2", "null", "3");
    assertEquals(expected, actual);
  }

  public void testCycleOfEmpty() {
    Iterator<String> cycle = Iterators.cycle();
    assertFalse(cycle.hasNext());
  }

  public void testCycleOfOne() {
    Iterator<String> cycle = Iterators.cycle("a");
    for (int i = 0; i < 3; i++) {
      assertTrue(cycle.hasNext());
      assertEquals("a", cycle.next());
    }
  }

  public void testCycleOfOneWithRemove() {
    Iterable<String> iterable = Lists.newArrayList("a");
    Iterator<String> cycle = Iterators.cycle(iterable);
    assertTrue(cycle.hasNext());
    assertEquals("a", cycle.next());
    cycle.remove();
    assertEquals(Collections.emptyList(), iterable);
    assertFalse(cycle.hasNext());
  }

  public void testCycleOfTwo() {
    Iterator<String> cycle = Iterators.cycle("a", "b");
    for (int i = 0; i < 3; i++) {
      assertTrue(cycle.hasNext());
      assertEquals("a", cycle.next());
      assertTrue(cycle.hasNext());
      assertEquals("b", cycle.next());
    }
  }

  public void testCycleOfTwoWithRemove() {
    Iterable<String> iterable = Lists.newArrayList("a", "b");
    Iterator<String> cycle = Iterators.cycle(iterable);
    assertTrue(cycle.hasNext());
    assertEquals("a", cycle.next());
    assertTrue(cycle.hasNext());
    assertEquals("b", cycle.next());
    assertTrue(cycle.hasNext());
    assertEquals("a", cycle.next());
    cycle.remove();
    assertEquals(Collections.singletonList("b"), iterable);
    assertTrue(cycle.hasNext());
    assertEquals("b", cycle.next());
    assertTrue(cycle.hasNext());
    assertEquals("b", cycle.next());
    cycle.remove();
    assertEquals(Collections.emptyList(), iterable);
    assertFalse(cycle.hasNext());
  }

  public void testCycleRemoveWithoutNext() {
    Iterator<String> cycle = Iterators.cycle("a", "b");
    assertTrue(cycle.hasNext());
    try {
      cycle.remove();
      fail("no exception thrown");
    } catch (IllegalStateException expected) {
    }
  }

  public void testCycleRemoveSameElementTwice() {
    Iterator<String> cycle = Iterators.cycle("a", "b");
    cycle.next();
    cycle.remove();
    try {
      cycle.remove();
      fail("no exception thrown");
    } catch (IllegalStateException expected) {
    }
  }

  public void testCycleWhenRemoveIsNotSupported() {
    Iterable<String> iterable = Arrays.asList("a", "b");
    Iterator<String> cycle = Iterators.cycle(iterable);
    cycle.next();
    try {
      cycle.remove();
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testCycleRemoveAfterHasNext() {
    Iterable<String> iterable = Lists.newArrayList("a");
    Iterator<String> cycle = Iterators.cycle(iterable);
    assertTrue(cycle.hasNext());
    assertEquals("a", cycle.next());
    assertTrue(cycle.hasNext());
    cycle.remove();
    assertEquals(Collections.emptyList(), iterable);
    assertFalse(cycle.hasNext());
  }

  public void testCycleUsingIteratorTester() throws Exception {
    new IteratorTester(7) {
      protected Iterator<?> newReferenceIterator() {
        return Arrays.asList(1, 2, 1, 2, 1, 2, 1).iterator();
      }
      protected Iterator<?> newTargetIterator() {
        return Iterators.cycle(Arrays.asList(1, 2));
      }
    }.test();
  }

  public void testConcatNoIteratorsYieldsEmpty() throws Exception {
    new EmptyIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat();
      }
    }.test();
  }

  public void testConcatOneEmptyIteratorYieldsEmpty() throws Exception {
    new EmptyIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(iterateOver());
      }
    }.test();
  }

  public void testConcatMultipleEmptyIteratorsYieldsEmpty() throws Exception {
    new EmptyIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(iterateOver(), iterateOver());
      }
    }.test();
  }

  public void testConcatSingletonYieldsSingleton() throws Exception {
    new SingletonIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(iterateOver(1));
      }
    }.test();
  }

  public void testConcatEmptyAndSingletonAndEmptyYieldsSingleton()
      throws Exception {
    new SingletonIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(iterateOver(), iterateOver(1), iterateOver());
      }
    }.test();
  }

  public void testConcatSingletonAndSingletonYieldsDoubleton() throws Exception
  {
    new DoubletonIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(iterateOver(1), iterateOver(2));
      }
    }.test();
  }

  public void testConcatSingletonAndSingletonWithEmptiesYieldsDoubleton()
      throws Exception {
    new DoubletonIteratorTester() {
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(
            iterateOver(1), iterateOver(), iterateOver(), iterateOver(2));
      }
    }.test();
  }

  public void testConcatUnmodifiable() throws Exception {
    new IteratorTester(7) {
      protected Iterator<?> newReferenceIterator() {
        return Arrays.asList(1, 2).iterator();
      }
      protected Iterator<?> newTargetIterator() {
        return Iterators.concat(Arrays.asList(1).iterator(),
            Arrays.asList().iterator(), Arrays.asList(2).iterator());
      }
    }.test();
  }

  /**
   * Illustrates the somewhat bizarre behavior when a null is passed in.
   */
  public void testConcatContainingNull() throws Exception {
    Iterator<Iterator<Integer>> input
        = Arrays.asList(iterateOver(1, 2), null, iterateOver(3)).iterator();
    Iterator<Integer> result = Iterators.concat(input);
    assertEquals(1, (int) result.next());
    assertEquals(2, (int) result.next());
    try {
      result.hasNext();
      fail("no exception thrown");
    } catch (NullPointerException e) {
    }
    try {
      result.next();
      fail("no exception thrown");
    } catch (NullPointerException e) {
    }
    // There is no way to get "through" to the 3.  Buh-bye
  }

  public void testAddAllWithEmptyIterator() {
    List<String> alreadyThere = Lists.newArrayList("already", "there");

    boolean changed = Iterators.addAll(alreadyThere,
                                       Iterators.<String>emptyIterator());
    assertEquals("already there", Join.join(" ", alreadyThere));
    assertFalse(changed);
  }

  public void testAddAllToList() {
    List<String> alreadyThere = Lists.newArrayList("already", "there");
    List<String> freshlyAdded = Lists.newArrayList("freshly", "added");

    boolean changed = Iterators.addAll(alreadyThere, freshlyAdded.iterator());

    String expected = "already there freshly added";
    assertEquals(expected, Join.join(" ", alreadyThere));
    assertTrue(changed);
  }

  public void testAddAllToSet() {
    Set<String> alreadyThere = Sets.newLinkedHashSet("already", "there");
    List<String> oneMore = Lists.newArrayList("one", "more");

    boolean changed = Iterators.addAll(alreadyThere, oneMore.iterator());

    assertEquals("already there one more", Join.join(" ", alreadyThere));
    assertTrue(changed);

    changed = Iterators.addAll(alreadyThere, oneMore.iterator());

    assertEquals("already there one more", Join.join(" ", alreadyThere));
    assertFalse(changed);
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Iterators.class);
  }

  private static abstract class EmptyIteratorTester extends IteratorTester {
    protected EmptyIteratorTester() {
      super(5);
    }
    protected Iterator<?> newReferenceIterator() {
      return iterateOver();
    }
  }

  private static abstract class SingletonIteratorTester extends IteratorTester {
    protected SingletonIteratorTester() {
      super(6);
    }
    protected Iterator<?> newReferenceIterator() {
      return iterateOver(1);
    }
  }

  private static abstract class DoubletonIteratorTester extends IteratorTester {
    protected DoubletonIteratorTester() {
      super(7);
    }
    protected Iterator<?> newReferenceIterator() {
      return iterateOver(1, 2);
    }
  }

  private static Iterator<Integer> iterateOver(final Integer... values) {
    return newArrayList(values).iterator();
  }

  public void testElementsEqual() throws Exception {
    Iterable<?> a;
    Iterable<?> b;

    // Base case.
    a = Lists.newArrayList();
    b = Collections.emptySet();
    assertTrue(Iterators.elementsEqual(a.iterator(), b.iterator()));

    // A few elements.
    a = Arrays.asList(4, 8, 15, 16, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertTrue(Iterators.elementsEqual(a.iterator(), b.iterator()));

    // The same, but with nulls.
    a = Arrays.asList(4, 8, null, 16, 23, 42);
    b = Arrays.asList(4, 8, null, 16, 23, 42);
    assertTrue(Iterators.elementsEqual(a.iterator(), b.iterator()));

    // Different Iterable types (still equal elements, though).
    a = Lists.immutableList(4, 8, 15, 16, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertTrue(Iterators.elementsEqual(a.iterator(), b.iterator()));

    // An element differs.
    a = Arrays.asList(4, 8, 15, 12, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterators.elementsEqual(a.iterator(), b.iterator()));

    // null versus non-null.
    a = Arrays.asList(4, 8, 15, null, 23, 42);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterators.elementsEqual(a.iterator(), b.iterator()));
    assertFalse(Iterators.elementsEqual(b.iterator(), a.iterator()));

    // Different lengths.
    a = Arrays.asList(4, 8, 15, 16, 23);
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterators.elementsEqual(a.iterator(), b.iterator()));
    assertFalse(Iterators.elementsEqual(b.iterator(), a.iterator()));

    // Different lengths, one is empty.
    a = Collections.emptySet();
    b = Arrays.asList(4, 8, 15, 16, 23, 42);
    assertFalse(Iterators.elementsEqual(a.iterator(), b.iterator()));
    assertFalse(Iterators.elementsEqual(b.iterator(), a.iterator()));
  }

  public void testPartition() {
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
    Iterator<Iterator<Integer>> iter;

    // test without padding
    iter = Iterators.partition(list.iterator(), 3, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 4, 5);
    assertFalse(iter.hasNext());

    // test with padding
    iter = Iterators.partition(list.iterator(), 3, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      4, 5, null);
    assertFalse(iter.hasNext());

    // test without iterating through the first row
    iter = Iterators.partition(list.iterator(), 3, false);
    iter.next();
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 4, 5);
    assertFalse(iter.hasNext());

    // test row size == list size
    iter = Iterators.partition(list.iterator(), 5, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size == list size with padding
    iter = Iterators.partition(list.iterator(), 5, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size longer than list
    iter = Iterators.partition(list.iterator(), 6, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3, 4, 5);
    assertFalse(iter.hasNext());

    // test row size longer than list with padding
    iter = Iterators.partition(list.iterator(), 6, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, 3, 4, 5, null);
    assertFalse(iter.hasNext());

    // test partition size 1
    iter = Iterators.partition(list.iterator(), 1, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 1);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 2);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 3);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 4);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 5);
    assertFalse(iter.hasNext());

    // test partition size 1 with padding
    iter = Iterators.partition(list.iterator(), 1, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 1);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 2);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 3);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 4);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 5);
    assertFalse(iter.hasNext());

    // test list size = multiple of partition size
    list = Arrays.asList(1, 2, 3, 4);
    iter = Iterators.partition(list.iterator(), 2, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 1, 2);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 3, 4);
    assertFalse(iter.hasNext());

    // test list size = multiple of partition size with padding
    iter = Iterators.partition(list.iterator(), 2, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 1, 2);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()), 3, 4);
    assertFalse(iter.hasNext());

    // test empty list
    list = Lists.newArrayList();
    iter = Iterators.partition(list.iterator(), 1, false);
    assertFalse(iter.hasNext());

    // test empty list with padding
    list = Lists.newArrayList();
    iter = Iterators.partition(list.iterator(), 1, true);
    assertFalse(iter.hasNext());

    // test with a null in the list
    list = Arrays.asList(1, 2, null, 4, 5);
    iter = Iterators.partition(list.iterator(), 3, false);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, null);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      4, 5);
    assertFalse(iter.hasNext());

    // test with a null in the list with padding
    list = Arrays.asList(1, 2, null, 4, 5);
    iter = Iterators.partition(list.iterator(), 3, true);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      1, 2, null);
    MoreAsserts.assertContentsInOrder(Lists.newArrayList(iter.next()),
                                      4, 5, null);
    assertFalse(iter.hasNext());
  }

  public void testForEnumerationEmpty() throws Exception {
    Enumeration<Integer> enumer = enumerate();
    Iterator<Integer> iter = Iterators.forEnumeration(enumer);

    assertFalse(iter.hasNext());
    try {
      iter.next();
      fail();
    } catch (NoSuchElementException expected) {
    }
  }

  public void testForEnumerationSingleton() throws Exception {
    Enumeration<Integer> enumer = enumerate(1);
    Iterator<Integer> iter = Iterators.forEnumeration(enumer);

    assertTrue(iter.hasNext());
    assertTrue(iter.hasNext());
    assertEquals(1, (int) iter.next());
    try {
      iter.remove();
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    assertFalse(iter.hasNext());
    try {
      iter.next();
      fail();
    } catch (NoSuchElementException expected) {
    }
  }

  public void testForEnumerationTypical() throws Exception {
    Enumeration<Integer> enumer = enumerate(1, 2, 3);
    Iterator<Integer> iter = Iterators.forEnumeration(enumer);

    assertTrue(iter.hasNext());
    assertEquals(1, (int) iter.next());
    assertTrue(iter.hasNext());
    assertEquals(2, (int) iter.next());
    assertTrue(iter.hasNext());
    assertEquals(3, (int) iter.next());
    assertFalse(iter.hasNext());
  }


  public void testAsEnumerationEmpty() throws Exception {
    Iterator<Integer> iter = Iterators.emptyIterator();
    Enumeration<Integer> enumer = Iterators.asEnumeration(iter);

    assertFalse(enumer.hasMoreElements());
    try {
      enumer.nextElement();
      fail();
    } catch (NoSuchElementException expected) {
    }
  }

  public void testAsEnumerationSingleton() throws Exception {
    Iterator<Integer> iter = Lists.immutableList(1).iterator();
    Enumeration<Integer> enumer = Iterators.asEnumeration(iter);

    assertTrue(enumer.hasMoreElements());
    assertTrue(enumer.hasMoreElements());
    assertEquals(1, (int) enumer.nextElement());
    assertFalse(enumer.hasMoreElements());
    try {
      enumer.nextElement();
      fail();
    } catch (NoSuchElementException expected) {
    }
  }

  public void testAsEnumerationTypical() throws Exception {
    Iterator<Integer> iter = Lists.immutableList(1, 2, 3).iterator();
    Enumeration<Integer> enumer = Iterators.asEnumeration(iter);

    assertTrue(enumer.hasMoreElements());
    assertEquals(1, (int) enumer.nextElement());
    assertTrue(enumer.hasMoreElements());
    assertEquals(2, (int) enumer.nextElement());
    assertTrue(enumer.hasMoreElements());
    assertEquals(3, (int) enumer.nextElement());
    assertFalse(enumer.hasMoreElements());
  }

  private static Enumeration<Integer> enumerate(Integer... ints) {
    Vector<Integer> vector = new Vector<Integer>();
    vector.addAll(Arrays.asList(ints));
    return vector.elements();
  }

  public void testToString() {
    List<String> list = Collections.emptyList();
    assertEquals("[]", Iterators.toString(list.iterator()));

    list = Lists.newArrayList("yam", "bam", "jam", "ham");
    assertEquals("[yam, bam, jam, ham]", Iterators.toString(list.iterator()));
  }
}
