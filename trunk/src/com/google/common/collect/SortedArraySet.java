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

import com.google.common.base.Nullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * A sorted set that keeps its elements in a sorted {@code ArrayList}. Null
 * elements are allowed when the {@code SortedArraySet} is constructed with an
 * explicit comparator that supports nulls.
 *
 * <p>This class is useful when you may have many sorted sets that only have
 * zero or one elements each. The performance of this implementation does not
 * scale to large numbers of elements as well as {@link java.util.TreeSet}, but
 * it is much more memory-efficient per entry.
 *
 * <p>Each {@code SortedArraySet} has a <i>capacity</i>, because it is backed by
 * an {@code ArrayList}. The capacity is the size of the array used to store the
 * elements in the set. It is always at least as large as the set size. As
 * elements are added to the set, its capacity grows automatically. The details
 * of the growth policy are not specified beyond the fact that adding an element
 * has O(lg n) amortized time cost.
 *
 * <p>An application can increase the capacity of the set before adding a large
 * number of elements using the {@code ensureCapacity} operation. This may
 * reduce the amount of incremental reallocation.
 *
 * <p><b>This implementation is not synchronized.</b> As with {@code ArrayList},
 * external synchronization is needed if multiple threads access a {@code
 * SortedArraySet} instance concurrently and at least one adds or deletes any
 * elements.
 *
 * @author Matthew Harris
 * @author Mike Bostock
 */
