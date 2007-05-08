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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of {@link Multiset} that supports deterministic iteration
 * order for elements. The iteration order is preserved across non-distinct
 * elements. For example,
 *
 * <pre>  Multiset&lt;String> m = ...
 *  m.add("a");
 *  m.add("b");
 *  m.add("a");</pre>
 *
 * In this case, the iteration order would be {@code [a, b, a]}. Unlike {@link
 * LinkedHashMultiset}, the iteration order is kept consistent across
 * non-distinct elements. For example, calling
 *
 * <pre>  map.remove("a");</pre>
 *
 * changes the iteration order to {@code [b, a]}.
 *
 * <p>This class provides {@link #first}, {@link #last} and {@link
 * #listIterator} methods on top of the standard {@code Multiset} interface.
 *
 * <p>Note that the performance of this implementation may differ from other
 * multiset implementations such as {@code HashMultiset}. Element addition takes
 * time and memory proportional to the number of elements being added, even if
 * those elements are not distinct. Element removal takes constant time if the
 * element to remove is not contained in the multiset, and otherwise takes time
 * proportional to the number of elements in the multiset.
 *
 * <p>This class makes no guarantees about thread-safety or concurrent
 * modifications.
 *
 * @author mbostock@google.com (Mike Bostock)
 * @param <E> the type of elements contained in this multiset
 */
public final class LinkedListMultiset<E> extends ForwardingMultiset<E>
    implements Cloneable {
  private static final long serialVersionUID = -1820525418990586868L;
  private static final Object NONE = new Object();

  /** Order is maintained using a linked list. */
  private static final class Node<E> implements Serializable {
    private static final long serialVersionUID = 7506018249774377845L;

    E element;
    Node<E> next;
    Node<E> previous;

    Node(@Nullable E element) {
      this.element = element;
    }

    public String toString() {
      return String.valueOf(element);
    }
  }

  private Node<E> head;
  private Node<E> tail;

  /* Lazily-initialized collection views. */
  private transient volatile Set<E> elementSet;
  private transient volatile Set<Entry<E>> entrySet;

  /** Constructs an empty {@code LinkedListMultiset}. */
  public LinkedListMultiset() {
    super(new HashMultiset<E>());
  }

  /**
   * Constructs a {@code LinkedListMultiset} with the same elements as the
   * specified {@code Collection}.
   */
  public LinkedListMultiset(Collection<? extends E> collection) {
    this();
    checkNotNull(collection);
    addAll(collection);
  }

  /**
   * Adds a new node for the specified element before the specified {@code next}
   * element, or at the end of the list if {@code next} is null.
   */
  private Node<E> addNode(@Nullable E element, @Nullable Node<E> next) {
    Node<E> node = new Node<E>(element);
    if (head == null) { // empty list
      head = tail = node;
    } else if (next == null) { // non-empty list, add to tail
      tail.next = node;
      node.previous = tail;
      tail = node;
    } else { // non-empty list, insert before next
      node.previous = next.previous;
      node.next = next;
      if (next.previous == null) { // next was head
        head = node;
      } else {
        next.previous.next = node;
      }
      next.previous = node;
    }
    super.add(element);
    return node;
  }

  /**
   * Removes the specified node from the linked list. This method is only
   * intended to be used from the {@code Iterator} classes. See also {@link
   * #removeAllNodes(Object)}.
   */
  private void removeNode(Node<E> node) {
    if (node.previous != null) {
      node.previous.next = node.next;
    } else { // node was head
      head = node.next;
    }
    if (node.next != null) {
      node.next.previous = node.previous;
    } else { // node was tail
      tail = node.previous;
    }
    super.remove(node.element);
  }

  /** Helper method for verifying that a node is present. */
  private static void checkNode(@Nullable Object node) {
    if (node == null) {
      throw new NoSuchElementException();
    }
  }

  /** An {@link Iterator} over all nodes. */
  private class NodeIterator implements Iterator<Node<E>> {
    private Node<E> next = head;
    private Node<E> current = null;

    public boolean hasNext() {
      return next != null;
    }

    public Node<E> next() {
      checkNode(next);
      current = next;
      next = next.next;
      return current;
    }

    public void remove() {
      checkState(current != null);
      removeNode(current);
      current = null;
    }
  }

  /** An {@link Iterator} over distinct elements in element head order. */
  private class DistinctElementIterator implements Iterator<E> {
    private final Set<E> seenElements
        = new HashSet<E>(Maps.capacity(elementSet().size()));
    private Node<E> next = head;
    private Node<E> current = null;

    public boolean hasNext() {
      return next != null;
    }

    public E next() {
      checkNode(next);
      current = next;
      seenElements.add(current.element);
      do { // skip ahead to next unseen element
        next = next.next;
      } while ((next != null) && !seenElements.add(next.element));
      return current.element;
    }

    public void remove() {
      checkState(current != null);
      removeAllOccurrences(current.element);
      current = null;
    }
  }

  @Override public boolean isEmpty() {
    return head == null;
  }

  @Override public void clear() {
    head = null;
    tail = null;
    super.clear();
  }

  @Override public boolean add(@Nullable E e) {
    addNode(e, null);
    return true;
  }

  @Override public boolean add(@Nullable E e, int n) {
    checkArgument(n >= 0);
    for (int i = 0; i < n; i++) {
      addNode(e, null);
    }
    return n > 0;
  }

  @Override public boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E e : c) {
      addNode(e, null);
      modified = true;
    }
    return modified; // not using c.isEmpty() for atomicity
  }

  @Override public boolean remove(@Nullable Object e) {
    if (!contains(e)) { // optimization
      return false;
    }
    for (Iterator<Node<E>> i = new NodeIterator(); i.hasNext();) {
      if (Objects.equal(i.next().element, e)) {
        i.remove();
        return true;
      }
    }
    return false;
  }

  @Override public int remove(@Nullable Object e, int n) {
    checkArgument(n >= 0);
    if ((n == 0) || !contains(e)) { // optimization
      return 0;
    }
    int m = 0;
    for (Iterator<Node<E>> i = new NodeIterator(); i.hasNext() && (n > m);) {
      if (Objects.equal(i.next().element, e)) {
        i.remove();
        m++;
      }
    }
    return m;
  }

  @Override public int removeAllOccurrences(@Nullable Object e) {
    if (!contains(e)) { // optimization
      return 0;
    }
    int n = 0;
    for (Iterator<Node<E>> i = new NodeIterator(); i.hasNext();) {
      if (Objects.equal(i.next().element, e)) {
        i.remove();
        n++;
      }
    }
    return n;
  }

  @Override public boolean removeAll(Collection<?> c) {
    return removeAllImpl(this, c);
  }

  @Override public boolean retainAll(Collection<?> c) {
    return retainAllImpl(this, c);
  }

  /** Returns an iterator over the elements in this multiset (in order). */
  @Override public Iterator<E> iterator() {
    return new ElementIterator(); // don't expose ListIterator
  }

  /** Returns a list iterator over the elements in this multiset (in order). */
  public ListIterator<E> listIterator() {
    return new ElementListIterator();
  }

  /** @see #iterator */
  private class ElementIterator implements Iterator<E> {
    protected int nextIndex;
    protected Node<E> next = head;
    protected Node<E> current;
    protected Node<E> previous;

    public boolean hasNext() {
      return next != null;
    }

    public E next() {
      checkNode(next);
      previous = current = next;
      next = next.next;
      nextIndex++;
      return current.element;
    }

    public void remove() {
      checkState(current != null);
      if (current != next) { // i.e., removing next element
        previous = current.previous;
        nextIndex--;
      } else {
        next = current.next;
      }
      removeNode(current);
      current = null;
    }
  }

  /** @see #listIterator */
  private class ElementListIterator extends ElementIterator
      implements ListIterator<E> {

    public boolean hasPrevious() {
      return previous != null;
    }

    public E previous() {
      checkNode(previous);
      next = current = previous;
      previous = previous.previous;
      nextIndex--;
      return current.element;
    }

    public int nextIndex() {
      return nextIndex;
    }

    public int previousIndex() {
      return nextIndex - 1;
    }

    public void set(E element) {
      checkState(current != null);
      LinkedListMultiset.super.remove(current.element);
      LinkedListMultiset.super.add(element);
      current.element = element;
    }

    public void add(E element) {
      previous = addNode(element, next);
      nextIndex++;
    }
  }

  /**
   * Returns the first element in this multiset.
   *
   * @throws NoSuchElementException if the multiset is empty
   */
  public E first() {
    if (head == null) {
      throw new NoSuchElementException();
    }
    return head.element;
  }

  /**
   * Returns the last element in this multiset.
   *
   * @throws NoSuchElementException if the multiset is empty
   */
  public E last() {
    if (tail == null) {
      throw new NoSuchElementException();
    }
    return tail.element;
  }

  @Override public Set<E> elementSet() {
    if (elementSet == null) {
      elementSet = new ElementSet();
    }
    return elementSet;
  }

  /** @see #elementSet */
  private class ElementSet extends AbstractSet<E> {
    public int size() {
      return LinkedListMultiset.super.elementSet().size();
    }
    public Iterator<E> iterator() {
      return new DistinctElementIterator();
    }

    /* optimizations */
    @Override public boolean contains(@Nullable Object e) {
      return LinkedListMultiset.this.contains(e);
    }
    @Override public boolean remove(@Nullable Object e) {
      return LinkedListMultiset.this.removeAllOccurrences(e) > 0;
    }
    @Override public boolean removeAll(Collection<?> c) {
      return LinkedListMultiset.this.removeAll(c);
    }
    @Override public boolean retainAll(Collection<?> c) {
      return LinkedListMultiset.this.retainAll(c);
    }
    @Override public void clear() {
      LinkedListMultiset.this.clear();
    }
  }

  @Override public Set<Entry<E>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  /** @see #entrySet */
  private class EntrySet extends AbstractSet<Entry<E>> {
    public int size() {
      return LinkedListMultiset.super.elementSet().size();
    }
    public Iterator<Entry<E>> iterator() {
      return new EntryIterator();
    }

    /* optimizations */
    @Override public boolean contains(@Nullable Object o) {
      if (!(o instanceof Entry<?>)) {
        return false;
      }
      Entry<?> e = (Entry<?>) o;
      int n = e.getCount();
      return (n > 0) && (count(e.getElement()) == n);
    }
    @Override public void clear() {
      LinkedListMultiset.this.clear();
    }
  }

  /** @see EntrySet#iterator */
  private class EntryIterator implements Iterator<Entry<E>> {
    final Iterator<E> elements = new DistinctElementIterator();
    public boolean hasNext() {
      return elements.hasNext();
    }
    public Entry<E> next() {
      final E element = elements.next();
      return new AbstractMultisetEntry<E>() {
          public E getElement() {
            return element;
          }
          public int getCount() {
            return count(element);
          }
        };
    }
    public void remove() {
      elements.remove();
    }
  }

  @Override public boolean equals(@Nullable Object o) {
    return (o == this) || super.equals(o);
  }

  /**
   * Returns a string representation of this multiset. The string representation
   * consists of a list of elements in the order returned by {@link #iterator},
   * enclosed in square brackets ("[]"). Adjacent elements are separated by the
   * characters ", " (comma and space). Adjacent equal elements are collapsed;
   * for example, "a, a, a" is represented as "a x 3".
   *
   * <p>For example:
   *
   * <pre>  Multiset&lt;String> multiset = new LinkedListMultiset&lt;String>();
   *  multiset.add("a", 3);
   *  multiset.add("b", 2);
   *  multiset.add("c");
   *  return multiset.toString();</pre>
   *
   * returns the string "[a x 3, b x 2, c]".
   */
  @Override public String toString() {
    StringBuilder builder = new StringBuilder().append('[');
    Object previous = NONE;
    int n = 1;
    for (E e : this) {
      if (Objects.equal(previous, e)) {
        n++;
      } else if (previous != NONE) {
        builder.append(previous);
        if (n > 1) {
          builder.append(" x ").append(n);
          n = 1;
        }
        builder.append(", ");
      }
      previous = e;
    }
    if (previous != NONE) {
      builder.append(previous);
      if (n > 1) {
        builder.append(" x ").append(n);
      }
    }
    return builder.append(']').toString();
  }

  @Override public Object[] toArray() {
    return ObjectArrays.toArrayImpl(this);
  }

  @Override public <T> T[] toArray(T[] a) {
    return ObjectArrays.toArrayImpl(this, a);
  }

  @Override public LinkedListMultiset<E> clone() {
    return new LinkedListMultiset<E>(this); // okay because we're final
  }
}
