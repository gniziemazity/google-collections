// Copyright 2006 Google Inc. All Rights Reserved.

package com.google.common.collect;

/**
 * Throws an exception as unchecked. Chances are you shouldn't use this.
 *
 * @author crazybob@google.com (Bob Lee)
 */
class UncheckedThrower<T extends Throwable> {

  @SuppressWarnings("unchecked")
  private void throwAsUnchecked2(Throwable t) throws T {
    // This cast is erased at runtime, so it won't throw ClassCastException.
    throw (T) t;
  }

  /**
   * This hack enables us to pass exceptions from the creation method through
   * sans wrapping. Thanks to Java Puzzlers for the idea. ;)
   */
  static void throwAsUnchecked(Throwable t) {
    new UncheckedThrower<Error>().throwAsUnchecked2(t);
  }
}
