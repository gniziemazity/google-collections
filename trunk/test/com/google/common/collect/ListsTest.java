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
import static com.google.common.collect.helpers.MoreAsserts.assertContentsInOrder;
import com.google.common.collect.helpers.NullPointerTester;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import junit.framework.TestCase;

/**
 * Unit test for {@code Lists}.
 *
 * @author kevinb
 * @author mbostock
 */
public class ListsTest extends TestCase {

  private static final Collection<Integer> SOME_COLLECTION =
      Arrays.asList(0, 1, 1);

  private static final Iterable<Integer> SOME_ITERABLE =
      new Iterable<Integer>() {
        public Iterator<Integer> iterator() {
          return SOME_COLLECTION.iterator();
        }
      };

  private static final List<Integer> SOME_LIST =
      Lists.newArrayList(1, 2, 3, 4);

  private static final List<Integer> SOME_SEQUENTIAL_LIST =
      Lists.newLinkedList(1, 2, 3, 4);

  private static final List<String> SOME_STRING_LIST =
      Arrays.asList("1", "2", "3", "4");

  private static final Function<Number, String> SOME_FUNCTION =
      new Function<Number, String>() {
        public String apply(Number n) {
          return String.valueOf(n);
        }
      };

  public void testImmutableList() throws Exception {
    List<Integer> list = Lists.immutableList(1, 0, 1);
    List<Integer> expected = Arrays.asList(1, 0, 1);
    assertEquals(expected, list);
    assertEquals(expected.hashCode(), list.hashCode());
    assertEquals(expected.hashCode(), list.hashCode());
    try {
      list.add(1);
      fail();
    } catch (UnsupportedOperationException e) {
    }
    try {
      list.set(1, 2);
      fail();
    } catch (UnsupportedOperationException e) {
    }
  }

  private static final Class SINGLETON_CLASS =
      Collections.singletonList(1).getClass();
  private static final Class IMMUTABLE_CLASS =
      Lists.immutableList(1, 2, 3).getClass();

  /**
   * Tests Lists.immutableList(Collection) on a given input.
   */
  private <T> void testImmutableListOfCollection(final Collection<T> input)
      throws Exception {
    List<T> output = Lists.immutableList(input);

    // Assert that it's an instance of a known immutable class.
    assertTrue("expected known immutable List implementation, but got "
               + output.getClass().getCanonicalName(),
               output == Collections.EMPTY_LIST
               || output.getClass() == SINGLETON_CLASS
               || output.getClass() == IMMUTABLE_CLASS);

    // Assert that it contains the right elements in the right order.
    assertContentsInOrder(output, input.toArray());

    // Assert that we don't bother cloning lists that are known to be immutable.
    assertSame(output, Lists.immutableList(output));

    // Assert that immutable lists containing the same elements are equal.
    assertEquals(output, Lists.immutableList(input));

    // Assert that we can also use an Iterable.
    assertEquals(output,
                 // we do this rather than casting to ensure that immutableList
                 // doesn't un-cast it.
                 Lists.immutableList(
                     new Iterable<T>() {
                       public Iterator<T> iterator() {
                         return input.iterator();
                       }
                     }));

    // Assert that we can also use an Iterator.
    assertEquals(output, Lists.immutableList(input.iterator()));
  }

  public void testImmutableListOfCollection() throws Exception {
    testImmutableListOfCollection(Lists.newArrayList());
    testImmutableListOfCollection(Lists.newArrayList(new Object()));
    testImmutableListOfCollection(Lists.newArrayList("inky", "blinky", "pinky",
                                                     "clyde"));
    testImmutableListOfCollection(Sets.newHashSet(4, 8, 15, 16, 23, 42));
  }

