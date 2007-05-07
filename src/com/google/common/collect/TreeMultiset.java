// Copyright 2005 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Multiset implementation backed by a TreeMap.
 *
 * @author nkanodia (Neal Kanodia)
 */
public final class TreeMultiset<E> extends AbstractMultiset<E>
    implements Cloneable {

  /**
   * Constructs a new, empty multiset, sorted according to the elements' natural
   * order.  All elements inserted into the multiset must implement the
   * <tt>Comparable</tt> interface.  Furthermore, all such elements must be
   * <i>mutually comparable</i>: <tt>e1.compareTo(e2)</tt> must not throw a
   * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
   * <tt>e2</tt> in the multiset.  If the user attempts to add an element to the
   * multiset that violates this constraint (for example, the user attempts to
   * add a string element to a set whose elements are integers), the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @see Comparable
   * @see TreeSet
   */
  public TreeMultiset() {
    super(new TreeMap<E, Frequency>());
  }

  /**
   * Constructs a new, empty multiset, sorted according to the specified
   * comparator.  All elements inserted into the multiset must be <i>mutually
   * comparable</i> by the specified comparator: <tt>comparator.compare(e1,
   * e2)</tt> must not throw a <tt>ClassCastException</tt> for any elements
   * <tt>e1</tt> and <tt>e2</tt> in the multiset.  If the user attempts to add
   * an element to the multiset that violates this constraint, the
   * <tt>add(Object)</tt> call will throw a <tt>ClassCastException</tt>.
   *
   * @param c the comparator that will be used to sort this multiset.  A
   *        <tt>null</tt> value indicates that the elements' <i>natural
   *        ordering</i> should be used.
   */
  public TreeMultiset(Comparator<? super E> c) {
    super(new TreeMap<E, Frequency>(c));
  }

  /**
   * Constructs an empty multiset containing the given initial elements.
   */
  public TreeMultiset(Collection<? extends E> initialElements) {
    this();
    addAll(initialElements); // careful if we ever make this class nonfinal
  }

  @SuppressWarnings("unchecked")
  @Override public TreeMultiset<E> clone() {
    try {
      return (TreeMultiset<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected Map<E, Frequency> cloneBackingMap() {
    return (Map<E, Frequency>) ((TreeMap<E, Frequency>) backingMap()).clone();
  }

  private static final long serialVersionUID = 980261132547708887L;
}
