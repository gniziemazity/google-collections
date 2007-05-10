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
 * <p>You can also create an empty {@code List}, or populate your new
 * {@code List} using any array, {@link Iterator} or {@link Iterable}.
 *
 * <p>Supported today are: {@link ArrayList} and {@link LinkedList}.
 *
 * <p>See also this class's counterparts {@link Sets} and {@link Maps}.
 *
 * <p>WARNING: These factories do not support the full variety of tuning
 * parameters available in the collection constructors.  Use them only for
 * collections which will always remain small, or for which the cost of future
 * growth operations is not a concern.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author mbostock@google.com (Mike Bostock)
 */
public final class Lists {
  private Lists() {}

  /**
   * Returns an immutable List instance containing the given elements.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code List<Base> list = Lists.immutableList(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code List<Base> list = Lists.<Base>immutableList(sub1, sub2);}
   *
   * @param elements the elements that the list should contain, in order.  If
   *     an array is given, its contents may later be altered without affecting
   *     the List returned by this method
   * @return an immutable {@code List} instance containing those elements
   */
  public static <E> List<E> immutableList(E... elements) {
    checkNotNull(elements);
    switch (elements.length) {
      case 0:
        return Collections.emptyList();
      case 1:
        return Collections.singletonList(elements[0]);
      default:
        return new ImmutableArrayList<E>(elements.clone());
    }
  }

  private static final Class<?> SINGLETON_CLASS =
      Collections.singletonList(1).getClass();

  /**
   * Returns an immutable {@code List} instance containing the given elements.
   * Note that if the input is an immutable list, then the input itself
   * <em>may</em> be returned.
   *
   * @param iterable the elements that the list should contain, in order
   * @return an immutable {@code List} instance containing those elements
   */
  public static <E> List<E> immutableList(Iterable<? extends E> iterable) {
    return (iterable instanceof Collection<?>)
        ? immutableList((Collection<? extends E>) iterable)
        : immutableList(iterable.iterator());
  }

  /**
   * Returns an immutable {@code List} instance containing the given elements.
   *
   * @param iterator the elements that the list should contain, in order
   * @return an immutable {@code List} instance containing those elements
   */
  public static <E> List<E> immutableList(Iterator<? extends E> iterator) {
    return immutableList(newArrayList(iterator));
  }

  /**
   * Returns an immutable {@code List} instance containing the given elements.
   * Note that if the input is an immutable list, then the input itself
   * <em>may</em> be returned.
   *
   * @param collection the elements that the list should contain, in order
   * @return an immutable {@code List} instance containing those elements
   */
  public static <E> List<E> immutableList(Collection<? extends E> collection) {
    checkNotNull(collection);
    int size = collection.size();
    if (size == 0) {
      return Collections.emptyList();
    } else if (SINGLETON_CLASS.isInstance(collection)
               || collection instanceof ImmutableArrayList) {
      // Casting away the wildcard on the generic type parameter should be safe
      // because the result is known to be immutable so any of the methods
      // we're exposing are unsupported anyway.
      // XXX This assumes that the implementation of Collections.singletonList
      // doesn't change in such a way that the class of instances returned by
      // it are sometimes mutable. Technically, that's a bit dirty.
      @SuppressWarnings("unchecked") List<E> result = (List<E>) collection;
      return result;
    } else if (size == 1) {
      // TODO(johannes): remove <E> when Eclipse is fixed
      return Collections.<E>singletonList(collection.iterator().next());
    } else {
      // This cast is also unchecked, but because of the impedance mismatch
      // between arrays and generics.
      @SuppressWarnings("unchecked") E[] array = (E[]) collection.toArray();
      return new ImmutableArrayList<E>(array);
    }
  }

  /**
   * Variant of {@code immutableList} for zero arguments.
   *
   * @return an immutable empty list
   * @see Collections#emptyList
   */
  public static <E> List<E> immutableList() {
    return Collections.emptyList();
  }