  public void testIllustrateVarargsWeirdness() {
    String[] array = { "foo", "bar" };

    // IDEA wrongly colors the next line red
    // http://jetbrains.net/jira/browse/IDEA-6863
    List<String> list = Lists.immutableList(array);

    // Fortunately, this does what the caller clearly intended
    assertEquals(2, list.size());

    // If the list-of-array is desired, a special method type parameter must be
    // inserted or the code won't compile.
    List<String[]> listOfArray = Lists.<String[]>immutableList(array);

    // This also behaves as expected
    assertEquals(1, listOfArray.size());

    // It's pretty strange that the selection of overload can depend on the
    // type parameter given.  But what if raw types are used?
    List whatIsThis = Lists.immutableList(array);

    // In most cases I think this will be what the caller intended.  If not...
    // well, ya shoulda used the generics, bub.
    assertEquals(2, whatIsThis.size());

    // Unfortunately, the below will blow up at runtime
    // But hey, can't say the compiler didn't warn ya
    try {
      // IDEA wrongly colors the next line red
      // http://jetbrains.net/jira/browse/IDEA-6864
      List<String> listofNull = Lists.immutableList((String[]) null);
      fail();
    } catch (NullPointerException expected) {
    }

    // The fix for the above
    List<String> listOfNull = Lists.immutableList((String) null);
    assertEquals(Collections.singletonList(null), listOfNull);
  }

  public void testNewArrayListEmpty() {
    ArrayList<Integer> list = Lists.newArrayList();
    assertEquals(Collections.emptyList(), list);
  }

  public void testNewArrayListWithCapacityEmpty() {
    ArrayList<Integer> list = Lists.newArrayListWithCapacity(0);
    assertEquals(Collections.emptyList(), list);

    list = Lists.newArrayListWithCapacity(256);
    assertEquals(Collections.emptyList(), list);
  }

