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

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Set;
import java.util.SortedSet;

/**
 * Factories and utilities pertaining to the {@link Constraint} interface.
 *
 * <p>Constraints and collections returned by this class are serializable.
 *
 * @see MapConstraints
 * @author Mike Bostock
 */
public final class Constraints {
  private Constraints() {}

  /**
   * A constraint that verifies that the element is not null. If the element is
   * null, a {@link NullPointerException} is thrown.
   */
  public static final Constraint<Object> NOT_NULL = new NotNullConstraint();

  /** @see Constraints#NOT_NULL */
  static class NotNullConstraint implements Constraint<Object>, Serializable {
    public void checkElement(Object element) {
      checkNotNull(element);
    }
    private Object readResolve() {
      return NOT_NULL; // preserve singleton property
    }
    @Override public String toString() {
      return "Not null";
    }
    private static final long serialVersionUID = 8771569713494573120L;
  }

  /**
   * Returns a constraint that verifies that the element is an instance of {@code
   * type}. A {@link ClassCastException} is thrown otherwise.
   *
   * @param type the required type for elements
   * @return a constraint which verifies the type of elements
   */
  static Constraint<Object> classConstraint(Class<?> type) {
    return new ClassConstraint(type);
  }

  /** @see Constraints#classConstraint */
  static class ClassConstraint implements Constraint<Object>, Serializable {
    private final Class<?> type;

    public ClassConstraint(Class<?> type) {
      this.type = checkNotNull(type);
    }
    public void checkElement(Object element) {
      if (!type.isInstance(element)) {
        throw new ClassCastException("Attempt to insert "
            + element.getClass() + " element into collection with element "
            + "type " + type);
      }
    }
    @Override public String toString() {
      return "Instance of " + type;
    }
    private static final long serialVersionUID = -4064640599187669705L;
  }

  /**
   * Returns a constrained view of the specified collection, using the specified
   * constraint. Any operations that add new elements to the collection will
   * call the provided constraint. However, this method does not verify that
   * existing elements satisfy the constraint. 
   *
   * @param collection the collection to constrain
   * @param constraint the constraint that validates added elements
   * @return a constrained view of the collection
   */
  public static <E> Collection<E> constrainedCollection(
      Collection<E> collection, Constraint<? super E> constraint) {
    return new ConstrainedCollection<E>(collection, constraint);
  }

  /** @see Constraints#constrainedCollection */
  static class ConstrainedCollection<E> extends ForwardingCollection<E> {
    private final Constraint<? super E> constraint;

    public ConstrainedCollection(
        Collection<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }
    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
    private static final long serialVersionUID = 8917285124050266452L;
  }

  /**
   * Returns a constrained view of the specified set, using the specified
   * constraint. Any operations that add new elements to the set will call the
   * provided constraint. However, this method does not verify that existing
   * elements satisfy the constraint.
   * 
   * @param set the set to constrain
   * @param constraint the constraint that validates added elements
   * @return a constrained view of the set
   */
  public static <E> Set<E> constrainedSet(
      Set<E> set, Constraint<? super E> constraint) {
    return new ConstrainedSet<E>(set, constraint);
  }

  /** @see Constraints#constrainedSet */
  static class ConstrainedSet<E> extends ForwardingSet<E> {
    private final Constraint<? super E> constraint;

    public ConstrainedSet(Set<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }
    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
    private static final long serialVersionUID = -830337517974610109L;
  }

  /**
   * Returns a constrained view of the specified sorted set, using the specified
   * constraint. Any operations that add new elements to the sorted set will
   * call the provided constraint. However, this method does not verify that
   * existing elements satisfy the constraint. 
   *
   * @param sortedSet the sorted set to constrain
   * @param constraint the constraint that validates added elements
   * @return a constrained view of the sorted set
   */
  public static <E> SortedSet<E> constrainedSortedSet(
      SortedSet<E> sortedSet, Constraint<? super E> constraint) {
    return new ConstrainedSortedSet<E>(sortedSet, constraint);
  }

  /** @see Constraints#constrainedSortedSet */
  private static class ConstrainedSortedSet<E> extends ForwardingSortedSet<E> {
    final Constraint<? super E> constraint;

    ConstrainedSortedSet(
        SortedSet<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }

    @Override public SortedSet<E> headSet(E toElement) {
      return constrainedSortedSet(super.headSet(toElement), constraint);
    }
    @Override public SortedSet<E> subSet(E fromElement, E toElement) {
      return constrainedSortedSet(
          super.subSet(fromElement, toElement), constraint);
    }
    @Override public SortedSet<E> tailSet(E fromElement) {
      return constrainedSortedSet(super.tailSet(fromElement), constraint);
    }
    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
    private static final long serialVersionUID = -286522409869875345L;
  }