  /**
   * Variant of {@code immutableList} for one argument.
   *
   * @param element the lone element to be in the returned list
   * @return an immutable list containing only the given element
   * @see Collections#singletonList
   */
  public static <E> List<E> immutableList(@Nullable E element) {
    return Collections.singletonList(element);
  }

  // ArrayList

  /**
   * Creates an empty {@code ArrayList} instance.
   *
   * <p><b>Note:</b> if you only need an <i>immutable</i> empty List, use
   * {@link Collections#emptyList} instead.
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
   * <p><b>Note:</b> if it is an immutable List you seek, you should use
   * {@link #immutableList}.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code List<Base> list = Lists.newArrayList(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code List<Base> list = Lists.<Base>newArrayList(sub1, sub2);}
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(E... elements) {
    checkNotNull(elements);
    int capacity = (elements.length * 110) / 100 + 5;
    ArrayList<E> list = new ArrayList<E>(capacity);
    Collections.addAll(list, elements);
    return list;
  }

  /**
   * Creates an {@code ArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
    checkNotNull(elements);

    // Let ArrayList's sizing logic work, if possible
    return (elements instanceof Collection<?>)
           ? new ArrayList<E>((Collection<? extends E>) elements)
           : newArrayList(elements.iterator());
  }

  /**
   * Creates an {@code ArrayList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code ArrayList} containing those elements
   */
  public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
    checkNotNull(elements);
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
   *   initial capacity
   * @throws IllegalArgumentException if the specified initial capacity
   *   is negative
   */
  public static <E> ArrayList<E> newArrayListWithCapacity(int initialCapacity) {
    return new ArrayList<E>(initialCapacity);
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
  public static <E> LinkedList <E> newLinkedList() {
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
  public static <E> LinkedList <E> newLinkedList(E... elements) {
    checkNotNull(elements);
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
  public static <E> LinkedList <E> newLinkedList(
      Iterable<? extends E> elements) {
    checkNotNull(elements);
    return newLinkedList(elements.iterator());
  }

  /**
   * Creates a {@code LinkedList} instance containing the given elements.
   *
   * @param elements the elements that the list should contain, in order
   * @return a newly-created {@code LinkedList} containing those elements
   */
  public static <E> LinkedList <E> newLinkedList(
      Iterator<? extends E> elements) {
    checkNotNull(elements);
    LinkedList<E> list = newLinkedList();
    while (elements.hasNext()) {
      list.add(elements.next());
    }
    return list;
  }

  /**
   * Returns a copy of the given iterable which has been sorted using {@code
   * Collections.sort}. The input is not modified.
   *
   * <p>Unlike {@link Sets#newTreeSet(Iterable)}, this method does not collapse
   * elements that compare as zero, and the resulting collection does not
   * maintain its own sort order.  If you have no preference on these issues,
   * these two alternatives are equivalent, so you can choose for performance
   * factors.
   *
   * @param iterable the elements to be copied and sorted
   * @return a new list containing the given elements in sorted order
   */
  public static <E extends Comparable<? super E>> List<E> sortedCopy(
      Iterable<E> iterable) {
    checkNotNull(iterable);
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list);
    return list;
  }

  /**
   * Returns a copy of the given iterable which has been sorted using {@code
   * Collections.sort}. The input is not modified.
   *
   * <p>Unlike {@link Sets#newTreeSet(Iterable)}, this method does not collapse
   * elements that compare as zero, and the resulting collection does not
   * maintain its own sort order.  If you have no preference on these issues,
   * these two alternatives are equivalent, so you can choose for performance
   * factors.
   *
   * @param iterable the elements to be copied and sorted
   * @param comparator a comparator capable of sorting the given elements
   * @return a new list containing the given elements in sorted order
   */
  public static <E> List<E> sortedCopy(Iterable<E> iterable,
      Comparator<? super E> comparator) {
    checkNotNull(iterable);
    checkNotNull(comparator);
    List<E> list = Lists.newArrayList(iterable);
    Collections.sort(list, comparator);
    return list;
  }

  /**
   * Returns an unmodifiable list containing the specified first element and
   * backed by the specified array of additional elements. Changes to the {@code
   * rest} array will be reflected in the returned list. Unlike {@link
   * Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (Foo firstFoo, Foo... moreFoos)}, in order to avoid overload
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

  /** @see Lists#asList(Object, Object[]) */
  private static class OnePlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    private static final long serialVersionUID = -263507107612916621L;
    private final E first;
    private final E[] rest;
    public OnePlusArrayList(@Nullable E first, E[] rest) {
      checkNotNull(rest);
      this.first = first;
      this.rest = rest;
    }
    public int size() {
      return rest.length + 1;
    }
    public E get(int index) {
      return (index == 0) ? first : rest[index - 1]; // allow IOOBE to throw
    }
  }

  /**
   * Returns an unmodifiable list containing the specified first and second
   * element, and backed by the specified array of additional elements. Changes
   * to the {@code rest} array will be reflected in the returned list. Unlike
   * {@link Arrays#asList}, the returned list is unmodifiable.
   *
   * <p>This is useful when a varargs method needs to use a signature such as
   * {@code (Foo firstFoo, Foo secondFoo, Foo... moreFoos)}, in order to avoid
   * overload ambiguity or to enforce a minimum argument count.
   *
   * <p>The returned list is serializable and implements {@link RandomAccess}.
   *
   * @param first the first element
   * @param rest an array of additional elements, possibly empty
   * @return an unmodifiable list containing the specified elements
   */
  public static <E> List<E> asList(@Nullable E first, @Nullable E second,
      E[] rest) {
    return new TwoPlusArrayList<E>(first, second, rest);
  }

  /** @see Lists#asList(Object, Object, Object[]) */
  private static class TwoPlusArrayList<E> extends AbstractList<E>
      implements Serializable, RandomAccess {
    private static final long serialVersionUID = -1789891963162733178L;
    private final E first;
    private final E second;
    private final E[] rest;
    public TwoPlusArrayList(@Nullable E first, @Nullable E second, E[] rest) {
      checkNotNull(rest);
      this.first = first;
      this.second = second;
      this.rest = rest;
    }
    public int size() {
      return rest.length + 2;
    }
    public E get(int index) {
      switch (index) {
        case 0: return first;
        case 1: return second;
        default: return rest[index - 2]; // allow IOOBE to throw
      }
    }
  }

  /**
   * Returns a list that applies {@code function} to each element of {@code
   * fromList}. The returned list is a transformed view of {@code fromList},
   * similar to {@link Iterators#transform}: changes to {@code fromList} will
   * be reflected in the returned list and vice versa.
   *
   * <p>Functions are not reversible, so the transform is one-way and new items
   * cannot be added to the returned list. The {@code add}, {@code addAll} and
   * {@code set} methods are unsupported in the returned list.
   *
   * <p>As with {@link Iterators#transform}, the function is applied
   * lazily. This is necessary for returned list to be a view, but also means
   * that the function will be applied many times for bulk operations like
   * {@link List#contains} and {@link List#hashCode}. For this to perform well,
   * {@code function} should be fast. If you want to avoid lazy evaluation and
   * you don't need the returned list to be a view, you can dump the returned
   * list into a new list of your choosing. Alternatively, you can use a
   * memoizing ("canonicalizing") function.
   *
   * <p>If {@code fromList} implements {@link RandomAccess}, so will the
   * returned list. TODO: provide similar support for
   * {@link java.io.Serializable}.
   */
  public static <F, T> List<T> transform(List<F> fromList,
      Function<? super F, ? extends T> function) {
    checkNotNull(fromList);
    checkNotNull(function);
    return (fromList instanceof RandomAccess)
      ? new TransformingRandomAccessList<F, T>(fromList, function)
      : new TransformingList<F, T>(fromList, function);
  }

  /**
   * Implementation of a transforming list. We try to make as many of these
   * methods pass-through to the source list as possible so that the
   * performance characteristics of the source list and transformed list are
   * similar.
   *
   * @see Lists#transform
   */
  private static class TransformingList<F, T> extends AbstractList<T>
      implements Serializable {
    private static final long serialVersionUID = -5874381536079320827L;
    private final List<F> fromList;
    private final Function<? super F, ? extends T> function;

    public TransformingList(List<F> fromList,
        Function<? super F, ? extends T> function) {
      this.fromList = fromList;
      this.function = function;
    }

    public boolean add(T t) {
      throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }

    public void clear() {
      fromList.clear();
    }

    public T get(int index) {
      return function.apply(fromList.get(index));
    }

    public boolean isEmpty() {
      return fromList.isEmpty();
    }

    public Iterator<T> iterator() {
      return Iterators.transform(fromList.iterator(), function);
    }

    public ListIterator<T> listIterator(int index) {
      return new TransformingListIterator<F, T>(fromList.listIterator(index),
          function);
    }

    public T remove(int index) {
      return function.apply(fromList.remove(index));
    }

    public int size() {
      return fromList.size();
    }

    public List<T> subList(int fromIndex, int toIndex) {
      return transform(fromList.subList(fromIndex, toIndex), function);
    }
  }

  /**
   * Trivial subclass of {@code TransformingList} that preserves the {@code
   * RandomAccess} interface marker.
   *
   * @see Lists#transform
   */
  private static class TransformingRandomAccessList<F, T>
      extends TransformingList<F, T> implements RandomAccess {
    private static final long serialVersionUID = -7837562545549389035L;
    public TransformingRandomAccessList(List<F> fromList,
        Function<? super F, ? extends T> function) {
      super(fromList, function);
    }
  }

  /**
   * Implementation of transforming list iterator. Again, as with {@link
   * TransformingList}, we try to make as many of these methods pass through to
   * the source list iterator as possible.
   *
   * @see Lists#transform
   */
  private static class TransformingListIterator<F, T>
      implements ListIterator<T> {
    private final ListIterator<F> fromIterator;
    private final Function<? super F, ? extends T> function;

    public TransformingListIterator(ListIterator<F> fromIterator,
        Function<? super F, ? extends T> function) {
      this.fromIterator = fromIterator;
      this.function = function;
    }

    public void add(T o) {
      throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
      return fromIterator.hasNext();
    }

    public boolean hasPrevious() {
      return fromIterator.hasPrevious();
    }

    public T next() {
      return function.apply(fromIterator.next());
    }

    public int nextIndex() {
      return fromIterator.nextIndex();
    }

    public T previous() {
      return function.apply(fromIterator.previous());
    }

    public int previousIndex() {
      return fromIterator.previousIndex();
    }

    public void remove() {
      fromIterator.remove();
    }

    public void set(T t) {
      throw new UnsupportedOperationException();
    }
  }

  private static class ImmutableArrayList<E> extends AbstractList<E>
      implements RandomAccess, Serializable {

    static final long serialVersionUID = 0x0a2ae799;

    final E[] array;

    /**
     * @param array underlying array for this ImmutableArrayList. Note that the
     * array is <b>not</b> cloned. The caller is responsible for ensuring that
     * the array can't "escape".
     */
    ImmutableArrayList(E[] array) {
      this.array = array;
    }

    public E get(int index) {
      return array[index];
    }

    public int size() {
      return array.length;
    }

    /* override some methods just to get tiny optimizations */

    @Override public Object[] toArray() {
      Object[] newArray = new Object[array.length];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    }

    @Override public String toString() {
      return Arrays.toString(array);
    }

    @Override public int hashCode() {
      return Arrays.hashCode(array);
    }
  }
}
