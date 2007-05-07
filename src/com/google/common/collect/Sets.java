// Copyright 2006 Google Inc. All rights reserved.

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides static methods for creating mutable {@link Set} instances easily
 * and other static methods for working with Sets.
 *
 * <p>You can replace code like:
 *
 * <p>{@code Set<String> set = new HashSet<String>();}
 * <br>{@code Collections.addAll(set, "foo", "bar", "baz");}
 *
 * <p>with just:
 *
 * <p>{@code Set<String> set = newHashSet("foo", "bar", "baz");}
 *
 * <p>You can also create an empty {@code Set}, or populate your new
 * {@code Set} using any array, {@link Iterator} or {@link Iterable}.
 *
 * <p>Supported today are: {@link HashSet}, {@link LinkedHashSet}, and
 * {@link TreeSet}.  Intentionally unsupported is {@link EnumSet}, which was
 * blessed with an excellent set of static creation methods already.
 *
 * <p>See also this class's counterparts {@link Lists} and {@link Maps}.
 *
 * <p>WARNING: These factories do not support the full variety of tuning
 * parameters available in the collection constructors.  Use them only for
 * collections which will always remain small, or for which the cost of future
 * growth operations is not a concern.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 */
public final class Sets {
  private static final SortedSet<?> EMPTY_SORTED_SET
      = new EmptySortedSet<Object>();

  private Sets() {}

  /**
   * Returns an immutable SortedSet instance containing the given elements
   * sorted by their natural ordering.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code SortedSet<Base> set = Sets.immutableSortedSet(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code SortedSet<Base> set = Sets.<Base>immutableSortedSet(sub1, sub2);}
   *
   * @param elements the elements that the set should contain
   * @return an immutable {@code SortedSet} instance containing those elements,
   *     minus duplicates
   */
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<E>> SortedSet<E> immutableSortedSet(
      E... elements) {
    checkNotNull(elements);
    switch (elements.length) {
      case 0:
        return (SortedSet<E>) EMPTY_SORTED_SET;
      default:
        return Collections.unmodifiableSortedSet(newSortedArraySet(elements));
    }
  }

  /**
   * Returns an immutable SortedSet instance containing the given elements
   * sorted by the given {@code Comparator}.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code SortedSet<Base> set = Sets.immutableSortedSet(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code SortedSet<Base> set = Sets.<Base>immutableSortedSet(sub1, sub2);}
   *
   * @param comparator the comparator to sort the elements by
   * @param elements the elements that the set should contain
   * @return an immutable {@code SortedSet} instance containing those elements,
   *     minus duplicates
   */
  public static <E> SortedSet<E> immutableSortedSet(
      @Nullable Comparator<? super E> comparator, E... elements) {
    checkNotNull(elements);
    switch (elements.length) {
      case 0:
        return immutableSortedSet(comparator);
      default:
        return Collections.unmodifiableSortedSet(
            newSortedArraySet(comparator, elements));
    }
  }

  /**
   * Returns an empty sorted set. This is an optimization of
   * {@link #immutableSortedSet(E...)} for zero arguments.
   *
   * @return an immutable empty sorted set
   */
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<E>> SortedSet<E> immutableSortedSet() {
    return (SortedSet<E>) EMPTY_SORTED_SET;
  }

  /**
   * Returns an empty sorted set having the given {@code Comparator}. This is
   * an optimization of {@link #immutableSortedSet(Comparator, E...)} for
   * zero arguments.
   *
   * @param comparator the comparator for the empty set
   * @return an immutable empty sorted set
   */
  public static <E> SortedSet<E> immutableSortedSet(
      @Nullable Comparator<? super E> comparator) {
    return Collections.unmodifiableSortedSet(
        Sets.<E>newSortedArraySet(comparator));
  }

  /**
   * Returns a sorted set having only the given element. This is an
   * optimization of {@link #immutableSortedSet(E...)} for one argument.
   *
   * @param element the lone element to be in the returned set
   * @return an immutable sorted set containing only the given element
   */
  @SuppressWarnings("unchecked")
  public static <E extends Comparable<E>> SortedSet<E> immutableSortedSet(
      E element) {
    return Collections.unmodifiableSortedSet(newSortedArraySet(element));
  }