  public void testNewArrayListWithCapacityThrowsExceptionOnNegativeValue() {
    try {
      Lists.newArrayListWithCapacity(-1);
      fail("Fail! Should have thrown an IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected, since initial capacity is negative
    }
  }

  public void testNewArrayListVarArgs() {
    ArrayList<Integer> list = Lists.newArrayList(0, 1, 1);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewArrayListFromCollection() {
    ArrayList<Integer> list = Lists.newArrayList(SOME_COLLECTION);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewArrayListFromIterable() {
    ArrayList<Integer> list = Lists.newArrayList(SOME_ITERABLE);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewArrayListFromIterator() {
    ArrayList<Integer> list = Lists.newArrayList(SOME_COLLECTION.iterator());
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewLinkedListEmpty() {
    LinkedList<Integer> list = Lists.newLinkedList();
    assertEquals(Collections.emptyList(), list);
  }

  public void testNewLinkedListVarArgs() {
    LinkedList<Integer> list = Lists.newLinkedList(0, 1, 1);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewLinkedListFromCollection() {
    LinkedList<Integer> list = Lists.newLinkedList(SOME_COLLECTION);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewLinkedListFromIterable() {
    LinkedList<Integer> list = Lists.newLinkedList(SOME_ITERABLE);
    assertEquals(SOME_COLLECTION, list);
  }

  public void testNewLinkedListFromIterator() {
    LinkedList<Integer> list
        = Lists.newLinkedList(SOME_COLLECTION.iterator());
    assertEquals(SOME_COLLECTION, list);
  }

  public void testSortedCopy() {
    List<Integer> UNORDERED_LIST = Lists.newArrayList(3, 4, 2, 1);

    List<Integer> FORWARDS_LIST = Lists.newArrayList(1, 2, 3, 4);
    List<Integer> BACKWARDS_LIST = Lists.newArrayList(4, 3, 2, 1);

    List<Integer> list1 = Lists.sortedCopy(UNORDERED_LIST);
    List<Integer> list2 = Lists.sortedCopy(UNORDERED_LIST,
                                           Collections.reverseOrder());
    assertEquals(list1, FORWARDS_LIST);
    assertEquals(list2, BACKWARDS_LIST);
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Lists.class);
  }

  /**
   * This is just here to illustrate how {@code Arrays#asList} differs from
   * {@code Lists#newArrayList}.
   */
  public void testArraysAsList() {
    List<String> ourWay = Lists.newArrayList("foo", "bar", "baz");
    List<String> otherWay = Arrays.asList("foo", "bar", "baz");

    // They're logically equal
    assertEquals(ourWay, otherWay);

    // The result of Arrays.asList() is mutable
    otherWay.set(0, "FOO");
    assertEquals("FOO", otherWay.get(0));

    // But it can't grow
    try {
      otherWay.add("nope");
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }

    // And it can't shrink
    try {
      otherWay.remove(2);
      fail("no exception thrown");
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testAsList1() throws Exception {
    List<String> list = Lists.asList("foo", new String[] { "bar", "baz" });
    checkFooBarBazList(list);

    new IteratorTester(7) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Arrays.asList("foo", "bar", "baz").iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return Lists.asList("foo", new String[] { "bar", "baz" }).iterator();
      }
    }.test();
  }

  private void checkFooBarBazList(List<String> list) {
    assertContentsInOrder(list, "foo", "bar", "baz");
    assertEquals(3, list.size());
    assertIndexIsOutOfBounds(list, -1);
    assertEquals("foo", list.get(0));
    assertEquals("bar", list.get(1));
    assertEquals("baz", list.get(2));
    assertIndexIsOutOfBounds(list, 3);
  }

  public void testAsList1Small() throws Exception {
    List<String> list = Lists.asList("foo", new String[0]);
    assertContentsInOrder(list, "foo");
    assertEquals(1, list.size());
    assertIndexIsOutOfBounds(list, -1);
    assertEquals("foo", list.get(0));
    assertIndexIsOutOfBounds(list, 1);

    new IteratorTester(4) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Collections.singletonList("foo").iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return Lists.asList("foo", new String[0]).iterator();
      }
    }.test();
  }

  public void testAsList2() throws Exception {
    List<String> list = Lists.asList("foo", "bar", new String[] { "baz" });
    checkFooBarBazList(list);

    new IteratorTester(7) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Arrays.asList("foo", "bar", "baz").iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return Lists.asList("foo", "bar", new String[] { "baz" }).iterator();
      }
    }.test();
  }

  public void testAsList2Small() throws Exception {
    List<String> list = Lists.asList("foo", "bar", new String[0]);
    assertContentsInOrder(list, "foo", "bar");
    assertEquals(2, list.size());
    assertIndexIsOutOfBounds(list, -1);
    assertEquals("foo", list.get(0));
    assertEquals("bar", list.get(1));
    assertIndexIsOutOfBounds(list, 2);

    new IteratorTester(7) {
      @Override protected Iterator<?> newReferenceIterator() {
        return Arrays.asList("foo", "bar").iterator();
      }
      @Override protected Iterator<?> newTargetIterator() {
        return Lists.asList("foo", "bar", new String[0]).iterator();
      }
    }.test();
  }

  private static void assertIndexIsOutOfBounds(List<String> list, int index) {
    try {
      list.get(index);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
  }

  public void testTransformEqualityRandomAccess() {
    List<String> list = Lists.transform(SOME_LIST, SOME_FUNCTION);
    assertEquals(SOME_STRING_LIST, list);
  }

  public void testTransformEqualitySequential() {
    List<String> list = Lists.transform(SOME_SEQUENTIAL_LIST, SOME_FUNCTION);
    assertEquals(SOME_STRING_LIST, list);
  }

  public void testTransformHashCodeRandomAccess() {
    List<String> list = Lists.transform(SOME_LIST, SOME_FUNCTION);
    assertEquals(SOME_STRING_LIST.hashCode(), list.hashCode());
  }

  public void testTransformHashCodeSequential() {
    List<String> list = Lists.transform(SOME_SEQUENTIAL_LIST, SOME_FUNCTION);
    assertEquals(SOME_STRING_LIST.hashCode(), list.hashCode());
  }

  public void testTransformModifiableRandomAccess() {
    List<Integer> fromList = Lists.newArrayList(SOME_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformModifiable(list);
  }

  public void testTransformModifiableSequential() {
    List<Integer> fromList = Lists.newLinkedList(SOME_SEQUENTIAL_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformModifiable(list);
  }

  private static void assertTransformModifiable(List<String> list) {
    try {
      list.add("5");
      fail("transformed list is addable");
    } catch (UnsupportedOperationException expected) {}
    list.remove(0);
    assertEquals(Arrays.asList("2", "3", "4"), list);
    list.remove("3");
    assertEquals(Arrays.asList("2", "4"), list);
    try {
      list.set(0, "5");
      fail("transformed list is setable");
    } catch (UnsupportedOperationException expected) {}
    list.clear();
    assertEquals(Collections.emptyList(), list);
  }

  public void testTransformViewRandomAccess() {
    List<Integer> fromList = Lists.newArrayList(SOME_LIST);
    List<String> toList = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformView(fromList, toList);
  }

  public void testTransformViewSequential() {
    List<Integer> fromList = Lists.newLinkedList(SOME_SEQUENTIAL_LIST);
    List<String> toList = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformView(fromList, toList);
  }

  private static void assertTransformView(List<Integer> fromList,
      List<String> toList) {
    /* fromList modifications reflected in toList */
    fromList.set(0, 5);
    assertEquals(Arrays.asList("5", "2", "3", "4"), toList);
    fromList.add(6);
    assertEquals(Arrays.asList("5", "2", "3", "4", "6"), toList);
    fromList.remove(Integer.valueOf(2));
    assertEquals(Arrays.asList("5", "3", "4", "6"), toList);
    fromList.remove(2);
    assertEquals(Arrays.asList("5", "3", "6"), toList);

    /* toList modifications reflected in fromList */
    toList.remove(2);
    assertEquals(Arrays.asList(5, 3), fromList);
    toList.remove("5");
    assertEquals(Arrays.asList(3), fromList);
    toList.clear();
    assertEquals(Collections.emptyList(), fromList);
  }

  public void testTransformRandomAccess() {
    List<String> list = Lists.transform(SOME_LIST, SOME_FUNCTION);
    assertTrue(list instanceof RandomAccess);
  }

  public void testTransformSequential() {
    List<String> list = Lists.transform(SOME_SEQUENTIAL_LIST, SOME_FUNCTION);
    assertFalse(list instanceof RandomAccess);
  }

  public void testTransformListIteratorRandomAccess() {
    List<Integer> fromList = Lists.newArrayList(SOME_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformListIterator(list);
  }

  public void testTransformListIteratorSequential() {
    List<Integer> fromList = Lists.newLinkedList(SOME_SEQUENTIAL_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformListIterator(list);
  }

  private static void assertTransformListIterator(List<String> list) {
    ListIterator<String> iterator = list.listIterator(1);
    assertEquals(1, iterator.nextIndex());
    assertEquals("2", iterator.next());
    assertEquals("3", iterator.next());
    assertEquals("4", iterator.next());
    assertEquals(4, iterator.nextIndex());
    try {
      iterator.next();
      fail("did not detect end of list");
    } catch (NoSuchElementException expected) {}
    assertEquals(3, iterator.previousIndex());
    assertEquals("4", iterator.previous());
    assertEquals("3", iterator.previous());
    assertEquals("2", iterator.previous());
    assertEquals("1", iterator.previous());
    assertEquals(-1, iterator.previousIndex());
    try {
      iterator.previous();
      fail("did not detect beginning of list");
    } catch (NoSuchElementException expected) {}
    iterator.remove();
    assertEquals(Arrays.asList("2", "3", "4"), list);
    try {
      iterator.add("1");
      fail("transformed list iterator is addable");
    } catch (UnsupportedOperationException expected) {}
    try {
      iterator.set("1");
      fail("transformed list iterator is setable");
    } catch (UnsupportedOperationException expected) {}
  }

  public void testTransformIteratorRandomAccess() {
    List<Integer> fromList = Lists.newArrayList(SOME_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformIterator(list);
  }

  public void testTransformIteratorSequential() {
    List<Integer> fromList = Lists.newLinkedList(SOME_SEQUENTIAL_LIST);
    List<String> list = Lists.transform(fromList, SOME_FUNCTION);
    assertTransformIterator(list);
  }

  private static void assertTransformIterator(List<String> list) {
    Iterator<String> iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("1", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("2", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("3", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("4", iterator.next());
    assertFalse(iterator.hasNext());
    try {
      iterator.next();
      fail("did not detect end of list");
    } catch (NoSuchElementException expected) {}
    iterator.remove();
    assertEquals(Arrays.asList("1", "2", "3"), list);
    assertFalse(iterator.hasNext());
  }
}
