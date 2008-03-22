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
import static com.google.common.base.Preconditions.checkState;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of {@code ListMultimap} that supports deterministic
 * iteration order for both keys and values. The iteration order is preserved
 * across non-distinct key values. For example,
 *
 * <pre>  Multimap&lt;K,V> map = ...
 *  map.put(key1, foo);
 *  map.put(key2, bar);
 *  map.put(key1, baz);</pre>
 *
 * In this case, the iteration order for {@link #keys()} would be {@code [key1,
 * key2, key1]}, and likewise for {@link #entries()}. Unlike {@link
 * LinkedHashMultimap}, the iteration order is kept consistent between keys,
 * entries and values. For example, calling
 *
 * <pre>  map.remove(key1, foo);</pre>
 *
 * changes the entries iteration order to {@code [key2=baz, key1=baz]} and the
 * key iteration order to {@code [key2, key1]}. The {@link #entries()} iterator
 * returns mutable map entries, and {@link #replaceValues} attempts to preserve
 * iteration order as much as possible.
 *
 * <p>All optional multimap methods are supported, and all returned views are
 * modifiable.
 * 
 * <p>The methods {@link #get}, {@link #keySet}, {@link #keys}, {@link #values},
 * {@link #entries}, and {@link #asMap} return collections that are views of the
 * multimap. If the multimap is modified while an iteration over any of those
 * collections is in progress, except through the iterator's own {@code remove}
 * operation, the results of the iteration are undefined. 
 * 
 * @author Mike Bostock
 */
public final class LinkedListMultimap<K, V>
    implements ListMultimap<K, V>, Serializable {
  /*
   * Order is maintained using a linked list containing all key-value pairs. In
   * addition, a series of disjoint linked lists of "siblings", each containing
   * the values for a specific key, is used to implement {@link
   * ValueForKeyIterator} in constant time.
   */

  private static final class Node<K, V> implements Serializable {
    final K key;
    V value;
    Node<K, V> next; // the next node (with any key)
    Node<K, V> previous; // the previous node (with any key)
    Node<K, V> nextSibling; // the next node with the same key
    Node<K, V> previousSibling; // the previous node with the same key

    Node(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override public String toString() {
      return key + "=" + value;
    }

    private static final long serialVersionUID = 1620110227870214382L;
  }

  private Node<K, V> head; // the head for all keys
  private Node<K, V> tail; // the tail for all keys
  private Multiset<K> keyCount; // the number of values for each key
  private Map<K, Node<K, V>> keyToKeyHead; // the head for a given key
  private Map<K, Node<K, V>> keyToKeyTail; // the tail for a given key

  /** Constructs an empty {@code LinkedListMultimap}. */
  public LinkedListMultimap() {
    clear();
  }

  /**
   * Constructs a {@code LinkedListMultimap} with the same mappings as the
   * specified {@code Multimap}.
   */
  public LinkedListMultimap(Multimap<? extends K, ? extends V> multimap) {
    int keySize = multimap.keySet().size();
    keyCount = new HashMultiset<K>(keySize);
    keyToKeyHead = Maps.newHashMapWithExpectedSize(keySize);
    keyToKeyTail = Maps.newHashMapWithExpectedSize(keySize);
    putAll(multimap);
  }

  /**
   * Adds a new node for the specified key-value pair before the specified
   * {@code nextSibling} element, or at the end of the list if {@code
   * nextSibling} is null. Note: if {@code nextSibling} is specified, it MUST be
   * for an node for the same {@code key}!
   */
  private Node<K, V> addNode(
      @Nullable K key, @Nullable V value, @Nullable Node<K, V> nextSibling) {
    Node<K, V> node = new Node<K, V>(key, value);
    if (head == null) { // empty list
      head = tail = node;
      keyToKeyHead.put(key, node);
      keyToKeyTail.put(key, node);
    } else if (nextSibling == null) { // non-empty list, add to tail
      tail.next = node;
      node.previous = tail;
      Node<K, V> keyTail = keyToKeyTail.get(key);
      if (keyTail == null) { // first for this key
        keyToKeyHead.put(key, node);
      } else {
        keyTail.nextSibling = node;
        node.previousSibling = keyTail;
      }
      keyToKeyTail.put(key, node);
      tail = node;
    } else { // non-empty list, insert before nextSibling
      node.previous = nextSibling.previous;
      node.previousSibling = nextSibling.previousSibling;
      node.next = nextSibling;
      node.nextSibling = nextSibling;
      if (nextSibling.previousSibling == null) { // nextSibling was key head
        keyToKeyHead.put(key, node);
      } else {
        nextSibling.previousSibling.nextSibling = node;
      }
      if (nextSibling.previous == null) { // nextSibling was head
        head = node;
      } else {
        nextSibling.previous.next = node;
      }
      nextSibling.previous = node;
      nextSibling.previousSibling = node;
    }
    keyCount.add(key);
    return node;
  }

  /**
   * Removes the specified node from the linked list. This method is only
   * intended to be used from the {@code Iterator} classes. See also {@link
   * LinkedListMultimap#removeAllNodes(Object)}.
   */
  private void removeNode(Node<K, V> node) {
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
    if (node.previousSibling != null) {
      node.previousSibling.nextSibling = node.nextSibling;
    } else if (node.nextSibling != null) { // node was key head
      keyToKeyHead.put(node.key, node.nextSibling);
    } else {
      keyToKeyHead.remove(node.key); // don't leak a key-null entry
    }
    if (node.nextSibling != null) {
      node.nextSibling.previousSibling = node.previousSibling;
    } else if (node.previousSibling != null) { // node was key tail
      keyToKeyTail.put(node.key, node.previousSibling);
    } else {
      keyToKeyTail.remove(node.key); // don't leak a key-null entry
    }
    keyCount.remove(node.key);
  }

  /** Removes all nodes for the specified key. */
  private void removeAllNodes(@Nullable Object key) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      i.next();
      i.remove();
    }
  }

  /** Helper method for verifying that an iterator element is present. */
  private static void checkElement(@Nullable Object node) {
    if (node == null) {
      throw new NoSuchElementException();
    }
  }

  /** An {@code Iterator} over all nodes. */
  private class NodeIterator implements Iterator<Node<K, V>> {
    Node<K, V> next = head;
    Node<K, V> current;

    public boolean hasNext() {
      return next != null;
    }
    public Node<K, V> next() {
      checkElement(next);
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

  /** An {@code Iterator} over distinct keys in key head order. */
  private class DistinctKeyIterator implements Iterator<K> {
    final Set<K> seenKeys = new HashSet<K>(Maps.capacity(keySet().size()));
    Node<K, V> next = head;
    Node<K, V> current;

    public boolean hasNext() {
      return next != null;
    }
    public K next() {
      checkElement(next);
      current = next;
      seenKeys.add(current.key);
      do { // skip ahead to next unseen key
        next = next.next;
      } while ((next != null) && !seenKeys.add(next.key));
      return current.key;
    }
    public void remove() {
      checkState(current != null);
      removeAllNodes(current.key);
      current = null;
    }
  }

  /** A {@code ListIterator} over values for a specified key. */
  private class ValueForKeyIterator implements ListIterator<V> {
    final Object key;
    int nextIndex;
    Node<K, V> next;
    Node<K, V> current;
    Node<K, V> previous;

    /** Constructs a new iterator over all values for the specified key. */
    ValueForKeyIterator(@Nullable Object key) {
      this.key = key;
      next = keyToKeyHead.get(key);
    }

    /**
     * Constructs a new iterator over all values for the specified key starting
     * at the specified index. This constructor is optimized so that it starts
     * at either the head or the tail, depending on which is closer to the
     * specified index. This allows adds to the tail to be done in constant
     * time.
     *
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public ValueForKeyIterator(@Nullable Object key, int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException("index too small");
      }
      int size = keyCount.count(key);
      if (index > size) {
        throw new IndexOutOfBoundsException("index too large");
      }
      if (index >= (size / 2)) {
        previous = keyToKeyTail.get(key);
        nextIndex = size;
        while (index++ < size) {
          previous();
        }
      } else {
        next = keyToKeyHead.get(key);
        while (index-- > 0) {
          next();
        }
      }
      this.key = key;
      current = null;
    }

    public boolean hasNext() {
      return next != null;
    }

    public V next() {
      checkElement(next);
      previous = current = next;
      next = next.nextSibling;
      nextIndex++;
      return current.value;
    }

    public boolean hasPrevious() {
      return previous != null;
    }

    public V previous() {
      checkElement(previous);
      next = current = previous;
      previous = previous.previousSibling;
      nextIndex--;
      return current.value;
    }

    public int nextIndex() {
      return nextIndex;
    }

    public int previousIndex() {
      return nextIndex - 1;
    }

    public void remove() {
      checkState(current != null);
      if (current != next) { // removing next element
        previous = current.previousSibling;
        nextIndex--;
      } else {
        next = current.nextSibling;
      }
      removeNode(current);
      current = null;
    }

    public void set(V value) {
      checkState(current != null);
      current.value = value;
    }

    @SuppressWarnings("unchecked")
    public void add(V value) {
      previous = addNode((K) key, value, next);
      nextIndex++;
      current = null;
    }
  }

  // Query Operations
  
  public int size() {
    return keyCount.size();
  }

  public boolean isEmpty() {
    return head == null;
  }

  public boolean containsKey(@Nullable Object key) {
    return keyToKeyHead.containsKey(key);
  }

  public boolean containsValue(@Nullable Object value) {
    for (Iterator<Node<K, V>> i = new NodeIterator(); i.hasNext();) {
      if (Objects.equal(i.next().value, value)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    for (Iterator<V> i = new ValueForKeyIterator(key); i.hasNext();) {
      if (Objects.equal(i.next(), value)) {
        return true;
      }
    }
    return false;
  }

  // Modification Operations
  
  /**
   * Stores a key-value pair in the multimap.
   *
   * @param key key to store in the multimap
   * @param value value to store in the multimap
   * @return {@code true} always
   */
  public boolean put(@Nullable K key, @Nullable V value) {
    addNode(key, value, null);
    return true;
  }

  public boolean remove(@Nullable Object key, @Nullable Object value) {
    Iterator<V> values = new ValueForKeyIterator(key);
    while (values.hasNext()) {
      if (Objects.equal(values.next(), value)) {
        values.remove();
        return true;
      }
    }
    return false;
  }
  
  // Bulk Operations
  
  public void putAll(@Nullable K key, Iterable<? extends V> values) {
    for (V value : values) {
      addNode(key, value, null);
    }
  }

  public void putAll(Multimap<? extends K, ? extends V> multimap) {
    for (Map.Entry<? extends K, ? extends V> entry : multimap.entries()) {
      addNode(entry.getKey(), entry.getValue(), null);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>If any entries for the specified {@code key} already exist in the
   * multimap, their values are changed in-place without affecting the iteration
   * order.
   */
  public List<V> replaceValues(@Nullable K key, Iterable<? extends V> values) {
    List<V> oldValues = getCopy(key);
    ListIterator<V> keyValues = new ValueForKeyIterator(key);
    Iterator<? extends V> newValues = values.iterator();

    // Replace existing values, if any.
    while (keyValues.hasNext() && newValues.hasNext()) {
      keyValues.next();
      keyValues.set(newValues.next());
    }

    // Remove remaining old values, if any.
    while (keyValues.hasNext()) {
      keyValues.next();
      keyValues.remove();
    }

    // Add remaining new values, if any.
    while (newValues.hasNext()) {
      keyValues.add(newValues.next());
    }

    return oldValues;
  }

  private List<V> getCopy(@Nullable Object key) {
    return Lists.newLinkedList(new ValueForKeyIterator(key));
  }
  
  public List<V> removeAll(@Nullable Object key) {
    List<V> oldValues = getCopy(key);
    removeAllNodes(key);
    return oldValues;
  }

  public void clear() {
    head = null;
    tail = null;
    keyCount = Multisets.newHashMultiset();
    keyToKeyHead = Maps.newHashMap();
    keyToKeyTail = Maps.newHashMap();
  }

  // Views

  /**
   * {@inheritDoc}
   *
   * <p>If the multimap is modified while an iteration over the list is in
   * progress (except through the iterator's own {@code add}, {@code set} or
   * {@code remove} operations) the results of the iteration are undefined.
   */
  public List<V> get(final @Nullable K key) {
    return new AbstractSequentialList<V>() {
      @Override public int size() {
        return keyCount.count(key);
      }
      @Override public ListIterator<V> listIterator(int index) {
        return new ValueForKeyIterator(key, index);
      }
    };
  }
  
  private transient volatile Set<K> keySet;
  
  public Set<K> keySet() {
    if (keySet == null) {
      keySet = new AbstractSet<K>() {
        @Override public int size() {
          return keyCount.elementSet().size();
        }
        @Override public Iterator<K> iterator() {
          return new DistinctKeyIterator();
        }
        @Override public boolean contains(Object key) { // for performance
          return keyCount.contains(key);
        }
      };
    }
    return keySet;
  }

  private transient volatile Multiset<K> keys;
  
  public Multiset<K> keys() {
    if (keys == null) {
      keys = new MultisetView();
    }
    return keys;
  }

  /*
   * This would be an anonymous class except it needs to both extend {@code
   * AbstractCollection} and implement {@code Multiset}.
   */
  private class MultisetView extends AbstractCollection<K>
      implements Multiset<K> {

    @Override public int size() {
      return keyCount.size();
    }

    @Override public Iterator<K> iterator() {
      final Iterator<Node<K, V>> nodes = new NodeIterator();
      return new Iterator<K>() {
        public boolean hasNext() {
          return nodes.hasNext();
        }
        public K next() {
          return nodes.next().key;
        }
        public void remove() {
          nodes.remove();
        }
      };
    }

    public int count(@Nullable Object key) {
      return keyCount.count(key);
    }

    public boolean add(@Nullable K key, int occurrences) {
      throw new UnsupportedOperationException();
    }

    public int remove(@Nullable Object key, int occurrences) {
      Iterator<V> values = new ValueForKeyIterator(key);
      int removed = 0;
      while ((occurrences-- > 0) && values.hasNext()) {
        values.next();
        values.remove();
        removed++;
      }
      return removed;
    }

    public int removeAllOccurrences(@Nullable Object key) {
      return LinkedListMultimap.this.removeAll(key).size();
    }

    public Set<K> elementSet() {
      return keySet();
    }

    public Set<Entry<K>> entrySet() {
      return new AbstractSet<Entry<K>>() {
        @Override public int size() {
          return keyCount.elementSet().size();
        }

        @Override public Iterator<Entry<K>> iterator() {
          final Iterator<K> keys = new DistinctKeyIterator();
          return new Iterator<Entry<K>>() {
            public boolean hasNext() {
              return keys.hasNext();
            }
            public Entry<K> next() {
              final K key = keys.next();
              return new AbstractMultisetEntry<K>() {
                public K getElement() {
                  return key;
                }
                public int getCount() {
                  return keyCount.count(key);
                }
              };
            }
            public void remove() {
              keys.remove();
            }
          };
        }
      };
    }

    @Override public boolean equals(@Nullable Object o) {
      return keyCount.equals(o);
    }

    @Override public int hashCode() {
      return keyCount.hashCode();
    }

    @Override public String toString() {
      return keyCount.toString(); // XXX observe order?
    }
  }

  private transient volatile Collection<V> values;
  
  /**
   * {@inheritDoc}
   *
   * <p>The iterator generated by the returned collection traverses the values
   * in the order they were added to the multimap.
   */
  public Collection<V> values() {
    if (values == null) {
      values = new AbstractCollection<V>() {
        @Override public int size() {
          return keyCount.size();
        }
        @Override public Iterator<V> iterator() {
          final Iterator<Node<K, V>> nodes = new NodeIterator();
          return new Iterator<V>() {
            public boolean hasNext() {
              return nodes.hasNext();
            }
            public V next() {
              return nodes.next().value;
            }
            public void remove() {
              nodes.remove();
            }
          };
        }
      };
    }
    return values;
  }

  private transient volatile Collection<Map.Entry<K, V>> entries;
  
  /**
   * {@inheritDoc}
   *
   * <p>The iterator generated by the returned collection traverses the entries
   * in the order they were added to the multimap.
   */
  public Collection<Map.Entry<K, V>> entries() {
    if (entries == null) {
      entries = new AbstractCollection<Map.Entry<K, V>>() {
        @Override public int size() {
          return keyCount.size();
        }

        @Override public Iterator<Map.Entry<K, V>> iterator() {
          final Iterator<Node<K, V>> nodes = new NodeIterator();
          return new Iterator<Map.Entry<K, V>>() {
            public boolean hasNext() {
              return nodes.hasNext();
            }

            public Map.Entry<K, V> next() {
              final Node<K, V> node = nodes.next();
              return new AbstractMapEntry<K, V>() {
                @Override public K getKey() {
                  return node.key;
                }
                @Override public V getValue() {
                  return node.value;
                }
                @Override public V setValue(V value) {
                  V oldValue = node.value;
                  node.value = value;
                  return oldValue;
                }
              };
            }

            public void remove() {
              nodes.remove();
            }
          };
        }
      };
    }
    return entries;
  }

  private class AsMapEntries extends AbstractSet<Map.Entry<K, Collection<V>>> {
    @Override public int size() {
      return keyCount.elementSet().size();
    }

    @Override public Iterator<Map.Entry<K, Collection<V>>> iterator() {
      final Iterator<K> keys = new DistinctKeyIterator();
      return new Iterator<Map.Entry<K, Collection<V>>>() {
        public boolean hasNext() {
          return keys.hasNext();
        }

        public Map.Entry<K, Collection<V>> next() {
          final K key = keys.next();
          return new AbstractMapEntry<K, Collection<V>>() {
            @Override public K getKey() {
              return key;
            }

            @Override public Collection<V> getValue() {
              return LinkedListMultimap.this.get(key);
            }
          };
        }

        public void remove() {
          keys.remove();
        }
      };
    }
  }

  private transient volatile Map<K, Collection<V>> map;
  
  public Map<K, Collection<V>> asMap() {
    if (map == null) {
      map = new AbstractMap<K, Collection<V>>() {
        volatile Set<Map.Entry<K, Collection<V>>> entrySet;

        @Override public Set<Map.Entry<K, Collection<V>>> entrySet() {
          if (entrySet == null) {
            entrySet = new AsMapEntries();
          }
          return entrySet;
        }

        // The following methods are included for performance.

        @Override public boolean containsKey(@Nullable Object key) {
          return LinkedListMultimap.this.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        @Override public Collection<V> get(@Nullable Object key) {
          Collection<V> collection = LinkedListMultimap.this.get((K) key);
          return collection.isEmpty() ? null : collection;
        }

        @Override public Collection<V> remove(@Nullable Object key) {
          Collection<V> collection = removeAll(key);
          return collection.isEmpty() ? null : collection;
        }
      };
    }
    
    return map;
  }

  // Comparison and hashing

  /**
   * Compares the specified object to this multimap for equality.
   *
   * <p>Two {@code ListMultimap} instances are equal if, for each key, they
   * contain the same values in the same order. If the value orderings disagree,
   * the multimaps will not be considered equal.
   */
  @Override public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (!(other instanceof Multimap<?, ?>)) {
      return false;
    }
    return asMap().equals(((Multimap<?, ?>) other).asMap());
  }
  
  /**
   * Returns the hash code for this multimap.
   *
   * <p>The hash code of a multimap is defined as the hash code of the map view,
   * as returned by {@link Multimap#asMap}.
   */
  @Override public int hashCode() {
    return asMap().hashCode();
  }

  /**
   * Returns a string representation of the multimap, generated by calling
   * {@code toString} on the map returned by {@link Multimap#asMap}.
   *
   * @return a string representation of the multimap
   */
  @Override public String toString() {
    return asMap().toString();
  }

  private static final long serialVersionUID = -2456602590668068301L;
}
