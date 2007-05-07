package com.google.common.base;

import java.lang.ref.PhantomReference;

/**
 * Phantom reference with a {@link #finalizeReferent()} method which a
 * background thread invokes after the garbage collector reclaims the
 * referent. This is a simpler alternative to using a {@link
 * java.lang.ref.ReferenceQueue}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public abstract class FinalizablePhantomReference<T>
    extends PhantomReference<T> implements FinalizableReference {

  protected FinalizablePhantomReference(T referent) {
    super(referent, FinalizableReferenceQueue.getInstance());
  }
}
