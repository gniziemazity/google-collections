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
import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * Provides static methods for creating {@code List} instances easily, and other
 * utility methods for working with lists. You can replace code like:
 *
 * <p>{@code List<String> list = new ArrayList<String>();}
 * <br>{@code Collections.addAll(list, "foo", "bar", "baz");}
 *
 * <p>with just:
 *
 * <p>{@code List<String> list = newArrayList("foo", "bar", "baz");}
 *
 * <p>You can also create an empty {@code List}, or populate your new {@code
 * List} using any array, {@link Iterator} or {@link Iterable}.
 *
 * <p>Supported today are: {@link ArrayList} and {@link LinkedList}.
 *
 * <p>See also this class's counterparts {@link Sets} and {@link Maps}.
 *
 * <p>WARNING: These factories do not support the full variety of tuning
 * parameters available in the collection constructors. Use them only for
 * collections which will always remain small, or for which the cost of future
 * growth operations is not a concern.
 *
 * @author Kevin Bourrillion
 * @author Mike Bostock
 */
public final class Lists {
  private Lists() {}

  // ArrayList

  /**
   * Creates an empty {@code ArrayList} instance.
   *
   * <p><b>Note:</b> if you only need an <i>immutable</i> empty List, use {@link
   * Collections#emptyList} instead.
   *
   * @return a newly-created, initially-empty {@code ArrayList}
   */
  public static <E> ArrayList<E> newArrayList() {
    return new ArrayList<E>();
  }

  /**
   * Creates a resizable {@code ArrayList} instance containing the given
   * elements.
   *
   * <p><b>Note:</b> if it is an immutable List you seek, you should use {@link
   * ImmutableList}.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code List<Base> list = Lists.newArrayList(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of {@code
   * Base}, not of {@code Base} itself. To get around this, you must use:
   *
   * <p>{@code List<Base> list = Lists.<Base>newArrayList(sub1, sub2);}
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(E... elements) {
    // Avoid integer overflow when a large array is passed in
    int capacity = computeArrayListCapacity(elements.length);
    ArrayList<E> list = new ArrayList<E>(capacity);
    Collections.addAll(list, elements);
    return list;
  }
  
  // @VisibleForTesting
  static int computeArrayListCapacity(int arraySize) {
    return (int) Math.min(5L + arraySize + (arraySize / 10), Integer.MAX_VALUE);
  }
  
  /**
   * Creates an {@code ArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
    // Let ArrayList's sizing logic work, if possible
    if (elements instanceof Collection<?>) {
      @SuppressWarnings("unchecked")
      Collection<? extends E> collection = (Collection<? extends E>) elements;
      return new ArrayList<E>(collection);
    } else {
      return newArrayList(elements.iterator());
    }
  }

  /**
   * Creates an {@code ArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
    ArrayList<E> list = newArrayList();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }

  /**
   * Creates an {@code ArrayList} instance with the given initial capacity.
   *
   * @param initialCapacity the initial capacity of the list
   * @return a newly-created, initially empty {@code ArrayList} with the given
   *     initial capacity
   * @throws IllegalArgumentException if the specified initial capacity is
   *     negative
   */
  public static <E> ArrayList<E> newArrayListWithCapacity(int initialCapacity) {
    return new ArrayList<E>(initialCapacity);
  }

  /**
   * Creates an {@code ArrayList} instance with the given expected size.
   *
   * @param expectedSize the expected size of the list
   * @return a newly-created, initially empty {@code ArrayList} with enough
   *     capacity for the given expected size
   * @throws IllegalArgumentException if the specified expected size is negative
   */
  public static <E> ArrayList<E> newArrayListWithExpectedSize(int expectedSize)
  {
    return new ArrayList<E>(computeArrayListCapacity(expectedSize));
  }

  // LinkedList

  /**
   * Creates an empty {@code LinkedList} instance.
   *
   * <p><b>Note:</b> if you only need an <i>immutable</i> empty {@link List},
   * use {@link Collections#emptyList} instead.
   *
   * @return a newly-created, initially-empty {@code LinkedList}
   */
  public static <E> LinkedList<E> newLinkedList() {
    return new LinkedList<E>();
  }

