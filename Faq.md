# General #

## Is the release candidate safe to use? ##

Absolutely. It's been proven extensively in production in services like GMail, Reader, Blogger, Docs & Spreadsheets, AdWords, AdSense and dozens more. However, it is still possible that we will need to make a small number of API-level changes between now and when the release is finalized.  If you're skittish about change, just wait for 1.0 to be finalized.

## Should I use it in my own library which I release publicly? ##

Sure! But until 1.0 is **final**, be careful. Use it only in the internals of your implementation; don't expose it in your public API. Use [Jar Jar Links](http://tonicsystems.com/products/jarjar/) or a similar tool to make sure your users won't have any conflicts.

Once 1.0 is released, you should be safe to do just about anything with it.

## Should I serialize your collections persistently and expect to be able to deserialize them with future versions of your library? ##

NOT YET!  Wait for 1.0 to go final, please!

## Why did Google build all this, when it could have tried to improve the Apache Commons Collections instead? ##

The [Apache Commons Collections](http://commons.apache.org/collections/) very clearly did not meet our needs. It does not use generics, which is a problem for us as we hate to get compilation warnings from our code. It has also been in a "holding pattern" for a long time. We could see that it would require a pretty major investment from us to fix it up until we were happy to use it, and in the meantime, our own library was already growing organically.

An important difference between the Apache library and ours is that our collections very faithfully adhere to the contracts specified by the JDK interfaces they implement. If you review the Apache documentation, you'll find countless examples of violations. They deserve credit for pointing these out so clearly, but still, deviating from standard collection behavior is risky! You must be careful what you do with such a collection; bugs are always just waiting to happen.

Our collections are fully generified and never violate their contracts (with isolated exceptions, where JDK implementations have set a strong precedent for acceptable violations). This means you can pass one of our collections to any method that expects a `Collection` and feel pretty confident that things will work exactly as they should.

## Why build on Java 5, instead of 6? ##

We had planned on moving to Java 6 in time for the 1.0 release, so that our library could build upon interfaces like `NavigableSet`.  However, this was deprioritized.  We can deliver 99% of the value while sticking to Java 5, so it's senseless to cut off a large chunk of user base.

## Why build on Java 5, instead of 1.4? ##

Because we hate Java 1.4. Just kidding (but we do). Basically, at Google we simply don't use Java 1.4 anymore, and haven't for years ([GWT](http://code.google.com/webtoolkit/overview.html) excepted, but even that has worked with 1.5 for a while now). If you're using 1.4, please try feeding our library into [Retrotranslator](http://retrotranslator.sourceforge.net/). Try out the results, tell us how it goes, and please kindly send us any patches you needed to make to our code to get it working.

# Design #

## Why so much emphasis on Iterators and Iterables? ##

In general, our methods do not require a `Collection` to be passed in when an `Iterable` or `Iterator` would suffice. This distinction is important to us, as sometimes at Google we work with very large quantities of data, which may be too large to fit in memory, but which can be traversed from beginning to end in the course of some computation. Such data structures _can_ be implemented as collections, but most of their methods would have to either throw an exception, return a wrong answer, or perform abysmally. For these situations, `Collection` is a very poor fit; a square peg in a round hole.

An `Iterator` represents a one-way scrollable "stream" of elements, and an `Iterable` is anything which can spawn independent iterators. A `Collection` is much, much more than this, so we only require it when we need to.

## Why do the non-Collection iterables you return implement `toString()` but not `equals()` or `hashCode()`? ##

It's hard to imagine any `equals()` implementation that would be useful, given that it must return false when given any `List` or `Set` in order to maintain the transitive property.

It's possible `hashCode()` would be safe to implement, but it seems pointless. If you have an iterable you want to store inside some other collection, you should really copy the contents into a real collection of their own first.

`toString()` is potentially harmful as it will not perform as anyone expects; it could take arbitrarily long to run if the data set is large.

## Why are so many implementation classes marked `final`? ##

Designing and documenting classes of a public API is hard stuff, and designing and documented to be safely extended is a hundred times harder. We decided that in most cases when we're interested in extending a collection, we'd really be fine with just writing a decorator for it instead. So, we've provided the full complement of "forwarding collections", to make writing decorators easy, and we're advocating that approach.

If there are final classes that you have a very compelling reason to subclass, we'll hear your case.

## Why are the names `Multiset` and `Multimap`, not `MultiSet` and `MultiMap`? ##

Because "multiset" is a single, unhyphenated word, and we don't capitalize random letters inside those unless we have a good reason to.

## Aha, but "bimap" is also a single word!  So why is it `BiMap`, not `Bimap`? ##

This case seems analogous but is slightly different. A `BiMap` is also a `Map`; it extends the `Map` interface, and we felt it was important to make that connection clear. This is not the case for `Multimap`, which emphatically does _not_ extend `Map`, nor does `Multiset` extend `Set`.

## Why does `BiMap.put(newKey, existingValue)` throw an exception instead of just remapping the value? ##

Because this method comes from the Map interface, and such behavior on a `Map` would violate the principle of least surprise. If this is the behavior you want, just use `BiMap.forcePut()` instead.

## Why is `BiMap.putAll()` allowed to leave the bimap in an indeterminate state if it throws an exception? ##

In general, a method that throws an exception ought to leave the instance with its state unchanged. However, this is not always feasible. For a typical bimap implementation, it would be downright ugly, and slow. Note that this is no different from the regular behavior of `Map.putAll()`, `Collection.addAll()`, etc.

## Why does `BiMap` have no `getKeyForValue()` method? ##

We did think about it (Doug Lea even half-jokingly suggested naming it `teg()`!). But you don't really need it; just call `inverse().get()`. If this method did exist, every implementor of the interface would have to write it over again, and would probably do it exactly like that.

## `ClassToInstanceMap` is interesting, but I need to map a type `T` to a `Foo<T>` / I need a ClassToInstanceMultimap / etc. How? ##

If your goal is to maintain type-safety and avoid casts when using the API "normally", but you don't care to go to great lengths to _prevent_ the wrong types of objects being added, it's always been easy to do this yourself:

```
  @SuppressWarnings("unchecked")
  public static class ClassToFooMap extends HashMap<Class<?>, Foo<?>> {
    public <T> Foo<T> putInstance(Class<T> type, Foo<T> value) {
      return (Foo<T>) put(type, value);
    }
    public <T> Foo<T> getInstance(Class<T> type) {
      return (Foo<T>) get(type);
    }
  }
```

## Why do you use the type `<E extends Comparable>` in various APIs, which is not "fully generified"?  Shouldn't it be `<E extends Comparable<?>>`, `<E extends Comparable<E>>` or `<E extends Comparable<? super E>>`? ##

The last suggestion is the correct one, as explained in Effective Java.  However, we will be using `<E extends Comparable<E>>` on parameterless methods in order to work around a hideous javac bug.  This will cause you problems when you use a very unusual type like `java.sql.Timestamp` which is comparable to a supertype.  (Needs more explanation.)

## Why does `Multimap` have no `putAll(K, Iterable<V>)` or `putAll(Map<K,V>)` methods? ##

These operations can be performed rather simply. First, it's important to realize that the `get(key)` method of `Multimap` returns a "live view" of the collection corresponding to that key. So, `multimap.get(myKey).putAll(myValues)` accomplishes the first task.

The second task is also simple to perform, because any `Map` can be viewed as a `Multimap`: `multimap.putAll(Multimaps.forMap(map))`.

These "workarounds" are simple enough that we did not want to add two additional methods to `Multimap` interface, which is already quite large enough as it is.