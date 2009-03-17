// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.common.collect;

import com.google.common.collect.MapConstraints.ConstrainedMap;
import java.util.Map;

/**
 * A {@code ClassToInstanceMap} instance backed by a map.
 *
 * @see Maps#newClassToInstanceMap(java.util.Map)
 *
 * @author Hayward Chan
 */
final class SimpleClassToInstanceMap<B>
    extends ConstrainedMap<Class<? extends B>, B>
    implements ClassToInstanceMap<B> {

  SimpleClassToInstanceMap(Map<Class<? extends B>, B> delegate) {
    super(delegate, VALUE_CAN_BE_CAST_TO_KEY);
  }

  private static final MapConstraint<Class<?>, Object> VALUE_CAN_BE_CAST_TO_KEY
      = new MapConstraint<Class<?>, Object>() {
    public void checkKeyValue(Class<?> key, Object value) {
      wrap(key).cast(value);
    }
  };

  public <T extends B> T putInstance(Class<T> type, T value) {
    B oldValue = put(type, value);
    return wrap(type).cast(oldValue);
  }

  public <T extends B> T getInstance(Class<T> type) {
    B value = get(type);
    return wrap(type).cast(value);
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> wrap(Class<T> c) {
    // This is correct and safe because both Long.class and
    // long.class are of type Class<Long>.
    return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
  }

  private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS
      = new ImmutableMap.Builder<Class<?>, Class<?>>()
          .put(boolean.class, Boolean.class)
          .put(byte.class, Byte.class)
          .put(char.class, Character.class)
          .put(double.class, Double.class)
          .put(float.class, Float.class)
          .put(int.class, Integer.class)
          .put(long.class, Long.class)
          .put(short.class, Short.class)
          .put(void.class, Void.class)
          .build();

  private static final long serialVersionUID = 0;
}
