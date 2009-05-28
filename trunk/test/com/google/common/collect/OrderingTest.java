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

import com.google.common.testutils.EqualsTester;
import com.google.common.testutils.NullPointerTester;

import junit.framework.TestCase;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Unit tests for {@code Ordering}.
 *
 * @author Jesse Wilson
 */
public class OrderingTest extends TestCase {

  private final Ordering<Number> numberOrdering = new NumberOrdering();

  public void testNatural() {
    Ordering<Integer> comparator = Ordering.natural();
    assertTrue(comparator.compare(1, 1) == 0);
    assertTrue(comparator.compare(1, 2) < 0);
    assertTrue(comparator.compare(2, 1) > 0);
    assertTrue(comparator.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0);
    try {
      comparator.compare(1, null);
      fail();
    } catch (NullPointerException expected) {}
    try {
      comparator.compare(null, 2);
      fail();
    } catch (NullPointerException expected) {}
    try {
      comparator.compare(null, null);
      fail();
    } catch (NullPointerException expected) {}
  }

  public void testFrom() {
    Ordering<String> caseInsensitiveOrdering
        = Ordering.from(String.CASE_INSENSITIVE_ORDER);
    assertEquals(0, caseInsensitiveOrdering.compare("A", "a"));
    assertTrue(caseInsensitiveOrdering.compare("a", "B") < 0);
    assertTrue(caseInsensitiveOrdering.compare("B", "a") > 0);

    @SuppressWarnings("deprecation") // test of deprecated method
    Ordering<String> orderingFromOrdering =
        Ordering.from(Ordering.<String>natural());
    new EqualsTester(caseInsensitiveOrdering)
        .addEqualObject(Ordering.from(String.CASE_INSENSITIVE_ORDER))
        .addNotEqualObject(orderingFromOrdering)
        .addNotEqualObject(Ordering.natural())
        .testEquals();
  }

  public void testReverse() {
    Ordering<Number> reverseOrder = numberOrdering.reverse();
    assertEquals(0, reverseOrder.compare(5, 5));
    assertTrue(reverseOrder.compare(5, 3) < 0);
    assertTrue(reverseOrder.compare(3, 5) > 0);

    new EqualsTester(reverseOrder)
      .addEqualObject(numberOrdering.reverse())
      .addNotEqualObject(Ordering.natural().reverse())
      .addNotEqualObject(Collections.reverseOrder())
      .testEquals();
  }

