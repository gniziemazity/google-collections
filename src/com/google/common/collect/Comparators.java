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
import com.google.common.base.Nullable;
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Standard comparators and utilities for creating and working with comparators.
 *
 * <p>{@link Comparator} instances returned by this class are serializable.
 *
 * @author Jared Levy
 * @author Kevin Bourrillion
 * @author Mike Bostock
 */
public final class Comparators {
  private Comparators() {}

  /**
   * Returns a comparator that uses the natural ordering of values as defined by
   * {@code Comparable}.
   *
   * @see java.util.Collections#reverseOrder()
   */
  @SuppressWarnings("unchecked")
  public static <C extends Comparable> Comparator<C> naturalOrder() {
    return (Comparator<C>) NATURAL_ORDER;
  }

  /** @see #naturalOrder */
  @SuppressWarnings("unchecked")
  private static final Comparator<Comparable> NATURAL_ORDER
      = new SerializableComparator<Comparable>() {
    public int compare(Comparable left, Comparable right) {
      if ((left == right)) {
        return 0;
      }

      /*
       * compareTo() may throw a ClassCastException if the elements are not
       * mutually comparable.
       */
      @SuppressWarnings("unchecked")
      int result = left.compareTo(right);
      return result;
    }

    // preserve singleton-ness, so equals() and hashCode() work correctly
    private Object readResolve() {
      return NATURAL_ORDER;
    }

    private static final long serialVersionUID = 4773556737939767552L;
  };

  /**
   * Returns a comparator that treats {@code null} as less than all other
   * values, and uses {@code comparator} to compare non-null values.
   */
  public static <T> Comparator<T> nullLeastOrder(Comparator<T> comparator) {
    checkNotNull(comparator);
    return new NullHandlingComparator<T>(comparator) {
      @Override int compareNullAndNonNull() {
        return -1;
      }
      private static final long serialVersionUID = 0x5AF3C26EB419D807L;
    };
  }

  /**
   * Returns a comparator that uses the natural ordering of values, but also
   * handles null values, treating them as less than all other values.
   */
  @SuppressWarnings("unchecked")
  public static <C extends Comparable> Comparator<C> nullLeastOrder() {
    return (Comparator<C>) NULL_LEAST_ORDER;
  }

  @SuppressWarnings("unchecked")
  private static final Comparator<Comparable> NULL_LEAST_ORDER
      = nullLeastOrder(NATURAL_ORDER);

  /**
   * Returns a comparator that treats {@code null} as greater than all other
   * values, and uses the given comparator to compare non-null values.
   */
  public static <T> Comparator<T> nullGreatestOrder(Comparator<T> comparator) {
    checkNotNull(comparator);
    return new NullHandlingComparator<T>(comparator) {
      @Override int compareNullAndNonNull() {
        return 1;
      }
      private static final long serialVersionUID = 0xB17D30AE62485CF9L;
    };
  }

  /**
   * Returns a comparator that uses the natural ordering of values, but also
   * handles null values, treating them as greater than all other values.
   */
  @SuppressWarnings("unchecked")
  public static <C extends Comparable> Comparator<C> nullGreatestOrder() {
    return (Comparator<C>) NULL_GREATEST_ORDER;
  }

  @SuppressWarnings("unchecked")
  private static final Comparator<Comparable> NULL_GREATEST_ORDER
      = nullGreatestOrder(NATURAL_ORDER);

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
   * @see #compound(List)
   */
  @SuppressWarnings("unchecked") // TODO: check that this is right
  public static <T> Comparator<T> compound(Comparator<? super T> primary,
      Comparator<? super T> secondary, Comparator<? super T>... rest) {
    checkNotNull(primary);
    checkNotNull(secondary);
    checkNotNull(rest);

    // TODO: is this really the best way?  if so, explain why.
    Comparator<T> primaryT = (Comparator<T>) primary;
    Comparator<T> secondaryT = (Comparator<T>) secondary;
    Comparator<T>[] restT = (Comparator<T>[]) rest;
    return compound(Lists.asList(primaryT, secondaryT, restT));
  }

