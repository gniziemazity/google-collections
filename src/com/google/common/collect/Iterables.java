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
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ListIterator;

/**
 * This class contains static utility methods that operate on or return
 * objects of type {@link Iterable}.  Also see the parallel implementations in
 * {@link Iterators}.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author bonneau@google.com (Scott Bonneau)
 */
public final class Iterables {
  private Iterables() {}

  /**
   * Returns the empty Iterable.
   */
  public static <T> Iterable<T> emptyIterable() {
    return Collections.emptySet();
  }

  /**
   * Returns an unmodifiable view of {@code iterable}.
   */
  public static <T> Iterable<T> unmodifiableIterable(
      final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(iterable.iterator());
      }
      /* no equals and hashCode; it would break the contract! */
      @Override public String toString() {
        return iterable.toString();
      }
    };
  }

  /**
   * Returns the lone element of a one-element Iterable.
   *
   * @param iterable an Iterable of size 1
   * @return the element
   * @throws NoSuchElementException if the Iterable is empty
   * @throws IllegalArgumentException if the Iterable has two or more elements
   */
  public static <T> T getLoneItem(Iterable<T> iterable) {
    checkNotNull(iterable);
    return Iterators.getLoneItem(iterable.iterator());
  }

  /**
   * Returns the lone element of a one-element Iterable, or null if the
   * iterable is empty.
   *
   * @param iterable an Iterable of size 0 or 1.
   * @return the element or null
   * @throws IllegalArgumentException if the iterable has two or more elements
   */
  public static <E> E getOptionalItem(Iterable<E> iterable) {
    checkNotNull(iterable);
    return Iterators.getOptionalItem(iterable.iterator());
  }

  /**
   * Converts an {@code Iterable} into an array.
   *
   * @param iterable any instance of {@code Iterable} (will not be modified)
   * @param type the type of the elements
   * @return a newly-allocated array into which all the elements of the iterable
   *     have been copied.  May be empty but never null.
   */
  public static <T> T[] newArray(Iterable<T> iterable, Class<T> type) {
    checkNotNull(iterable);
    checkNotNull(type);
    Collection<T> collection = (iterable instanceof Collection<?>)
        ? (Collection<T>) iterable
        : Lists.newArrayList(iterable);
    T[] array = ObjectArrays.newArray(type, collection.size());
    return collection.toArray(array);
  }

  /**
   * A convenient form of {@link Iterators#filter(Iterator,Predicate)}, which
   * accepts and returns an Iterable instead of an Iterator.
   */
  public static <T> Iterable<T> filter(final Iterable<T> unfiltered,
                                       final Predicate<? super T> predicate) {
    checkNotNull(unfiltered);
    checkNotNull(predicate);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), predicate);
      }
    };
  }

  /**
   * Returns all instances of {@code type} found in {@code unfiltered}.
   * Similar to {@link #filter(Iterable,Predicate)}.
   *
   * @param unfiltered an iterable containing objects of any type
   * @param type the type of elements desired
   * @return an unmodifiable iterable containing all elements of the original
   *     iterable that were of the requested type
   */
  public static <T> Iterable<T> filter(
      final Iterable<?> unfiltered, final Class<T> type) {
    checkNotNull(unfiltered);
    checkNotNull(type);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), type);
      }
    };
  }

  /**
   * Returns {@code true} if some element in {@code iterable} evaluates as
   * {@code true} under {@code predicate}. Returns {@code false} if
   * {@code iterable} is empty.
   */
  public static <T> boolean any(Iterable<T> iterable,
                                Predicate<? super T> predicate) {
    checkNotNull(iterable);
    checkNotNull(predicate);
    return Iterators.any(iterable.iterator(), predicate);
  }

  /**
   * Returns {@code true} if no element in {@code iterable} evaluates as
   * {@code false} under {@code predicate}. Returns {@code true} if
   * {@code iterable} is empty.
   */
  public static <T> boolean all(Iterable<T> iterable,
                                Predicate<? super T> predicate) {
    checkNotNull(iterable);
    checkNotNull(predicate);
    return Iterators.all(iterable.iterator(), predicate);
  }

  /**
   * Returns the first element in {@code iterable} for which the given predicate
   * matches.
   *
   * @return the first matching element in {@code iterable}
   * @throws NoSuchElementException if no element in {@code iterable} matches
   *         the given predicate
   */
  public static <E> E find(Iterable<E> iterable,
                           Predicate<? super E> predicate)
      throws NoSuchElementException {
    checkNotNull(iterable);
    return Iterators.find(iterable.iterator(), predicate);
  }

  /**
   * Returns an iterable that applies {@code function} to each element of
   * {@code fromIterable}.
   */
  public static <F, T> Iterable<T> transform(final Iterable<F> fromIterable,
                                             final Function<? super F, ? extends T> function) {
    checkNotNull(fromIterable);
    checkNotNull(function);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.transform(fromIterable.iterator(), function);
      }
    };
  }

  /**
   * Variant of {@link Iterators#cycle} which returns an {@code Iterable}.
   */
  public static <T> Iterable<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.cycle(iterable);
      }
    };
  }

  /**
   * Variant of {@link #cycle(Iterable)} accepting varargs parameters.
   */
  public static <T> Iterable<T> cycle(T... elements) {
    checkNotNull(elements);
    return cycle(Lists.newArrayList(elements));
  }

  /**
   * Variant of {@link Iterators#concat} that acts on and returns instances of
   * {@code Iterable}.
   */
  public static <T> Iterable<T> concat(
      Iterable<? extends T>... iterables) {
    checkNotNull(iterables);
    return concat(Arrays.asList(iterables));
  }

  /**
   * Variant of {@link Iterators#concat} that acts on and returns instances of
   * {@code Iterable}.
   */
  public static <T> Iterable<T> concat(
      Iterable<? extends Iterable<? extends T>> iterables) {
    checkNotNull(iterables);

    /*
     * Admittedly the below is just about the most insanely unreadable
     * code I've ever written in my life.
     *
     * Hint: if you let A represent Iterable<? extends T> and B represent
     * Iterator<? extends T>, then this Function would look simply like:
     *
     *   Function<A, B> function = new Function<A, B> {
     *     public B apply(A from) {
     *       return from.iterator();
     *     }
     *   }
     *
     * TODO(kevinb): there may be a better way to do this.
     */

    Function<Iterable<? extends T>, Iterator<? extends T>> function
        = new Function<Iterable<? extends T>, Iterator<? extends T>>() {
      public Iterator<? extends T> apply(Iterable<? extends T> from) {
        return from.iterator();
      }
    };
    final Iterable<Iterator<? extends T>> iterators
        = transform(iterables, function);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.concat(iterators.iterator());
      }
    };
  }

  /**
   * Adds all elements in <code>iterable</code> to <code>collection</code>.
   *
   * @return <code>true<code> iff <code>collection</code> was modified as a
   *         result of this operation.
   */
  public static <T> boolean addAll(Collection<T> collection,
      Iterable<? extends T> iterable) {
    checkNotNull(collection);
    checkNotNull(iterable);
    if (iterable instanceof Collection<?>) {
      return collection.addAll((Collection<? extends T>) iterable);
    }
    return Iterators.addAll(collection, iterable.iterator());
  }

  /** Variant of {@link Collections#frequency} for iterables. */
  public static int frequency(Iterable<?> iterable, @Nullable Object element) {
    checkNotNull(iterable);
    if ((iterable instanceof Multiset<?>)) {
      return ((Multiset<?>) iterable).count(element);
    }
    if ((iterable instanceof Set<?>)) {
      return ((Set<?>) iterable).contains(element) ? 1 : 0;
    }
    return Iterators.frequency(iterable.iterator(), element);
  }

  /**
   * Determines whether the two Iterables contain equal elements.
   *
   * @return true iff {@code iterable1} and {@code iterable2} contain the same
   * number of elements and every element of {@code iterable1} is equal to the
   * corresponding element of {@code iterable2}.
   */
  public static boolean elementsEqual(Iterable<?> iterable1,
                                      Iterable<?> iterable2) {
    return Iterators.elementsEqual(iterable1.iterator(), iterable2.iterator());
  }

  /**
   * Provides a rotated view of a list.  Differs from {@link Collections#rotate}
   * only in that it leaves the underlying list unchanged.  Note that this is a
   * "live" view of the list that will change as the list changes.  However, the
   * behavior of an {@link Iterator} constructed from a rotated view of the list
   * is undefined if the list is changed after the Iterator is constructed.
   *
   * @param list the list to return a rotated view of.
   * @param distance the distance to rotate the list.  There are no constraints
   * on this value; it may be zero, negative, or greater than
   * {@code list.size()}.
   * @return a rotated view of the given list
   */
  public static <T> Iterable<T> rotate(final List<T> list, final int distance) {
    checkNotNull(list);

    /*
     * If no rotation is requested or there is nothing to rotate (list of size
     * 0 or 1), just return the original list
     */
    if (distance == 0 || list.size() <= 1) {
      return list;
    }

    return new AbstractIterable<T>() {
      /**
       * Determines the actual distance we need to rotate (distance provided
       * might be larger than the size of the list or negative).
       */
      private int calcActualDistance(int size) {
        // we already know distance and size are non-zero
        int actualDistance = distance % size;
        if (actualDistance < 0) {
          // distance must have been negative
          actualDistance += size;
        }
        return actualDistance;
      }

      public Iterator<T> iterator() {
        int size = list.size();
        int actualDistance = calcActualDistance(size);
        // optimization:
        // lists of a size that go into the distance evenly don't need rotation
        if (actualDistance == 0) {
          return list.iterator();
        }

        @SuppressWarnings("unchecked")
        Iterable<T> rotated = concat(
            list.subList(actualDistance, size), list.subList(0, actualDistance));
        return rotated.iterator();
      }
    };
  }

  /**
   * Partition an iterable into sub iterables of the given size like so:
   * {A, B, C, D, E, F} with partition size 3 =>
   * {A, B, C} and {D, E, F}
   *
   * NOTE: You must read partitions one at a time from the returned iterable
   *       Once you read forward any iterables from previous partitions will
   *       become invalid.
   *
   * NOTE: This is optimized for a the simple case of iterating through each
   *       sub iterable in order only once.  Other opperations will succeed,
   *       but will suffer a performance penalty to maintain correctness.
   *
   * @param iterable the iterable to partition
   * @param partitionSize the size of each partition
   * @param padToSize whether to pad the last partition to the partition size.
   *        if {@code true}, pads last partition with nulls
   * @return an iterable of partitioned iterables
   */
  public static <T> Iterable<Iterable<T>> partition(
      final Iterable<? extends T> iterable,
      final int partitionSize,
      final boolean padToSize) {
    checkNotNull(iterable);
    return new AbstractIterable<Iterable<T>>() {
      public Iterator<Iterable<T>> iterator() {
        final Iterator<Iterator<T>> iterator = Iterators.partition(
            iterable.iterator(), partitionSize, padToSize);
        return new AbstractIterator<Iterable<T>>() {
          int howFarIn = 0;
          @Override protected Iterable<T> computeNext() {
            howFarIn++;
            if(!iterator.hasNext()) {
              endOfData();
              return null;
            }
            return new AbstractIterable<T>() {
              Iterator<T> innerIter = iterator.next();
              boolean firstIteratorRequest = true;
              public Iterator<T> iterator() {
                if(firstIteratorRequest) {
                  firstIteratorRequest = false;
                  return innerIter;
                } else {
                  Iterator<Iterator<T>> iterator = Iterators.partition(
                      iterable.iterator(), partitionSize, padToSize);
                  for(int i = 0; i < howFarIn; i++) {
                    innerIter = iterator.next();
                  }
                  return innerIter;
                }
              }
            };
          }
        };
      }
    };
  }

  /**
   * Adapt a list to an Iterable over a reversed version of the list.
   * Requires a List so that we can reverse it with no copying required.
   *
   * Especially useful in foreach-style loops:
   * <pre>
   * List<String> mylist = ...
   * for (String str : Iterables.reverse(mylist)) {
   *   ...
   * }
   * </pre>
   *
   * @return an Iterable<T> with the same elements as the list, in reverse.
   */
  public static <T> Iterable<T> reverse(final List<T> list) {
    checkNotNull(list);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        final ListIterator<T> listIter = list.listIterator(list.size());
        return new Iterator<T>() {
          public boolean hasNext() {
            return listIter.hasPrevious();
          }
          public T next() {
            return listIter.previous();
          }
          public void remove() {
            listIter.remove();
          }
        };
      }
    };
  }

  /**
   * Returns a string representation of {@code iterable} in the same format as
   * {@link Iterators#toString(java.util.Iterator)}.
   */
  public static String toString(Iterable<?> iterable) {
    checkNotNull(iterable);
    return Iterators.toString(iterable.iterator());
  }
}
