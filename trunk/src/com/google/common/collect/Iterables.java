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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/**
 * This class contains static utility methods that operate on or return objects
 * of type {@code Iterable}. Also see the parallel implementations in {@link
 * Iterators}.
 *
 * @author Kevin Bourrillion
 * @author Scott Bonneau
 */
public final class Iterables {
  private Iterables() {}

  private static final Iterable<Object> EMPTY_ITERABLE = new Iterable<Object>()
  {
    public Iterator<Object> iterator() {
      return Iterators.EMPTY_ITERATOR;
    }
  };

  /** Returns the empty Iterable. */
  // Casting to any type is safe since there are no actual elements.
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> emptyIterable() {
    return (Iterable<T>) EMPTY_ITERABLE;
  }

  /** Returns an unmodifiable view of {@code iterable}. */
  public static <T> Iterable<T> unmodifiableIterable(final Iterable<T> iterable)
  {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(iterable.iterator());
      }
      @Override public String toString() {
        return iterable.toString();
      }
      // no equals and hashCode; it would break the contract!
    };
  }

  /**
   * Returns the number of elements present in {@code iterable}.
   */
  public static int size(Iterable<?> iterable) {
    return (iterable instanceof Collection<?>)
        ? ((Collection<?>) iterable).size()
        : Iterators.size(iterable.iterator());
  }

  /**
   * Determines whether the two Iterables contain equal elements. More
   * specifically, this method returns {@code true} if {@code iterable1} and
   * {@code iterable2} contain the same number of elements and every element of
   * {@code iterable1} is equal to the corresponding element of {@code
   * iterable2}.
   */
  public static boolean elementsEqual(
      Iterable<?> iterable1, Iterable<?> iterable2) {
    return Iterators.elementsEqual(iterable1.iterator(), iterable2.iterator());
  }

  /**
   * Returns a string representation of {@code iterable} in the same format as
   * {@code Iterators#toString(java.util.Iterator)}.
   */
  public static String toString(Iterable<?> iterable) {
    return Iterators.toString(iterable.iterator());
  }

  /**
   * Returns the single element contained in {@code iterable}.
   *
   * @throws NoSuchElementException if the iterable is empty
   * @throws IllegalArgumentException if the iterable contains multiple
   *     elements
   */
  public static <T> T getOnlyElement(Iterable<T> iterable) {
    return Iterators.getOnlyElement(iterable.iterator());
  }

  /**
   * Returns the single element contained in {@code iterator}, or {@code
   * defaultValue} if the iterator is empty.
   *
   * @throws IllegalArgumentException if the iterator contains multiple
   *     elements
   */
  public static <T> T getOnlyElement(
      Iterable<T> iterable, @Nullable T defaultValue) {
    return Iterators.getOnlyElement(iterable.iterator(), defaultValue);
  }

  /**
   * Converts an {@code Iterable} into an array.
   *
   * @param iterable any instance of {@code Iterable} (will not be modified)
   * @param type the type of the elements
   * @return a newly-allocated array into which all the elements of the iterable
   *     have been copied. May be empty but never null.
   */
  public static <T> T[] newArray(Iterable<T> iterable, Class<T> type) {
    Collection<T> collection = (iterable instanceof Collection<?>)
        ? (Collection<T>) iterable
        : Lists.newArrayList(iterable);
    T[] array = ObjectArrays.newArray(type, collection.size());
    return collection.toArray(array);
  }

  /**
   * Adds all elements in {@code iterable} to {@code collection}.
   *
   * @return {@code true} if {@code collection} was modified as a result of this
   *     operation.
   */
  public static <T> boolean addAll(
      Collection<T> collection, Iterable<? extends T> iterable) {
    if (iterable instanceof Collection<?>) {
      @SuppressWarnings("unchecked")
      Collection<? extends T> c = (Collection<? extends T>) iterable;
      return collection.addAll(c);
    }
    return Iterators.addAll(collection, iterable.iterator());
  }

  /** Variant of {@code Collections.frequency} for iterables. */
  public static int frequency(Iterable<?> iterable, @Nullable Object element) {
    if ((iterable instanceof Multiset<?>)) {
      return ((Multiset<?>) iterable).count(element);
    }
    if ((iterable instanceof Set<?>)) {
      return ((Set<?>) iterable).contains(element) ? 1 : 0;
    }
    return Iterators.frequency(iterable.iterator(), element);
  }

  /**
   * Variant of {@code Iterators.cycle} which returns an {@code Iterable}.
   *
   * @see Iterators#cycle(Iterable)
   */
  public static <T> Iterable<T> cycle(final Iterable<T> iterable) {
    checkNotNull(iterable);
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.cycle(iterable);
      }
      @Override public String toString() {
        return iterable.toString() + " (cycled)";
      }
    };
  }

  /** Variant of {@code cycle(Iterable)} accepting varargs parameters. */
  public static <T> Iterable<T> cycle(T... elements) {
    return cycle(Lists.newArrayList(elements));
  }

  /**
   * Variant of {@code Iterators.concat} that acts on and returns instances of
   * {@code Iterable}.
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> concat(
      Iterable<? extends T> firstElements, Iterable<? extends T> nextElements) {
    checkNotNull(firstElements);
    checkNotNull(nextElements);
    return concat(Arrays.asList(firstElements, nextElements));
  }

  /**
   * Variant of {@code Iterators.concat} that acts on and returns instances of
   * {@code Iterable}.
   */
  public static <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
    return concat(Arrays.asList(iterables));
  }

  /**
   * Variant of {@code Iterators.concat} that acts on and returns instances of
   * {@code Iterable}.
   */
  public static <T> Iterable<T> concat(
      Iterable<? extends Iterable<? extends T>> iterables) {
    checkNotNull(iterables);

    /*
     * Hint: if you let A represent Iterable<? extends T> and B represent
     * Iterator<? extends T>, then this Function would look simply like:
     *
     *   Function<A, B> function = new Function<A, B> {
     *     public B apply(A from) {
     *       return from.iterator();
     *     }
     *   }
     *
     * TODO: there may be a better way to do this.
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
   * Partition an iterable into sub iterables of the given size like so: {A, B,
   * C, D, E, F} with partition size 3 => {A, B, C} and {D, E, F}.
   *
   * <p>NOTE: You must read partitions one at a time from the returned iterable
   * Once you read forward any iterables from previous partitions will become
   * invalid.
   *
   * <p>NOTE: This is optimized for a the simple case of iterating through each
   * sub-iterable in order only once. Other operations will succeed, but will
   * suffer a performance penalty to maintain correctness.
   *
   * @param iterable the iterable to partition
   * @param partitionSize the size of each partition
   * @param padToSize whether to pad the last partition to the partition size
   *     with {@code null}.
   * @return an iterable of partitioned iterables
   */
  public static <T> Iterable<Iterable<T>> partition(
      final Iterable<? extends T> iterable, final int partitionSize,
      final boolean padToSize) {
    checkNotNull(iterable);
    return new AbstractIterable<Iterable<T>>() {
      public Iterator<Iterable<T>> iterator() {
        final Iterator<Iterator<T>> iterator = Iterators.partition(
            iterable.iterator(), partitionSize, padToSize);
        return new AbstractIterator<Iterable<T>>() {
          int howFarIn;

          @Override protected Iterable<T> computeNext() {
            howFarIn++;
            if (!iterator.hasNext()) {
              return endOfData();
            }
            return new AbstractIterable<T>() {
              Iterator<T> innerIter = iterator.next();
              boolean firstIteratorRequest = true;

              public Iterator<T> iterator() {
                if (firstIteratorRequest) {
                  firstIteratorRequest = false;
                  return innerIter;
                } else {
                  Iterator<Iterator<T>> iterator = Iterators.partition(
                      iterable.iterator(), partitionSize, padToSize);
                  for (int i = 0; i < howFarIn; i++) {
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
   * Variant of {@code Iterators.filter(Iterator,Predicate)}, which accepts and
   * returns an iterable instead of an iterator.
   *
   * @see Iterators#filter(Iterator, Predicate)
   */
  public static <T> Iterable<T> filter(
      final Iterable<T> unfiltered, final Predicate<? super T> predicate) {
    checkNotNull(unfiltered);
    checkNotNull(predicate);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.filter(unfiltered.iterator(), predicate);
      }
    };
  }

  /**
   * Returns all instances of {@code type} found in {@code unfiltered}. Similar
   * to {@link #filter(Iterable, Predicate)}.
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
   * Returns {@code true} if some element in {@code iterable} evaluates to
   * {@code true} under {@code predicate}. Returns {@code false} if {@code
   * iterable} is empty.
   */
  public static <T> boolean any(
      Iterable<T> iterable, Predicate<? super T> predicate) {
    return Iterators.any(iterable.iterator(), predicate);
  }

  /**
   * Returns {@code true} if no element in {@code iterable} evaluates to {@code
   * false} under {@code predicate}. Returns {@code true} if {@code iterable} is
   * empty.
   */
  public static <T> boolean all(
      Iterable<T> iterable, Predicate<? super T> predicate) {
    return Iterators.all(iterable.iterator(), predicate);
  }

  /**
   * Returns the first element in {@code iterable} for which the given predicate
   * matches.
   *
   * @throws NoSuchElementException if no element in {@code iterable} matches
   *     the given predicate
   */
  public static <E> E find(Iterable<E> iterable,
      Predicate<? super E> predicate) {
    return Iterators.find(iterable.iterator(), predicate);
  }

  /**
   * Returns an iterable that applies {@code function} to each element of {@code
   * fromIterable}.
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

  // Methods only in Iterables, not in Iterators

  /**
   * Adapt a list to an Iterable over a reversed version of the list. Requires a
   * List so that we can reverse it with no copying required. Especially useful
   * in foreach-style loops:
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
   * Provides a rotated view of a list. Differs from {@link Collections#rotate}
   * only in that it leaves the underlying list unchanged. Note that this is a
   * "live" view of the list that will change as the list changes. However, the
   * behavior of an {@link Iterator} constructed from a rotated view of the list
   * is undefined if the list is changed after the Iterator is constructed.
   *
   * @param list the list to return a rotated view of.
   * @param distance the distance to rotate the list. There are no constraints
   *     on this value; it may be zero, negative, or greater than {@code
   *     list.size()}.
   * @return a rotated view of the given list
   */
  public static <T> Iterable<T> rotate(final List<T> list, final int distance) {
    checkNotNull(list);

    
    // If no rotation is requested, just return the original list
    if (distance == 0) {
      return list;
    }

    return new AbstractIterable<T>() {
      /**
       * Determines the actual distance we need to rotate (distance provided
       * might be larger than the size of the list or negative).
       */
      int calcActualDistance(int size) {
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
        if (size <= 1) {
          return list.iterator();
        }
        
        int actualDistance = calcActualDistance(size);
        // lists of a size that go into the distance evenly don't need rotation
        if (actualDistance == 0) {
          return list.iterator();
        }

        @SuppressWarnings("unchecked")
        Iterable<T> rotated = concat(list.subList(actualDistance, size),
            list.subList(0, actualDistance));
        return rotated.iterator();
      }
    };
  }

  /**
   * Variant of {@code Iterators.limit(Iterator,int)}, which accepts and
   * returns an iterable instead of an iterator.
   *
   * @see Iterators#limit(Iterator, int)
   */
  public static <T> Iterable<T> limit(
      final Iterable<T> iterable, final int limit) {
    checkNotNull(iterable);
    return new AbstractIterable<T>() {
      public Iterator<T> iterator() {
        return Iterators.limit(iterable.iterator(), limit);
      }
    };
  }

  /**
   * Returns whether the given iterable contains no elements.
   *
   * @return {@code true} if the iterable has no elements, {@code false} if the
   *     iterable has one or more elements
   */
  public static <T> boolean isEmpty(Iterable<T> iterable) {
    return !iterable.iterator().hasNext();
  }

  /**
   * Returns a view of {@code iterable} whose {@link Iterator} skips its first
   * {@code numberToSkip} elements or, if {@code iterable} contains fewer than
   * {@code numberToSkip} elements, skips all its elements.
   * <p>
   * Structural modifications to the underlying {@link Iterable}
   * <strong>before</strong> a call to {@link Iterable#iterator() iterator()}
   * are reflected in the returned {@code Iterator}. That is, the returned
   * {@code Iterator} skips the first {@code numberToSkip} elements that exist
   * when the {@code Iterator} is created, not when {@code skip()} is called.
   * <p>
   * Structural modifications to the underlying {@link Iterable}
   * <strong>after</strong> a call to {@code iterator()} may result in undefined
   * behavior by the returned {@code Iterator}, depending upon the
   * concurrent-modification policy of the underlying {@code Iterable}.
   * Non-structural changes to the underlying {@code Iterable} are always
   * reflected in the returned {@code Iterable}.
   * <p>
   * {@link Iterator#remove()} is supported if iterators of the underlying
   * {@code Iterable} supports it. (Note that it is <strong>not</strong>
   * possible to delete the last skipped element by immediately calling
   * {@code remove()} on a new iterator, as the {@code Iterator} contract
   * requires that a call to {@code remove()} before a call to
   * {@link Iterator#next() next()} will throw an
   * {@link IllegalStateException}.)
   */
  public static <T> Iterable<T> skip(final Iterable<T> iterable,
      final int numberToSkip) {
    checkNotNull(iterable);
    checkArgument(numberToSkip >= 0, "number to skip cannot be negative");

    if (iterable instanceof List<?>) {
      final List<T> list = (List<T>) iterable;      
      return new Iterable<T>() {
        public Iterator<T> iterator() {           
          return (numberToSkip >= list.size())
              ? Iterators.<T>emptyIterator() 
              : list.subList(numberToSkip, list.size()).iterator();
        }        
      };
    }
    
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        final Iterator<T> iterator = iterable.iterator();

        Iterators.skip(iterator, numberToSkip);

        /*
         * We can't just return the iterator because an immediate call to its
         * remove() method would remove one of the skipped elements instead of
         * throwing an IllegalStateException.
         */
        return new Iterator<T>() {
          boolean atStart = true;

          public boolean hasNext() {
            return iterator.hasNext();
          }

          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }

            try {
              return iterator.next();
            } finally {
              atStart = false;
            }
          }

          public void remove() {
            if (atStart) {
              throw new IllegalStateException();
            }
            iterator.remove();
          }
        };
      }
    };
  }

  /**
   * Returns the last element of {@code iterable}, or throws a
   * {@link NoSuchElementException} if it has no elements.
   */
  public static <T> T getLast(Iterable<T> iterable) {
    if (iterable instanceof List<?>) {
      List<T> list = (List<T>) iterable;
      if (list.isEmpty()) {
        throw new NoSuchElementException();
      }
      return list.get(list.size() - 1);
    }
    
    if (iterable instanceof SortedSet<?>) {
      SortedSet<T> sortedSet = (SortedSet<T>) iterable;
      return sortedSet.last();
    }
    
    return Iterators.getLast(iterable.iterator());    
  }
}
