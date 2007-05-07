// Copyright 2007 Google Inc. All Rights Reserved.

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
 * Factory and utilities for {@link Constraint}s.
 *
 * <p>{@link Constraint} and {@code Collection} instances returned by this class
 * are serializable.
 *
 * @see MapConstraints
 * @author mbostock@google.com (Mike Bostock)
 */
public final class Constraints {
  private Constraints() {}

  /**
   * A constraint that verifies that the element is not {@code null}. If the
   * element is {@code null}, a {@link NullPointerException} is thrown.
   */
  public static final Constraint<Object> NOT_NULL = new NotNullConstraint();

  /** @see #NOT_NULL */
  static class NotNullConstraint implements Constraint<Object>, Serializable {
    private static final long serialVersionUID = 8771569713494573120L;
    public void checkElement(Object element) {
      checkNotNull(element);
    }
    private Object readResolve() {
      return NOT_NULL; // preserve singleton property
    }
  }

  /**
   * Returns a constraint that verfies that the element is an instance of {@code
   * type}. A {@link ClassCastException} is thrown otherwise.
   *
   * @param type the required type for elements
   * @return a constraint which verifies the type of elements
   */
  static Constraint<Object> classConstraint(Class<?> type) {
    return new ClassConstraint(type);
  }

  /** @see #classConstraint */
  static class ClassConstraint implements Constraint<Object>, Serializable {
    private static final long serialVersionUID = -4064640599187669705L;
    private final Class<?> type;

    public ClassConstraint(Class<?> type) {
      checkNotNull(type);
      this.type = type;
    }

    public void checkElement(Object element) {
      if (!type.isInstance(element)) {
        throw new ClassCastException("Attempt to insert "
            + element.getClass() + " element into collection with element "
            + "type " + type);
      }
    }
  }

  /**
   * Returns a constrained view of the specified collection, using the specified
   * constraint. Any operations that would add new elements to the collection
   * will be verified by the constraint.
   *
   * @param collection the collection for which to return a constrained view
   * @param constraint the constraint for elements in the collection
   * @return a constrained view of the specified collection
   */
  public static <E> Collection<E> constrainedCollection(
      Collection<E> collection, Constraint<? super E> constraint) {
    return new ConstrainedCollection<E>(collection, constraint);
  }

  /** @see #constrainedCollection */
  static class ConstrainedCollection<E> extends ForwardingCollection<E> {
    private static final long serialVersionUID = 8917285124050266452L;
    private final Constraint<? super E> constraint;

    public ConstrainedCollection(Collection<E> delegate,
        Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
    }

    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
  }

  /**
   * Returns a constrained view of the specified set, using the specified
   * constraint. Any operations that would add new elements to the set will be
   * verified by the constraint.
   *
   * @param set the set for which to return a constrained view
   * @param constraint the constraint for elements in the set
   * @return a constrained view of the specified set
   */
  public static <E> Set<E> constrainedSet(Set<E> set,
      Constraint<? super E> constraint) {
    return new ConstrainedSet<E>(set, constraint);
  }

  /** @see #constrainedSet */
  static class ConstrainedSet<E> extends ForwardingSet<E> {
    private static final long serialVersionUID = -830337517974610109L;
    private final Constraint<? super E> constraint;

    public ConstrainedSet(Set<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
    }

    @Override public boolean add(E element) {
      constraint.checkElement(element);
      return super.add(element);
    }
    @Override public boolean addAll(Collection<? extends E> elements) {
      return super.addAll(checkElements(elements, constraint));
    }
  }

  /**
   * Returns a constrained view of the specified sorted set, using the specified
   * constraint. Any operations that would add new elements to the sorted set
   * will be verified by the constraint.
   *
   * @param sortedSet the sorted set for which to return a constrained view
   * @param constraint the constraint for elements in the sorted set
   * @return a constrained view of the specified sorted set
   */
  public static <E> SortedSet<E> constrainedSortedSet(SortedSet<E> sortedSet,
      Constraint<? super E> constraint) {
    return new ConstrainedSortedSet<E>(sortedSet, constraint);
  }

  /** @see #constrainedSortedSet */
  static class ConstrainedSortedSet<E> extends ForwardingSortedSet<E> {
    private static final long serialVersionUID = -286522409869875345L;
    private final Constraint<? super E> constraint;

