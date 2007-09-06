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
 * @author Mike Bostock
 */
public final class LinkedListMultiset<E> extends ForwardingMultiset<E> {
  private Node<E> head;
  private Node<E> tail;

  private static class Node<E> implements Serializable {
    E element;
    Node<E> next;
    Node<E> previous;

    Node(@Nullable E element) {
      this.element = element;
    }
    public String toString() {
      return String.valueOf(element);
    }
    private static final long serialVersionUID = 7506018249774377845L;
  }

  /** Constructs a new empty {@code LinkedListMultiset}. */
  public LinkedListMultiset() {
    super(new HashMultiset<E>());
  }

  /**
   * Constructs a new {@code LinkedListMultiset} containing the specified
   * elements.
   */
  public LinkedListMultiset(Iterable<? extends E> elements) {
    this();
    Iterables.addAll(this, elements); // careful if we make this class non-final
  }

  // Query Operations

  @Override public boolean isEmpty() {
    return head == null;
  }

  @Override public Iterator<E> iterator() {
    return new ElementIterator();
  }

  private class ElementIterator implements Iterator<E> {
    int nextIndex;
    Node<E> next = head;
    Node<E> current;
    Node<E> previous;

    public boolean hasNext() {
      return next != null;
    }

    public E next() {
      previous = current = checkNode(next);
      next = next.next;
      nextIndex++;
      return current.element;
    }

    public void remove() {
      checkState(current != null);
      if (current != next) { // removing next element
        previous = current.previous;
        nextIndex--;
      } else {
        next = current.next;
      }
      removeNode(current);
      current = null;
    }
  }

  @Override public Object[] toArray() {
    return ObjectArrays.toArrayImpl(this);
  }

  @Override public <T> T[] toArray(T[] a) {
    return ObjectArrays.toArrayImpl(this, a);
  }

  /**
   * Returns the first element in this multiset.
   *
   * @throws NoSuchElementException if the multiset is empty
   */
  public E first() {
    return checkNode(head).element;
  }

  /**
   * Returns the last element in this multiset.
   *
   * @throws NoSuchElementException if the multiset is empty
   */
  public E last() {
    return checkNode(tail).element;
  }

  // Modification Operations

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

  /** An {@code Iterator} over all nodes. */
  private class NodeIterator implements Iterator<Node<E>> {
    Node<E> next = head;
    Node<E> current;

    public boolean hasNext() {
      return next != null;
    }
    public Node<E> next() {
      current = checkNode(next);
      next = next.next;
      return current;
    }
    public void remove() {
      checkState(current != null);
      removeNode(current);
      current = null;
    }
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
   * intended to be used from the {@code Iterator} classes.
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

  // Bulk Operations

  @Override public boolean addAll(Collection<? extends E> c) {
    // TODO: we should be able to use this from AbstractCollection
    boolean modified = false;
    for (E e : c) {
      modified |= add(e);
    }
    return modified; // not using c.isEmpty() for atomicity
  }

  @Override public boolean removeAll(Collection<?> c) {
    return removeAllImpl(this, c);
  }

  @Override public boolean retainAll(Collection<?> c) {
    return retainAllImpl(this, c);
  }

  @Override public void clear() {
    head = null;
    tail = null;
    super.clear();
  }

  // Views

  public ListIterator<E> listIterator() {
    return new ElementListIterator();
  }

  private class ElementListIterator extends ElementIterator
      implements ListIterator<E> {
    // Query Operations

    public boolean hasPrevious() {
      return previous != null;
    }

    public E previous() {
      next = current = checkNode(previous);
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

    // Modification Operations

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

  private transient volatile Set<E> elementSet;

  @Override public Set<E> elementSet() {
    if (elementSet == null) {
      elementSet = new ElementSet();
    }
    return elementSet;
  }

  /** @see LinkedListMultiset#elementSet */
  private class ElementSet extends AbstractSet<E> {
    public int size() {
      return LinkedListMultiset.super.elementSet().size();
    }
    public Iterator<E> iterator() {
      return new DistinctElementIterator();
    }

    // optimizations

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

  private transient volatile Set<Entry<E>> entrySet;

  @Override public Set<Entry<E>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }
    return entrySet;
  }

  private class EntrySet extends AbstractSet<Entry<E>> {
    public int size() {
      return LinkedListMultiset.super.elementSet().size();
    }
    public Iterator<Entry<E>> iterator() {
      return new EntryIterator();
    }

    class EntryIterator implements Iterator<Entry<E>> {
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

    // optimizations

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

  /** An {@code Iterator} over distinct elements in element head order. */
  private class DistinctElementIterator implements Iterator<E> {
    final Set<E> seenElements
        = new HashSet<E>(Maps.capacity(elementSet().size()));
    Node<E> next = head;
    Node<E> current;

    public boolean hasNext() {
      return next != null;
    }
    public E next() {
      current = checkNode(next);
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

  private static final Object NONE = new Object();

  /**
   * {@inheritDoc}
   *
   * <p>Returns a string representation of this multiset. The string
   * representation consists of a list of elements in the order returned by
   * {@link #iterator}, enclosed in square brackets ("[]"). Adjacent elements
   * are separated by the characters ", " (comma and space). Adjacent equal
   * elements are collapsed; for example, "a, a, a" is represented as "a x 3".
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

  private Node<E> checkNode(@Nullable Node<E> node) {
    if (node == null) {
      throw new NoSuchElementException();
    }
    return node;
  }

  private static final long serialVersionUID = -1820525418990586868L;
}
