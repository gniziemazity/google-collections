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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;

/**
 * Standard comparators and utilities for creating and working with comparators.
 *
 * <p>{@link Comparator} instances returned by this class are serializable.
 *
 * @author jlevy@google.com (Jared Levy)
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
public final class Comparators {
  private Comparators() {}

  /**
   * Returns a comparator that uses the natural ordering of elements as defined
   * by {@link Comparable}.
   *
   * @see Collections#reverseOrder
   */
  @SuppressWarnings("unchecked")
  public static <C extends Comparable<? super C>> Comparator<C> naturalOrder() {
    return (Comparator<C>) NATURAL_ORDER;
  }

  /** @see #naturalOrder */
  private static final Comparator<Comparable<Object>> NATURAL_ORDER
      = new NaturalOrder();

  /** @see #naturalOrder */
  static class NaturalOrder
      implements Comparator<Comparable<Object>>, Serializable {
    private static final long serialVersionUID = 4773556737939767552L;
    public int compare(Comparable<Object> left, Comparable<Object> right) {
      return (left == right) ? 0 : left.compareTo(right);
    }
    private Object readResolve() {
      return NATURAL_ORDER; // preserve singleton property
    }
  }

  /**
   * Returns a comparator which tries each given comparator in order until a
   * non-zero result is found, returning this result, and returning zero only if
   * all comparators return zero.
   *
   * <p>The returned comparator is a "view" of the specified {@code rest} array;
   * changes to the array will be reflected in the behavior of the returned
   * comparator.
   *
   * @param primary the primary comparator
   * @param secondary the secondary comparator
   * @param rest additional comparators to invoke as necessary
   * @see #compound(Iterable)
   */
  public static <T> Comparator<T> compound(
      Comparator<? super T> primary,
      Comparator<? super T> secondary,
      Comparator<? super T>... rest) {
    checkNotNull(primary);
    checkNotNull(secondary);
    return compound(Lists.asList(primary, secondary, rest));
  }

  /**
   * Returns a comparator which tries each given comparator in order until a
   * non-zero result is found, returning this result, and returning zero only if
   * all comparators return zero.
   *
   * <p>The returned comparator is a "view" of the specified {@code Iterable}
   * instance; changes to the collection of comparators will be reflected in the
   * behavior of the returned comparator.
   *
   * @param comparators a collection of comparators to try in order
   * @see #compound(Comparator, Comparator, Comparator...)
   */
  public static <T> Comparator<T> compound(
      Iterable<? extends Comparator<? super T>> comparators) {
    return new CompoundOrder<T>(comparators);
  }

  /** @see #compound(Iterable) */
  static class CompoundOrder<T> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 5950260273184699058L;
    private final Iterable<? extends Comparator<? super T>> comparators;

    public CompoundOrder(
        Iterable<? extends Comparator<? super T>> comparators) {
      checkNotNull(comparators);
      this.comparators = comparators;
    }

    public int compare(T left, T right) {
      if (left == right) { // optimization
        return 0;
      }
      for (Comparator<? super T> comparator : comparators) {
        int result = comparator.compare(left, right);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }
  }

  /**
   * Creates a comparator that compares any two items by applying a function to
   * each of them and using the natural ordering of the results. The returned
   * comparator will generally fail if the function returns {@code null}.
   *
   * @param function the function returning the value to compare
   * @return the generated comparator
   */
  public static <F, T extends Comparable<? super T>> Comparator<F>
      fromFunction(Function<F,T> function) {
    return new TransformingNaturalOrder<F,T>(function);
  }

  /** @see #fromFunction(Function) */
  static class TransformingNaturalOrder<F, T extends Comparable<? super T>>
      implements Comparator<F>, Serializable {
    private static final long serialVersionUID = 4211028873657370047L;
    private final Function<F,T> function;

    public TransformingNaturalOrder(Function<F,T> function) {
      checkNotNull(function);
      this.function = function;
    }

    public int compare(F left, F right) {
      return function.apply(left).compareTo(function.apply(right));
    }
  }

  /**
   * Creates a comparator that compares any two items by applying a function to
   * each of them and using the supplied comparator to compare the results.
   *
   * @param function the function returning the value to compare
   * @param comparator the comparator that receives the function output
   * @return the generated comparator
   */
  public static <F,T> Comparator<F> fromFunction(Function<F,T> function,
      Comparator<? super T> comparator) {
    return new TransformingOrder<F,T>(function, comparator);
  }

  /** @see #fromFunction(Function, Comparator) */
  static class TransformingOrder<F,T> implements Comparator<F>, Serializable {
    private static final long serialVersionUID = 5364346520892770700L;
    private final Function<F,T> function;
    private final Comparator<? super T> comparator;

    public TransformingOrder(Function<F,T> function,
        Comparator<? super T> comparator) {
      checkNotNull(function);
      checkNotNull(comparator);
      this.function = function;
      this.comparator = comparator;
    }

    public int compare(F left, F right) {
      return comparator.compare(function.apply(left), function.apply(right));
    }
  }

  /**
   * A comparator that compares objects by the natural ordering of their string
   * representations as returned by {@link Object#toString}. Does not allow null
   * values.
   */
  public static final Comparator<Object> STRING_FORM_ORDER
      = new StringFormOrder();

  /** @see #STRING_FORM_ORDER */
  static class StringFormOrder implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = -8779076514758027173L;
    public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
    }
    private Object readResolve() {
      return STRING_FORM_ORDER; // preserve singleton property
    }
  }

  /*
   * TODO(kevinb): decorating comparator to sort nulls low or high
   *
   * TODO(kevinb): strengthening a comparator so a sorted set can 'act like'
   * either a hash set or an identity hash set
   */

  /**
   * Returns the smaller of the two values, according to their <i>natural
   * ordering</i>. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if less than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not
   *     <i>mutually comparable</i> (for example, strings and integers).
   */
  public static <T extends Comparable<? super T>> T min(T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return a.compareTo(b) <= 0 ? a : b;
  }

  /**
   * Returns the larger of the two values, according to their <i>natural
   * ordering</i>. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if greater than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not
   *     <i>mutually comparable</i> (for example, strings and integers).
   */
  public static <T extends Comparable<? super T>> T max(T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return a.compareTo(b) >= 0 ? a : b;
  }

  /**
   * Returns the smaller of the two values according to the specified
   * comparator. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if less than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not
   *     <i>mutually comparable</i> using the specified comparator.
   */
  public static <T> T min(Comparator<? super T> comparator, T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return comparator.compare(a, b) <= 0 ? a : b;
  }

  /**
   * Returns the larger of the two values according to the specified
   * comparator. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if greater than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not
   *     <i>mutually comparable</i> using the specified comparator.
   */
  public static <T> T max(Comparator<? super T> comparator, T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return comparator.compare(a, b) >= 0 ? a : b;
  }

  /**
   * Returns a comparator that imposes ascending frequency ordering on a
   * collection of objects, using the specified multiset to determine the
   * frequency of each object. This enables a simple idiom for sorting (or
   * maintaining) collections (or arrays) of objects that are sorted by
   * ascending frequency.  For example, suppose {@code m} is a multiset of
   * strings. Then:
   *
   * <pre>  Collections.max(m.elementSet(), frequencyOrder(m));</pre>
   *
   * returns a string that occurs most frequently in {@code m}.
   *
   * <p>The returned comparator is a view into the backing multiset, so the
   * comparator's behavior will change if the backing multiset changes. This can
   * be dangerous; for example, if the comparator is used by a {@code TreeSet}
   * and the backing multiset changes, the behavior of the {@code TreeSet}
   * becomes undefined. Use a copy of the multiset to isolate against such
   * changes when necessary.
   *
   * @param m the multiset specifying the frequency of objects to compare
   * @throws NullPointerException if {@code m} is null
   */
  public static <T> Comparator<T> frequencyOrder(Multiset<? extends T> m) {
    return new FrequencyOrder<T>(m);
  }

  /** @see #frequencyOrder */
  static class FrequencyOrder<T> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = -6424503578659119387L;
    private final Multiset<? extends T> multiset;

    public FrequencyOrder(Multiset<? extends T> multiset) {
      checkNotNull(multiset);
      this.multiset = multiset;
    }

    public int compare(T a, T b) {
      int ca = multiset.count(a);
      int cb = multiset.count(b);
      return (ca < cb) ? -1 : ((ca > cb) ? 1 : 0);
    }
  }

  /**
   * Compares the two specified {@code byte} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Byte(a).compareTo(new Byte(b))</pre>
   *
   * @param a the first {@code byte} to compare
   * @param b the second {@code byte} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   */
  public static int compare(byte a, byte b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code char} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Character(a).compareTo(new Character(b))</pre>
   *
   * @param a the first {@code char} to compare
   * @param b the second {@code char} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   */
  public static int compare(char a, char b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code short} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Short(a).compareTo(new Short(b))</pre>
   *
   * @param a the first {@code short} to compare
   * @param b the second {@code short} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   */
  public static int compare(short a, short b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code int} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Integer(a).compareTo(new Integer(b))</pre>
   *
   * @param a the first {@code int} to compare
   * @param b the second {@code int} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   */
  public static int compare(int a, int b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code long} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Long(a).compareTo(new Long(b))</pre>
   *
   * @param a the first {@code long} to compare
   * @param b the second {@code long} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   */
  public static int compare(long a, long b) {
    return (a < b) ? -1 : ((a > b) ? 1 : 0);
  }

  /**
   * Compares the two specified {@code double} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Double(a).compareTo(new Double(b))</pre>
   *
   * @param a the first {@code double} to compare
   * @param b the second {@code double} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   * @see Double#compare
   */
  public static int compare(double a, double b) {
    return Double.compare(a, b); // takes care of Double.NaN
  }

  /**
   * Compares the two specified {@code float} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Float(a).compareTo(new Float(b))</pre>
   *
   * @param a the first {@code float} to compare
   * @param b the second {@code float} to compare
   * @return a negative value if {@code a} is less than {@code b}; a positive
   *     value if {@code a} is greater than {@code b}; otherwise zero.
   * @see Float#compare
   */
  public static int compare(float a, float b) {
    return Float.compare(a, b); // takes care of Float.NaN
  }

  /**
   * Compares the two specified {@code boolean} values. The sign of the value
   * returned is the same as that of the value that would be returned by the
   * call:
   *
   * <pre>  new Boolean(a).compareTo(new Boolean(b))</pre>
   *
   * @param a the first {@code boolean} to compare
   * @param b the second {@code boolean} to compare
   * @return a negative value if {@code a} is false and {@code b} is true; a
   *     positive value if {@code a} is true and {@code b} is false; otherwise
   *     zero.
   */
  public static int compare(boolean a, boolean b) {
    return (a == b) ? 0 : (a ? 1 : -1);
  }
}