    public ConstrainedSortedSet(SortedSet<E> delegate,
        Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
    }

    @Override public SortedSet<E> headSet(E toElement) {
      return constrainedSortedSet(super.headSet(toElement), constraint);
    }
    @Override public SortedSet<E> subSet(E fromElement, E toElement) {
      return constrainedSortedSet(super.subSet(fromElement, toElement),
          constraint);
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
  }

  /**
   * Returns a constrained view of the specified list, using the specified
   * constraint. Any operations that would add new elements to the list will be
   * verified by the constraint.
   *
   * <p>If {@code list} implements {@link RandomAccess}, so will the returned
   * list.
   *
   * @param list the list for which to return a constrained view
   * @param constraint the constraint for elements in the list
   * @return a constrained view of the list
   */
  public static <E> List<E> constrainedList(List<E> list,
      Constraint<? super E> constraint) {
    return (list instanceof RandomAccess)
        ? new ConstrainedRandomAccessList<E>(list, constraint)
        : new ConstrainedList<E>(list, constraint);
  }

  /** @see #constrainedList */
  static class ConstrainedList<E> extends ForwardingList<E> {
    private static final long serialVersionUID = 771378862182031456L;
    private final Constraint<? super E> constraint;

    public ConstrainedList(List<E> delegate, Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
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
    @Override public boolean addAll(int index,
        Collection<? extends E> elements) {
      return super.addAll(index, checkElements(elements, constraint));
    }
    @Override public ListIterator<E> listIterator() {
      return constrainedListIterator(super.listIterator(), constraint);
    }
    @Override public ListIterator<E> listIterator(int index) {
      return constrainedListIterator(super.listIterator(index),
          constraint);
    }
    @Override public E set(int index, E element) {
      constraint.checkElement(element);
      return super.set(index, element);
    }
    @Override public List<E> subList(int fromIndex, int toIndex) {
      return constrainedList(super.subList(fromIndex, toIndex),
          constraint);
    }
  }

  /** @see #constrainedList */
  static class ConstrainedRandomAccessList<E> extends ConstrainedList<E>
      implements RandomAccess {
    private static final long serialVersionUID = 2847441657918308440L;

    public ConstrainedRandomAccessList(List<E> delegate,
        Constraint<? super E> constraint) {
      super(delegate, constraint);
    }
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
  static <E> ListIterator<E> constrainedListIterator(
      ListIterator<E> listIterator, Constraint<? super E> constraint) {
    return new ConstrainedListIterator<E>(listIterator, constraint);
  }

  /** @see #constrainedListIterator */
  static class ConstrainedListIterator<E> // not Serializable
      extends ForwardingListIterator<E> {
    private final Constraint<? super E> constraint;

    public ConstrainedListIterator(ListIterator<E> delegate,
        Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
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
    if (collection instanceof SortedSet) {
      return constrainedSortedSet((SortedSet<E>) collection, constraint);
    } else if (collection instanceof Set) {
      return constrainedSet((Set<E>) collection, constraint);
    } else if (collection instanceof List) {
      return constrainedList((List<E>) collection, constraint);
    } else {
      return constrainedCollection(collection, constraint);
    }
  }

  /**
   * Returns a constrained view of the specified multiset, using the specified
   * constraint. Any operations that would add new elements to the underlying
   * multiset will be verified by the constraint.
   *
   * @param multiset the multiset for which to return a constrained view
   * @param constraint the constraint for elements in the multiset
   * @return a constrained view of the specified multiset
   */
  public static <E> Multiset<E> constrainedMultiset(Multiset<E> multiset,
      Constraint<? super E> constraint) {
    return new ConstrainedMultiset<E>(multiset, constraint);
  }

  /** @see #constrainedMultiset */
  static class ConstrainedMultiset<E> extends ForwardingMultiset<E> {
    private static final long serialVersionUID = -7523018223761091862L;
    private final Constraint<? super E> constraint;

    public ConstrainedMultiset(Multiset<E> delegate,
        Constraint<? super E> constraint) {
      super(delegate);
      checkNotNull(constraint);
      this.constraint = constraint;
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

  private static <E> Collection<E> checkElements(Collection<E> elements,
      Constraint<? super E> constraint) {
    Collection<E> copy = Lists.newArrayList(elements);
    for (E element : copy) {
      constraint.checkElement(element);
    }
    return copy;
  }
}