  /**
   * Returns a sorted set having only the given element and the given
   * {@code Comparator}. This is an optimization of
   * {@link #immutableSortedSet(Comparator, E...)} for one argument.
   *
   * @param comparator the comparator for the one-element set
   * @param element the lone element to be in the returned set
   * @return an immutable sorted set containing only the given element
   */
  @SuppressWarnings("unchecked")
  public static <E> SortedSet<E> immutableSortedSet(
      @Nullable Comparator<? super E> comparator, E element) {
    return Collections.unmodifiableSortedSet(
        newSortedArraySet(comparator, element));
  }

  /**
   * Returns an immutable Set instance containing the given elements.
   *
   * <p>Unlike an <i>unmodifiable</i> set such as that returned by {@code
   * Collections.unmodifiableSet()}, which provides a read-only view of an
   * underlying set which may itself be mutable, an <i>immutable</i> set makes
   * a copy of the original set or collection, so that changes to the original
   * are not reflected in the immutable set.
   *
   * <p>Immutability has two important advantages over unmodifiability.  First,
   * it allows the hash code to be computed once and cached, rather than
   * computed every time it is needed, which takes O(n) time for a set of n
   * elements.  Second, it prevents <i>any</i> inadvertent modification of the
   * value of the set.  This is critical, for example, if the set is an element
   * of a {@code HashSet} or a key in a {@code HashMap}.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code Set<Base> set = Sets.immutableSet(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code Set<Base> set = Sets.<Base>immutableSet(sub1, sub2);}
   *
   * <p><b>Note:</b> If {@code <E>} is an {@code enum} type, use
   * {@link #immutableEnumSet} instead.
   *
   * @param elements the elements that the set should contain
   * @return an immutable {@code Set} instance containing those elements,
   *     minus duplicates
   */
  public static <E> Set<E> immutableSet(E... elements) {
    checkNotNull(elements);
    switch (elements.length) {
      case 0:
        return Collections.emptySet();
      case 1:
        return Collections.singleton(elements[0]);
      default:
        return new ImmutableHashSet<E>(elements);
    }
  }

  /**
   * Optimization of {@link #immutableSet} for zero arguments.
   *
   * @return an immutable empty set
   * @see Collections#emptySet
   */
  public static <E> Set<E> immutableSet() {
    return Collections.emptySet();
  }

  /**
   * Optimization of {@link #immutableSet} for one argument.
   *
   * @param element the lone element to be in the returned set
   * @return an immutable set containing only the given element
   * @see Collections#singleton
   */
  public static <E> Set<E> immutableSet(@Nullable E element) {
    return Collections.singleton(element);
  }

  /**
   * Returns an immutable Set instance containing the elements in the provided
   * set. See {@link #immutableSet(E...)} for details.
   *
   * @param collection a collection containing the elements to be in the
   *     returned set
   * @return an immutable {@code Set} instance containing those elements
   *
   * TODO(adonovan) allow Collection instead of Set (see bug #668414).
   */
  public static <E> Set<E> immutableSet(Collection<E> collection) {
    checkNotNull(collection);
    switch (collection.size()) {
      case 0:
        return Collections.emptySet();
      case 1:
        return Collections.singleton(collection.iterator().next());
      default:
        return new ImmutableHashSet<E>(collection);
    }
  }

  /**
   * Returns an immutable {@code Set} instance containing the given elements of
   * an enumerated type.  Internally this set will be backed by an
   * {@link EnumSet}.
   *
   * @param anElement one of the elements the set should contain
   * @param otherElements the rest of the elements the set should contain
   * @return an immutable {@code Set} instance containing these elements,
   *     minus duplicates
   */
  public static <E extends Enum<E>> Set<E> immutableEnumSet(
      E anElement, E... otherElements) {
    checkNotNull(anElement);
    checkNotNull(otherElements);
    return Collections.unmodifiableSet(EnumSet.of(anElement, otherElements));
  }