  public void testReverseEqualsAndHashCode() {
    Ordering<Number> a = numberOrdering.reverse();
    Ordering<Number> b = new NumberOrdering().reverse();
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(Collections.reverseOrder()));
  }

  public void testReverseOfReverseSameAsForward() {
    Ordering<Number> reverseOfReverse
        = new NumberOrdering().reverse().reverse();
    assertTrue(reverseOfReverse.compare(1, 1) == 0);
    assertTrue(reverseOfReverse.compare(1, 2) < 0);
    assertTrue(reverseOfReverse.compare(2, 1) > 0);
  }

  // Note that other compound tests are still in ComparatorsTest and will be
  // copied over soonish.
  public void testCompound_instance_generics() {
    Ordering<Object> objects = Ordering.explicit((Object) 1);
    Ordering<Number> numbers = Ordering.explicit((Number) 1);
    Ordering<Integer> integers = Ordering.explicit(1);

    // Like by like equals like
    Ordering<Number> a = numbers.compound(numbers);

    // The compound takes the more specific type of the two, regardless of order

    Ordering<Number> b = numbers.compound(objects);
    Ordering<Number> c = objects.compound(numbers);

    Ordering<Integer> d = numbers.compound(integers);
    Ordering<Integer> e = integers.compound(numbers);

    // This works with three levels too (IDEA falsely reports errors as noted
    // below. Both javac and eclipse handle these cases correctly.)

    Ordering<Number> f = numbers.compound(objects).compound(objects); //bad IDEA
    Ordering<Number> g = objects.compound(numbers).compound(objects);
    Ordering<Number> h = objects.compound(objects).compound(numbers);

    Ordering<Number> i = numbers.compound(objects.compound(objects));
    Ordering<Number> j = objects.compound(numbers.compound(objects)); //bad IDEA
    Ordering<Number> k = objects.compound(objects.compound(numbers));

    // You can also arbitrarily assign a more restricted type - not an intended
    // feature, exactly, but unavoidable (I think) and harmless
    Ordering<Integer> l = objects.compound(numbers);

    // This correctly doesn't work:
    // Ordering<Object> m = numbers.compound(objects);

    // Sadly, the following works in javac 1.6, but at least it fails for
    // eclipse, and is *correctly* highlighted red in IDEA.
    // Ordering<Object> n = objects.compound(numbers);
  }

  public void testBinarySearch() {
    List<Integer> ints = Lists.newArrayList(0, 2, 3, 5, 7, 9);
    assertEquals(4, numberOrdering.binarySearch(ints, 7));
  }

  public void testSortedCopy() {
    List<Integer> unsortedInts = Lists.newArrayList(5, 3, 0, 9);
    List<Integer> sortedInts = numberOrdering.sortedCopy(unsortedInts);
    assertEquals(Lists.newArrayList(0, 3, 5, 9), sortedInts);
    assertEquals(Lists.newArrayList(5, 3, 0, 9), unsortedInts);
  }

  public void testIsOrdered() {
    assertFalse(numberOrdering.isOrdered(asList(5, 3, 0, 9)));
    assertFalse(numberOrdering.isOrdered(asList(0, 5, 3, 9)));
    assertTrue(numberOrdering.isOrdered(asList(0, 3, 5, 9)));
    assertTrue(numberOrdering.isOrdered(asList(0, 0, 3, 3)));
    assertTrue(numberOrdering.isOrdered(asList(0, 3)));
    assertTrue(numberOrdering.isOrdered(Collections.singleton(1)));
    assertTrue(numberOrdering.isOrdered(Collections.<Integer>emptyList()));
  }

  public void testIsStrictlyOrdered() {
    assertFalse(numberOrdering.isStrictlyOrdered(asList(5, 3, 0, 9)));
    assertFalse(numberOrdering.isStrictlyOrdered(asList(0, 5, 3, 9)));
    assertTrue(numberOrdering.isStrictlyOrdered(asList(0, 3, 5, 9)));
    assertFalse(numberOrdering.isStrictlyOrdered(asList(0, 0, 3, 3)));
    assertTrue(numberOrdering.isStrictlyOrdered(asList(0, 3)));
    assertTrue(numberOrdering.isStrictlyOrdered(Collections.singleton(1)));
    assertTrue(numberOrdering.isStrictlyOrdered(
        Collections.<Integer>emptyList()));
  }

  public void testIterableMinAndMax() {
    List<Integer> ints = Lists.newArrayList(5, 3, 0, 9);
    assertEquals(9, (int) numberOrdering.max(ints));
    assertEquals(0, (int) numberOrdering.min(ints));

    // when the values are the same, the first argument should be returned
    Integer a = new Integer(4);
    Integer b = new Integer(4);
    ints = Lists.newArrayList(a, b, b);
    assertSame(a, numberOrdering.max(ints));
    assertSame(a, numberOrdering.min(ints));
  }

  public void testVarargsMinAndMax() {
    // try the min and max values in all positions, since some values are proper
    // parameters and others are from the varargs array
    assertEquals(9, (int) numberOrdering.max(9, 3, 0, 5, 8));
    assertEquals(9, (int) numberOrdering.max(5, 9, 0, 3, 8));
    assertEquals(9, (int) numberOrdering.max(5, 3, 9, 0, 8));
    assertEquals(9, (int) numberOrdering.max(5, 3, 0, 9, 8));
    assertEquals(9, (int) numberOrdering.max(5, 3, 0, 8, 9));
    assertEquals(0, (int) numberOrdering.min(0, 3, 5, 9, 8));
    assertEquals(0, (int) numberOrdering.min(5, 0, 3, 9, 8));
    assertEquals(0, (int) numberOrdering.min(5, 3, 0, 9, 8));
    assertEquals(0, (int) numberOrdering.min(5, 3, 9, 0, 8));
    assertEquals(0, (int) numberOrdering.min(5, 3, 0, 9, 0));

    // when the values are the same, the first argument should be returned
    Integer a = new Integer(4);
    Integer b = new Integer(4);
    assertSame(a, numberOrdering.max(a, b, b));
    assertSame(a, numberOrdering.min(a, b, b));
  }

  public void testParameterMinAndMax() {
    assertEquals(5, (int) numberOrdering.max(3, 5));
    assertEquals(5, (int) numberOrdering.max(5, 3));
    assertEquals(3, (int) numberOrdering.min(3, 5));
    assertEquals(3, (int) numberOrdering.min(5, 3));

    // when the values are the same, the first argument should be returned
    Integer a = new Integer(4);
    Integer b = new Integer(4);
    assertSame(a, numberOrdering.max(a, b));
    assertSame(a, numberOrdering.min(a, b));
  }

  private static class NumberOrdering extends Ordering<Number> {
    public int compare(Number a, Number b) {
      return ((Double) a.doubleValue()).compareTo(b.doubleValue());
    }
    @Override public int hashCode() {
      return NumberOrdering.class.hashCode();
    }
    @Override public boolean equals(Object other) {
      return other instanceof NumberOrdering;
    }
    private static final long serialVersionUID = 0;
  }

  public void testNullsFirst() {
    Ordering<Integer> ordering = Ordering.natural().reverse().nullsFirst();
    assertEquivalent(ordering, 1, 1);
    assertEquivalent(ordering, Integer.MIN_VALUE, Integer.MIN_VALUE);
    assertEquivalent(ordering, null, null);

    assertIncreasing(ordering, 1, 0);
    assertIncreasing(ordering, null, Integer.MIN_VALUE);
    assertIncreasing(ordering, null, Integer.MAX_VALUE);
  }

  public void testNullsLast() {
    Ordering<Integer> ordering = Ordering.natural().reverse().nullsLast();
    assertEquivalent(ordering, 1, 1);
    assertEquivalent(ordering, Integer.MIN_VALUE, Integer.MIN_VALUE);
    assertEquivalent(ordering, null, null);

    assertIncreasing(ordering, 1, 0);
    assertIncreasing(ordering, Integer.MIN_VALUE, null);
    assertIncreasing(ordering, Integer.MAX_VALUE, null);
  }

  public void testNullPointerExceptions() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Ordering.class);
  }

  static <T> void assertEquivalent(
      Comparator<T> comparator, @Nullable T left, @Nullable T right) {
    assertEquals(0, comparator.compare(left, right));
    assertEquals(0, comparator.compare(right, left));
  }

  static <T> void assertIncreasing(
      Comparator<T> comparator, @Nullable T left, @Nullable T right) {
    assertTrue(comparator.compare(left, right) < 0);
    assertTrue(comparator.compare(right, left) > 0);
  }
}
