// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.common.collect;

import java.util.List;

/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Hayward Chan
 */
class Platform {

  /**
   * Calls {@link List#subList(int, int)}.  Factored out so that it can be
   * emulated in GWT.
   *
   * <p>This method is not supported in GWT yet.  See <a
   * href="http://code.google.com/p/google-web-toolkit/issues/detail?id=1791">
   * GWT issue 1791</a>
   */
  static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  /**
   * Calls {@link Class#isInstance(Object)}.  Factored out so that it can be
   * emulated in GWT.
   *
   * <p>This method is not supported in GWT yet.
   */
  static boolean isInstance(Class<?> clazz, Object obj) {
    return clazz.isInstance(obj);
  }

  /**
   * Clone the given array using {@link Object#clone()}.  It is factored out so
   * that it can be emulated in GWT.
   */
  static <T> T[] clone(T[] array) {
    return array.clone();
  }

  private Platform() {}
}
