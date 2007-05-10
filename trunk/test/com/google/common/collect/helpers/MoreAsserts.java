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

package com.google.common.collect.helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Multisets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;

/**
 * Contains additional assertion methods not found in JUnit.
 *
 * @author kevinb
 */
public final class MoreAsserts {

  private MoreAsserts() { }

  /**
   * Asserts that {@code actual} is not equal {@code unexpected}, according
   * to both {@code ==} and {@code Object#equals}.
   */
  public static void assertNotEqual(
      String message, Object unexpected, Object actual) {
    if (equal(unexpected, actual)) {
      failEqual(message, unexpected);
    }
  }

  /**
   * Variant of {@code #assertNotEqual(String,Object,Object)} using a
   * generic message.
   */
  public static void assertNotEqual(Object unexpected, Object actual) {
    assertNotEqual(null, unexpected, actual);
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates specific
   * element mismatch.
   */
  public static void assertEquals(
      String message, byte[] expected, byte[] actual) {
    if (expected.length != actual.length) {
      failWrongLength(message, expected.length, actual.length);
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        failWrongElement(message, i, expected[i], actual[i]);
      }
    }
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates specific
   * element mismatch.
   */
  public static void assertEquals(byte[] expected, byte[] actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates first
   * specific element mismatch.
   */
  public static void assertEquals(
      String message, int[] expected, int[] actual) {
    if (expected.length != actual.length) {
      failWrongLength(message, expected.length, actual.length);
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        failWrongElement(message, i, expected[i], actual[i]);
      }
    }
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates first
   * specific element mismatch.
   */
  public static void assertEquals(int[] expected, int[] actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates first
   * specific element mismatch.
   */
  public static void assertEquals(
      String message, double[] expected, double[] actual) {
    if (expected.length != actual.length) {
      failWrongLength(message, expected.length, actual.length);
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        failWrongElement(message, i, expected[i], actual[i]);
      }
    }
  }

  /**
   * Asserts that array {@code actual} is the same size and every element equals
   * those in array {@code expected}. On failure, message indicates first
   * specific element mismatch.
   */
  public static void assertEquals(double[] expected, double[] actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that array {@code actual} is the same size and every element
   * is the same as those in array {@code expected}. Note that this uses
   * {@code ==} instead of {@code equals()} to compare the objects.
   * On failure, message indicates first specific element mismatch.
   */
  public static void assertEquals(
      String message, Object[] expected, Object[] actual) {
    if (expected.length != actual.length) {
      failWrongLength(message, expected.length, actual.length);
    }
    for (int i = 0; i < expected.length; i++) {
      if (expected[i] != actual[i]) {
        failWrongElement(message, i, expected[i], actual[i]);
      }
    }
  }

  /**
   * Asserts that array {@code actual} is the same size and every element
   * is the same as those in array {@code expected}. Note that this uses
   * {@code ==} instead of {@code equals()} to compare the objects.
   * On failure, message indicates first specific element mismatch.
   */
  public static void assertEquals(Object[] expected, Object[] actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that {@code expectedRegex} exactly matches {@code actual} and
   * fails with {@code message} if it does not.  The MatchResult is returned
   * in case the test needs access to any captured groups.  Note that you can
   * also use this for a literal string, by wrapping your expected string in
   * {@link Pattern#quote}.
   */
  public static MatchResult assertMatchesRegex(
      String message, String expectedRegex, String actual) {
    if (actual == null) {
      failNotMatches(message, expectedRegex, actual);
    }
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (!matcher.matches()) {
      failNotMatches(message, expectedRegex, actual);
    }
    return matcher;
  }

  /**
   * Variant of {@code #assertMatchesRegex(String,String,String)} using a
   * generic message.
   */
  public static MatchResult assertMatchesRegex(
      String expectedRegex, String actual) {
    return assertMatchesRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} matches any substring of {@code actual}
   * and fails with {@code message} if it does not.  The Matcher is returned in
   * case the test needs access to any captured groups.  Note that you can also
   * use this for a literal string, by wrapping your expected string in
   * {@link Pattern#quote}.
   */
  public static MatchResult assertContainsRegex(
      String message, String expectedRegex, String actual) {
    if (actual == null) {
      failNotContains(message, expectedRegex, actual);
    }
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (!matcher.find()) {
      failNotContains(message, expectedRegex, actual);
    }
    return matcher;
  }

  /**
   * Variant of {@code #assertContainsRegex(String,String,String)} using a
   * generic message.
   */
  public static MatchResult assertContainsRegex(
      String expectedRegex, String actual) {
    return assertContainsRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} does not exactly match {@code actual},
   * and fails with {@code message} if it does. Note that you can also use
   * this for a literal string, by wrapping your expected string in
   * {@link Pattern#quote}.
   */
  public static void assertNotMatchesRegex(
      String message, String expectedRegex, String actual) {
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (matcher.matches()) {
      failMatch(message, expectedRegex, actual);
    }
  }

  /**
   * Variant of {@code #assertNotMatchesRegex(String,String,String)} using a
   * generic message.
   */
  public static void assertNotMatchesRegex(
      String expectedRegex, String actual) {
    assertNotMatchesRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} does not match any substring of
   * {@code actual}, and fails with {@code message} if it does.  Note that you
   * can also use this for a literal string, by wrapping your expected string
   * in {@link Pattern#quote}.
   */
  public static void assertNotContainsRegex(
      String message, String expectedRegex, String actual) {
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (matcher.find()) {
      failContains(message, expectedRegex, actual);
    }
  }

  /**
   * Variant of {@code #assertNotContainsRegex(String,String,String)} using a
   * generic message.
   */
  public static void assertNotContainsRegex(
      String expectedRegex, String actual) {
    assertNotContainsRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, and in the same order.
   */
  public static void assertContentsInOrder(
      String message, Iterable<?> actual, Object... expected) {
    Assert.assertEquals(message,
        Arrays.asList(expected), Lists.newArrayList(actual));
  }

  /**
   * Variant of {@code #assertContentsInOrder(String,Iterable,Object...)}
   * using a generic message.
   */
  public static void assertContentsInOrder(
      Iterable<?> actual, Object... expected) {
    assertContentsInOrder((String) null, actual, expected);
  }

  /**
   * Asserts that {@code actual} contains precisely the elements
   * {@code expected}, in any order.  Both collections may contain
   * duplicates, and this method will only pass if the quantities are
   * exactly the same.
   */
  public static void assertContentsAnyOrder(
      String message, Iterable<?> actual, Object... expected) {
    Assert.assertEquals(message,
        Multisets.newHashMultiset(expected), Multisets.newHashMultiset(actual));
  }

  /**
   * Asserts that {@code collection} is empty.
   */
  public static void assertEmpty(String message, Collection<?> collection) {
    if (!collection.isEmpty()) {
      failNotEmpty(message, collection.toString());
    }
  }

  /**
   * Variant of {@code #assertEmpty(String, Collection)} using a
   * generic message.
   */
  public static void assertEmpty(Collection<?> collection) {
    assertEmpty(null, collection);
  }

  /**
   * Asserts that {@code map} is empty.
   */
  public static void assertEmpty(String message, Map<?,?> map) {
    if (!map.isEmpty()) {
      failNotEmpty(message, map.toString());
    }
  }

  /**
   * Variant of {@code #assertEmpty(String, Map)} using a generic
   * message.
   */
  public  static void assertEmpty(Map<?,?> map) {
    assertEmpty(null, map);
  }

  /**
   * Asserts that {@code collection} is not empty.
   */
  public static void assertNotEmpty(String message, Collection<?> collection) {
    if (collection.isEmpty()) {
      failEmpty(message);
    }
  }

  /**
   * Variant of {@code #assertNotEmpty(String, Collection<?>)}
   * using a generic message.
   */
  public static void assertNotEmpty(Collection<?> collection) {
    assertNotEmpty(null, collection);
  }

  /**
   * Asserts that {@code map} is not empty.
   */
  public static void assertNotEmpty(String message, Map<?,?> map) {
    if (map.isEmpty()) {
      failEmpty(message);
    }
  }

  /**
   * Variant of {@code #assertNotEmpty(String, Map)} using a generic
   * message.
   */
  public static void assertNotEmpty(Map<?,?> map) {
    assertNotEmpty(null, map);
  }

  /**
   * Variant of {@code #assertContentsAnyOrder(String,Iterable,Object...)}
   * using a generic message.
   */
  public static void assertContentsAnyOrder(
      Iterable<?> actual, Object... expected) {
    assertContentsAnyOrder((String) null, actual, expected);
  }

  /**
   * Utility for testing equals() and hashCode() results at once.
   * Tests that lhs.equals(rhs) matches expectedResult, as well as
   * rhs.equals(lhs).  Also tests that hashCode() return values are
   * equal if expectedResult is true.  (hashCode() is not tested if
   * expectedResult is false, as unequal objects can have equal hashCodes.)
   *
   * @param lhs An Object for which equals() and hashCode() are to be tested.
   * @param rhs As lhs.
   * @param expectedResult True if the objects should compare equal,
   *   false if not.
   */
  public static void checkEqualsAndHashCodeMethods(
      String message, Object lhs, Object rhs, boolean expectedResult) {

    if ((lhs == null) && (rhs == null)) {
      Assert.assertTrue(
          "Your check is dubious...why would you expect null != null?",
          expectedResult);
      return;
    }

    if ((lhs == null) || (rhs == null)) {
      Assert.assertFalse(
          "Your check is dubious...why would you expect an object "
          + "to be equal to null?", expectedResult);
    }

    if (lhs != null) {
      Assert.assertEquals(message, expectedResult, lhs.equals(rhs));
    }
    if (rhs != null) {
      Assert.assertEquals(message, expectedResult, rhs.equals(lhs));
    }

    if (expectedResult) {
      String hashMessage =
          "hashCode() values for equal objects should be the same";
      if (message != null) {
        hashMessage += ": " + message;
      }
      Assert.assertTrue(hashMessage, lhs.hashCode() == rhs.hashCode());
    }
  }

  /**
   * Variant of
   * {@code #checkEqualsAndHashCodeMethods(String,Object,Object,boolean...)}
   * using a generic message.
   */
  public static void checkEqualsAndHashCodeMethods(Object lhs, Object rhs,
                                             boolean expectedResult) {
    checkEqualsAndHashCodeMethods((String) null, lhs, rhs, expectedResult);
  }

  private static Matcher getMatcher(String expectedRegex, String actual) {
    Pattern pattern = Pattern.compile(expectedRegex);
    return pattern.matcher(actual);
  }

  private static void failEqual(String message, Object unexpected) {
    failWithMessage(message, "expected not to be:<" + unexpected + ">");
  }

  private static void failWrongLength(
      String message, int expected, int actual) {
    failWithMessage(message, "expected array length:<" + expected
        + "> but was:<" + actual + '>');
  }

  private static void failWrongElement(
      String message, int index, Object expected, Object actual) {
    failWithMessage(message, "expected array element[" + index + "]:<"
        + expected + "> but was:<" + actual + '>');
  }

  private static void failNotMatches(
      String message, String expectedRegex, String actual) {
    String actualDesc = (actual == null) ? "null" : ('<' + actual + '>');
    failWithMessage(message, "expected to match regex:<" + expectedRegex
        + "> but was:" + actualDesc);
  }

  private static void failNotContains(
      String message, String expectedRegex, String actual) {
    String actualDesc = (actual == null) ? "null" : ('<' + actual + '>');
    failWithMessage(message, "expected to contain regex:<" + expectedRegex
        + "> but was:" + actualDesc);
  }

  private static void failMatch(
      String message, String expectedRegex, String actual) {
    failWithMessage(message, "expected not to match regex:<" + expectedRegex
        + "> but was:<" + actual + '>');
  }

  private static void failContains(
      String message, String expectedRegex, String actual) {
    failWithMessage(message, "expected not to contain regex:<" + expectedRegex
        + "> but was:<" + actual + '>');
  }

  private static void failNotEmpty(
      String message, String actual) {
    failWithMessage(message, "expected to be empty, but contained: <"
        + actual + ">");
  }

  private static void failEmpty(String message) {
    failWithMessage(message, "expected not to be empty, but was");
  }

  private static void failWithMessage(String userMessage, String ourMessage) {
    Assert.fail((userMessage == null)
        ? ourMessage
        : userMessage + ' ' + ourMessage);
  }

  private static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }
}