  // HashSet

  /**
   * Creates an empty {@code HashSet} instance.
   *
   * <p><b>Notes:</b> if {@code E} is an {@link Enum} type, use {@link
   * EnumSet#noneOf} instead. If you only need an <i>immutable</i> empty Set,
   * use {@link Collections#emptySet} instead.
   *
   * @return a newly-created, initially-empty {@code HashSet}
   */
  public static <E> HashSet<E> newHashSet() {
    return new HashSet<E>();
  }

  /**
   * Creates a {@code HashSet} instance containing the given elements.
   *
   * <p><b>Note:</b> if {@code E} is an {@link Enum} type, use {@link
   * EnumSet#of} instead.
   *
   * <p><b>Note:</b> if it is an immutable Set you seek, you should use
   * {@link #immutableSet}.
   *
   * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
   * following:
   *
   * <p>{@code Set<Base> set = Sets.newHashSet(sub1, sub2);}
   *
   * <p>where {@code sub1} and {@code sub2} are references to subtypes of
   * {@code Base}, not of {@code Base} itself.  To get around this, you must
   * use:
   *
   * <p>{@code Set<Base> set = Sets.<Base>newHashSet(sub1, sub2);}
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code HashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> HashSet<E> newHashSet(E... elements) {
    checkNotNull(elements);
    int capacity = elements.length * 4 / 3 + 1;
    HashSet<E> set = new HashSet<E>(capacity);
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a {@code HashSet} instance containing the given elements.
   * <b>Note:</b> if {@code E} is an {@link Enum} type, and {@code elements}
   * is a {@link Collection}, use {@link EnumSet#copyOf} instead.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code HashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> HashSet<E> newHashSet(Iterable<? extends E> elements) {
    checkNotNull(elements);
    return (elements instanceof Collection<?>)
        ? new HashSet<E>((Collection<? extends E>) elements)
        : newHashSet(elements.iterator());
  }

  /**
   * Creates a {@code HashSet} instance containing the given elements.
   * <b>Note:</b> if {@code E} is an {@link Enum} type, you should manually
   * create an {@link EnumSet} instead.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code HashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> HashSet<E> newHashSet(Iterator<? extends E> elements) {
    checkNotNull(elements);
    HashSet<E> set = newHashSet();
    while (elements.hasNext()) {
      set.add(elements.next());
    }
    return set;
  }

  // ConcurrentHashSet

  /**
   * Creates a thread-safe {@code Set} backed by a hash table. The {@code Set}
   * is backed by a {@link ConcurrentHashMap} instance, and thus makes all of
   * the same guarentees as {@link ConcurrentHashMap} in the face of concurrent
   * operations.
   *
   * Unlike {@code HashSet}, this class does NOT allow <tt>null</tt>
   * to be used as an element.
   *
   * @return a newly-created, initially-empty thread-safe {@code Set}
   */
  public static <E> Set<E> newConcurrentHashSet() {
    return newSetFromMap(new ConcurrentHashMap<E, Boolean>());
  }

