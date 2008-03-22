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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A high-performance, immutable, hash-based {@code Set} with reliable,
 * user-specified iteration order. Does not permit null elements.
 *
 * <p>Unlike {@link Collections#unmodifiableSet}, which is a <i>view</i> of a
 * separate collection that can still change, an instance of {@code
 * ImmutableSet} contains its own private data and will <i>never</i> change.
 * {@code ImmutableSet} is convenient for {@code public static final} sets
 * ("constant sets") and also lets you easily make a "defensive copy" of a set
 * provided to your class by a caller.
 * 
 * <p><b>Warning:</b> Like most sets, an {@code ImmutableSet} will not function
 * correctly if an element is modified after being placed in the set. For this
 * reason, and to avoid general confusion, it is strongly recommended to place
 * only immutable objects into this collection.
 *
 * <p>This class has been observed to perform significantly better than {@link
 * HashSet} for objects with very fast {@link Object#hashCode} implementations
 * (as a well-behaved immutable object should).
 *
 * <p><b>Note</b>: Although this class is not final, it cannot be subclassed as
 * it has no public or protected constructors. Thus, the immutability guarantee
 * can be trusted.
 *
 * @see ImmutableList
 * @author Kevin Bourrillion
 */
@SuppressWarnings("serial") // we're overriding default serialization
public abstract class ImmutableSet<E> implements Set<E>, Serializable {
  private static final ImmutableSet<?> EMPTY_IMMUTABLE_SET
      = new EmptyImmutableSet();

  /**
   * Returns the empty immutable set. This set behaves and performs comparably
   * to {@link Collections#emptySet}, and is preferable mainly for consistency
   * and maintainability of your code.
   */
  // Casting to any type is safe because the set will never hold any elements.
  @SuppressWarnings({"unchecked"})
  public static <E> ImmutableSet<E> of() {
    return (ImmutableSet<E>) EMPTY_IMMUTABLE_SET;
  }

  /**
   * Returns an immutable set containing a single element. This set behaves and
   * performs comparably to {@link Collections#singleton}, but will not accept
   * a null element. It is preferable mainly for consistency and
   * maintainability of your code.
   */
  public static <E> ImmutableSet<E> of(E element) {
    return new SingletonImmutableSet<E>(element, element.hashCode());
  }

  /**
   * Returns an immutable set containing the given elements, in order. Repeated
   * occurrences of an element (according to {@link Object#equals}) after the
   * first are ignored (but too many of these may result in the set being
   * sized inappropriately).
   *
   * @throws NullPointerException if any of {@code elements} are null
   */
  public static <E> ImmutableSet<E> of(E... elements) {
    switch (elements.length) {
      case 0:
        return of();
      case 1:
        return of(elements[0]);
      default:
        return create(Arrays.asList(elements), elements.length);
    }
  }

  /**
   * Returns an immutable set containing the given elements, in order. Repeated
   * occurrences of an element (according to {@link Object#equals}) after the
   * first are ignored (but too many of these may result in the set being
   * sized inappropriately).
   *
   * <p>Note that if {@code s} is a {@code Set<String>}, then {@code
   * ImmutableSet.copyOf(s)} returns a {@code ImmutableSet<String>} containing
   * each of the strings in {@code s}, while ImmutableSet.of(s)} returns a
   * {@code ImmutableSet<Set<String>>} containing one element (the given set
   * itself).</p>
   *
   * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}
   * is an {@code ImmutableSet}, no copy will actually be performed, and the
   * given set itself will be returned.
   *
   * @throws NullPointerException if any of {@code elements} are null
   */
  public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
    if (elements instanceof ImmutableSet<?>) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableSet<E> set = (ImmutableSet<E>) elements;
      return set;
    }
    int size = Iterables.size(elements);
    switch (size) {
      case 0:
        return of();
      case 1:
        // TODO: remove "ImmutableSet.<E>" when eclipse bug is fixed
        return ImmutableSet.<E>of(elements.iterator().next());
      default:
        return create(elements, size);
    }
  }

  private final int hashCode;

  private ImmutableSet(int hashCode) {
    this.hashCode = hashCode;
  }

  // Overriding to mark it Nullable
  public abstract boolean contains(@Nullable Object target);

  public boolean containsAll(Collection<?> targets) {
    for (Object target : targets) {
      if (!contains(target)) {
        return false;
      }
    }
    return true;
  }

  /** Not supported. */
  public final boolean add(E newElement) {
    throw new UnsupportedOperationException();
  }
  /** Not supported. */
  public final boolean remove(Object oldElement) {
    throw new UnsupportedOperationException();
  }
  /** Not supported. */
  public final boolean addAll(Collection<? extends E> newElements) {
    throw new UnsupportedOperationException();
  }
  /** Not supported. */
  public final boolean removeAll(Collection<?> oldElements) {
    throw new UnsupportedOperationException();
  }
  /** Not supported. */
  public final boolean retainAll(Collection<?> elementsToKeep) {
    throw new UnsupportedOperationException();
  }
  /** Not supported. */
  public final void clear() {
    throw new UnsupportedOperationException();
  }

  @Override public final int hashCode() {
    return hashCode;
  }

  private static final class EmptyImmutableSet extends ImmutableSet<Object> {
    EmptyImmutableSet() {
      super(0);
    }

    public int size() {
      return 0;
    }

    public boolean isEmpty() {
      return true;
    }

    @Override public boolean contains(Object target) {
      return false;
    }

    public Iterator<Object> iterator() {
      return Iterators.emptyIterator();
    }

    public Object[] toArray() {
      return ObjectArrays.EMPTY_ARRAY;
    }

    public <T> T[] toArray(T[] a) {
      if (a.length > 0) {
        a[0] = null;
      }
      return a;
    }

    @Override public boolean containsAll(Collection<?> targets) {
      return targets.isEmpty();
    }

    @Override public boolean equals(Object object) {
      return object == this
          || (object instanceof Set<?> && ((Set<?>) object).isEmpty());
    }

    @Override public String toString() {
      return "[]";
    }
  }

  private static final class SingletonImmutableSet<E> extends ImmutableSet<E> {
    final E element;

    SingletonImmutableSet(E element, int hashCode) {
      super(hashCode);
      this.element = element;
    }

    public int size() {
      return 1;
    }

    public boolean isEmpty() {
      return false;
    }

    @Override public boolean contains(Object target) {
      return element.equals(target);
    }

    public Iterator<E> iterator() {
      // TODO: may need to reuse this somewhere
      return new Iterator<E>() {
        boolean done;
        public boolean hasNext() {
          return !done;
        }
        public E next() {
          if (done) {
            throw new NoSuchElementException();
          }
          done = true;
          return element;
        }
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    public Object[] toArray() {
      return new Object[] { element };
    }

    @SuppressWarnings({"unchecked"})
    public <T> T[] toArray(T[] array) {
      if (array.length == 0) {
        array = ObjectArrays.newArray(array, 1);
      } else if (array.length > 1) {
        array[1] = null;
      }
      array[0] = (T) element;
      return array;
    }

    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof Set<?>) {
        Set<?> set = (Set<?>) object;
        return set.size() == 1 && contains(set.iterator().next());
      }
      return false;
    }

    @Override public String toString() {
      String elementToString = element.toString();
      return new StringBuilder(elementToString.length() + 2)
          .append('[')
          .append(elementToString)
          .append(']')
          .toString();
    }
  }

  private static <E> ImmutableSet<E> create(
      Iterable<? extends E> iterable, int count) {
    // count is always the (nonzero) number of elements in the iterable
    int tableSize = chooseTableSize(count);
    Object[] table = new Object[tableSize];
    int mask = tableSize - 1;

    List<E> elements = new ArrayList<E>(count);
    int hashCode = 0;

    for (E element : iterable) {
      int hash = element.hashCode();
      for (int i = smear(hash); true; i++) {
        int index = i & mask;
        Object value = table[index];
        if (value == null) {
          // Came to an empty bucket. Put the element here.
          table[index] = element;
          elements.add(element);
          hashCode += hash;
          break;
        } else if (value.equals(element)) {
          break; // Found a duplicate. Nothing to do.
        }
      }
    }

    // The iterable might have contained only duplicates of the same element.
    return (elements.size() == 1)
        ? new SingletonImmutableSet<E>(elements.get(0), hashCode)
        : new RegularImmutableSet<E>(elements.toArray(), table, mask, hashCode);
  }

  private static final class RegularImmutableSet<E> extends ImmutableSet<E> {
    final Object[] elements; // the elements (two or more) in the desired order
    final Object[] table; // the same elements in hashed positions (plus nulls)
    final int mask; // 'and' with an int to get a valid table index

    RegularImmutableSet(
        Object[] elements, Object[] table, int mask, int hashCode) {
      super(hashCode);
      this.elements = elements;
      this.table = table;
      this.mask = mask;
    }

    public int size() {
      return elements.length;
    }

    public boolean isEmpty() {
      return false;
    }

    /*
     * The cast is safe because the only way to create an instance is via the
     * create() method above, which only permits elements of type E.
     */
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
      return (Iterator<E>) Iterators.forArray(elements);
    }

    public Object[] toArray() {
      Object[] array = new Object[size()];
      System.arraycopy(elements, 0, array, 0, size());
      return array;
    }

    public <T> T[] toArray(T[] array) {
      int size = size();
      if (array.length < size) {
        array = ObjectArrays.newArray(array, size);
      } else if (array.length > size) {
        array[size] = null;
      }
      System.arraycopy(elements, 0, array, 0, size);
      return array;
    }

    @Override public boolean contains(Object target) {
      if (target == null) {
        return false;
      }
      for (int i = smear(target.hashCode()); true; i++) {
        Object candidate = table[i & mask];
        if (candidate == null) {
          return false;
        }
        if (candidate.equals(target)) {
          return true;
        }
      }
    }

    @Override public boolean containsAll(Collection<?> targets) {
      if (targets == this) {
        return true;
      }
      if (!(targets instanceof RegularImmutableSet<?>)) {
        return super.containsAll(targets);
      }
      if (targets.size() > size()) {
        return false;
      }
      for (Object target : ((RegularImmutableSet<?>) targets).elements) {
        if (!contains(target)) {
          return false;
        }
      }
      return true;
    }

    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ImmutableSet<?> && hashCode() != object.hashCode())
      {
        return false;
      }
      if (object instanceof Set<?>) {
        Set<?> that = (Set<?>) object;
        return size() == that.size() && containsAll(that);
      }
      return false;
    }

    @Override public String toString() {
      StringBuilder result = new StringBuilder(size() * 16);
      result.append('[').append(elements[0].toString());
      for (int i = 1; i < size(); i++) {
        result.append(", ").append(elements[i].toString());
      }
      return result.append(']').toString();
    }
  }

  // We use power-of-2 tables, and this is the highest int that's a power of 2
  private static final int MAX_TABLE_SIZE = 1 << 30;

  // If the set has this many elements, it will "max out" the table size
  private static final int CUTOFF = 1 << 29;

  // Size the table to be at most 50% full, if possible
  /*@VisibleForTesting*/ static int chooseTableSize(int setSize) {
    if (setSize < CUTOFF) {
      return Integer.highestOneBit(setSize) << 2;
    }
    
    // The table can't be completely full or we'll get infinite reprobes
    checkArgument(setSize < MAX_TABLE_SIZE, "set too large");
    return MAX_TABLE_SIZE;
  }

  /*
   * Doug Lea's defensive hash code transform (this also appears in
   * java/util/HashMap.java.) TODO: make this available at least as a
   * protected method inside common.collect; possibly public.
   */
  private static int smear(int hashCode) {
    hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
    return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
  }

  /*
   * This class is used to serialize all ImmutableSet instances, regardless of
   * implementation type. It captures their "logical contents" and they are
   * reconstructed using public static factories. This is necessary to ensure
   * that the existence of a particular implementation type is an implementation
   * detail.
   */
  private static class SerializedForm implements Serializable {
    final Object[] elements;
    SerializedForm(Object[] elements) {
      this.elements = elements;
    }
    Object readResolve() {
      return of(elements);
    }
    private static final long serialVersionUID = 0;
  }

  private Object writeReplace() {
    return new SerializedForm(toArray());
  }
}