  /**
   * Creates a {@code LinkedList} instance containing the given elements.
   *
   * <p>Please see the caveat in {@link #newArrayList(Object...)}.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code LinkedList} containing those elements
   */
  public static <E> LinkedList<E> newLinkedList(E... elements) {
    LinkedList<E> list = newLinkedList();
    Collections.addAll(list, elements);
    return list;
  }

  /**
   * Creates a {@code LinkedList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code LinkedList} containing those elements
   */
  public static <E> LinkedList<E> newLinkedList(
      Iterable<? extends E> elements) {
    return newLinkedList(elements.iterator());
  }

  /**
   * Creates a {@code LinkedList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code LinkedList} containing those elements
   */
  public static <E> LinkedList<E> newLinkedList(
      Iterator<? extends E> elements) {
    LinkedList<E> list = newLinkedList();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }

  /**
   * Returns a copy of the given iterable sorted by the natural ordering of its
   * elements. The input is not modified.
   *
   * <p>Unlike {@link Sets#newTreeSet(Iterable)}, this method does not collapse
   * elements that compare as zero, and the resulting collection does not
   * maintain its own sort order. If you have no preference on these issues,
   * these two alternatives are equivalent, so you can choose for performance
   * factors.
   *
   * @param iterable the elements to be copied and sorted
   * @return a new list containing the given elements in sorted order
   * @throws ClassCastException if {@code iterable} contains elements which are
   *     not <i>mutually comparable</i>
   */  
  @SuppressWarnings("unchecked")
  public static <E extends Comparable> List<E> sortedCopy(Iterable<E> iterable)
  {
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list);
    return list;
  }

  /**
   * Returns a copy of the given iterable sorted by an explicit comparator. The
   * input is not modified.
   * 
   * <p>Unlike {@link Sets#newTreeSet(Comparator, Iterable)}, this method does
   * not collapse elements that compare as zero, and the resulting collection
   * does not maintain its own sort order. If you have no preference on these
   * issues, these two alternatives are equivalent, so you can choose for
   * performance factors.
   * 
   * @param iterable the elements to be copied and sorted
   * @param comparator a comparator capable of sorting the given elements
   * @return a new list containing the given elements in sorted order
   */
  public static <E> List<E> sortedCopy(
      Iterable<E> iterable, Comparator<? super E> comparator) {
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list, checkNotNull(comparator));
    return list;
  }

  /**
   * Returns an unmodifiable list containing the specified first element and
   * backed by the specified array of additional elements. Changes to the {@code
   * rest} array will be reflected in the returned list. Unlike {@link
   * Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (FoofirstFoo,Foo...moreFoos)}, in order to avoid overload
   * ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(@Nullable E first, E[] rest) {
    return new OnePlusArrayList<E>(first, rest);
  }

  /** @see Lists#asList(Object,Object[]) */
  private static class OnePlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    final E first;
    final E[] rest;

    OnePlusArrayList(@Nullable E first, E[] rest) {
      this.first = first;
      this.rest = checkNotNull(rest);
    }
    @Override public int size() {
      return rest.length + 1;
    }
    @Override public E get(int index) {
      return (index == 0) ? first : rest[index - 1]; // allow IOOBE to throw
    }
    private static final long serialVersionUID = -263507107612916621L;
  }

  /**
   * Returns an unmodifiable list containing the specified first and second
   * element, and backed by the specified array of additional elements. Changes
   * to the {@code rest} array will be reflected in the returned list. Unlike
   * {@link Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (FoofirstFoo,FoosecondFoo,Foo...moreFoos)}, in order to avoid
   * overload ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(
      @Nullable E first, @Nullable E second, E[] rest) {
    return new TwoPlusArrayList<E>(first, second, rest);
  }

  /** @see Lists#asList(Object,Object,Object[]) */
  private static class TwoPlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    final E first;
    final E second;
    final E[] rest;

    TwoPlusArrayList(@Nullable E first, @Nullable E second, E[] rest) {
      this.first = first;
      this.second = second;
      this.rest = checkNotNull(rest);
    }
    @Override public int size() {
      return rest.length + 2;
    }
    @Override public E get(int index) {
      switch (index) {
        case 0:
          return first;
        case 1:
          return second;
        default:
          return rest[index - 2]; // allow IOOBE to throw
      }
    }
    private static final long serialVersionUID = -1789891963162733178L;
  }

  /**
   * Returns a list that applies {@code function} to each element of {@code
   * fromList}. The returned list is a transformed view of {@code fromList},
   * similar to {@link Iterators#transform}: changes to {@code fromList} will be
   * reflected in the returned list and vice versa.
   *
   * <p>Functions are not reversible, so the transform is one-way and new items
   * cannot be added to the returned list. The {@code add}, {@code addAll} and
   * {@code set} methods are unsupported in the returned list.
   *
   * <p>As with {@link Iterators#transform}, the function is applied lazily.
   * This is necessary for returned list to be a view, but also means that the
   * function will be applied many times for bulk operations like {@link
   * List#contains} and {@link List#hashCode}. For this to perform well, {@code
   * function} should be fast. If you want to avoid lazy evaluation and you
   * don't need the returned list to be a view, you can dump the returned list
   * into a new list of your choosing. Alternatively, you can use a memoizing
   * ("canonicalizing") function.
   *
   * <p>If {@code fromList} implements {@link RandomAccess}, so will the
   * returned list. TODO: provide similar support for {@link
   * java.io.Serializable}.
   */
  public static <F, T> List<T> transform(
      List<F> fromList, Function<? super F, ? extends T> function) {
    return (fromList instanceof RandomAccess)
        ? new TransformingRandomAccessList<F, T>(fromList, function)
        : new TransformingSequentialList<F, T>(fromList, function);
  }

  /**
   * Implementation of a sequential transforming list. We try to implement
   * as few methods as possible here to avoid duplicating code from {@link
   * AbstractSequentialList}.
   *
   * @see Lists#transform
   */
  private static class TransformingSequentialList<F, T>
      extends AbstractSequentialList<T> implements Serializable {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingSequentialList(
        List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }
    /**
     * The default implementation inherited is based on iteration and removal of
     * each element which can be overkill. That's why we forward this call
     * directly to the backing list.
     */
    @Override public void clear() {
      fromList.clear();
    }
    @Override public int size() {
      return fromList.size();
    }
    @Override
    public ListIterator<T> listIterator(final int index) {
      final ListIterator<F> delegate = fromList.listIterator(index);
      return new ListIterator<T>() {
        public void add(T e) {
          throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
          return delegate.hasNext();
        }

        public boolean hasPrevious() {
          return delegate.hasPrevious();
        }

        public T next() {
          return function.apply(delegate.next());
        }

        public int nextIndex() {
          return delegate.nextIndex();
        }

        public T previous() {
          return function.apply(delegate.previous());
        }

        public int previousIndex() {
          return delegate.previousIndex();
        }

        public void remove() {
          delegate.remove();
        }

        public void set(T e) {
          throw new UnsupportedOperationException("not supported");
        }
      };
    }
    private static final long serialVersionUID = -5874381536079320827L;
  }

  /**
   * Implementation of a transforming random access list. We try to make as many
   * of these methods pass-through to the source list as possible so that the
   * performance characteristics of the source list and transformed list are
   * similar.
   *
   * @see Lists#transform
   */
  private static class TransformingRandomAccessList<F, T>
      extends AbstractList<T> implements RandomAccess, Serializable {
    final List<F> fromList;
    final Function<? super F, ? extends T> function;

    TransformingRandomAccessList(
        List<F> fromList, Function<? super F, ? extends T> function) {
      this.fromList = checkNotNull(fromList);
      this.function = checkNotNull(function);
    }
    @Override public void clear() {
      fromList.clear();
    }
    @Override public T get(int index) {
      return function.apply(fromList.get(index));
    }
    @Override public boolean isEmpty() {
      return fromList.isEmpty();
    }
    @Override public T remove(int index) {
      return function.apply(fromList.remove(index));
    }
    @Override public int size() {
      return fromList.size();
    }
    private static final long serialVersionUID = -7837562545549389035L;
  }
}
