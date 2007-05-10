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
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import junit.framework.TestCase;

/**
 * Unit tests for {@code Comparators}.
 *
 * @author jlevy@google.com (Jared Levy)
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public class ComparatorsTest extends TestCase {

  public void testNaturalOrder() throws Exception {
    Comparator<Integer> comparator = Comparators.<Integer>naturalOrder();
    assertTrue(comparator instanceof Serializable);
    assertTrue(comparator.compare(1, 1) == 0);
    assertTrue(comparator.compare(1, 2) < 0);
    assertTrue(comparator.compare(2, 1) > 0);
    assertTrue(comparator.compare(Integer.MIN_VALUE, Integer.MAX_VALUE) < 0);
  }

  enum Foo { ALPHA, BETA, GAMMA, DELTA }

  public void testByStringForm() throws Exception {
    Comparator<Object> comparator = Comparators.STRING_FORM_ORDER;
    assertTrue(comparator instanceof Serializable);
    assertTrue(comparator.compare(Foo.ALPHA, Foo.ALPHA) == 0);
    assertTrue(comparator.compare(Foo.DELTA, Foo.GAMMA) < 0);
    assertTrue(comparator.compare(Foo.GAMMA, Foo.DELTA) > 0);
  }

  public void testCompoundComparator() throws Exception {
    Comparator<String> byLastCharacter = new Comparator<String>() {
      public int compare(String left, String right) {
        Character leftChar = left.charAt(left.length() - 1);
        Character rightChar = right.charAt(right.length() - 1);
        return leftChar.compareTo(rightChar);
      }
    };
    Comparator<String> byWhole = Comparators.<String>naturalOrder();
    Comparator<String> comparator
        = Comparators.compound(byLastCharacter, byWhole);
    assertTrue(comparator instanceof Serializable);
    assertTrue(comparator.compare("sergey", "sergey") == 0);
    assertTrue(comparator.compare("kevinb", "eric") < 0);
    assertTrue(comparator.compare("eric", "kevinb") > 0);
    assertTrue(comparator.compare("kevinb", "michaelb") < 0);
    assertTrue(comparator.compare("michaelb", "kevinb") > 0);
  }

  private static final Function<String, Integer> STRING_LENGTH
      = new Function<String, Integer>() {
          public Integer apply(String string) {
            return string.length();
          }
        };

  private static final Comparator<Integer> DECREASING_INTEGER
      = Collections.reverseOrder();

  public void testFromFunctionNatural() {
    Comparator<String> comparator = Comparators.fromFunction(STRING_LENGTH);
    assertTrue(comparator instanceof Serializable);
    assertTrue(comparator.compare("to", "be") == 0);
    assertTrue(comparator.compare("or", "not") < 0);
    assertTrue(comparator.compare("that", "to") > 0);
  }

  public void testFromFunctionExplicit() {
    Comparator<String> comparator
        = Comparators.fromFunction(STRING_LENGTH, DECREASING_INTEGER);
    assertTrue(comparator instanceof Serializable);
    assertTrue(comparator.compare("to", "be") == 0);
    assertTrue(comparator.compare("not", "or") < 0);
    assertTrue(comparator.compare("to", "that") > 0);
  }

  public void testMinAndMax() {
    /* regular comparison */
    assertEquals("A", Comparators.min("A", "B"));
    assertEquals("A", Comparators.min("B", "A"));
    assertEquals("B", Comparators.max("A", "B"));
    assertEquals("B", Comparators.max("B", "A"));

    /* with a comparator */
    assertEquals(Foo.DELTA,
        Comparators.min(Comparators.STRING_FORM_ORDER, Foo.DELTA, Foo.GAMMA));
    assertEquals(Foo.GAMMA,
        Comparators.max(Comparators.STRING_FORM_ORDER, Foo.DELTA, Foo.GAMMA));

    /* when the values are the same, the first argument should be returned */
    Integer a = new Integer(1);
    Integer b = new Integer(1);
    assertSame(a, Comparators.min(a, b));
    assertSame(a, Comparators.max(a, b));

    /* when the values are the same, with a comparator */
    assertEquals("a", Comparators.min(String.CASE_INSENSITIVE_ORDER, "a", "A"));
    assertEquals("a", Comparators.max(String.CASE_INSENSITIVE_ORDER, "a", "A"));
  }

  public void testFrequencyOrder() {
    Multiset<String> m = Multisets.newHashMultiset();
    m.add("a", 2);
    m.add("b", 3);
    m.add("c");
    m.add("d");
    Comparator<String> c = Comparators.frequencyOrder(m);
    assertTrue(c instanceof Serializable);

    /* compare elements with different frequencies */
    assertTrue(c.compare("a", "b") < 0);
    assertTrue(c.compare("b", "a") > 0);

    /* compare elements with the same frequencies */
    assertEquals(0, c.compare("a", "a"));
    assertEquals(0, c.compare("c", "d"));
    assertEquals(0, c.compare("d", "c"));

    /* compare elements not in the multiset (zero occurences) */
    assertTrue(c.compare("e", "a") < 0);
    assertTrue(c.compare("a", "e") > 0);
    assertEquals(0, c.compare("e", "f"));

    /* compare null (zero occurences) */
    assertTrue(c.compare(null, "a") < 0);
    assertTrue(c.compare("a", null) > 0);
    assertEquals(0, c.compare(null, null));

    /* verify that the comparator is a view on the multiset */
    m.add("d", 10);
    assertTrue(c.compare("d", "a") > 0);
    assertTrue(c.compare("a", "d") < 0);
    assertTrue(c.compare("d", "e") > 0);
    assertTrue(c.compare("e", "d") < 0);
    assertEquals(0, c.compare("d", "d"));
  }

  public void testFrequencyOrderTreeSet() {
    Multiset<String> m = Multisets.newHashMultiset("a", "a", "b", "b", "b", "c");
    Comparator<String> c = Comparators.frequencyOrder(m);
    SortedSet<String> s = Sets.newTreeSet(c, m); // or m.elementSet()
    assertContentsInOrder(s, "c", "a", "b");
  }

  public void testCompareByte() {
    assertTrue(Comparators.compare((byte) 0x01, (byte) 0x01) == 0);
    assertTrue(Comparators.compare((byte) 0x01, (byte) 0x02) < 0);
    assertTrue(Comparators.compare((byte) 0x02, (byte) 0x01) > 0);
  }

  public void testCompareChar() {
    assertTrue(Comparators.compare('a', 'a') == 0);
    assertTrue(Comparators.compare('a', 'b') < 0);
    assertTrue(Comparators.compare('b', 'a') > 0);
  }

  public void testCompareShort() {
    assertTrue(Comparators.compare((short) 0x01, (short) 0x01) == 0);
    assertTrue(Comparators.compare((short) 0x01, (short) 0x02) < 0);
    assertTrue(Comparators.compare((short) 0x02, (short) 0x01) > 0);
  }

  public void testCompareInt() {
    assertTrue(Comparators.compare(1, 1) == 0);
    assertTrue(Comparators.compare(1, 2) < 0);
    assertTrue(Comparators.compare(2, 1) > 0);
  }

  public void testCompareLong() {
    assertTrue(Comparators.compare(1L, 1L) == 0);
    assertTrue(Comparators.compare(1L, 2L) < 0);
    assertTrue(Comparators.compare(2L, 1L) > 0);
  }

  public void testCompareDouble() {
    assertTrue(Comparators.compare(1D, 1D) == 0);
    assertTrue(Comparators.compare(1D, 2D) < 0);
    assertTrue(Comparators.compare(2D, 1D) > 0);
    assertTrue(Comparators.compare(Double.NaN, Double.NaN) == 0);
    assertTrue(Comparators.compare(Double.NaN, Double.POSITIVE_INFINITY) > 0);
    assertTrue(Comparators.compare(Double.MAX_VALUE, Double.MIN_VALUE) > 0);
  }

  public void testCompareFloat() {
    assertTrue(Comparators.compare(1F, 1F) == 0);
    assertTrue(Comparators.compare(1F, 2F) < 0);
    assertTrue(Comparators.compare(2F, 1F) > 0);
    assertTrue(Comparators.compare(Float.NaN, Float.NaN) == 0);
    assertTrue(Comparators.compare(Float.NaN, Float.POSITIVE_INFINITY) > 0);
    assertTrue(Comparators.compare(Float.MAX_VALUE, Float.MIN_VALUE) > 0);
  }

  public void testCompareBoolean() {
    assertTrue(Comparators.compare(false, false) == 0);
    assertTrue(Comparators.compare(true, true) == 0);
    assertTrue(Comparators.compare(false, true) < 0);
    assertTrue(Comparators.compare(true, false) > 0);
  }
}
