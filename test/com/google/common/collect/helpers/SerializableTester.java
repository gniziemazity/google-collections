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

package com.google.common.collect.helpers;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Helper class for testing whether a class is serializable. This class tests
 * not only whether serialization succeeds, but also whether the serialized form
 * is <i>correct</i>: i.e., whether an equivalent object can be reconstructed by
 * <i>deserializing</i> the serialized form.
 *
 * <p>If serialization fails, you can use {@code SerializationChecker} to
 * diagnose which referenced fields were not serializable.
 *
 * @see Serializable
 * @author mbostock@google.com (Mike Bostock)
 */
public final class SerializableTester {
  private SerializableTester() {}

  /**
   * Serializes and deserializes the specified object.
   *
   * <p>Note that the specified object may not be known by the compiler to be a
   * {@link Serializable} instance, and is thus declared an {@code Object}. For
   * example, it might be declared as a {@code List}.
   *
   * @return the re-serialized object
   * @throws SerializationException if the specified object was not successfully
   *     serialized or deserialized
   */
  @SuppressWarnings("unchecked")
  public static <T> T reserialize(T object) {
    checkNotNull(object);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try {
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(object);
      ObjectInputStream in = new ObjectInputStream(
          new ByteArrayInputStream(bytes.toByteArray()));
      return (T) in.readObject();
    } catch (RuntimeException e) {
      throw new SerializationException(e);
    } catch (IOException e) {
      throw new SerializationException(e);
    } catch (ClassNotFoundException e) {
      throw new SerializationException(e);
    }
  }

  public static class SerializationException extends RuntimeException {
    public SerializationException(Throwable cause) {
      super(cause);
    }
  }
}
