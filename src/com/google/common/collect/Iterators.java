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
import com.google.common.base.Nullable;
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class contains static utility methods that operate on or return
 * objects of type {@code Iterator}. Also see the parallel implementations in
 * {@link Iterables}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author bonneau@google.com (Scott Bonneau)
 */
public class Iterators {

  private Iterators() { }

  /**
   * Returns the empty Iterator.
   */
  public static <T> Iterator<T> emptyIterator() {
    return Collections.<T>emptySet().iterator();
  }

  /**
   * Returns an unmodifiable view of {@code iterator}.
   */
  public static <T> Iterator<T> unmodifiableIterator(
      final Iterator<T> iterator) {
    checkNotNull(iterator);
    return new Iterator<T>() {
      public boolean hasNext() {
        return iterator.hasNext();
      }
      public T next() {
        return iterator.next();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns the lone element of a one-element Iterator.
   *
   * @param iterator an Iterator of size 1
   * @return the element
   * @throws NoSuchElementException if the Iterator is empty
   * @throws IllegalArgumentException if the Iterator has two or more elements
   */
  public static <T> T getLoneItem(Iterator<T> iterator) {
    checkNotNull(iterator);
    if (!iterator.hasNext()) {
      throw new NoSuchElementException();
    }
    T element = iterator.next();
    if (iterator.hasNext()) {
      throw new IllegalArgumentException("too many elements");
    }
    return element;
  }

  /**
   * Returns the lone element of a one-element Iterator, or null if the iterator
   * is empty.
   *
   * @param iterator an Iterator of size 0 or 1
   * @return the element or null
   * @throws IllegalArgumentException if the Iterator has two or more elements
   */
  public static <T> T getOptionalItem(Iterator<T> iterator) {
    checkNotNull(iterator);
    if (!iterator.hasNext()) {
      return null;
    }
    return getLoneItem(iterator);
  }

  /**
   * Converts an {@code Iterator} into an array.
   *
   * @param iterator any instance of {@code Iterator} (will not be modified)
   * @param type the type of the elements
   * @return a newly-allocated array into which all the elements of the iterator
   *     have been copied.  May be empty but never null.
   */
  public static <T> T[] newArray(Iterator<T> iterator, Class<T> type) {
    checkNotNull(iterator);
    checkNotNull(type);
    List<T> list = Lists.newArrayList(iterator);
    return Iterables.newArray(list, type);
  }

  /**
   * Returns the elements of {@code unfiltered} for which {@code predicate}
   * evaluates to {@code true}. May return an empty Iterator, but never null.
   * The resulting Iterator does not support {@link Iterator#remove} (this
   * seems impossible).
   */
  public static <T> Iterator<T> filter(final Iterator<T> unfiltered,
                                       final Predicate<? super T> predicate) {
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
        endOfData();
        return null;
      }
    };
  }

  /**
   * Returns {@code true} if some element returned by {@code iterator} evaluates
   * as {@code true} under {@code predicate}. Returns {@code false} if
   * {@code iterator} is empty.
   */
  public static <T> boolean any(Iterator<T> iterator,
                                Predicate<? super T> predicate) {
    checkNotNull(iterator);
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
   * Returns {@code true} if no element returned by {@code iterator} evaluates
   * as {@code false} under {@code predicate}. Returns {@code true} if
   * {@code iterator} is empty.
   */
  public static <T> boolean all(Iterator<T> iterator,
                                Predicate<? super T> predicate) {
    checkNotNull(iterator);
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
   * Returns all instances of {@code type} found in {@code unfiltered}.
   * Similar to {@link #filter(Iterator,Predicate)}.
   *
   * @param unfiltered an iterator containing objects of any type
   * @param type the type of elements desired
   * @return an unmodifiable iterator containing all elements of the original
   *     iterator that were of the requested type
   */

  @SuppressWarnings("unchecked")
  public static <T> Iterator<T> filter(
      Iterator<?> unfiltered, final Class<T> type) {
    checkNotNull(unfiltered);
    checkNotNull(type);
    Predicate<Object> predicate = new Predicate<Object>() {
      public boolean apply(Object object) {
        return type.isInstance(object);
      }
    };
    return (Iterator<T>) filter(unfiltered, predicate);
  }

  /**
   * Returns the first element in {@code iterator} for which the given predicate
   * matches.
   * <br><br>
   * If a matching element is found, the iterator will be left in a state such
   * that calling {@code iterator.remove()} will remove the found item.
   * <br><br>
   * If no such element is found, the iterator will be left exhausted such that
   * {@code iterator.hasNext()} returns false.
   *
   * @return the first matching element in {@code iterator}
   * @throws NoSuchElementException if no element in {@code iterator} matches
   *         the given predicate
   */
  public static <E> E find(Iterator<E> iterator,
                           Predicate<? super E> predicate)
      throws NoSuchElementException {
    checkNotNull(iterator);
    checkNotNull(predicate);
    return filter(iterator, predicate).next();
  }

  /**
   * Returns an iterator that applies {@code function} to each element of {@code
   * fromIterator}.
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
   * Returns an iterator that cycles indefinitely over the elements of {@code
   * iterable} until it is empty. <b>Warning:</b> typical uses of the resulting
   * iterator may produce an infinite loop. You should use an explicit break,
   * or be certain that you will eventually remove all the elements.
   */
  public static <T> Iterator<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterator<T>() {
      private Iterator<T> iterator = emptyIterator();
      private Iterator<T> removeFrom;
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
   * Variant of {@code #cycle(Iterable)} accepting varargs parameters.
   */
  public static <T> Iterator<T> cycle(T... elements) {
    checkNotNull(elements);
    return cycle(Lists.newArrayList(elements));
  }

  /**
   * Varargs form of {@code #concat(Iterator)}.
   */
  public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
    checkNotNull(iterators);
    return concat(Arrays.asList(iterators).iterator());
  }

  /**
   * Concatenates multiple iterators into a single iterator.  No references are
   * copied, and the source iterators are never polled until necessary.
   *
   * <p>The returned iterator supports {@link Iterator#remove()} when and only
   * when the appropriate source iterator supports it. The methods of the
   * returned iterator may throw {@code NullPointerException} if any of the
   * source iterators are {@code null}.
   */
  public static <T> Iterator<T> concat(
      final Iterator<? extends Iterator<? extends T>> iterators) {
    checkNotNull(iterators);
    return new Iterator<T>() {
      Iterator<? extends T> current = emptyIterator();
      Iterator<? extends T> removeFrom;

      public boolean hasNext() {
        while (!current.hasNext() && iterators.hasNext()) {
          current = iterators.next();
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
   * Adds all elements in <code>iterator</code> to <code>collection</code>.
   *
   * @return <code>true<code> iff <code>collection</code> was modified as a
   *         result of this operation.
   */
  public static <T> boolean addAll(Collection<T> collection,
                                   Iterator<? extends T> iterator) {
    checkNotNull(collection);
    checkNotNull(iterator);
    boolean wasModified = false;
    while (iterator.hasNext()) {
      wasModified |= collection.add(iterator.next());
    }
    return wasModified;
  }

  /** Variant of {@code Collections#frequency} for iterators. */
  public static int frequency(Iterator<?> iterator, @Nullable Object element) {
    checkNotNull(iterator);
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
   * Determines whether the two {@code Iterator}s contain equal elements. Note
   * that this will actually modify the supplied {@code Iterator}s such that
   * they will not be usable after calling this method. (they will have been
   * advanced some number of elements forward)
   *
   * @return true iff {@code iterator1} and {@code iterator2} contain the same
   * number of elements and every element of {@code iterator1} is equal to the
   * corresponding element of {@code iterator2}.
   */
  public static boolean elementsEqual(Iterator<?> iterator1,
                                      Iterator<?> iterator2) {
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
   * Partition an iterator into sub iterators of the given size like so:
   * {A, B, C, D, E, F} with partition size 3 =>
   * {A, B, C} and {D, E, F}
   *
   * NOTE: You must read partitions one at a time from the returned iterator
   *       Once you read forward any iterators from previous partitions will
   *       become invalid.
   *
   * @param iterator the iterator to partition
   * @param partitionSize the size of each partition
   * @param padToSize whether to pad the last partition to the partition size.
   *        if {@code true}, pads last partition with nulls
   * @return an iterator of partitioned iterators
   */
  public static <T> Iterator<Iterator<T>> partition(
      final Iterator<? extends T> iterator,
      final int partitionSize,
      final boolean padToSize) {
    checkNotNull(iterator);
    return new AbstractIterator<Iterator<T>>() {
      Iterator<T> currentRow = null;

      @Override protected Iterator<T> computeNext() {
        if(currentRow != null) {
          while(currentRow.hasNext()) {
            currentRow.next();
          }
        }
        if(!iterator.hasNext()) {
          endOfData();
          return null;
        }
        currentRow = new AbstractIterator<T>() {
          private int count = partitionSize;
          @Override protected T computeNext() {
            if(count == 0) {
              endOfData();
              return null;
            }
            count--;
            if(iterator.hasNext()) {
              return iterator.next();
            } else {
              if(!padToSize) {
                endOfData();
              }
              return null;
            }
          }
        };
        return currentRow;
      }
    };
  }

  /**
   * Adapts an Enumeration to the Iterator interface.
   */
  public static <T> Iterator<T> forEnumeration(
      final Enumeration<T> enumeration) {
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
   * Adapts an Iterator to the Enumeration interface.
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
   * Returns a string representation of the elements in {@code
   * iterator}, in the format "{@code [e1, e2, ..., en]}".
   */
  public static String toString(Iterator<?> iterator) {
    checkNotNull(iterator);
    StringBuilder builder = new StringBuilder().append('[');
    Join.join(builder, ", ", iterator);
    return builder.append(']').toString();
  }
}
