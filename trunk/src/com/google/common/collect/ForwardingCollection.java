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
import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Collection;
import java.util.Iterator;

/**
 * A collection which forwards all its method calls to another collection.
 * Subclasses should override one or more methods to modify the behavior of
 * the backing collection as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * @see ForwardingObject
 * @author Kevin Bourrillion
 */
public abstract class ForwardingCollection<E> extends ForwardingObject
    implements Collection<E> {

  /**
   * Constructs a forwarding collection that forwards to the provided delegate.
   */
  protected ForwardingCollection(Collection<E> delegate) {
    super(delegate);
  }

  @SuppressWarnings("unchecked")
  @Override protected Collection<E> delegate() {
    return (Collection<E>) super.delegate();
  }

  public Iterator<E> iterator() {
    return delegate().iterator();
  }

  public int size() {
    return delegate().size();
  }

  public boolean removeAll(Collection<?> collection) {
    return delegate().removeAll(collection);
  }

  public boolean isEmpty() {
    return delegate().isEmpty();
  }

  public boolean contains(Object object) {
    return delegate().contains(object);
  }

  public Object[] toArray() {
    return delegate().toArray();
  }

  public <T>T[] toArray(T[] array) {
    return delegate().toArray(array);
  }

  public boolean add(E element) {
    return delegate().add(element);
  }

  public boolean remove(Object object) {
    return delegate().remove(object);
  }

  public boolean containsAll(Collection<?> collection) {
    return delegate().containsAll(collection);
  }

  public boolean addAll(Collection<? extends E> collection) {
    return delegate().addAll(collection);
  }

  public boolean retainAll(Collection<?> c) {
    return delegate().retainAll(c);
  }

  public void clear() {
    delegate().clear();
  }

  /* Standard implementations from AbstractCollection. */

  /**
   * Returns an array containing all of the elements in the specified
   * collection. This method returns the elements in the order they are returned
   * by the collection's iterator. The returned array is "safe" in that no
   * references to it are maintained by the collection. The caller is thus free
   * to modify the returned array.
   *
   * @param c the collection for which to return an array of elements
   * @see java.util.AbstractCollection#toArray()
   */
  protected static Object[] toArrayImpl(Collection<?> c) {
    return ObjectArrays.toArrayImpl(c);
  }

  /**
   * Returns an array containing all of the elements in the specified
   * collection; the runtime type of the returned array is that of the specified
   * array. If the collection fits in the specified array, it is returned
   * therein. Otherwise, a new array is allocated with the runtime type of the
   * specified array and the size of the specified collection.
   *
   * <p>If the collection fits in the specified array with room to spare (i.e.,
   * the array has more elements than the collection), the element in the array
   * immediately following the end of the collection is set to null. This is
   * useful in determining the length of the collection <i>only</i> if the
   * caller knows that the collection does not contain any null elements.
   *
   * <p>This method returns the elements in the order they are returned by the
   * collection's iterator.
   *
   * @param c the collection for which to return an array of elements
   * @param array the array into which elements of the collection are to be
   * stored, if it is big enough; otherwise a new array of the same runtime type
   * is allocated for this purpose
   * @throws ArrayStoreException if the runtime type of the specified array is
   * not a supertype of the runtime type of every element in the specified
   * collection
   * @see java.util.AbstractCollection#toArray(Object[])
   */
  protected static <T> T[] toArrayImpl(Collection<?> c, T[] array) {
    return ObjectArrays.toArrayImpl(c, array);
  }

  /**
   * Returns a string representation of the specified collection. The string
   * representation consists of a list of the collection's elements in the order
   * they are returned by its iterator, enclosed in square brackets ("[]").
   * Adjacent elements are separated by the characters ", " (comma and space).
   * Elements are converted to strings as by {@code String.valueOf(Object)}.
   *
   * @param c the collection for which to return a string representation
   * @see java.util.AbstractCollection#toString
   */
  protected static String toStringImpl(Collection<?> c) {
    return Iterators.toString(c.iterator());
  }

  /**
   * Returns true if the specified collection {@code c} contains the element
   * {@code o}.
   *
   * <p>This method iterates over the specified collection {@code c}, checking
   * each element returned by the iterator in turn to see if it equals the 
   * provided object {@code o}. If any element is equal, true is returned,
   * otherwise false is returned.
   *
   * @param c a collection which might contain the element.
   * @param o an element that might be contained by {@code c}
   */
  protected static boolean containsImpl(Collection<?> c, @Nullable Object o) {
    for (Object member : c) {
      if (Objects.equal(member, o)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns true if the specified collection {@code c} contains all of the
   * elements in the specified collection {@code d}.
   *
   * <p>This method iterates over the specified collection {@code d}, checking
   * each element returned by the iterator in turn to see if it is contained in
   * the specified collection {@code c}. If all elements are so contained, true
   * is returned, otherwise false.
   *
   * @param c a collection which might contain all elements in {@code d}
   * @param d a collection whose elements might be contained by {@code c}
   * @see java.util.AbstractCollection#containsAll(Collection)
   */
  protected static boolean containsAllImpl(Collection<?> c, Collection<?> d) {
    checkNotNull(c);
    for (Object o : d) {
      if (!c.contains(o)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Removes from the specified collection {@code c} the specified element 
   * {@code o} (optional operation).
   *
   * <p>This method iterates over the collection {@code c}, checking each
   * element returned by the iterator in turn to see if it equal to the provided
   * element {@code o}. If they are equal, it is removed from the collection
   * {@code c} with the iterator's {@code remove} method
   *
   * @param c the collection from which to remove
   * @param o an element to remove from the collectoin
   * @return true if collection {@code c} changed as a result
   * @throws UnsupportedOperationException if {@code c}'s iterator does not
   * support the {@code remove} method and {@code c} contains one or more
   * elements in common with {@code d}
   */
  protected static boolean removeImpl(Collection<?> c, @Nullable Object o) {
    Iterator<?> i = c.iterator();
    while (i.hasNext()) {
      if (Objects.equal(i.next(), o)) {
        i.remove();
        return true;
      }
    }
    return false;
  }

  /**
   * Removes from the specified collection {@code c} all of its elements that
   * are contained in the specified collection {@code d} (optional operation).
   *
   * <p>This method iterates over the collection {@code c}, checking each
   * element returned by the iterator in turn to see if it is contained in the
   * collection {@code d}. If it is so contained, it is removed from the
   * collection {@code c} with the iterator's {@code remove} method.
   *
   * @param c the collection from which to remove
   * @param d a collection of elements to remove from collection {@code c}
   * @return true if collection {@code c} changed as a result
   * @throws UnsupportedOperationException if {@code c}'s iterator does not
   * support the {@code remove} method and {@code c} contains one or more
   * elements in common with {@code d}
   * @see java.util.AbstractCollection#removeAll(Collection)
   */
  protected static boolean removeAllImpl(Collection<?> c, Collection<?> d) {
    checkNotNull(d);
    boolean modified = false;
    Iterator<?> i = c.iterator();
    while (i.hasNext()) {
      if (d.contains(i.next())) {
        i.remove();
        modified = true;
      }
    }
    return modified;
  }

  /**
   * Retains only the elements in the specified collection {@code c} that are
   * contained in the specified collection {@code d} (optional operation). In
   * other words, removes from the collection {@code c} all of its elements that
   * are not contained in the collection {@code d}.
   *
   * <p>This implementation iterates over the collection {@code c}, checking
   * each element returned by the iterator in turn to see if it is contained in
   * the collection {@code d}. If it is not so contained, it is removed from the
   * collection {@code c} with the iterator's {@code remove} method.
   *
   * @param c the collection from which to remove
   * @param d a collection of elements to retain in collection {@code c}
   * @return true if collection {@code c} changed as a result
   * @throws UnsupportedOperationException if {@code c}'s iterator does not
   * support the {@code remove} method and {@code c} contains one or more
   * elements not present in the {@code d}
   */
  protected static boolean retainAllImpl(Collection<?> c, Collection<?> d) {
    checkNotNull(d);
    boolean modified = false;
    Iterator<?> i = c.iterator();
    while (i.hasNext()) {
      if (!d.contains(i.next())) {
        i.remove();
        modified = true;
      }
    }
    return modified;
  }
}
