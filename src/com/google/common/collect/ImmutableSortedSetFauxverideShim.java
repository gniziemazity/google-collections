/*
 * Copyright (C) 2009 Google Inc.
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

import com.google.common.annotations.GwtCompatible;

import java.util.Iterator;

/**
 * "Overrides" the {@link ImmutableSet} static methods that lack
 * {@link ImmutableSortedSet} equivalents with deprecated, exception-throwing
 * versions. This prevents accidents like the following:<pre>   {@code
 * 
 *   List<Object> objects = ...;
 *   // Sort them: 
 *   Set<Object> sorted = ImmutableSortedSet.copyOf(objects);
 *   // BAD CODE! The returned set is actually an unsorted ImmutableSet!}</pre>
 * 
 * <p>We would put the overrides in {@link ImmutableSortedSet} itself, but it is
 * impossible to define both {@code <E> copyOf(Iterable)} and {@code <E extends
 * Comparable<? super E>> copyOf(Iterable)} in the same class (ditto for the
 * {@code Iterator} variant).
 * 
 * @author Chris Povirk
 */
@GwtCompatible
abstract class ImmutableSortedSetFauxverideShim<E> extends ImmutableSet<E> {
  /**
   * Not supported. Use {@link ImmutableSortedSet#naturalOrder}, which offers
   * better type-safety, instead. This method exists only to hide
   * {@link ImmutableSet#builder} from consumers of {@code ImmutableSortedSet}.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated Use {@link ImmutableSortedSet#naturalOrder}, which offers
   *     better type-safety.
   */
  @Deprecated public static <E> ImmutableSortedSet.Builder<E> builder() {
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. <b>You are attempting to create a set that may contain a
   * non-{@code Comparable} element.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass a parameter of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(Comparable)}.</b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(E element) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Not supported. <b>You are attempting to create a set that may contain a
   * non-{@code Comparable} element.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(Comparable, Comparable)}.</b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(E e1, E e2) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Not supported. <b>You are attempting to create a set that may contain a
   * non-{@code Comparable} element.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(Comparable, Comparable, Comparable)}.</b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(E e1, E e2, E e3) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Not supported. <b>You are attempting to create a set that may contain a
   * non-{@code Comparable} element.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(Comparable, Comparable, Comparable, Comparable)}.
   * </b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Not supported. <b>You are attempting to create a set that may contain a
   * non-{@code Comparable} element.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass the parameters of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(
   *     Comparable, Comparable, Comparable, Comparable, Comparable)}. </b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(
      E e1, E e2, E e3, E e4, E e5) {
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. <b>You are attempting to create a set that may contain
   * non-{@code Comparable} elements.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass parameters of type {@code Comparable} to use {@link
   *     ImmutableSortedSet#of(Comparable[])}.</b>
   */
  @Deprecated public static <E> ImmutableSortedSet<E> of(E... elements) {
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. <b>You are attempting to copy a collection that may contain
   * non-{@code Comparable} elements.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass a collection whose element type implements {@code
   *     Comparable} to use {@link ImmutableSortedSet#copyOf(Iterable)}.</b>
   */
  /*
   * Do NOT declare a return type of "ImmutableSortedSet": See the comment in
   * the method body for details.
   */
  @Deprecated public static <E> ImmutableSet<E> copyOf(
      Iterable<? extends E> elements) {
    /*
     * The compiler will interpret a call to
     * "ImmutableSortedSet.copyOf(noncomparables)" as a call to this method and
     * report that it's deprecated, but it will write
     * "ImmutableSet ImmutableSortedSet#copyOf(Iterable)" in the bytecode
     * because that's how indirect static-method invocations are compiled. Only
     * later will the VM will resolve which version to call -- and without
     * generic-type information, we need another way to keep it from choosing
     * the version in ImmutableSortedSet. (That version assumes that the given
     * elements all implement Comparable, but this version doesn't guarantee
     * that, so the caller might get a ClassCastException.)
     * 
     * The key is the return type. Method references in bytecode contain the
     * return type. If this method returned ImmutableSortedSet, it would match
     * the version in ImmutableSortedSet exactly, and at runtime, that version
     * would be selected over this one. By returning ImmutableSet here, we
     * distinguish this method from the ImmutableSortedSet method, and the VM
     * will choose this version at runtime.
     * 
     * (The of() methods, by contrast, can declare a return type of
     * ImmutableSortedSet because their parameter types distinguish them from
     * the versions in ImmutableSet. And builder() isn't overridden in
     * ImmutableSortedSet at all; we could define it there if we wanted.)
     */
    throw new UnsupportedOperationException();
  }

  /**
   * Not supported. <b>You are attempting to copy an iterator that may contain
   * non-{@code Comparable} elements.</b> Proper calls will resolve to the
   * version in {@code ImmutableSortedSet}, not this dummy version.
   * 
   * @throws UnsupportedOperationException always
   * @deprecated <b>Pass an iterator whose element type implements {@code
   *     Comparable} to use {@link ImmutableSortedSet#copyOf(Iterator)}.</b>
   */
  /*
   * Do NOT declare a return type of "ImmutableSortedSet": See the comments on
   * copyOf(Iterable) for details.
   */
  @Deprecated public static <E> ImmutableSet<E> copyOf(
      Iterator<? extends E> elements) {
    throw new UnsupportedOperationException();
  }
}
