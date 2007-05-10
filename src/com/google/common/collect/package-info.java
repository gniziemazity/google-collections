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

/**
 * This package contains generic collection interfaces and implementations, and
 * other utilities for working with collections.
 *
 * <h2>Collection Types</h2>
 *
 * <dl>
 * <dt>{@link com.google.common.collect.BiMap}
 * <dd>An extension of {@link java.util.Map} that guarantees the uniqueness of
 *     its values as well as that of its keys.  This is sometimes called an
 *     "invertible map," since the restriction on values enables it to support
 *     an {@linkplain com.google.common.collect.BiMap#inverse inverse view} --
 *     which is another instance of {@code BiMap}.
 *
 * <dt>{@link com.google.common.collect.Multiset}
 * <dd>An extension of {@link java.util.Collection} that may contain duplicate
 *     values like a {@link java.util.List}, yet has order-independent equality
 *     like a {@link java.util.Set}.  One typical use for a multiset is to
 *     represent a histogram.
 *
 * <dt>{@link com.google.common.collect.Multimap}
 * <dd>A new type, which is similar to {@link java.util.Map}, but may contain
 *     multiple entries with the same key.  Some behaviors of
 *     {@link com.google.common.collect.Multimap} are left unspecified and are
 *     provided only by the two subtypes mentioned next.
 *
 * <dt>{@link com.google.common.collect.SetMultimap}
 * <dd>An extension of {@link com.google.common.collect.Multimap} which has
 *     order-independent equality and does not allow duplicate entries; that is,
 *     while a key may appear twice in a {@code SetMultimap}, each must map to a
 *     different value.  {@code SetMultimap} takes its name from the fact that
 *     the {@linkplain com.google.common.collect.SetMultimap#get collection of
 *     values} associated with a fixed key fulfills the {@link java.util.Set}
 *     contract.
 *
 * <dt>{@link com.google.common.collect.ListMultimap}
 * <dd>An extension of {@link com.google.common.collect.Multimap} which permits
 *     duplicate entries, supports random access of values for a particular key,
 *     and has <i>partially order-dependent equality</i> as defined by
 *     {@link com.google.common.collect.ListMultimap#equals}. {@code
 *     ListMultimap} takes its name from the fact that the {@linkplain
 *     com.google.common.collect.ListMultimap#get collection of values}
 *     associated with a fixed key fulfills the {@link java.util.List} contract.
 *
 * <dt>{@link com.google.common.collect.SortedSetMultimap}
 * <dd>An extension of {@link com.google.common.collect.SetMultimap} for which
 *     the {@linkplain com.google.common.collect.SortedSetMultimap#get
 *     collection values} associated with a fixed key is a
 *     {@link java.util.SortedSet}.
 *
 * <dt>{@link com.google.common.collect.PrefixMap}
 * <dd>A very simple quasi-collection type which stores values indexed by a key
 *     <i>prefix</i> rather than by precise key values.
 * </dl>
 *
 * <h2>Collection Implementations</h2>
 *
 * <h3>of {@link java.util.Map}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.ReferenceMap}
 * <dd>TODO
 * </dl>
 *
 * <h3>of {@link java.util.SortedSet}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.SortedArraySet}
 * <dd>TODO
 * </dl>
 *
 * <h3>of {@link com.google.common.collect.BiMap}</h3>
 * <ul>
 * <li>HashBiMap
 * <li>EnumBiMap
 * <li>EnumHashBiMap
 * </ul>
 *
 * <h3>of {@link com.google.common.collect.Multiset}</h3>
 * <ul>
 * <li>{@link com.google.common.collect.HashMultiset}
 * <li>{@link com.google.common.collect.LinkedHashMultiset}
 * <li>{@link com.google.common.collect.LinkedListMultiset}
 * <li>{@link com.google.common.collect.TreeMultiset}
 * </ul>
 *
 * <h3>of {@link com.google.common.collect.Multimap}</h3>
 * <ul>
 * <li>{@link com.google.common.collect.ArrayListMultimap}
 * <li>{@link com.google.common.collect.LinkedListMultimap}
 * <li>{@link com.google.common.collect.HashMultimap}
 * <li>{@link com.google.common.collect.LinkedHashMultimap}
 * <li>{@link com.google.common.collect.TreeMultimap}
 * </ul>
 *
 * <h3>of {@link com.google.common.collect.PrefixMap}</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.PrefixTrie}
 * <dd>TODO
 * </dl>
 *
 * <h3>For primitive integers</h3>
 * <dl>
 * <dt>{@link com.google.common.collect.IntQueue}
 * <dd>TODO
 * <dt>{@link com.google.common.collect.IntStack}
 * <dd>TODO
 * </dl>
 *
 * <h2>Skeletal implementations</h2>
 * <ul>
 * <li>{@link com.google.common.collect.AbstractIterator}
 * <li>{@link com.google.common.collect.AbstractIterable}
 * <li>{@link com.google.common.collect.AbstractMultiset}
 * <li>{@link com.google.common.collect.AbstractMultisetEntry}
 * <li>{@link com.google.common.collect.AbstractMultimap}
 * <li>{@link com.google.common.collect.AbstractListMultimap}
 * <li>{@link com.google.common.collect.AbstractSetMultimap}
 * <li>{@link com.google.common.collect.AbstractSortedSetMultimap}
 * <li>{@link com.google.common.collect.AbstractMapEntry}
 * </ul>
 *
 * <h2>Classes of static utility methods</h2>
 *
 * <ul>
 * <li>{@link com.google.common.collect.Comparators}
 * <li>{@link com.google.common.collect.Iterators}
 * <li>{@link com.google.common.collect.Iterables}
 * <li>{@link com.google.common.collect.Lists}
 * <li>{@link com.google.common.collect.Maps}
 * <li>{@link com.google.common.collect.Sets}
 * <li>{@link com.google.common.collect.Multisets}
 * <li>{@link com.google.common.collect.Multimaps}
 * <li>{@link com.google.common.collect.ObjectArrays}
 * <li>{@link com.google.common.collect.PrimitiveArrays}
 * </ul>
 *
 * <h2>Builders</h2>
 *
 * <h2>Constraints stuff</h2>
 *
 * <h2>Forwarding objects</h2>
 *
 * <p>The methods of this package always throw {@link
 * java.lang.NullPointerException} in response to a {@code null} value for any
 * parameter that is not explicitly annotated as being {@link
 * com.google.common.base.Nullable @Nullable}.
 *
 * @author mbostock@google.com (Mike Bostock)
 * @author kevinb@google.com (Kevin Bourrillion)
 * @author jlevy@google.com (Jared Levy)
 */
package com.google.common.collect;