public final class SortedArraySet<E> extends AbstractSet<E>
    implements SortedSet<E>, Serializable {
  private ArrayList<E> contents; // initialized lazily
  private final Comparator<? super E> comparator;
  
  /**
   * Constructs a new empty sorted set, sorted according to the element's
   * natural order, with an initial capacity of ten. All elements inserted into
   * the set must implement the {@code Comparable} interface. Furthermore, all
   * such elements must be <i>mutally comparable</i>: {@code e1.compareTo(e2)}
   * must not throw a {@code ClassCastException} for any elements {@code e1} and
   * {@code e2} in the set. If the user attempts to add an element to the set
   * that violates this constraint (for example, the user attempts to add a
   * string element to a set whose elements are integers), the {@code add}
   * method may throw a {@code ClassCastException}.
   *
   * @see Comparable
   * @see Comparators#naturalOrder
   */
  public SortedArraySet() {
    this(10);
  }

  /**
   * Constructs a new empty sorted set, sorted according to the element's
   * natural order, with the specified initial capacity. All elements inserted
   * into the set must implement the {@code Comparable} interface. Furthermore,
   * all such elements must be <i>mutually comparable</i>: {@code
   * e1.compareTo(e2)} must not throw a {@code ClassCastException} for any
   * elements {@code e1} and {@code e2} in the set. If the user attempts to add
   * an element to the set that violates this constraint (for example, the user
   * attempts to add a string element to a set whose elements are integers), the
   * {@code add} method may throw a {@code ClassCastException}.
   *
   * @param initialCapacity the initial capacity of the list
   * @throws IllegalArgumentException if {@code initialCapacity} is negative
   * @see Comparators#naturalOrder
   */
  public SortedArraySet(int initialCapacity) {
    checkArgument(initialCapacity >= 0);
    comparator = orNaturalOrder(null);
    if (initialCapacity > 0) {
      contents = new ArrayList<E>(initialCapacity);
    }
  }

  /**
   * Creates a new empty sorted set, sorted according to the specified
   * comparator, with the initial capacity of ten. All elements inserted into
   * the set must be <i>mutually comparable</i> by the specified comparator:
   * {@code comparator.compare(e1,e2)} must not throw a {@code
   * ClassCastException} for any elements {@code e1} and {@code e2} in the set.
   * If the user attempts to add an element to the set that violates this
   * constraint, the {@code add} method may throw a {@code ClassCastException}.
   *
   * @param comparator the comparator used to sort elements in this set
   */
  public SortedArraySet(Comparator<? super E> comparator) {
    this(comparator, 10);
  }

  /**
   * Creates a new empty sorted set, sorted according to the specified
   * comparator, with the specified initial capacity. All elements inserted into
   * the set must be <i>mutually comparable</i> by the specified comparator:
   * {@code comparator.compare(e1,e2)} must not throw a {@code
   * ClassCastException} for any elements {@code e1} and {@code e2} in the set.
   * If the user attempts to add an element to the set that violates this
   * constraint, the {@code add} method may throw a {@code ClassCastException}.
   *
   * @param comparator the comparator used to sort elements in this set
   */
  public SortedArraySet(Comparator<? super E> comparator, int initialCapacity) {
    checkNotNull(comparator);
    this.comparator = comparator;
    if (initialCapacity > 0) {
      contents = new ArrayList<E>(initialCapacity);
    }
  }

  /**
   * Creates a new sorted set with the same elements as the specified
   * collection. If the specified collection is a {@code SortedSet} instance,
   * this constructor behaves identically to {@link #SortedArraySet(SortedSet)}.
   * Otherwise, the elements are sorted according to the elements' natural
   * order; see {@link #SortedArraySet()}.
   *
   * @param collection the elements that will comprise the new set
   * @throws ClassCastException if the elements in the specified collection are
   *     not mutually comparable
   */
  @SuppressWarnings("unchecked")
  public SortedArraySet(Collection<? extends E> collection) {
    if (collection instanceof SortedSet<?>) {
      comparator = orNaturalOrder(((SortedSet<E>) collection).comparator());
    } else {
      comparator = orNaturalOrder(null);
    }
    addAll(collection); // careful if we make this class non-final
  }

  /**
   * Creates a new sorted set with the same elements and the same ordering as
   * the specified sorted set.
   *
   * @param set the set whose elements will comprise the new set
   */
  public SortedArraySet(SortedSet<E> set) {
    comparator = orNaturalOrder(set.comparator());
    addAll(set); // careful if we make this class non-final
  }

  /**
   * Increases the capacity of this sorted set, if necessary, to ensure that it
   * can hold at least the number of elements specified by the minimum capacity
   * argument.
   *
   * @param minCapacity the desired minimum capacity
   */
  public void ensureCapacity(int minCapacity) {
    if (contents == null) {
      if (minCapacity > 0) {
        contents = new ArrayList<E>(minCapacity);
      }
    } else {
      contents.ensureCapacity(minCapacity);
    }
  }

  /**
   * Trims the capacity of this sorted set to be the set's current size. An
   * application can use this operation to minimize the storage of a sorted
   * set.
   */
  public void trimToSize() {
    if (size() == 0) {
      contents = null;
    } else {
      contents.trimToSize();
    }
  }

  @Override public boolean add(E o) {
    if (contents == null) {
      contents = new ArrayList<E>(1);
      contents.add(o);
      return true;
    }
    int pos = binarySearch(o);
    if (pos < 0) {
      contents.add(-pos - 1, o);
      return true;
    }
    return false;
  }

  @Override public boolean addAll(Collection<? extends E> c) {
    // optimize the case where c is sorted and we're empty
    if (((contents == null) || contents.isEmpty())
        && !c.isEmpty() && (c instanceof SortedSet<?>)) {
      Comparator<?> comparator2 = ((SortedSet<?>) c).comparator();
      if (((comparator == Comparators.naturalOrder())
          && (comparator2 == null)) ||
          (comparator == comparator2)) {
        if (contents == null) {
          contents = new ArrayList<E>(c);
        } else {
          contents.addAll(c);
        }
        return true;
      }
    }
    return super.addAll(c);
  }

  @Override public void clear() {
    contents = null;
  }

  @Override public boolean contains(Object o) {
    return binarySearch(o) >= 0;
  }

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SortedArraySet<?>) {
      SortedArraySet<?> set = (SortedArraySet<?>) o;
      if (comparator == set.comparator) {
        int n = size();
        return (n == set.size()) // beware of null contents
            && ((n == 0) || contents.equals(set.contents));
      }
    }
    return super.equals(o);
  }

  @Override public Iterator<E> iterator() {
    return (contents == null)
        ? Iterators.<E>emptyIterator() : contents.iterator();
  }

  @Override public boolean remove(Object o) {
    int pos = binarySearch(o);
    if (pos < 0) {
      return false;
    }
    contents.remove(pos);
    return true;
  }

  @Override public int size() {
    return (contents == null) ? 0 : contents.size();
  }

  /**
   * Returns the comparator associated with this sorted set, or {@code
   * Comparators.naturalOrder} if it uses its elements' natural ordering.
   */
  public Comparator<? super E> comparator() {
    return comparator;
  }

  public SortedSet<E> subSet(E fromElement, E toElement) {
    checkArgument(comparator.compare(toElement, fromElement) >= 0);
    return new SubSet(fromElement, toElement, true, true);
  }

  public SortedSet<E> headSet(E toElement) {
    return new SubSet(null, toElement, false, true);
  }

  public SortedSet<E> tailSet(E fromElement) {
    return new SubSet(fromElement, null, true, false);
  }

  public E first() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return get(0);
  }

  public E last() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return get(size() - 1);
  }

  /**
   * Searches the backing list of the specified object using the binary search
   * algorithm.
   *
   * <p>This method runs in O(lg n) time.
   *
   * @param o the object to be searched for
   * @return the index of the found object, if it is contained in the list;
   *     otherwise, {@code (-(insertionpoint) - 1)}. The <i>insertion point</i>
   *     is defined as the point at which the object would be inserted into the
   *     list: the index of the first element greater than the key, or {@code
   *     list.size()}, if all elements in the list are less than the specified
   *     key. Note that this guarantees that the return value will be &gt;= 0 if
   *     and only if the key is found.
   * @throws ClassCastException if the list contains elements that are not
   *     <i>mutually comparable</i> (for example, strings and integers), or the
   *     specified object is not mutually comparable with the elements of the
   *     list
   * @see Collections#binarySearch(java.util.List, Object, Comparator)
   */
  private int binarySearch(Object o) {
    if (contents == null) {
      return -1;
    }
    @SuppressWarnings("unchecked")
    E e = (E) o;
    return Collections.binarySearch(contents, e, comparator);
  }

  /**
   * Returns the element at the specified position in the backing list.
   *
   * @param index the index of the element to return
   * @throws NoSuchElementException if the specified index is out of bounds
   */
  private E get(int index) {
    if (contents == null) {
      throw new NoSuchElementException();
    }
    try {
      return contents.get(index);
    } catch (IndexOutOfBoundsException e) {
      throw new NoSuchElementException();
    }
  }

  /**
   * Returns the specified comparator if not null; otherwise returns {@code
   * Comparators.naturalOrder}. This method is an abomination of generics; the
   * only purpose of this method is to contain the ugly type-casting in one
   * place.
   */
  @SuppressWarnings("unchecked")
  private Comparator<? super E> orNaturalOrder(
      @Nullable Comparator<? super E> comparator) {
    if (comparator != null) { // can't use ? : because of javac bug 5080917
      return comparator;
    }
    return (Comparator<E>) Comparators.naturalOrder();
  }

  /** @see #subSet */
  private class SubSet extends AbstractSet<E> implements SortedSet<E> {
    final E head;
    final E tail;
    final boolean hasHead;
    final boolean hasTail;

    /**
     * Constructs a subset view into the SortedArraySet.
     *
     * @param fromElement the low endpoint (inclusive) of the subset, or
     *     {@code null}
     * @param toElement the high endpoint (exclusive) of the subset, or
     *     {@code null}
     * @param hasHead whether this subset has a lower bound
     * @param hasTail whether this subset has an upper bound 
     */
    SubSet(@Nullable E fromElement, @Nullable E toElement,
        boolean hasHead, boolean hasTail) {
      this.head = fromElement;
      this.tail = toElement;
      this.hasHead = hasHead;
      this.hasTail = hasTail;
    }

    /**
     * Returns the index of the low endpoint (inclusive) of the subset, or zero
     * if the low endpoint is undefined.
     */
    int headIndex() {
      if (!hasHead) {
        return 0;
      }
      int pos = binarySearch(head);
      return (pos < 0) ? (-pos - 1) : pos;
    }

    /**
     * Returns the position of the high endpoint (exclusive) of the subset, or
     * the size of the list if the high endpoint is undefined.
     */
    int tailIndex() {
      if (contents == null) {
        return 0;
      }
      if (!hasTail) {
        return contents.size();
      }
      int pos = binarySearch(tail);
      return (pos < 0) ? (-pos - 1) : pos;
    }

    /**
     * Throws an {@code IllegalArgumentException} if the head of this subset
     * does not precede or equal the specified element.
     *
     * @param fromElement the element to compare to the head
     */
    void checkHead(E fromElement) {
      if (hasHead) {
        checkArgument(comparator.compare(fromElement, head) >= 0);
      }
    }

    /**
     * Throws an {@code IllegalArgumentException} if the specified element does
     * not precede the tail of this subset.
     *
     * @param toElement the element to compare to the tail
     */
    void checkTail(E toElement) {
      if (hasTail) {
        checkArgument(comparator.compare(tail, toElement) > 0);
      }
    }

    @Override public int size() {
      return tailIndex() - headIndex();
    }

    public Comparator<? super E> comparator() {
      return comparator;
    }

    @Override public Iterator<E> iterator() {
      return (contents == null) ?
          Iterators.<E>emptyIterator() :
          contents.subList(headIndex(), tailIndex()).iterator();
    }

    @Override public boolean contains(Object o) {
      @SuppressWarnings("unchecked") // throws ClassCastException
      E e = (E) o;
      if ((hasHead && (comparator.compare(e, head) < 0))
          || (hasTail && (comparator.compare(tail, e) <= 0))) {
        return false;
      }
      return SortedArraySet.this.contains(o);
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
      checkArgument(comparator.compare(toElement, fromElement) >= 0);
      checkHead(fromElement);
      checkTail(toElement);
      return new SubSet(fromElement, toElement, true, true);
    }

    public SortedSet<E> headSet(E toElement) {
      checkHead(toElement);
      checkTail(toElement);
      return new SubSet(head, toElement, hasHead, true);
    }

    public SortedSet<E> tailSet(E fromElement) {
      checkHead(fromElement);
      checkTail(fromElement);
      return new SubSet(fromElement, tail, true, hasTail);
    }

    public E first() {
      E o = get(headIndex());
      if (hasTail && (comparator.compare(tail, o) <= 0)) {
        throw new NoSuchElementException();
      }
      return o;
    }

    public E last() {
      E o = get(tailIndex() - 1);
      if (hasHead && (comparator.compare(o, head) < 0)) {
        throw new NoSuchElementException();
      }
      return o;
    }
  }
  
  private static final long serialVersionUID = -296929484947694088L;  
}
