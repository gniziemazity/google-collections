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