  /**
   * Returns a constrained view of the specified list, using the specified
   * constraint. Any operations that add new elements to the list will call the
   * provided constraint. However, this method does not verify that existing
   * elements satisfy the constraint.
   * 
   * <p>If {@code list} implements {@link RandomAccess}, so will the returned
   * list.
   * 
   * @param list the list to constrain
   * @param constraint the constraint that validates added elements
   * @return a constrained view of the list
   */
  public static <E> List<E> constrainedList(
      List<E> list, Constraint<? super E> constraint) {
    return (list instanceof RandomAccess)
        ? new ConstrainedRandomAccessList<E>(list, constraint)
        : new ConstrainedList<E>(list, constraint);
  }

  /** @see Constraints#constrainedList */
  private static class ConstrainedList<E> extends ForwardingList<E> {
    final Constraint<? super E> constraint;

    ConstrainedList(List<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }

    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public void add(int index, E element) {
      constraint.checkElement(element);
      super.add(index, element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
    @Override public boolean addAll(int index, Collection<? extends E> elements)
    {
      return super.addAll(index, checkElements(elements, constraint));
    }
    @Override public ListIterator<E> listIterator() {
      return constrainedListIterator(super.listIterator(), constraint);
    }
    @Override public ListIterator<E> listIterator(int index) {
      return constrainedListIterator(super.listIterator(index), constraint);
    }
    @Override public E set(int index, E element) {
      constraint.checkElement(element);
      return super.set(index, element);
    }
    @Override public List<E> subList(int fromIndex, int toIndex) {
      return constrainedList(super.subList(fromIndex, toIndex), constraint);
    }
    private static final long serialVersionUID = 771378862182031456L;
  }

  /** @see Constraints#constrainedList */
  static class ConstrainedRandomAccessList<E> extends ConstrainedList<E>
      implements RandomAccess {
    ConstrainedRandomAccessList(
        List<E> delegate, Constraint<? super E> constraint) {
      super(delegate, constraint);
    }
    private static final long serialVersionUID = 2847441657918308440L;
  }

  /**
   * Returns a constrained view of the specified list iterator, using the
   * specified constraint. Any operations that would add new elements to the
   * underlying list will be verified by the constraint.
   *
   * @param listIterator the iterator for which to return a constrained view
   * @param constraint the constraint for elements in the list
   * @return a constrained view of the specified iterator
   */
  private static <E> ListIterator<E> constrainedListIterator(
      ListIterator<E> listIterator, Constraint<? super E> constraint) {
    return new ConstrainedListIterator<E>(listIterator, constraint);
  }

  /** @see Constraints#constrainedListIterator */
  static class ConstrainedListIterator<E> // not Serializable
      extends ForwardingListIterator<E> {
    private final Constraint<? super E> constraint;

    public ConstrainedListIterator(
        ListIterator<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }

    @Override public void add(E element) {
      constraint.checkElement(element);
      super.add(element);
    }
    @Override public void set(E element) {
      constraint.checkElement(element);
      super.set(element);
    }
  }

  @SuppressWarnings("unchecked")
  static <E> Collection<E> constrainedTypePreservingCollection(
      Collection<E> collection, Constraint<E> constraint) {
    if (collection instanceof SortedSet<?>) {
      return constrainedSortedSet((SortedSet<E>) collection, constraint);
    } else if (collection instanceof Set<?>) {
      return constrainedSet((Set<E>) collection, constraint);
    } else if (collection instanceof List<?>) {
      return constrainedList((List<E>) collection, constraint);
    } else {
      return constrainedCollection(collection, constraint);
    }
  }

  /**
   * Returns a constrained view of the specified multiset, using the specified
   * constraint. Any operations that add new elements to the multiset will call
   * the provided constraint. However, this method does not verify that
   * existing elements satisfy the constraint. 
   *
   * @param multiset the multiset to constrain
   * @param constraint the constraint that validates added elements
   * @return a constrained view of the multiset
   */
  public static <E> Multiset<E> constrainedMultiset(
      Multiset<E> multiset, Constraint<? super E> constraint) {
    return new ConstrainedMultiset<E>(multiset, constraint);
  }

  /** @see Constraints#constrainedMultiset */
  static class ConstrainedMultiset<E> extends ForwardingMultiset<E> {
    private final Constraint<? super E> constraint;

    public ConstrainedMultiset(
        Multiset<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      this.constraint = checkNotNull(constraint);
    }

    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
    @Override public boolean add(E element, int occurrences) {
      constraint.checkElement(element);
      return super.add(element, occurrences);
    }
  }

  private static <E> Collection<E> checkElements(
      Collection<E> elements, Constraint<? super E> constraint) {
    Collection<E> copy = Lists.newArrayList(elements);
    for (E element : copy) {
      constraint.checkElement(element);
    }
    return copy;
  }

  private static final long serialVersionUID = -7523018223761091862L;
}