  /**
   * Creates a thread-safe {@link Set} backed by a hash table, and
   * containing the given elements. The concurrency properties of this set are
   * detailed in {@link ConcurrentHashMap}.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created thread-safe {@code Set} containing those elements
   *     (minus duplicates)
   */
  public static <E> Set<E> newConcurrentHashSet(E... elements) {
    checkNotNull(elements);
    int capacity = elements.length * 4 / 3 + 1;
    Set<E> set = newSetFromMap(new ConcurrentHashMap<E, Boolean>(capacity));
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a thread-safe {@link Set} backed by a hash table, and
   * containing the given elements. The concurrency properties of this set are
   * detailed in {@link ConcurrentHashMap}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created thread-safe {@code Set} containing those elements
   *     (minus duplicates)
   */
  public static <E> Set<E>
      newConcurrentHashSet(Iterable<? extends E> elements) {
    checkNotNull(elements);
    return newConcurrentHashSet(elements.iterator());
  }

  /**
   * Creates a thread-safe {@link Set} backed by a hash table, and
   * containing the given elements. The concurrency properties of this set are
   * detailed in {@link ConcurrentHashMap}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created thread-safe {@code Set} containing those elements
   *     (minus duplicates)
   */
  public static <E> Set<E>
      newConcurrentHashSet(Iterator<? extends E> elements) {
    checkNotNull(elements);
    Set<E> set = newConcurrentHashSet();
    while (elements.hasNext()) {
      set.add(elements.next());
    }
    return set;
  }

  // LinkedHashSet

  /**
   * Creates an empty {@code LinkedHashSet} instance.
   *
   * @return a newly-created, initially-empty {@code LinkedHashSet}
   */
  public static <E> LinkedHashSet<E> newLinkedHashSet() {
    return new LinkedHashSet<E>();
  }

  /**
   * Creates a {@code LinkedHashSet} instance containing the given elements.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param elements the elements that the set should contain, in order
   * @return a newly-created {@code LinkedHashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> LinkedHashSet<E> newLinkedHashSet(E... elements) {
    checkNotNull(elements);
    LinkedHashSet<E> set = new LinkedHashSet<E>(elements.length * 4 / 3 + 1);
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a {@code LinkedHashSet} instance containing the given elements.
   *
   * @param elements the elements that the set should contain, in order
   * @return a newly-created {@code LinkedHashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> LinkedHashSet<E> newLinkedHashSet(
      Iterable<? extends E> elements) {
    checkNotNull(elements);

    // Use LinkedHashSet's sizing logic
    return (elements instanceof Collection<?>)
           ? new LinkedHashSet<E>((Collection<? extends E>) elements)
           : newLinkedHashSet(elements.iterator());
  }

  /**
   * Creates a {@code LinkedHashSet} instance containing the given elements.
   *
   * @param elements the elements that the set should contain, in order
   * @return a newly-created {@code LinkedHashSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> LinkedHashSet<E> newLinkedHashSet(
      Iterator<? extends E> elements) {
    checkNotNull(elements);
    LinkedHashSet<E> set = newLinkedHashSet();
    while (elements.hasNext()) {
      set.add(elements.next());
    }
    return set;
  }

  // TreeSet, without Comparator

  /**
   * Creates a {@code TreeSet} instance using the default {@link Comparator}.
   * <b>Note:</b> If {@code E} is an {@link Enum} type, and you don't require
   * the set to implement {@link SortedSet} (only ordered iteration), use
   * {@link EnumSet#noneOf} instead.
   *
   * @return a newly-created, initially-empty {@code TreeSet}
   */
  public static <E extends Comparable<E>> TreeSet<E> newTreeSet() {
    return new TreeSet<E>();
  }

  /**
   * Creates a {@code TreeSet} instance using the default {@link Comparator}
   * and containing the given elements.  <b>Note:</b> If {@code E} is an
   * {@link Enum} type, and you don't require the set to implement {@link
   * SortedSet} (only ordered iteration), use {@link EnumSet#of} instead.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E extends Comparable<E>> TreeSet<E> newTreeSet(E... elements) {
    checkNotNull(elements);
    TreeSet<E> set = newTreeSet();
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a {@code TreeSet} instance using the default {@link Comparator}
   * and containing the given elements.
   *
   * <p><b>Note:</b> If {@code E} is an {@link Enum} type, and you don't
   * require the set to implement {@link SortedSet} (only ordered iteration),
   * use {@link EnumSet#copyOf} instead.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E extends Comparable<E>> TreeSet<E> newTreeSet(
      Iterable<? extends E> elements) {
    checkNotNull(elements);
    return newTreeSet(elements.iterator());
  }

  /**
   * Creates a {@code TreeSet} instance using the default {@link Comparator}
   * and containing the given elements.  <b>Note:</b> if {@code E} is an
   * {@link Enum} type, and you don't require the set to implement {@link
   * SortedSet} (only ordered iteration), you should manually create an
   * {@link EnumSet} instead.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E extends Comparable<E>> TreeSet<E> newTreeSet(
      Iterator<? extends E> elements) {
    checkNotNull(elements);
    TreeSet<E> set = newTreeSet();
    while (elements.hasNext()) {
      set.add(elements.next());
    }
    return set;
  }

  // TreeSet, with Comparator

  /**
   * Creates a {@code TreeSet} instance using the given {@code Comparator}.
   *
   * @param comparator the comparator to use to sort the set
   * @return a newly-created, initially-empty {@code TreeSet}
   */
  public static <E> TreeSet<E> newTreeSet(
      @Nullable Comparator<? super E> comparator) {
    return new TreeSet<E>(comparator);
  }

  /**
   * Creates a {@code TreeSet} instance using the given {@code Comparator}
   * and containing the given elements.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param comparator the comparator to use to sort the set
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> TreeSet<E> newTreeSet(
      @Nullable Comparator<? super E> comparator, E... elements) {
    checkNotNull(elements);
    TreeSet<E> set = newTreeSet(comparator);
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a {@code TreeSet} instance using the given {@code Comparator}
   * and containing the given elements.
   *
   * @param comparator the comparator to use to sort the set
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> TreeSet<E> newTreeSet(
      @Nullable Comparator<? super E> comparator,
      Iterable<? extends E> elements) {
    checkNotNull(elements);
    return newTreeSet(comparator, elements.iterator());
  }

  /**
   * Creates a {@code TreeSet} instance using the given {@code Comparator}
   * and containing the given elements.
   *
   * @param comparator the comparator to use to sort the set
   * @param elements the elements that the set should contain
   * @return a newly-created {@code TreeSet} containing those elements
   *     (minus duplicates)
   */
  public static <E> TreeSet<E> newTreeSet(
      @Nullable Comparator<? super E> comparator,
      Iterator<? extends E> elements) {
    checkNotNull(elements);
    TreeSet<E> set = newTreeSet(comparator);
    while (elements.hasNext()) {
      set.add(elements.next());
    }
    return set;
  }

  // SortedArraySet

  /**
   * Creates an empty {@code SortedArraySet} instance, with an initial capacity
   * of zero.
   *
   * <p>TODO(mbostock): change the initial capacity to the traditional default
   * of ten.
   *
   * @return a newly-created, initially-empty {@code SortedArraySet}
   */
  public static <E extends Comparable<E>> SortedArraySet<E>
      newSortedArraySet() {
    return new SortedArraySet<E>(0);
  }

  /**
   * Creates an empty {@code SortedArraySet} instance, with an initial capacity
   * of zero.
   *
   * <p>TODO(mbostock): change the initial capacity to the traditional default
   * of ten.
   *
   * @param comparator the comparator to use
   * @return a newly-created, initially-empty {@code SortedArraySet}
   */
  public static <E> SortedArraySet<E> newSortedArraySet(
      Comparator<? super E> comparator) {
    return new SortedArraySet<E>(comparator, 0);
  }

  /**
   * Creates a {@code SortedArraySet} instance containing the given elements.
   *
   * <p>TODO(mbostock): change this method to preserve the ordering of the
   * specified iterable if it is an instance of {@code SortedSet}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code SortedArraySet} containing those elements
   *     (minus duplicates)
   */
  public static <E extends Comparable<E>> SortedArraySet<E>
      newSortedArraySet(Iterable<? extends E> elements) {
    return newSortedArraySet(elements, Comparators.<E>naturalOrder());
  }

  /**
   * Creates a {@code SortedArraySet} instance containing the given elements.
   *
   * <p>TODO(mbostock): change the initial capacity to the traditional default
   * of ten, if the iterable is not a collection.
   *
   * @param elements the elements that the set should contain
   * @param comparator the comparator to use
   * @return a newly-created {@code SortedArraySet} containing those elements
   *     (minus duplicates)
   */
  @SuppressWarnings("unchecked")
  public static <E> SortedArraySet<E> newSortedArraySet(
      Iterable<? extends E> elements, Comparator<? super E> comparator) {
    SortedArraySet<E> set = new SortedArraySet<E>(
        comparator, (elements instanceof Collection<?>)
            ? ((Collection<?>) elements).size() : 0);
    for (E element : elements) {
      set.add(element);
    }
    return set;
  }

  /**
   * Creates a {@code SortedArraySet} instance containing the given elements.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param comparator the comparator to use
   * @param elements the elements that the set should contain
   * @return a newly-created {@code SortedArraySet} containing those elements
   *     (minus duplicates)
   */
  public static <E> SortedArraySet<E> newSortedArraySet(
      Comparator<? super E> comparator, E... elements) {
    SortedArraySet<E> set = new SortedArraySet<E>(comparator, elements.length);
    Collections.addAll(set, elements);
    return set;
  }

  /**
   * Creates a {@code SortedArraySet} instance containing the given elements.
   *
   * <p>Please see the caveat in {@link #newHashSet(Object...)}.
   *
   * @param elements the elements that the set should contain
   * @return a newly-created {@code SortedArraySet} containing those elements
   *     (minus duplicates)
   */
  public static <E extends Comparable<E>> SortedArraySet<E>
      newSortedArraySet(E... elements) {
    return newSortedArraySet(Comparators.<E>naturalOrder(), elements);
  }

  /**
   * Returns one of the given collections, which is smaller than, or the same
   * size as, each of the other given collections.  If multiple collections
   * tie for smallest, it is not guaranteed which will be returned.
   */
  public static <C extends Collection<?>> C smallest(
      C aCollection, C anotherCollection, C... moreCollections) {
    checkNotNull(aCollection);
    checkNotNull(anotherCollection);
    checkNotNull(moreCollections);
    return smallest(
        Lists.asList(aCollection, anotherCollection, moreCollections));
  }

  /**
   * Returns one of the given collections, which is smaller than, or the same
   * size as, each of the other given collections.  If multiple collections
   * tie for smallest, it is not guaranteed which will be returned.
   */
  public static <C extends Collection<?>> C smallest(Iterable<C> collections) {
    checkNotNull(collections);
    C smallest = null;
    int smallestSize = Integer.MAX_VALUE;
    for (C collection : collections) {
      checkNotNull(collection);
      int thisSize = collection.size();
      if (thisSize < smallestSize) {
        smallestSize = thisSize;
        smallest = collection;
      }
    }
    checkArgument(smallest != null, "no collections given");
    return smallest;
  }

  /**
   * Returns the number of elements the given sets have in common.
   *
   * @param aSet one of the sets to compute the intersection size for
   * @param anotherSet another one of the sets to compute the
   *     intersection size for
   * @param moreSets optionally, the rest of the sets to compute the
   *     intersection size for
   * @return the number of elements in the intersection of the given sets
   */
  public static int sizeOfIntersection(
      Set<?> aSet, Set<?> anotherSet, Set<?>... moreSets) {
    checkNotNull(aSet);
    checkNotNull(anotherSet);
    checkNotNull(moreSets);
    return sizeOfIntersection(Lists.asList(aSet, anotherSet, moreSets));
  }

  /**
   * Returns the number of elements the given sets have in common.
   *
   * @param sets the sets to compute the intersection size for
   * @return the number of elements in the intersection of the given sets
   */
  public static int sizeOfIntersection(Iterable<? extends Set<?>> sets) {
    checkNotNull(sets);
    Set<?> smallest = smallest(sets);
    int result = smallest.size();
    for (Object element : smallest) {
      for (Set<?> set : sets) {
        if (set != smallest && !set.contains(element)) {
          result--;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Creates an enum set with the given element type, initially containing all
   * the elements of this type that are <i>not</i> contained in the specified
   * set.  If the specified collection is an {@link EnumSet} instance, this
   * method behaves identically to {@link EnumSet#complementOf}.  Otherwise,
   * the specified collection must contain at least one element (in order to
   * determine the new enum set's element type).  If the collection could
   * possibly be empty, use {@link #complementOf(Collection,Class)} instead of
   * this method.
   *
   * @param collection the collection from whose complement to initialize the
   *     enum set
   * @return a new, modifiable enum set initially containing all the values of
   *     the enum not present in the given set
   * @throws IllegalArgumentException if {@code set} is not an {@code EnumSet}
   *     instance and contains no elements
   */
  public static <E extends Enum<E>> Set<E> complementOf(
      Collection<E> collection) {
    checkNotNull(collection);
    if (collection instanceof EnumSet<?>) {
      return EnumSet.complementOf((EnumSet<E>) collection);
    }
    checkArgument(!collection.isEmpty(),
        "collection is empty; use the other version of this method");
    Class<E> type = collection.iterator().next().getDeclaringClass();
    return makeComplementByHand(collection, type);
  }

  /**
   * Creates an enum set with the given element type, initially containing all
   * the elements of this type that are <i>not</i> contained in the specified
   * set.  This is equivalent to {@link EnumSet#complementOf}, but can act on
   * any type of set, so long as the elements are of enum type.
   *
   * @param collection the collection from whose complement to initialize this
   *     enum set
   * @param type the type of the elements in the set
   * @return a new, modifiable enum set initially containing all the values of
   *     the enum not present in the given set
   */
  public static <E extends Enum<E>> Set<E> complementOf(
      Collection<E> collection, Class<E> type) {
    checkNotNull(collection);
    return (collection instanceof EnumSet<?>)
        ? EnumSet.complementOf((EnumSet<E>) collection)
        : makeComplementByHand(collection, type);
  }

  private static <E extends Enum<E>> Set<E> makeComplementByHand(
      Collection<E> collection, Class<E> type) {
    Set<E> result = EnumSet.allOf(type);
    result.removeAll(collection);
    return result;
  }

  private static class ImmutableHashSet<E> extends HashSet<E> {
    static final long serialVersionUID = 1241522570505539952L;

    ImmutableHashSet(E... elements) {
      // Avoid collisions by using 2-4x as many buckets as expected entries.
      super(elements.length * 2);
      for (E element : elements) {
        super.add(element);
      }
    }

    ImmutableHashSet(Collection<E> elements) {
      // Avoid collisions by using 2-4x as many buckets as expected entries.
      super(elements.size() * 2);
      for (E element : elements) {
        super.add(element);
      }
    }

    @Override public Iterator<E> iterator() {
      return Iterators.unmodifiableIterator(super.iterator());
    }

    transient Integer cachedHashCode;

    @Override public int hashCode() {
      if (cachedHashCode == null) {
        cachedHashCode = super.hashCode();
      }
      return cachedHashCode;
    }

    @Override public boolean add(E o) {
      throw up();
    }
    @Override public boolean remove(Object o) {
      throw up();
    }
    @Override public void clear() {
      throw up();
    }
    @Override public boolean removeAll(Collection<?> c) {
      throw up();
    }
    @Override public boolean addAll(Collection<? extends E> c) {
      throw up();
    }
    @Override public boolean retainAll(Collection<?> c) {
      throw up();
    }

    private static UnsupportedOperationException up() {
      return new UnsupportedOperationException();
    }
  }

  private static final Iterator EMPTY_ITERATOR = new Iterator() {

        public boolean hasNext() {
          return false;
        }

        public Object next() {
          throw new NoSuchElementException();
        }

        public void remove() {
          throw new NoSuchElementException();
        }

    };

  /**
   * Returns a set backed by the specified map.  The resulting set displays
   * the same ordering, concurrency, and performance characteristics as the
   * backing map.  In essence, this factory method provides a {@link Set}
   * implementation corresponding to any {@link Map} implementation.  There
   * is no need to use this method on a {@link Map} implementation that
   * already has a corresponding {@link Set} implementation (such as {@link
   * HashMap} or {@link TreeMap}).
   *
   * <p><em>Note that this method was temporarily imported from Java 6's
   * Collections.newSetFromMap, and will be deprecated upon Java 6's
   * release.</em>
   *
   *
   * <p>Each method invocation on the set returned by this method results in
   * exactly one method invocation on the backing map or its <tt>keySet</tt>
   * view, with one exception.  The <tt>addAll</tt> method is implemented
   * as a sequence of <tt>put</tt> invocations on the backing map.
   *
   * <p>The specified map must be empty at the time this method is invoked,
   * and should not be accessed directly after this method returns.  These
   * conditions are ensured if the map is created empty, passed directly
   * to this method, and no reference to the map is retained, as illustrated
   * in the following code fragment:
   * <pre>
   *    Set&lt;Object&gt; weakHashSet = Collections.newSetFromMap(
   *        new WeakHashMap&lt;Object, Boolean&gt;());
   * </pre>
   *
   * @param map the backing map
   * @return the set backed by the map
   * @throws IllegalArgumentException if <tt>map</tt> is not empty
   */
  public static <E> Set<E> newSetFromMap(Map<E, Boolean> map) {
    return new SetFromMap<E>(map);
  }

  /**
   * Temporarily imported from Java 6's Collections.newSetFromMap.
   */
  private static class SetFromMap<E> extends AbstractSet<E>
    implements Set<E>, Serializable {
    private final Map<E, Boolean> m;  // The backing map
    private transient Set<E> s;       // Its keySet

    SetFromMap(Map<E, Boolean> map) {
      if (!map.isEmpty())
        throw new IllegalArgumentException("Map is non-empty");
      m = map;
      s = map.keySet();
    }

    public void clear()               {        m.clear(); }
    public int size()                 { return m.size(); }
    public boolean isEmpty()          { return m.isEmpty(); }
    public boolean contains(Object o) { return m.containsKey(o); }
    public boolean remove(Object o)   { return m.remove(o) != null; }
    public boolean add(E e) { return m.put(e, Boolean.TRUE) == null; }
    public Iterator<E> iterator()     { return s.iterator(); }
    public Object[] toArray()         { return s.toArray(); }
    public <T> T[] toArray(T[] a)     { return s.toArray(a); }
    public String toString()          { return s.toString(); }
    public int hashCode()             { return s.hashCode(); }
    public boolean equals(Object o)   { return o == this || s.equals(o); }
    public boolean containsAll(Collection<?> c) { return s.containsAll(c); }
    public boolean removeAll(Collection<?> c)   { return s.removeAll(c); }
    public boolean retainAll(Collection<?> c)   { return s.retainAll(c); }
    // addAll is the only inherited implementation

    private static final long serialVersionUID = 2454657854757543876L;

    private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      s = m.keySet();
    }
  }

  /** Returns the empty sorted set (immutable). This set is serializable. */
  @SuppressWarnings("unchecked")
  public static <E> SortedSet<E> emptySortedSet() {
    return (SortedSet<E>) EMPTY_SORTED_SET;
  }

  /** @see #emptySortedSet */
  private static class EmptySortedSet<E> extends AbstractSet<E>
      implements SortedSet<E>, Serializable {
    private static final long serialVersionUID = -1674208229134945065L;
    @Override public int size() {
      return 0;
    }
    @Override public Iterator<E> iterator() {
      return Iterators.emptyIterator();
    }
    public Comparator<? super E> comparator() {
      return null;
    }
    public SortedSet<E> subSet(E fromElement, E toElement) {
      throw new IllegalArgumentException();
    }
    public SortedSet<E> headSet(E toElement) {
      throw new IllegalArgumentException();
    }
    public SortedSet<E> tailSet(E fromElement) {
      throw new IllegalArgumentException();
    }
    public E first() {
      throw new NoSuchElementException();
    }
    public E last() {
      throw new NoSuchElementException();
    }
    private Object readResolve() {
      return EMPTY_SORTED_SET; // preserve singleton property
    }
  }
}
