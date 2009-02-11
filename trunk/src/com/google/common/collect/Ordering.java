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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A comparator with added methods to support common functions. For example:
 * <pre>   {@code
 *
 *   if (Ordering.from(plainComparator).reverse().isSorted(list)) { ... }}</pre>
 *
 * The orderings returned by the factory methods of this class are serializable
 * if and only if the provided instances that back them are. For example, if
 * {@code function} can be serialized, {@code Ordering.from(function)} can as
 * well.
 *
 * @author Jesse Wilson
 */
public abstract class Ordering<T> implements Comparator<T> {
  // Static factories

  /**
   * Returns a serializable ordering that uses the natural order of the values.
   * The ordering throws a {@link NullPointerException} when passed a null
   * parameter.
   *
   * <p>The type specification is {@code <C extends Comparable>}, instead of
   * the technically correct {@code <C extends Comparable<? super C>>}, to
   * support legacy types from before Java 5.
   */
  @SuppressWarnings("unchecked") // TODO: the right way to explain this??
  public static <C extends Comparable> Ordering<C> natural() {
    return (Ordering) NATURAL_ORDER;
  }

  private static final NaturalOrdering NATURAL_ORDER = new NaturalOrdering();

  private static class NaturalOrdering extends Ordering<Comparable>
      implements Serializable {
    public int compare(Comparable left, Comparable right) {
      checkNotNull(right); // left null is caught later
      if (left == right) {
        return 0;
      }

      @SuppressWarnings("unchecked") // we're permitted to throw CCE
      int result = left.compareTo(right);
      return result;
    }

    // preserving singleton-ness gives equals()/hashCode() for free
    private Object readResolve() {
      return NATURAL_ORDER;
    }

    @Override public String toString() {
      return "Ordering.natural()";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns an ordering for a pre-existing {@code comparator}. Note
   * that if the comparator is not pre-existing, and you don't require
   * serialization, you can subclass {@code Ordering} and implement its
   * {@link #compare(Object, Object) compare} method instead.
   *
   * @param comparator the comparator that defines the order
   */
  public static <T> Ordering<T> from(final Comparator<T> comparator) {
    return (comparator instanceof Ordering)
        ? (Ordering<T>) comparator
        : new ComparatorOrdering<T>(comparator);
  }

  private static final class ComparatorOrdering<T> extends Ordering<T>
      implements Serializable {
    final Comparator<T> comparator;

    ComparatorOrdering(Comparator<T> comparator) {
      this.comparator = checkNotNull(comparator);
    }

    public int compare(T a, T b) {
      return comparator.compare(a, b);
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ComparatorOrdering) {
        ComparatorOrdering<?> that = (ComparatorOrdering<?>) object;
        return this.comparator.equals(that.comparator);
      }
      return false;
    }

    @Override public int hashCode() {
      return comparator.hashCode();
    }

    @Override public String toString() {
      return comparator.toString();
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns an ordering that compares objects according to the order in
   * which they appear in the given list. Only objects present in the list
   * (according to {@link Object#equals}) may be compared. This comparator
   * imposes a "partial ordering" over the type {@code T}. Subsequent changes
   * to the {@code valuesInOrder} list will have no effect on the returned
   * comparator. Null values in the list are supported.
   *
   * <p>The returned comparator throws an {@link ClassCastException} when it
   * receives an input parameter that isn't among the provided values.
   *
   * <p>The generated comparator is serializable if all the provided values are
   * serializable.
   *
   * @param valuesInOrder the values that the returned comparator will be able
   *     to compare, in the order the comparator should induce
   * @return the comparator described above
   * @throws IllegalArgumentException if {@code valuesInOrder} contains any
   *     non-consecutive duplicate values (according to {@link Object#equals})
   */
  public static <T> Ordering<T> givenOrder(List<? extends T> valuesInOrder) {
    return new GivenOrder<T>(valuesInOrder);
  }

  /**
   * Returns an ordering that compares objects according to the order in
   * which they are given to this method. Only objects present in the argument
   * list (according to {@link Object#equals}) may be compared. This comparator
   * imposes a "partial ordering" over the type {@code T}. Null values in the
   * argument list are supported.
   *
   * <p>The returned comparator throws a {@link ClassCastException} when it
   * receives an input parameter that isn't among the provided values.
   *
   * <p>The generated comparator is serializable if all the provided values are
   * serializable.
   *
   * @param leastValue the value which the returned comparator should consider
   *     the "least" of all values
   * @param remainingValuesInOrder the rest of the values that the returned
   *     comparator will be able to compare, in the order the comparator should
   *     follow
   * @return the comparator described above
   * @throws IllegalArgumentException if any non-consecutive duplicate values
   *     (according to {@link Object#equals(Object)}) are present among the
   *     method arguments
   */
  public static <T> Ordering<T> givenOrder(
      @Nullable T leastValue, T... remainingValuesInOrder) {
    // TODO: use Lists.asList() when gwt issues are resolved
    List<T> list = new ArrayList<T>();
    list.add(leastValue);
    Collections.addAll(list, remainingValuesInOrder);
    return givenOrder(list);
  }

  private static class GivenOrder<T> extends Ordering<T>
      implements Serializable {
    // TODO: change to ImmutableMap when that's gwtable
    // (requires that we stop supporting null.)
    final Map<T, Integer> rankMap;

    GivenOrder(List<? extends T> valuesInOrder) {
      rankMap = buildRankMap(valuesInOrder);
    }

    public int compare(T left, T right) {
      return rank(left) - rank(right); // safe because both are nonnegative
    }

    int rank(T value) {
      Integer rank = rankMap.get(value);
      if (rank == null) {
        throw new IncomparableValueException(value);
      }
      return rank;
    }

    static <T> Map<T, Integer> buildRankMap(
        Collection<? extends T> valuesInOrder) {
      // TODO: change to ImmutableMap when that's gwtable
      // (requires that we stop supporting null.)
      Map<T, Integer> ranks
          = new LinkedHashMap<T, Integer>(valuesInOrder.size() * 2);
      T previousValue = null;
      int rank = 0;
      for (T value : valuesInOrder) {
        if ((rank == 0) || !Objects.equal(value, previousValue)) {
          Integer priorRank = ranks.put(value, rank);
          if (priorRank != null) {
            throw new DuplicateValueException(value, priorRank, rank);
          }
        }
        rank++;
        previousValue = value;
      }
      return ranks;
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof GivenOrder) {
        GivenOrder<?> that = (GivenOrder<?>) object;
        return this.rankMap.equals(that.rankMap);
      }
      return false;
    }

    @Override public int hashCode() {
      return rankMap.hashCode();
    }

    @Override public String toString() {
      return "Ordering.givenOrder(" + rankMap.keySet() + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Exception thrown by a {@link Ordering#givenOrder(List)} or {@link
   * Ordering#givenOrder(Object, Object...)} comparator when comparing a value
   * outside the set of values it can compare. Extending {@link
   * ClassCastException} may seem odd, but it is required.
   *
   * <p>Package-private for testing.
   */
  static class IncomparableValueException extends ClassCastException {
    final Object value;

    IncomparableValueException(Object value) {
      super("Cannot compare value: " + value);
      this.value = value;
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Exception thrown when a duplicate value is found in a list or array which
   * is not expected to contain any.
   *
   * <p>Package-private for testing.
   */
  static class DuplicateValueException extends IllegalArgumentException {
    private static final long serialVersionUID = 0;
    final Object value;
    final int firstIndex;
    final int secondIndex;

    DuplicateValueException(Object value, int firstIndex, int secondIndex) {
      super("Duplicate value at indices " + firstIndex + " and " + secondIndex
          + ": " + value + "");
      this.value = value;
      this.firstIndex = firstIndex;
      this.secondIndex = secondIndex;
    }
  }

  /**
   * Returns an ordering which tries each given comparator in order until a
   * non-zero result is found, returning that result, and returning zero only if
   * all comparators return zero. The returned ordering is based on the state of
   * the {@code comparators} iterable at the time it was provided to this
   * method.
   *
   * <p>The returned ordering is equivalent to that produced using {@code
   * Ordering.from(comp1).compound(comp2).compound(comp3) . . .}.
   *
   * <p><b>Warning:</b> Supplying an argument with undefined iteration order,
   * such as a {@link HashSet}, will produce non-deterministic results.
   *
   * @param comparators the comparators to try in order
   */
  public static <T> Ordering<T> compound(
      Iterable<? extends Comparator<? super T>> comparators) {
    return new CompoundOrdering<T>(comparators);
  }

  private static class CompoundOrdering<T> extends Ordering<T>
      implements Serializable {
    // TODO: use ImmutableList when available to gwt
    final List<Comparator<? super T>> comparators;

    CompoundOrdering(Comparator<? super T> primary,
        Comparator<? super T> secondary) {
      this.comparators = new ArrayList<Comparator<? super T>>();
      this.comparators.add(primary);
      this.comparators.add(secondary);
    }

    CompoundOrdering(Iterable<? extends Comparator<? super T>> comparators) {
      this.comparators = new ArrayList<Comparator<? super T>>();
      for (Comparator<? super T> comparator : comparators) {
        this.comparators.add(checkNotNull(comparator));
      }
    }

    CompoundOrdering(List<? extends Comparator<? super T>> comparators,
        Comparator<? super T> lastComparator) {
      this.comparators = new ArrayList<Comparator<? super T>>();
      this.comparators.addAll(comparators);
      this.comparators.add(lastComparator);
    }

    public int compare(T left, T right) {
      for (Comparator<? super T> comparator : comparators) {
        int result = comparator.compare(left, right);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }

    @Override
    public <U extends T> Ordering<U> compound(Comparator<? super U> secondary) {
      return new CompoundOrdering<U>(comparators, checkNotNull(secondary));
    }

    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof CompoundOrdering) {
        CompoundOrdering<?> that = (CompoundOrdering<?>) object;
        return this.comparators.equals(that.comparators);
      }
      return false;
    }

    @Override public int hashCode() {
      return comparators.hashCode();
    }

    @Override public String toString() {
      return "Ordering.compound(" + comparators + ")";
    }

    private static final long serialVersionUID = 0;
  }

  // Non-static factories

  /**
   * Returns an ordering which first uses the ordering {@code this}, but which
   * in the event of a "tie", then delegates to {@code secondaryComparator}.
   * For example, to sort a bug list first by status and second by priority, you
   * might use {@code byStatus.compound(byPriority)}. For a compound ordering
   * with three or more components, simply chain multiple calls to this method.
   *
   * <p>An ordering produced by this method, or a chain of calls to this method,
   * is equivalent to one created using {@link Ordering#compound(Iterable)} on
   * the same component comparators.
   */
  public <U extends T> Ordering<U> compound(
      Comparator<? super U> secondaryComparator) {
    return new CompoundOrdering<U>(this, checkNotNull(secondaryComparator));
  }

  /**
   * Returns the reverse of this ordering; the {@code Ordering} equivalent to
   * {@link Collections#reverseOrder(Comparator)}.
   */
  public Ordering<T> reverse() {
    return new ReverseOrdering<T>(this);
  }

  private static class ReverseOrdering<T> extends Ordering<T>
      implements Serializable {
    final Ordering<T> forwardOrder;

    ReverseOrdering(Ordering<T> forwardOrder) {
      this.forwardOrder = checkNotNull(forwardOrder);
    }

    @Override public Ordering<T> reverse() {
      return forwardOrder;
    }

    public int compare(T a, T b) {
      return forwardOrder.compare(b, a);
    }

    @Override public int hashCode() {
      return -forwardOrder.hashCode();
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ReverseOrdering) {
        ReverseOrdering<?> that = (ReverseOrdering<?>) object;
        return this.forwardOrder.equals(that.forwardOrder);
      }
      return false;
    }

    @Override public String toString() {
      return forwardOrder + ".reverse()";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a new ordering on {@code F} which orders elements by first applying
   * a function to them, then comparing those results using {@code this}. For
   * example, to compare objects by their string forms, in a case-insensitive
   * manner, use: <pre>   {@code
   *
   *   Ordering.from(String.CASE_INSENSITIVE_ORDER)
   *       .onResultOf(Functions.toStringFunction())}</pre>
   */
  public <F> Ordering<F> onResultOf(Function<F, ? extends T> function) {
    return new ByFunctionOrdering<F, T>(function, this);
  }

  private static class ByFunctionOrdering<F, T> extends Ordering<F>
      implements Serializable {
    final Function<F, ? extends T> function;
    final Ordering<T> ordering;

    ByFunctionOrdering(
        Function<F, ? extends T> function, Ordering<T> ordering) {
      this.function = checkNotNull(function);
      this.ordering = checkNotNull(ordering);
    }

    public int compare(F left, F right) {
      return ordering.compare(function.apply(left), function.apply(right));
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ByFunctionOrdering) {
        ByFunctionOrdering<?, ?> that = (ByFunctionOrdering<?, ?>) object;
        return this.function.equals(that.function)
            && this.ordering.equals(that.ordering);
      }
      return false;
    }

    @Override public int hashCode() {
      return Objects.hashCode(function, ordering);
    }

    @Override public String toString() {
      return ordering + ".onResultOf(" + function + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns an ordering that treats {@code null} as less than all other values
   * and uses {@code this} to compare non-null values.
   */
  public Ordering<T> nullsFirst() {
    return new NullsFirstOrdering<T>(this);
  }

  private static class NullsFirstOrdering<T> extends Ordering<T>
      implements Serializable {
    final Ordering<T> ordering;

    NullsFirstOrdering(Ordering<T> ordering) {
      this.ordering = ordering;
    }

    public int compare(T left, T right) {
      if (left == right) {
        return 0;
      }
      if (left == null) {
        return -1;
      }
      if (right == null) {
        return 1;
      }
      return ordering.compare(left, right);
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof NullsFirstOrdering) {
        NullsFirstOrdering<?> that = (NullsFirstOrdering<?>) object;
        return this.ordering.equals(that.ordering);
      }
      return false;
    }

    @Override public int hashCode() {
      return ordering.hashCode(); // TODO: adjust it how?
    }

    @Override public String toString() {
      return ordering + ".nullsFirst()";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns an ordering that treats {@code null} as greater than all other
   * values and uses this ordering to compare non-null values.
   */
  public Ordering<T> nullsLast() {
    return new NullsLastOrdering<T>(this);
  }

  private static class NullsLastOrdering<T> extends Ordering<T>
      implements Serializable {
    final Ordering<T> ordering;

    NullsLastOrdering(Ordering<T> ordering) {
      this.ordering = ordering;
    }

    public int compare(T left, T right) {
      if (left == right) {
        return 0;
      }
      if (left == null) {
        return 1;
      }
      if (right == null) {
        return -1;
      }
      return ordering.compare(left, right);
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof NullsLastOrdering) {
        NullsLastOrdering<?> that = (NullsLastOrdering<?>) object;
        return this.ordering.equals(that.ordering);
      }
      return false;
    }

    @Override public int hashCode() {
      return ordering.hashCode(); // TODO: adjust it how?
    }

    @Override public String toString() {
      return ordering + ".nullsLast()";
    }

    private static final long serialVersionUID = 0;
  }

  // The rest

  /**
   * {@link Collections#binarySearch(List, Object, Comparator) Searches}
   * {@code sortedList} for {@code key} using the binary search algorithm. The
   * list must be sorted using this ordering.
   *
   * @param sortedList the list to be searched
   * @param key the key to be searched for
   */
  public int binarySearch(List<? extends T> sortedList, T key) {
    return Collections.binarySearch(sortedList, key, this);
  }

  /**
   * Returns a copy of the given iterable sorted by this ordering. The input is
   * not modified. The returned list is modifiable, serializable, and has random
   * access.
   *
   * <p>Unlike {@link Sets#newTreeSet(Iterable)}, this method does not collapse
   * elements that compare as zero, and the resulting collection does not
   * maintain its own sort order.
   *
   * @param iterable the elements to be copied and sorted
   * @return a new list containing the given elements in sorted order
   */
  public <E extends T> List<E> sortedCopy(Iterable<E> iterable) {
    List<E> list = newArrayList(iterable);
    Collections.sort(list, this);
    return list;
  }

  /**
   * Returns {@code true} if each element in {@code iterable} after the first is
   * greater than or equal to the element that preceded it, according to this
   * ordering. Note that this is always true when the iterable has fewer than
   * two elements.
   */
  public boolean isOrdered(Iterable<? extends T> iterable) {
    Iterator<? extends T> it = iterable.iterator();
    if (it.hasNext()) {
      T prev = it.next();
      while (it.hasNext()) {
        T next = it.next();
        if (compare(prev, next) > 0) {
          return false;
        }
        prev = next;
      }
    }
    return true;
  }

  /**
   * Returns {@code true} if each element in {@code iterable} after the first is
   * <i>strictly</i> greater than the element that preceded it, according to
   * this ordering. Note that this is always true when the iterable has fewer
   * than two elements.
   */
  public boolean isStrictlyOrdered(Iterable<? extends T> iterable) {
    Iterator<? extends T> it = iterable.iterator();
    if (it.hasNext()) {
      T prev = it.next();
      while (it.hasNext()) {
        T next = it.next();
        if (compare(prev, next) >= 0) {
          return false;
        }
        prev = next;
      }
    }
    return true;
  }

  /**
   * Inlined {@link Lists#newArrayList(Iterable)} here because {@link Lists}
   * is not GWT-safe.
   *
   * <p>TODO(hhchan): Remove this once Lists is GWT-safe.
   */
  private static <E> List<E> newArrayList(Iterable<E> iterable) {
    if (iterable instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<? extends E> collection = (Collection<? extends E>) iterable;
      return new ArrayList<E>(collection);
    } else {
      Iterator<E> iterator = iterable.iterator();
      ArrayList<E> list = new ArrayList<E>();
      while (iterator.hasNext()) {
        list.add(iterator.next());
      }
      return list;
    }
  }

  /**
   * Returns the largest of the specified values according to this ordering. If
   * there are multiple largest values, the first of those is returned.
   *
   * @param iterable the iterable whose maximum element is to be determined
   * @throws java.util.NoSuchElementException if {@code iterable} is empty
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E max(Iterable<E> iterable) {
    Iterator<E> iterator = iterable.iterator();

    // let this throw NoSuchElementException as necessary
    E maxSoFar = iterator.next();

    while (iterator.hasNext()) {
      maxSoFar = max(maxSoFar, iterator.next());
    }

    return maxSoFar;
  }

  /**
   * Returns the largest of the specified values according to this ordering. If
   * there are multiple largest values, the first of those is returned.
   *
   * @param a value to compare, returned if greater than or equal to the rest.
   * @param b value to compare
   * @param c value to compare
   * @param rest values to compare
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E max(E a, E b, E c, E... rest) {
    E maxSoFar = max(max(a, b), c);

    for (E r : rest) {
      maxSoFar = max(maxSoFar, r);
    }

    return maxSoFar;
  }

  /**
   * Returns the larger of the two values according to this ordering. If the
   * values compare as 0, the first is returned.
   *
   * @param a value to compare, returned if greater than or equal to b.
   * @param b value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E max(E a, E b) {
    return compare(a, b) >= 0 ? a : b;
  }

  /**
   * Returns the smallest of the specified values according to this ordering. If
   * there are multiple smallest values, the first of those is returned.
   *
   * @param iterable the iterable whose minimum element is to be determined
   * @throws java.util.NoSuchElementException if {@code iterable} is empty
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E min(Iterable<E> iterable) {
    Iterator<E> iterator = iterable.iterator();

    // let this throw NoSuchElementException as necessary
    E minSoFar = iterator.next();

    while (iterator.hasNext()) {
      minSoFar = min(minSoFar, iterator.next());
    }

    return minSoFar;
  }

  /**
   * Returns the smallest of the specified values according to this ordering. If
   * there are multiple smallest values, the first of those is returned.
   *
   * @param a value to compare, returned if less than or equal to the rest.
   * @param b value to compare
   * @param c value to compare
   * @param rest values to compare
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E min(E a, E b, E c, E... rest) {
    E minSoFar = min(min(a, b), c);

    for (E r : rest) {
      minSoFar = min(minSoFar, r);
    }

    return minSoFar;
  }

  /**
   * Returns the smaller of the two values according to this ordering. If the
   * values compare as 0, the first is returned.
   *
   * @param a value to compare, returned if less than or equal to b.
   * @param b value to compare.
   * @throws ClassCastException if the parameters are not <i>mutually
   *     comparable</i> under this ordering.
   */
  public <E extends T> E min(E a, E b) {
    return compare(a, b) <= 0 ? a : b;
  }
}
