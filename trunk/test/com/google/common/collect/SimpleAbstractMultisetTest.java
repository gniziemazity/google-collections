// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.common.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Unit test for {@link com.google.common.collect.HashMultiset}.
 *
 * @author kevinb
 */
public class SimpleAbstractMultisetTest extends AbstractMultisetTest {

  @Override protected <E> Multiset<E> create() {
    return new SimpleAbstractMultiset<E>();
  }

  @Override public void testNullPointerExceptions() {}

  // TODO(kevinb): obviously writing this is FAR from as "simple" as we'd like
  // We will make it simpler.

  public static class SimpleAbstractMultiset<E> extends AbstractMultiset<E>
      implements Serializable {
    Map<E, Integer> backingMap = Maps.newHashMap();

    @Override public boolean add(E element, int occurrences) {
      checkArgument(occurrences >= 0);
      if (occurrences == 0) {
        return false;
      }
      Integer frequency = backingMap.get(element);
      if (frequency == null) {
        frequency = 0;
      }
      checkArgument(occurrences <= Integer.MAX_VALUE - frequency);
      backingMap.put(element, frequency + occurrences);
      return true;
    }

    @Override public int remove(Object element, int occurrences) {
      checkArgument(occurrences >= 0);
      Integer count = backingMap.get(element);
      if (count == null) {
        return 0;
      } else if (count - occurrences >= 1) {
        backingMap.put((E) element, count - occurrences);
        return occurrences;
      } else {
        return backingMap.remove(element);
      }
    }

    @Override public int removeAllOccurrences(Object element) {
      Integer frequency = backingMap.remove(element);
      return (frequency == null) ? 0 : frequency;
    }

    public Set<E> elementSet() {
      return new ForwardingSet<E>(backingMap.keySet()) {
        @Override public Iterator<E> iterator() {
          final Iterator<Map.Entry<E, Integer>> entries
              = backingMap.entrySet().iterator();
          return new Iterator<E>() {
            Map.Entry<E, Integer> toRemove = null;

            public boolean hasNext() {
              return entries.hasNext();
            }
            public E next() {
              toRemove = entries.next();
              return toRemove.getKey();
            }
            public void remove() {
              checkState(toRemove != null);
              entries.remove();
              toRemove = null;
            }
          };
        }
        @Override public boolean remove(Object element) {
          return removeAllOccurrences(element) != 0;
        }
        @Override public boolean removeAll(Collection<?> elementsToRemove) {
          return SimpleAbstractMultiset.this.removeAll(elementsToRemove);
        }
        @Override public boolean retainAll(Collection<?> elementsToRetain) {
          return SimpleAbstractMultiset.this.retainAll(elementsToRetain);
        }
        @Override public void clear() {
          backingMap.clear();
        }
      };
    }

    public Set<Entry<E>> entrySet() {
      return new AbstractSet<Entry<E>>() {
        @Override public int size() {
          return backingMap.size();
        }

        @Override public Iterator<Multiset.Entry<E>> iterator() {
          final Iterator<Map.Entry<E, Integer>> backingEntries
              = backingMap.entrySet().iterator();
          return new Iterator<Multiset.Entry<E>>() {
            Map.Entry<E, Integer> toRemove = null;

            public boolean hasNext() {
              return backingEntries.hasNext();
            }
            public Multiset.Entry<E> next() {
              final Map.Entry<E, Integer> mapEntry
                  = backingEntries.next();
              toRemove = mapEntry;
              return new AbstractMultisetEntry<E>() {
                public E getElement() {
                  return mapEntry.getKey();
                }
                public int getCount() {
                  return mapEntry.getValue();
                }
              };
            }
            public void remove() {
              checkState(toRemove != null);
              backingEntries.remove();
              toRemove = null;
            }
          };
        }
      };
    }
  }
}
