# Release 1.0 (FINAL - 20091230) #

  * No further changes!

[Download](http://google-collections.googlecode.com/files/google-collect-1.0.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0/)

# Release 1.0 (RC5 - 20091209) #

  * Bug fixes and doc fixes only!

[Download](http://google-collections.googlecode.com/files/google-collect-1.0-rc5.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0-rc5/)

# Release 1.0 (RC4 - 20091110) #

  * `Functions.forMap(Map)` now has no default-default value. The function it returns just throws an exception now on an unrecognized key. For the old behavior, use `forMap(Map, null)`.
  * `ForwardingMap` no longer has `createEntrySet()`, `createKeySet()` and `createValues()` methods. Just override `entrySet()`, `keySet()` and `values()` like usual. Cache your view collection in a field IF you want to; this is no longer done for you.
  * In `ImmutableMultiset.Builder`, the `add(E, int)` overload is renamed to `addCopies`, while other overloads are unchanged.
  * `Ordering.givenOrder()`, which was superseded by `Ordering.explicit()` in rc1, is now removed.
  * `MapMaker.loadFactor()`, a method no one should really use, was removed; unfortunately, there's no time left to go through a deprecation cycle, but I doubt this will affect many people. This alters the serialized form of "MapMade" maps, so the serialversionuid has been incremented.

[Download](http://google-collections.googlecode.com/files/google-collect-1.0-rc4.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0-rc4/)


# Release 1.0 (RC3 - 20091016) #

A much smaller set of API-level changes this time:

  * ImmutableSetMultimap.Builder silently ignores duplicates now, just like ImmutableSet.Builder
  * MapMaker config methods like concurrencyLevel() can't be called repeatedly
  * ImmutableMap no longer implements ConcurrentMap
  * Multimap methods removeAll() and replaceValues() now return immutable collections
  * Preconditions.checkElementIndex and checkPositionIndex now return the index that was just checked
  * add(E...) and allAll(Iterator) overloads now exist on all immutable collection builders, not just some
  * Ordering methods are no longer final -- override with care!

[Download](http://google-collections.googlecode.com/files/google-collect-1.0-rc3.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0-rc3/)

# Release 1.0 (RC2 - 20090602) #

## NEW: ##

  * ImmutableSortedMap (!)
  * ImmutableMultiset.Builder
  * ImmutableListMultimap (well, it's essentially the same as ImmutableMultimap.)
  * ImmutableSortedSet.copyOf() (new overloads)
  * Sets.immutableEnumSet(Iterable) (new overload)
  * Ordering.usingToString()

## Changed: ##

  * Ordering.givenOrder() renamed to Ordering.explicit() (note: givenOrder() is still there, deprecated, to ease your pain)
  * Sets.immutableEnumSet() now returns ImmutableSet, not Set
  * ImmutableMultimap.get() now returns ImmutableCollection, not Collection
  * Multimaps.index() now returns ImmutableListMultimap, not ImmutableMultimap
  * TreeMultimap.asMap() now returns SortedMap, not Map
  * Immutable Builder classes now made final (whoops)
  * Fixed the `Ordering.<Long>natural().reverse()` problem (also applies to nullsFirst(), nullsLast())

[Download](http://google-collections.googlecode.com/files/google-collect-1.0-rc2.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0-rc2/)

# Release 1.0 (RC1 - 20090406) #

## NEW: ##

  * [com.google.common.annotations](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/annotations/package-summary.html) package with new annotations for marking types as gwt compatible ([GwtCompatible](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/annotations/GwtCompatible.html), [GwtIncompatible](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/annotations/GwtIncompatible.html)), and marking a type or member as being visible only for testing ([VisibleForTesting](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/annotations/VisibleForTesting.html))
  * [Joiner](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Joiner.html), which joins pieces of text together, replacing the old static methods on the Join class.
  * [Predicate.instanceOf(Class<?>)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Predicates.html#instanceOf(java.lang.Class)) returns a Predicate that evaluates to true if the object being tested is an instance of the given class.
  * [MutableClassToInstanceMap](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/MutableClassToInstanceMap.html) and [ImmutableClassToInstanceMap](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/ImmutableClassToInstanceMap.html), which map a class to an instance of that class.
  * [ImmutableSetMultimap](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/ImmutableSetMultimap.html) is a SetMultimap with reliable user specified key and value iteration order.
  * [Maps.difference(...)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Maps.html#difference(java.util.Map,%20java.util.Map)) computes the difference between two maps, returning the result as a MapDifference.
  * [Collections2.transform(...)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Collections2.html#transform(java.util.Collection,%20com.google.common.base.Function)) is the Collection-based equivalent of Iterables.transform(..), and returns the Collection that results from applying a Function to each element of a given Collection.
  * New ImmutableMultimap.of(..) static methods were added to quickly create immutable maps of specified values.
  * ImmutableMultimap.Builder has a new [putAll(...)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/ImmutableMultimap.Builder.html#putAll(com.google.common.collect.Multimap)) method to store another multimap's entries in the built multimap.
  * [Multisets.immutableEntry(E, int)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multisets.html#immutableEntry(E,%20int)) returns a new immutable Multiset.Entry with the specified element and count.
  * Multiset has new [setCount()](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multiset.html#setCount(E,%20int)) methods that can be used to add or remove the necessary number of occurences of an element such that the element attains the desired count.

## Removed: ##

  * The Join class was removed in favor of the new Joiner class.
  * The Nullable annotation was removed in favor of the javax.annotation.Nullable annotation from the JSR-305 reference implementation (http://code.google.com/p/jsr-305/)
  * Deprecated Functions.TO\_STRING removed in favor of Functions.toStringFunction().
  * Functions.toHashCode(). This isn't a commonly used function, and can be implemented by hand if needed.
  * Preconditions.checkContentsNotNull(...)
  * Predicates.isSameAs(Object). This wasn't commonly used, and can be easily implemented by hand.
  * The following methods were removed from Iterables: containsNull(Iterable`<?>`), emptyIterable(), limit(Iterable`<T>`, int), rotate(List`<T>`, int), skip(Iterable`<T>`, int).
  * The following methods were removed from Iterators: containsNull(Iterable`<?>`), emptyListIterator(), limit(Iterator`<T>`, int), skip(Iterator`<T>`, int).
  * The Multimaps.index(..) method that takes a multimap as the third argument has been removed.
  * The Sets.SetView constructor is no longer public (this was an oversight).
  * Multiset.removeAllOccurrences(Object). You can replace usages of this with elementSet().remove(Object).
  * AbstractMapEntry. Most usages of this can be replaced with java.util.AbstractMap.SimpleEntry/SimpleImmutableEntry provided their keys and values are directly stored in the Entry instance.
  * Constraints, Constraint and MapConstraints.
  * CustomConcurrentHashMap.
  * Serialization.

## Changed: ##

  * ConcurrentMultiset was renamed to [ConcurrentHashMultiset](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/collect/ConcurrentHashMultiset.html).
  * Predicates.isEqualTo(T) renamed to [Predicates.equalTo(T)](http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Predicates.html#equalTo(T))
  * AbstractIterator now extends UnmodifiableIterator.
  * The constructors on ArrayListMultimap, HashBiMap, HashMultimap, HashMultiset, ImmutableBiMap, LinkedHashMultimap, LinkedHashMultiset, LinkedListMultimap, TreeMultimap, and TreeMultiset were removed in favor of new static create() methods which provide type inference. The various static creation methods on Multimaps were removed in favor of  create() methods on each Multimap class.
  * Iterators.newArray(..) and Iterables.newArray(..) were renamed to toArray(..).
  * The instance methods in Ordering are all now final.

[Download](http://google-collections.googlecode.com/files/google-collect-1.0-rc1.zip)

[Maven bundle](http://repo2.maven.org/maven2/com/google/collections/google-collections/1.0-rc1/)

# Release 0.9 (snapshot 20090211) #

## NEW: ##

  * MapMaker. Now officially the single coolest thing in our library.  Easily build yourself a ConcurrentMap with any combination of features like timed expiration, weak keys or values, and automatic on-demand computation of values.  Perfect for most typical in-memory caching uses.
  * CustomConcurrentHashMap.  For extreme power users only.  The guts that underlie MapMaker.  Allows for extremely configurable maps.
  * ImmutableList.builder() and ImmutableSet.builder().  Yummy builders for creating immutable collections.
  * New Preconditions methods checkElementIndex and checkPositionIndex(es).  Mostly useful for collection implementations, but if you ever find yourself needing to throw an IndexOutOfBoundsException, these will come in really handy.
  * ObjectArrays methods concat(T[.md](.md), T) and concat(T, T[.md](.md)) to go with the existing concat(T[.md](.md), T[.md](.md), Class).
  * Maps methods filterKeys, filterValues and filterEntries.
  * New optimized Iterators methods singletonIterator and forArray.
  * LinkedListMultimap is back!  This is a ListMultimap with insertion-ordered keys and entries.
  * Ordering.isStrictlyOrdered method to go along with the duplicate-allowing Ordering.isOrdered.

## Removed: ##

  * AbstractIterable.  This did nothing except provide a default toString() which is very often not the toString() you want anyway.
  * AbstractMultiset, AbstractMultisetEntry. These are not properly designed for extensibility and we didn't mean to expose them. You can extend ForwardingMultiset instead.
  * AbstractRemovableIterator.  This was hard to use correctly and easy to use incorrectly, and didn't have many uses.
  * PrimitiveArrays, Comparators.compare(a, b).  These libraries will appear later as part of the com.google.common.primitives package (in some cases, in **greatly** improved form).
  * Map.containsEntry().  Instead, use map.entrySet().contains(Maps.immutableEntry(k, v)).
  * ObjectArrays.emptyArray()/EMPTY\_ARRAY.  These were not useful.
  * Multisets.frequencyOrder() is not as useful as it seems and can be easily done using Ordering.onResultOf() if necessary.
  * Multisets.forSet(), immutableEntry() - as far as we know no one used these.  And no one had ever asked for them.  Do you use them?
  * Collections2.forIterable() was released in the last snapshot by mistake.  You shouldn't use it; it's too weird.
  * Sets.newIdentityHashSet() has been removed; use MapMaker or Maps.newIdentityHashMap() to construct a map and pass that to Sets.newSetFromMap().  We may support this more directly in the future.
  * ReferenceMap is gone; use, for example, new MapMaker.weakKeys().softValues().makeMap().
  * Serialization.setFinalField() should never have been public; our mistake.
  * Multisets.synchronizedMultiset() is gone now.  Use ConcurrentMultiset instead.
  * Ordering.sort() added no value over Collections.sort(), so it's gone.  Use Collections.sort() or Ordering.sortedCopy().

## Moved: ##

  * Factory methods for BiMap instances in Maps have moved to the BiMap implementation classes in new methods called create(). (Constructors in those classes have also been removed.)
  * Factory methods for Multiset instances in Multisets have moved to the Multiset implementation classes in new methods called create(). (Constructors in those classes have also been removed.)
  * The Comparators class is gone.  Its methods have moved to Ordering:
    * Comparators.compound() -> Ordering.compound()
    * Comparators.fromFunction() -> Ordering.onResultOf()
    * Comparators.givenOrder() -> Ordering.givenOrder()
    * Comparators.min() -> Ordering.min()
    * Comparators.max() -> Ordering.max()
    * Comparators.naturalOrder() -> Ordering.natural()
    * Comparators.nullLeastOrder() -> Ordering.nullsFirst()
    * Comparators.nullGreatestOrder() -> Ordering.nullsLast()
    * Comparators.STRING\_FORM\_ORDER/toStringOrder() -> Ordering.onResultOf(Functions.TO\_STRING)
  * More Ordering renames:
    * Ordering.forComparator() -> Ordering.from()
    * Ordering.reverseOrder() -> Ordering.reverse()
    * Ordering.isSorted() -> Ordering.isOrdered()
  * Lists.sortedCopy() is now Ordering.sortedCopy().
  * With these changes, Ordering is now your one-stop shop for comparator-related goodness.

## Changed: ##

  * Iterators.partition(Iterator, int, boolean) has been cleaved into new methods Iterators.partition(Iterator, int) and Iterators.paddedPartition(Iterator, int); these also have much nicer semantics now.  The same goes for Iterables.partition().
  * The three methods in Multimaps called inverseArrayListMultimap, inverseHashMultimap and inverseTreeMultimap have been replaced with a single Multimaps.invertFrom() method.
  * The syntax for creating an ImmutableSortedSet has been improved using the new ImmutableSortedSet.Builder class (which replaces ImmutableSortedSet.Factory).
  * Maps.newConcurrentHashMap() has been removed; use new MapMaker().makeMap()!
  * Many methods that returned Iterator now return UnmodifiableIterator; this change should be transparent to you.

[Download](http://google-collections.googlecode.com/files/google-collect-snapshot-20090211.zip)

[Maven bundle](http://repo1.maven.org/maven2/com/google/collections/google-collections/0.9/)

# Release 0.8 (snapshot 20080820) #

## Added classes ##

  * Collections2: Provides static methods for working with Collection instances
  * ImmutableBiMap: An immutable BiMap with reliable user-specified iteration order
  * ImmutableMultimap: An immutable ListMultimap with reliable user-specified key and value iteration order
  * ImmutableMultiset: An immutable hash-based multiset
  * Serialization: Provides static method for serializing collection classes
  * UnmodifiableIterator: An iterator that does not support remove()

## New functionality ##

  * Added Join methods taking a Map parameter
  * Added Comparators.toStringOrder()
  * Added Constraints.notNull()
  * Added HashMultiset.create() methods
  * Added ImmutableList.of() methods taking 2-5 parameters and ImmutableList.copyOf(Iterator)
  * Added ImmutableSet.copyOf(Iterator)
  * ImmutableSortedSet now extends ImmutableSet
  * Added ImmutableSortedSet.orderedBy() and reverseOrder()
  * Added contains(), containsNull(), removeAll() and retainAll() methods to Iterables and Iterators
  * Added Sets.filter()
  * Widespread Javadoc improvements
  * Explicit serialized forms for most collection classes
  * To support more serialization options, Forwarding classes now have an abstract delegate() method, have a no-argument constructor, and don't implement Serializable
  * Moved static ForwardingCollection methods to Iterables

## Removed code ##

  * Removed Functions.TRIM\_STRING and trimString()
  * Removed Objects method deepEquals(), deepHashCode(), deepToString(), and nonNull()
  * Removed classes ImmutableBiMapBuilder, ImmutableMultimapBuilder, and LinkedListMultimap
  * Removed interface SerializableComparator
  * Removed constructor ConcurrentMultiset(ConcurrentMap)
  * Removed constructors EnumBiMap(EnumBiMap) and EnumHashBiMap(EnumHashBiMap),
  * Removed constructors HashMultiset(int) and HashMultiset(Iterable),
  * Removed Maps methods immutableBiMap(), newBiMap(), sortedKeySet(), uniqueIndex(Collection, Function), and uniqueIndex(Iterator, Function)
  * Removed Multimaps.immutableMultimap() methods
  * Removed Multisets methods emptyMultiset() and immutableMultiset()

[Download](http://google-collections.googlecode.com/files/google-collect-snapshot-20080820.zip)

[Maven bundle](http://repo1.maven.org/maven2/com/google/collections/google-collections/0.8/)

# Release 0.7 (snapshot 20080530) #

## New functionality ##

  * ImmutableMap: an immutable Map that requires less memory than other Map implementations
  * ImmutableSortedSet: an immutable SortedSet that requires less memory than other SortedSet implementations
  * ArrayListMultimap.trimToSize(): reduces memory usage
  * Iterables.get(int) and Iterators.get(int): retrieves the indexed element of an Iterable or Iterator
  * Iterables.getLast(): retrieves the last element of an Iterable
  * Multimap.index(): creates an index multimap that contains the results of applying a specified function to each item in an Iterable of values
  * Ordering.nullsFirst() and Ordering.nullsLast(): generates an Ordering that includes nulls first or last in the iteration order
  * Sets.union(), Sets.intersection(), and Sets.difference(): creates a set view of the union, intersection, or difference of two provided sets
  * Sets.newIdentityHashSet(ReferenceType): creates an identity-based hash set with strong, weak, or soft references

## General improvements ##

  * Extensive Javadoc rewrite
  * Bug fixes
  * Better performance


## Removed code ##

  * SortedArraySet [ImmutableSortedSet (if immutable) or TreeSet](use.md)
  * Lists.newArrayListWithCapacity()  [Lists.newArrayListWithExpectedSize()](use.md)
  * Lists.newLinkedList(E...) and Lists.newLinkedList(Iterator)
  * ImmutableMapBuilder  [ImmuableMap.builder()](use.md)
  * Map.immutableMap methods   [ImmutableMap](use.md)
  * Sets.newLinkedHashSet(E...) and newLinkedHashSet(Iterator)
  * Sets.immutableSortedSet methods  [ImmutableSortedSet](use.md)
  * Sets.newWeakHashSet() and Sets.newSoftHashSet()  [Sets.newIdentityHashSet()](use.md)

[Download](http://google-collections.googlecode.com/files/google-collect-snapshot-20080530.zip)

[Maven bundle](http://repo1.maven.org/maven2/com/google/code/google-collections/google-collect/snapshot-20080530/)

# Release 0.6 (snapshot 20080321) #

[Download](http://google-collections.googlecode.com/files/google-collect-snapshot-20080321.zip)

[Maven bundle](http://repo1.maven.org/maven2/com/google/code/google-collections/google-collect/snapshot-20080321/)

# Release 0.5 (snapshot 20071022) #

## Initial release ##

[Download](http://google-collections.googlecode.com/files/google-collect-snapshot-20071022.zip)

[Maven bundle](http://repo1.maven.org/maven2/com/google/code/google-collections/google-collect/snapshot-20071022/)