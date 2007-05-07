package com.google.common.base;

/**
 * Package-private interface implemented by references that have code to run
 * after garbage collection of their referents.
 *
 * @author crazybob@google.com (Bob Lee)
 */
interface FinalizableReference {

  /**
   * Invoked on a background thread after the referent has been garbage
   * collected.
   */
  void finalizeReferent();
}
