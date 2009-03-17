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

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * This class contains static utility methods that operate on or return objects
 * of type {@code Iterator}. Also see the parallel implementations in {@link
 * Iterables}.
 *
 * @author Kevin Bourrillion
 * @author Scott Bonneau
 */
@GwtCompatible
public final class Iterators {
  private Iterators() {}

  static final UnmodifiableIterator<Object> EMPTY_ITERATOR
      = new UnmodifiableIterator<Object>() {
        public boolean hasNext() {
          return false;
        }
        public Object next() {
          throw new NoSuchElementException();
        }
      };


  /** Returns the empty {@code Iterator}. */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  public static <T> UnmodifiableIterator<T> emptyIterator() {
    return (UnmodifiableIterator<T>) EMPTY_ITERATOR;
  }

  private static final ListIterator<Object> EMPTY_LIST_ITERATOR =
      new ListIterator<Object>() {
        public boolean hasNext() {
          return false;
        }
        public boolean hasPrevious() {
          return false;
        }
        public int nextIndex() {
          return 0;
        }
        public int previousIndex() {
          return -1;
        }
        public Object next() {
          throw new NoSuchElementException();
        }
        public Object previous() {
          throw new NoSuchElementException();
        }
        public void set(Object o) {
          throw new UnsupportedOperationException();
        }
        public void add(Object o) {
          throw new UnsupportedOperationException();
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };

  /** Returns the empty {@code ListIterator}. */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  public static <T> ListIterator<T> emptyListIterator() {
    return (ListIterator<T>) EMPTY_LIST_ITERATOR;
  }

  private static final Iterator<Object> EMPTY_MODIFIABLE_ITERATOR =
      new Iterator<Object>() {
        /*@Override*/ public boolean hasNext() {
          return false;
        }

        /*@Override*/ public Object next() {
          throw new NoSuchElementException();
        }

        /*@Override*/ public void remove() {
          throw new IllegalStateException();
        }
      };

  /**
   * Returns the empty {@code Iterator} that throws
   * {@link IllegalStateException} instead of
   * {@link UnsupportedOperationException} on a call to
   * {@link Iterator#remove()}.
   */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  static <T> Iterator<T> emptyModifiableIterator() {
    return (Iterator<T>) EMPTY_MODIFIABLE_ITERATOR;
  }

  /** Returns an unmodifiable view of {@code iterator}. */
  public static <T> UnmodifiableIterator<T> unmodifiableIterator(
      final Iterator<T> iterator) {
    checkNotNull(iterator);
    return new UnmodifiableIterator<T>() {
      public boolean hasNext() {
        return iterator.hasNext();
      }
      public T next() {
        return iterator.next();
      }
    };
  }

  /**
   * Returns the number of elements remaining in {@code iterator}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   */
  public static int size(Iterator<?> iterator) {
    int count = 0;
    while (iterator.hasNext()) {
      iterator.next();
      count++;
    }
    return count;
  }

  /**
   * Returns {@code true} if {@code iterator} contains {@code element}.
   */
  public static boolean contains(Iterator<?> iterator, @Nullable Object element)
  {
    if (element == null) {
      while (iterator.hasNext()) {
        if (iterator.next() == null) {
          return true;
        }
      }
    } else {
      while (iterator.hasNext()) {
        if (element.equals(iterator.next())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Traverses an iterator and removes every element that belongs to the
   * provided collection. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @param iterator the iterator to (potentially) remove elements from
   * @param c the elements to remove
   * @return {@code true} if any elements are removed from {@code iterator}
   */
  public static boolean removeAll(Iterator<?> iterator, Collection<?> c) {
    checkNotNull(c);
    boolean modified = false;
    while (iterator.hasNext()) {
      if (c.contains(iterator.next())) {
        iterator.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Traverses an iterator and removes every element that does not belong to the
   * provided collection. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @param iterator the iterator to (potentially) remove elements from
   * @param c the elements to retain
   * @return {@code true} if any elements are removed from {@code iterator}
   */
  public static boolean retainAll(Iterator<?> iterator, Collection<?> c) {
    checkNotNull(c);
    boolean modified = false;
    while (iterator.hasNext()) {
      if (!c.contains(iterator.next())) {
        iterator.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Determines whether two iterators contain equal elements in the same order.
   * More specifically, this method returns {@code true} if {@code iterator1}
   * and {@code iterator2} contain the same number of elements and every element
   * of {@code iterator1} is equal to the corresponding element of
   * {@code iterator2}.
   *
   * <p>Note that this will modify the supplied iterators, since they will have
   * been advanced some number of elements forward.
   */
  public static boolean elementsEqual(
      Iterator<?> iterator1, Iterator<?> iterator2) {
    while (iterator1.hasNext()) {
      if (!iterator2.hasNext()) {
        return false;
      }
      Object o1 = iterator1.next();
      Object o2 = iterator2.next();
      if (!Objects.equal(o1, o2)) {
        return false;
      }
    }
    return !iterator2.hasNext();
  }

  /**
   * Returns a string representation of {@code iterator}, with the format
   * {@code [e1, e2, ..., en]}. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   */
  public static String toString(Iterator<?> iterator) {
    if (!iterator.hasNext()) {
      return "[]";
    }
    StringBuilder builder = new StringBuilder();
    builder.append('[').append(iterator.next());
    while (iterator.hasNext()) {
      builder.append(", ").append(iterator.next());
    }
    return builder.append(']').toString();
  }

  /**
   * Returns the single element contained in {@code iterator}.
   *
   * @throws NoSuchElementException if the iterator is empty
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements.  The state of the iterator is unspecified.
   */
  public static <T> T getOnlyElement(Iterator<T> iterator) {
    T first = iterator.next();
    if (!iterator.hasNext()) {
      return first;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("expected one element but was: <" + first);
    for (int i = 0; i < 4 && iterator.hasNext(); i++) {
      sb.append(", " + iterator.next());
    }
    if (iterator.hasNext()) {
      sb.append(", ...");
    }
    sb.append(">");

    throw new IllegalArgumentException(sb.toString());
  }

  /**
   * Returns the single element contained in {@code iterator}, or {@code
   * defaultValue} if the iterator is empty.
   *
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements.  The state of the iterator is unspecified.
   */
  public static <T> T getOnlyElement(
      Iterator<T> iterator, @Nullable T defaultValue) {
    return iterator.hasNext() ? getOnlyElement(iterator) : defaultValue;
  }

  /**
   * Copies an iterator's elements into an array. The iterator will be left
   * exhausted: its {@code hasNext()} method will return {@code false}.
   *
   * @param iterator the iterator to copy
   * @param type the type of the elements
   * @return a newly-allocated array into which all the elements of the iterator
   *         have been copied
   */
  @GwtIncompatible("Array.newArray")
  public static <T> T[] toArray(
      Iterator<? extends T> iterator, Class<T> type) {
    List<T> list = Lists.newArrayList(iterator);
    return Iterables.toArray(list, type);
  }

  /**
   * Adds all elements in {@code iterator} to {@code collection}. The iterator
   * will be left exhausted: its {@code hasNext()} method will return
   * {@code false}.
   *
   * @return {@code true} if {@code collection} was modified as a result of this
   *         operation
   */
  public static <T> boolean addAll(
      Collection<T> collection, Iterator<? extends T> iterator) {
    checkNotNull(collection);
    boolean wasModified = false;
    while (iterator.hasNext()) {
      wasModified |= collection.add(iterator.next());
    }
    return wasModified;
  }

  /**
   * Returns the number of elements in the specified iterator that equal the
   * specified object. The iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @see Collections#frequency
   */
  public static int frequency(Iterator<?> iterator, @Nullable Object element) {
    int result = 0;
    if (element == null) {
      while (iterator.hasNext()) {
        if (iterator.next() == null) {
          result++;
        }
      }
    } else {
      while (iterator.hasNext()) {
        if (element.equals(iterator.next())) {
          result++;
        }
      }
    }
    return result;
  }

  /**
   * Returns an iterator that cycles indefinitely over the elements of {@code
   * iterable}.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After {@code remove()} is called, subsequent cycles omit the removed
   * element, which is no longer in {@code iterable}. The iterator's
   * {@code hasNext()} method returns {@code true} until {@code iterable} is
   * empty.
   *
   * <p><b>Warning:</b> Typical uses of the resulting iterator may produce an
   * infinite loop. You should use an explicit {@code break} or be certain that
   * you will eventually remove all the elements.
   */
  public static <T> Iterator<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterator<T>() {
      Iterator<T> iterator = emptyIterator();
      Iterator<T> removeFrom;

      public boolean hasNext() {
        if (!iterator.hasNext()) {
          iterator = iterable.iterator();
        }
        return iterator.hasNext();
      }
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        removeFrom = iterator;
        return iterator.next();
      }
      public void remove() {
        checkState(removeFrom != null,
            "no calls to next() since last call to remove()");
        removeFrom.remove();
        removeFrom = null;
      }
    };
  }

  /**
   * Returns an iterator that cycles indefinitely over the provided elements.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After {@code remove()} is called, subsequent cycles omit the removed
   * element, but {@code elements} does not change. The iterator's
   * {@code hasNext()} method returns {@code true} until all of the original
   * elements have been removed.
   *
   * <p><b>Warning:</b> Typical uses of the resulting iterator may produce an
   * infinite loop. You should use an explicit {@code break} or be certain that
   * you will eventually remove all the elements.
   */
  public static <T> Iterator<T> cycle(T... elements) {
    return cycle(Lists.newArrayList(elements));
  }

  /**
   * Combines two iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}. The source iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b) {
    checkNotNull(a);
    checkNotNull(b);
    return concat(Arrays.asList(a, b).iterator());
  }

  /**
   * Combines three iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}, followed by the elements in {@code c}. The source iterators
   * are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b, Iterator<? extends T> c) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    return concat(Arrays.asList(a, b, c).iterator());
  }

  /**
   * Combines four iterators into a single iterator. The returned iterator
   * iterates across the elements in {@code a}, followed by the elements in
   * {@code b}, followed by the elements in {@code c}, followed by the elements
   * in {@code d}. The source iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> concat(Iterator<? extends T> a,
      Iterator<? extends T> b, Iterator<? extends T> c,
      Iterator<? extends T> d) {
    checkNotNull(a);
    checkNotNull(b);
    checkNotNull(c);
    checkNotNull(d);
    return concat(Arrays.asList(a, b, c, d).iterator());
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it.
   *
   * @throws NullPointerException if any of the provided iterators is null
   */
  public static <T> Iterator<T> concat(Iterator<? extends T>... inputs) {
    return concat(ImmutableList.of(inputs).iterator());
  }

  /**
   * Combines multiple iterators into a single iterator. The returned iterator
   * iterates across the elements of each iterator in {@code inputs}. The input
   * iterators are not polled until necessary.
   *
   * <p>The returned iterator supports {@code remove()} when the corresponding
   * input iterator supports it. The methods of the returned iterator may throw
   * {@code NullPointerException} if any of the input iterators are null.
   */
  public static <T> Iterator<T> concat(
      final Iterator<? extends Iterator<? extends T>> inputs) {
    checkNotNull(inputs);
    return new Iterator<T>() {
      Iterator<? extends T> current = emptyIterator();
      Iterator<? extends T> removeFrom;

      public boolean hasNext() {
        while (!current.hasNext() && inputs.hasNext()) {
          current = inputs.next();
        }
        return current.hasNext();
      }
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        removeFrom = current;
        return current.next();
      }
      public void remove() {
        checkState(removeFrom != null,
            "no calls to next() since last call to remove()");
        removeFrom.remove();
        removeFrom = null;
      }
    };
  }

  /**
   * Divides an iterator into unmodifiable sublists of the given size (the final
   * list may be smaller). For example, partitioning an iterator containing
   * {@code [a, b, c, d, e]} with a partition size of 3 yields {@code
   * [[a, b, c], [d, e]]} -- an outer iterator containing two inner lists of
   * three and two elements, all in the original order.
   *
   * <p>The iterator does not support the {@link Iterator#remove()} method.
   *
   * @param iterator the iterator to return a partitioned view of
   * @param size the desired size of each partition (the last may be smaller)
   * @return an iterator of immutable lists containing the elements of {@code
   *     iterator} divided into partitions
   * @throws IllegalArgumentException if {@code size} is nonpositive
   */
  public static <T> UnmodifiableIterator<List<T>> partition(
      Iterator<T> iterator, int size) {
    return partitionImpl(iterator, size, false);
  }

  /**
   * Divides an iterator into unmodifiable sublists of the given size, padding
   * the final iterator with null values if necessary. For example, partitioning
   * an iterator containing {@code [a, b, c, d, e]} with a partition size of 3
   * yields {@code [[a, b, c], [d, e, null]]} -- an outer iterator containing
   * two inner lists of three elements each, all in the original order.
   *
   * <p>The iterator does not support the {@link Iterator#remove()} method.
   *
   * @param iterator the iterator to return a partitioned view of
   * @param size the desired size of each partition
   * @return an iterator of immutable lists containing the elements of {@code
   *     iterator} divided into partitions (the final iterable may have
   *     trailing null elements)
   * @throws IllegalArgumentException if {@code size} is nonpositive
   */
  public static <T> UnmodifiableIterator<List<T>> paddedPartition(
      Iterator<T> iterator, int size) {
    return partitionImpl(iterator, size, true);
  }

  private static <T> UnmodifiableIterator<List<T>> partitionImpl(
      final Iterator<T> iterator, final int size, final boolean pad) {
    checkNotNull(iterator);
    checkArgument(size > 0);
    return new UnmodifiableIterator<List<T>>() {
      public boolean hasNext() {
        return iterator.hasNext();
      }
      public List<T> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Object[] array = new Object[size];
        int count = 0;
        for (; count < size && iterator.hasNext(); count++) {
          array[count] = iterator.next();
        }

        @SuppressWarnings("unchecked") // we only put Ts in it
        List<T> list = Collections.unmodifiableList(
            (List<T>) Arrays.asList(array));
        return (pad || count == size) ? list : Platform.subList(list, 0, count);
      }
    };
  }

  /**
   * Returns the elements of {@code unfiltered} that satisfy a predicate. The
   * resulting iterator does not support {@code remove()}.
   */
  public static <T> Iterator<T> filter(
      final Iterator<T> unfiltered, final Predicate<? super T> predicate) {
    checkNotNull(unfiltered);
    checkNotNull(predicate);
    return new AbstractIterator<T>() {
      @Override protected T computeNext() {
        while (unfiltered.hasNext()) {
          T element = unfiltered.next();
          if (predicate.apply(element)) {
            return element;
          }
        }
        return endOfData();
      }
    };
  }

  /**
   * Returns all instances of class {@code type} in {@code unfiltered}. The
   * returned iterator has elements whose class is {@code type} or a subclass of
   * {@code type}. The returned iterator does not support {@code remove()}.
   *
   * @param unfiltered an iterator containing objects of any type
   * @param type the type of elements desired
   * @return an unmodifiable iterator containing all elements of the original
   *     iterator that were of the requested type
   */
  @SuppressWarnings("unchecked")
  @GwtIncompatible("Class.isInstance")
  public static <T> Iterator<T> filter(
      Iterator<?> unfiltered, final Class<T> type) {
    checkNotNull(type);
    Predicate<Object> predicate = new Predicate<Object>() {
      public boolean apply(Object object) {
        return Platform.isInstance(type, object);
      }
    };
    return (Iterator<T>) filter(unfiltered, predicate);
  }

  /**
   * Returns {@code true} if one or more elements returned by {@code iterator}
   * satisfy the given predicate.
   */
  public static <T> boolean any(
      Iterator<T> iterator, Predicate<? super T> predicate) {
    checkNotNull(predicate);
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (predicate.apply(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if every element returned by {@code iterator}
   * satisfies the given predicate. If {@code iterator} is empty, {@code true}
   * is returned.
   */
  public static <T> boolean all(
      Iterator<T> iterator, Predicate<? super T> predicate) {
    checkNotNull(predicate);
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (!predicate.apply(element)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the first element in {@code iterator} that satisfies the given
   * predicate. If a matching element is found, the iterator will be left in a
   * state such that calling {@code iterator.remove()} will remove the found
   * item. If no such element is found, the iterator will be left exhausted: its
   * {@code hasNext()} method will return {@code false}.
   *
   * @return the first matching element in {@code iterator}
   * @throws NoSuchElementException if no element in {@code iterator} matches
   *     the given predicate
   */
  public static <T> T find(Iterator<T> iterator, Predicate<? super T> predicate)
  {
    return filter(iterator, predicate).next();
  }

  /**
   * Returns an iterator that applies {@code function} to each element of {@code
   * fromIterator}.
   *
   * <p>The returned iterator supports {@code remove()} if the provided iterator
   * does. After a successful {@code remove()} call, {@code fromIterator} no
   * longer contains the corresponding element.
   */
  public static <F, T> Iterator<T> transform(final Iterator<F> fromIterator,
      final Function<? super F, ? extends T> function) {
    checkNotNull(fromIterator);
    checkNotNull(function);
    return new Iterator<T>() {
      public boolean hasNext() {
        return fromIterator.hasNext();
      }
      public T next() {
        F from = fromIterator.next();
        return function.apply(from);
      }
      public void remove() {
        fromIterator.remove();
      }
    };
  }

  /**
   * Advances {@code iterator} {@code position + 1} times, returning the element
   * at the {@code position}th position.
   *
   * @param position position of the element to return
   * @return the element at the specified position in {@code iterator}
   * @throws IndexOutOfBoundsException if {@code position} is negative or
   *     greater than or equal to the number of elements remaining in
   *     {@code iterator}
   */
  public static <T> T get(Iterator<T> iterator, int position) {
    checkNotNull(iterator);
    if (position < 0) {
      throw new IndexOutOfBoundsException("position (" + position
          + ") must not be negative");
    }

    int skipped = skip(iterator, position);
    if (skipped < position || !iterator.hasNext()) {
      throw new IndexOutOfBoundsException(
          "position (" + position + ") must be less than the number of " +
          "elements that remained (" + skipped + ")");
    } else {
      return iterator.next();
    }
  }

  /**
   * Advances {@code iterator} to the end, returning the last element.
   *
   * @return the last element of {@code iterator}
   * @throws NoSuchElementException if the iterator has no remaining elements
   */
  public static <T> T getLast(Iterator<T> iterator) {
    while (true) {
      T current = iterator.next();
      if (!iterator.hasNext()) {
        return current;
      }
    }
  }

  /**
   * Calls {@code next()} on {@code iterator}, either {@code numberToSkip} times
   * or until {@code hasNext()} returns {@code false}, whichever comes first.
   *
   * @return the number of elements skipped
   */
  public static <T> int skip(Iterator<T> iterator, int numberToSkip) {
    checkNotNull(iterator);
    checkArgument(numberToSkip >= 0, "number to skip cannot be negative");

    int i;
    for (i = 0; i < numberToSkip && iterator.hasNext(); i++) {
      iterator.next();
    }
    return i;
  }

  /**
   * Creates an iterator returning the first {@code limitSize} elements of the
   * given iterator. If the original iterator does not contain that many
   * elements, the returned iterator will have the same behavior as the original
   * iterator. The returned iterator supports {@code remove()} if the original
   * iterator does.
   *
   * @param iterator the iterator to limit
   * @param limitSize the maximum number of elements in the returned iterator
   * @throws IllegalArgumentException if {@code limitSize} is negative
   */
  public static <T> Iterator<T> limit(
      final Iterator<T> iterator, final int limitSize) {
    checkNotNull(iterator);
    checkArgument(limitSize >= 0, "limit is negative");
    return new Iterator<T>() {
      private int count;

      public boolean hasNext() {
        return count < limitSize && iterator.hasNext();
      }

      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        count++;
        return iterator.next();
      }

      public void remove() {
        iterator.remove();
      }
    };
  }

  // Methods only in Iterators, not in Iterables

  /**
   * Returns an iterator containing the elements of {@code array} in order. The
   * returned iterator is a view of the array; subsequent changes to the array
   * will be reflected in the iterator.
   *
   * <p><b>Note:</b> It is often preferable to represent your data using a
   * collection type, for example using {@link Arrays#asList(Object[])}, making
   * this method unnecessary.
   */
  public static <T> UnmodifiableIterator<T> forArray(final T... array) {
    // optimized. benchmarks at nearly 2x of the straightforward impl
    return new UnmodifiableIterator<T>() {
      final int length = array.length;
      int i = 0;
      public boolean hasNext() {
        return i < length;
      }
      public T next() {
        try {
          // 'return array[i++];' almost works
          T t = array[i];
          i++;
          return t;
        } catch (ArrayIndexOutOfBoundsException e) {
          throw new NoSuchElementException();
        }
      }
    };
  }

  /**
   * Returns an iterator containing the elements in the specified range of
   * {@code array} in order. The returned iterator is a view of the array;
   * subsequent changes to the array will be reflected in the iterator.
   *
   * @param array array to read elements out of
   * @param offset index of first array element to retrieve
   * @param length number of elements in iteration
   *
   * @throws IndexOutOfBoundsException if {@code offset} is negative,
   *    {@code length} is negative, or {@code offset + length > array.length}
   */
  public static <T> UnmodifiableIterator<T> forArray(
      final T[] array, final int offset, final int length) {
    checkArgument(length >= 0);
    final int end = offset + length;

    // Technically we should give a slightly more descriptive error on overflow
    Preconditions.checkPositionIndexes(offset, end, array.length);

    // If length == 0 is a common enough case, we could return emptyIterator().

    return new UnmodifiableIterator<T>() {
      int i = offset;
      public boolean hasNext() {
        return i < end;
      }
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return array[i++];
      }
    };
  }

  /**
   * Returns an iterator containing only {@code value}.
   */
  public static <T> UnmodifiableIterator<T> singletonIterator(
      @Nullable final T value) {
    return new UnmodifiableIterator<T>() {
      boolean done;
      public boolean hasNext() {
        return !done;
      }
      public T next() {
        if (done) {
          throw new NoSuchElementException();
        }
        done = true;
        return value;
      }
    };
  }

  /**
   * Adapts an {@code Enumeration} to the {@code Iterator} interface. The
   * returned iterator does not support {@code remove()}.
   */
  public static <T> Iterator<T> forEnumeration(final Enumeration<T> enumeration)
  {
    checkNotNull(enumeration);
    return new Iterator<T>() {
      public boolean hasNext() {
        return enumeration.hasMoreElements();
      }
      public T next() {
        return enumeration.nextElement();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Adapts an {@code Iterator} to the {@code Enumeration} interface.
   *
   * @see Collections#enumeration(Collection)
   */
  public static <T> Enumeration<T> asEnumeration(final Iterator<T> iterator) {
    checkNotNull(iterator);
    return new Enumeration<T>() {
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }
      public T nextElement() {
        return iterator.next();
      }
    };
  }

  /**
   * Implementation of PeekingIterator that avoids peeking unless necessary.
   */
  private static class PeekingImpl<E> implements PeekingIterator<E> {

    private final Iterator<? extends E> iterator;
    private boolean hasPeeked;
    private E peekedElement;

    public PeekingImpl(Iterator<? extends E> iterator) {
      this.iterator = checkNotNull(iterator);
    }

    public boolean hasNext() {
      return hasPeeked || iterator.hasNext();
    }

    public E next() {
      if (!hasPeeked) {
        return iterator.next();
      }
      E result = peekedElement;
      hasPeeked = false;
      peekedElement = null;
      return result;
    }

    public void remove() {
      checkState(!hasPeeked, "Can't remove after you've peeked at next");
      iterator.remove();
    }

    public E peek() {
      if (!hasPeeked) {
        peekedElement = iterator.next();
        hasPeeked = true;
      }
      return peekedElement;
    }
  }

  /**
   * Wraps the supplied iterator in a {@code PeekingIterator}. The
   * {@link PeekingIterator} assumes ownership of the supplied iterator, so
   * users should cease making direct calls to it after calling this method.
   *
   * <p>If the {@link PeekingIterator#peek()} method of the constructed
   * {@code PeekingIterator} is never called, the returned iterator will
   * behave exactly the same as the supplied iterator.
   *
   * <p>Subsequent calls to {@code peek()} with no intervening calls to
   * {@code next()} do not affect the iteration, and hence return the same
   * object each time. After a call to {@code peek()}, the next call to
   * {@code next()} is guaranteed to return the same object that the
   * {@code peek()} call returned. For example:
   *
   * <pre>
   *   PeekingIterator&lt;E&gt; peekingIterator = ...;
   *   // Either the next three calls will each throw
   *   // NoSuchElementExceptions, or...
   *   E e1 = peekingIterator.peek();
   *   E e2 = peekingIterator.peek(); // e2 is the same as e1
   *   E e3 = peekingIterator.next(); // e3 is the same as e1/e2
   * </pre>
   *
   * <p>Calling {@link Iterator#remove()} after {@link PeekingIterator#peek()}
   * is unsupported by the returned iterator and will throw an
   * {@link IllegalStateException}.
   */
  public static <T> PeekingIterator<T> peekingIterator(
      Iterator<? extends T> iterator) {
    return new PeekingImpl<T>(iterator);
  }
}