  /**
   * Returns a comparator which tries each given comparator in order until a
   * non-zero result is found, returning this result, and returning zero only if
   * all comparators return zero.
   *
   * <p>The returned comparator is a "view" of the specified {@code List}
   * instance; changes to the collection of comparators will be reflected in the
   * behavior of the returned comparator.
   *
   * @param comparators a collection of comparators to try in order
   */
  public static <T> Comparator<T> compound(
      List<? extends Comparator<? super T>> comparators) {
    return new CompoundOrder<T>(comparators);
  }

  /** @see Comparators#compound(List) */
  static class CompoundOrder<T> implements SerializableComparator<T> {
    private final List<? extends Comparator<? super T>> comparators;

    public CompoundOrder(List<? extends Comparator<? super T>> comparators) {
      checkNotNull(comparators);
      this.comparators = comparators;
    }

    public int compare(T left, T right) {
      if (left == right) {
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

    @Override public boolean equals(Object object) {
      if (object instanceof CompoundOrder<?>) {
        CompoundOrder<?> that = (CompoundOrder<?>) object;
        return (this.comparators).equals(that.comparators);
      }
      return false;
    }

    @Override public int hashCode() {
      return comparators.hashCode();
    }

    private static final long serialVersionUID = 5950260273184699058L;
  }

  /**
   * Creates a comparator that compares any two items by applying a function to
   * each of them and using the natural ordering of the results. The returned
   * comparator will generally fail if the function returns {@code null}.
   *
   * @param function the function returning the value to compare
   * @return the generated comparator
   */
  @SuppressWarnings("unchecked")
  public static <F, T extends Comparable> Comparator<F>
      fromFunction(Function<F, T> function) {
    return new TransformingNaturalOrder<F, T>(function);
  }

  /** @see Comparators#fromFunction(Function) */
  @SuppressWarnings("unchecked")
  static class TransformingNaturalOrder<F, T extends Comparable>
      implements Comparator<F>, Serializable {
    private final Function<F, T> function;

    public TransformingNaturalOrder(Function<F, T> function) {
      checkNotNull(function);
      this.function = function;
    }

    public int compare(F left, F right) {
      T leftTransformed = function.apply(left);
      T rightTransformed = function.apply(right);

      /*
       * Let this throw a ClassCastException if T is a bizarre Comparable that
       * can't be compared to itself.
       */
      @SuppressWarnings("unchecked")
      int result = leftTransformed.compareTo(rightTransformed);
      return result;
    }

    @Override public boolean equals(Object object) {
      if (object instanceof TransformingNaturalOrder<?, ?>) {
        TransformingNaturalOrder<?, ?> that
            = (TransformingNaturalOrder<?, ?>) object;
        return (this.function).equals(that.function);
      }
      return false;
    }

    @Override public int hashCode() {
      return function.hashCode();
    }

    private static final long serialVersionUID = 4211028873657370047L;
  }

  /**
   * Creates a comparator that compares any two items by applying a function to
   * each of them and using the supplied comparator to compare the results.
   *
   * @param function the function returning the value to compare
   * @param comparator the comparator that receives the function output
   * @return the generated comparator
   */
  public static <F, T> Comparator<F> fromFunction(
      Function<F, T> function, Comparator<? super T> comparator) {
    return new TransformingOrder<F, T>(function, comparator);
  }

  /** @see Comparators#fromFunction(Function,Comparator) */
  static class TransformingOrder<F, T> implements SerializableComparator<F> {
    private final Function<F, T> function;
    private final Comparator<? super T> comparator;

    public TransformingOrder(
        Function<F, T> function, Comparator<? super T> comparator) {
      checkNotNull(function);
      checkNotNull(comparator);
      this.function = function;
      this.comparator = comparator;
    }

    public int compare(F left, F right) {
      return comparator.compare(function.apply(left), function.apply(right));
    }

    @Override public boolean equals(Object object) {
      if (object instanceof TransformingOrder<?, ?>) {
        TransformingOrder<?, ?> that = (TransformingOrder<?, ?>) object;
        return (this.function).equals(that.function)
            && (this.comparator).equals(that.comparator);
      }
      return false;
    }

    @Override public int hashCode() {
      return Objects.hashCode(function, comparator);
    }

    private static final long serialVersionUID = 5364346520892770700L;
  }

  /**
   * A comparator that compares objects by the natural ordering of their string
   * representations as returned by {@code toString}. Does not allow null
   * values.
   */
  public static final Comparator<Object> STRING_FORM_ORDER
      = new SerializableComparator<Object>() {
    public int compare(Object left, Object right) {
      return left.toString().compareTo(right.toString());
    }

    // preserve singleton-ness, so equals() and hashCode() work correctly
    private Object readResolve() {
      return STRING_FORM_ORDER;
    }

    private static final long serialVersionUID = -8779076514758027173L;
  };

  /**
   * Returns the smaller of the two values, according to their <i>natural
   * ordering</i>. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if less than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> (for example, strings and integers).
   */
  @SuppressWarnings("unchecked")
  public static <T extends Comparable> T min(T a, T b) {
    checkNotNull(a);
    checkNotNull(b);

    /*
     * Let this throw a ClassCastException if T is a bizarre Comparable that
     * can't be compared to itself, as documented.
     */
    @SuppressWarnings("unchecked")
    int result = a.compareTo(b);
    return result <= 0 ? a : b;
  }

  /**
   * Returns the larger of the two values, according to their <i>natural
   * ordering</i>. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if greater than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> (for example, strings and integers).
   */
  @SuppressWarnings("unchecked")
  public static <T extends Comparable> T max(T a, T b) {
    checkNotNull(a);
    checkNotNull(b);

    /*
     * Let this throw a ClassCastException if T is a bizarre Comparable that
     * can't be compared to itself, as documented.
     */
    @SuppressWarnings("unchecked")
    int result = a.compareTo(b);
    return result >= 0 ? a : b;
  }

  /**
   * Returns the smaller of the two values according to the specified
   * comparator. If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if less than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> using the specified comparator.
   */
  public static <T> T min(Comparator<? super T> comparator, T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return comparator.compare(a, b) <= 0 ? a : b;
  }

  /**
   * Returns the larger of the two values according to the specified comparator.
   * If the values are equal, the first is returned.
   *
   * @param a non-null value to compare, returned if greater than or equal to b.
   * @param b non-null value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> using the specified comparator.
   */
  public static <T> T max(Comparator<? super T> comparator, T a, T b) {
    checkNotNull(a);
    checkNotNull(b);
    return comparator.compare(a, b) >= 0 ? a : b;
  }

  /**
   * Returns a comparator that compares objects by the order in which they
   * appear in a given list. Only objects present in the list (according to
   * {@link Object#equals}) may be compared. This comparator imposes a "partial
   * ordering" over the type {@code T}. Subsequent changes to the {@code
   * valuesInOrder} list will have no effect on the returned comparator.
   *
   * @param valuesInOrder the values that the returned comparator will be able
   *     to compare, in the order the comparator should follow
   * @return the comparator described above
   * @throws IllegalArgumentException if {@code valuesInOrder} contains any
   *     duplicate values (according to {@link Object#equals})
   */
  public static <T> Comparator<T> givenOrder(List<T> valuesInOrder) {
    checkNotNull(valuesInOrder);
    return new GivenOrder<T>(valuesInOrder);
  }

  /**
   * Returns a comparator that compares objects by the order in which they are
   * given to this method. Only objects present in the argument list (according
   * to {@link Object#equals}) may be compared. This comparator imposes a
   * "partial ordering" over the type {@code T}.
   *
   * @param leastValue the value which the returned comparator should consider
   *     the "least" of all values
   * @param remainingValuesInOrder the rest of the values that the returned
   *     comparator will be able to compare, in the order the comparator should
   *     follow
   * @return the comparator described above
   * @throws IllegalArgumentException if any duplicate values (according to
   *     {@link Object#equals}) are present among {@code leastValue} and {@code
   *     remainingValuesInOrder}
   */
  public static <T> Comparator<T> givenOrder(
      @Nullable T leastValue, T... remainingValuesInOrder) {
    return givenOrder(Lists.asList(leastValue, remainingValuesInOrder));
  }

  /** @see Comparators#givenOrder(List) */
  private static class GivenOrder<T> implements SerializableComparator<T> {
    final Map<T, Integer> rankMap;

    GivenOrder(List<T> valuesInOrder) {
      rankMap = buildRankMap(valuesInOrder);
    }

    public int compare(T left, T right) {
      return rank(left) - rank(right); // both are nonnegative
    }

    int rank(T value) {
      Integer rank = rankMap.get(value);
      if (rank == null) {
        throw new IncomparableValueException(value);
      }
      return rank;
    }

    static <T> Map<T, Integer> buildRankMap(Collection<T> valuesInOrder) {
      Map<T, Integer> ranks
          = Maps.newHashMapWithExpectedSize(valuesInOrder.size());
      int rank = 0;
      for (T value : valuesInOrder) {
        Integer priorRank = ranks.put(value, rank);
        if (priorRank != null) {
          throw new DuplicateValueException(value, priorRank, rank);
        }
        rank++;
      }
      return ranks;
    }

    @Override public boolean equals(Object object) {
      if (object instanceof GivenOrder<?>) {
        GivenOrder<?> that = (GivenOrder<?>) object;
        return (this.rankMap).equals(that.rankMap);
      }
      return false;
    }

    @Override public int hashCode() {
      return rankMap.hashCode();
    }

    private static final long serialVersionUID = 1841692415248046482L;
  }

  /**
   * Exception thrown by a "partial-ordering" comparator when asked to compare
   * at least one value outside the set of values it can compare. Extending
   * ClassCastException may seem odd, but it fits the spirit of the
   * Comparator.compare() specification, if you consider that we are handling
   * what is conceptually a "subtype" of T.
   */
  // @VisibleForTesting
  static class IncomparableValueException extends ClassCastException {
    final Object value;

    IncomparableValueException(Object value) {
      super("Cannot compare value: " + value);
      this.value = value;
    }
  }

  /**
   * Exception thrown when a duplicate value is found in a list or array which
   * is not expected to contain any. TODO: this can probably be reused in a
   * couple places.
   */
  // @VisibleForTesting
  static class DuplicateValueException extends IllegalArgumentException {
    final Object value;
    final int firstIndex;
    final int secondIndex;

    DuplicateValueException(Object value, int firstIndex, int secondIndex) {
      super(String.format("Duplicate value at indices %s and %s: %s",
          firstIndex, secondIndex, value));
      this.value = value;
      this.firstIndex = firstIndex;
      this.secondIndex = secondIndex;
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

  private static abstract class NullHandlingComparator<T>
      implements SerializableComparator<T> {
    final Comparator<T> comparator;

    public NullHandlingComparator(Comparator<T> comparator) {
      this.comparator = comparator;
    }

    public int compare(T left, T right) {
      if (left == right) {
        return 0;
      }
      if (left == null) {
        return compareNullAndNonNull();
      }
      if (right == null) {
        return -compareNullAndNonNull();
      }
      return comparator.compare(left, right);
    }

    /**
     * Returns the value this comparator should produce when comparing {@code
     * null} to any non-null value (in that order).
     */
    abstract int compareNullAndNonNull();

    @Override public boolean equals(Object object) {
      if (object.getClass() == getClass()) {
        NullHandlingComparator<?> that = (NullHandlingComparator<?>) object;
        return (this.comparator).equals(that.comparator);
      }
      return false;
    }

    @Override public int hashCode() {
      return comparator.hashCode();
    }
  }
}
