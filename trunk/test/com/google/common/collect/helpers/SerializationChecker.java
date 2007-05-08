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

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Determine whether an object is serializable and, if not, why.
 *
 * @author Arthur A. Gleckler
 */
public class SerializationChecker {
  /**
   * Never compute more than this many paths to explain serialization
   * problems.
   */
  private static final int MAX_EXPLANATION_PATHS = 10;

  private static final Set<Class> EMPTY_CLASS_SET
      = Collections.<Class>emptySet();

  static abstract class Component {
    public abstract String getLabel();
    public abstract Object getValue();
  }

  static class ComponentField extends Component {
    private final Field field;
    private final Object object;

    public ComponentField(Field field, Object object) {
      this.field = field;
      this.object = object;
    }

    public String getLabel() {
      return getValue().getClass().getSimpleName() + ": " + field.getName();
    }

    public Object getValue() {
      try {
        return field.get(object);
      } catch (IllegalAccessException exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  static class ComponentArrayElement extends Component {
    private final int index;
    private final Object element;

    public ComponentArrayElement(int index, Object element) {
      this.element = element;
      this.index = index;
    }

    public String getLabel() {
      return getValue().getClass().getSimpleName() + ": [" + index + "]";
    }

    public Object getValue() {
      return element;
    }
  }

  static abstract class ComponentMapElement extends Component {
    protected Object element;

    public ComponentMapElement(Object element) {
      this.element = element;
    }

    public Object getValue() {
      return element;
    }
  }

  static class ComponentMapKey extends ComponentMapElement {
    public ComponentMapKey(Object key) {
      super(key);
    }

    public String getLabel() {
      return "key: " + stringOrClassName(element);
    }
  }

  static class ComponentMapValue extends ComponentMapElement {
    private Object key;

    public ComponentMapValue(Object key, Object value) {
      super(value);
      this.key = key;
    }

    public String getLabel() {
      return "value (key: "
          + stringOrClassName(key)
          + "): "
          + stringOrClassName(element);
    }
  }

  public static String summarizeSerializabilityInfo(
      Set<List<String>> pathsToNonSerializableObjects) {
    StringBuffer buffer = new StringBuffer(0);

    if (!pathsToNonSerializableObjects.isEmpty()) {
      buffer.append("---- Paths to non-serializable fields:\n");

      for (List<String> path : pathsToNonSerializableObjects) {
        int indentationLevel = 1;
        int pathLength = path.size();

        buffer.append("---- ");
        buffer.append(path.get(0));
        for (String element : path.subList(1, pathLength)) {
          buffer.append('\n');
          indent(buffer, indentationLevel++);
          buffer.append(" ==> ");
          buffer.append(element);
        }
        buffer.append('\n');
      }
    }
    return buffer.toString();
  }

  private static void indent(StringBuffer buffer, int indentationLevel) {
    for (int level = 0; level < indentationLevel; level++) {
      buffer.append("    ");
    }
  }

  /**
   * @see #checkSerializability(Object root, Set stopClasses, int
   *      maxExplanationPaths)
   */
  public static Set<List<String>> checkSerializability(Object root) {
    return checkSerializability(root, EMPTY_CLASS_SET, MAX_EXPLANATION_PATHS);
  }

  /**
   * @see #checkSerializability(Object root, Set stopClasses, int
   *      maxExplanationPaths)
   */
  public static Set<List<String>> checkSerializability(
      Object root, Set<Class> stopClasses) {
    return checkSerializability(root, stopClasses, MAX_EXPLANATION_PATHS);
  }

  /**
   * @see #checkSerializability(Object root, Set stopClasses, int
   *      maxExplanationPaths)
   */
  public static Set<List<String>> checkSerializability(
      Object root, int maxExplanationPaths) {
    return checkSerializability(root, EMPTY_CLASS_SET, maxExplanationPaths);
  }

  /**
   * Determines whether an object is serializable and, if not, why.
   *
   * Walks a serializable object, finding a set of objects reachable
   * through it that are not serializable.  For each such object,
   * returns a list of class and field names or array indices that
   * describe a path from the root object to that object.  Stops
   * tracing any path early if a class assignable from a class in
   * stopClasses is encountered.  Returns at most maxExplanationPaths
   * paths.
   */
  public static Set<List<String>> checkSerializability(
      Object root, Set<Class>stopClasses, int maxExplanationPaths) {
    if (canSerialize(root)) {
      return Collections.<List<String>>emptySet();
    }

    Set<List<String>> pathsToNonSerializableObjects
        = new HashSet<List<String>>();
    Set<Object> visited = new HashSet<Object>();
    List<String> initialPath
        = Lists.newLinkedList(root.getClass().getSimpleName());

    findCulpritComponents(
        root, stopClasses, pathsToNonSerializableObjects, visited, initialPath,
        maxExplanationPaths);
    return pathsToNonSerializableObjects;
  }

  /**
   * Find the paths to non-serializable fields, array elements,
   * collection elements, and map keys and values starting from root
   * and objects reachable from it.  Stop tracing any path early if a
   * class assignable from a class in stopClasses is encountered.
   *
   * @param root where to start
   * @param stopClasses classes at which to stop
   * @param pathsToNonSerializableObjects accumulator for paths
   * @param visited set of objects visited so far
   * @param pathSoFar path to root
   * @param maxExplanationPaths max. number of paths to compute
   */
  private static void findCulpritComponents(
      Object root, Set<Class> stopClasses,
      Set<List<String>> pathsToNonSerializableObjects,
      Set<Object> visited, List<String> pathSoFar, int maxExplanationPaths) {
    if (pathsToNonSerializableObjects.size() == maxExplanationPaths) {
      return;
    }
    if (!visited.contains(root)) {
      visited.add(root);
      if (isAssignableFrom(root.getClass(), stopClasses)) {
        pathsToNonSerializableObjects.add(pathSoFar);
        return;
      }

      Set<Component> components = getAllComponents(root);
      boolean allComponentsSerializable = true;

      scanComponents:
      for (Component component : components) {
        Object value = component.getValue();

        if (!visited.contains(value) && !canSerialize(value)) {
          allComponentsSerializable = false;
          break scanComponents;
        }
      }
      if (allComponentsSerializable) {
        pathsToNonSerializableObjects.add(pathSoFar);
      } else {
        for (Component component : components) {
          Object value = component.getValue();

          if (!canSerialize(value)) {
            findCulpritComponents(
                value,
                stopClasses,
                pathsToNonSerializableObjects,
                visited,
                extendPath(pathSoFar, component.getLabel()),
                maxExplanationPaths);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static boolean isAssignableFrom(Class objectClass,
                                          Set<Class> stopClasses) {
    for (Class stopClass : stopClasses) {
      if (stopClass.isAssignableFrom(objectClass)) {
        return true;
      }
    }
    return false;
  }

  /**
   * If object is an array, returns its elements as {@link
   * Component}s.  Otherwise, returns its non-transient fields.
   */
  @SuppressWarnings("unchecked")
  private static Set<Component> getAllComponents(Object object) {
    Class objectClass = object.getClass();

    if (objectClass.isArray()) {
      return getAllArrayElements((Object []) object);
    } else if (Collection.class.isAssignableFrom(objectClass)) {
      return getAllCollectionElements((Collection) object);
    } else if (Map.class.isAssignableFrom(objectClass)) {
      return getAllMapElements((Map) object);
    } else {
      return getAllFields(object);
    }
  }

  /**
   * Returns a {@link ComponentArrayElement} for each element of array.
   */
  private static Set<Component> getAllArrayElements(
      Object[] array) {
    int size = array.length;
    Set<Component> elements = new HashSet<Component>(size);

    for (int index = 0; index < size; index++) {
      elements.add(new ComponentArrayElement(index, array[index]));
    }
    return elements;
  }

  /**
   * Returns a {@link ComponentArrayElement} for each element of a
   * {@link java.util.Collection}.
   */
  private static Set<Component> getAllCollectionElements(
      Collection collection) {
    return getAllArrayElements(collection.toArray());
  }

  /**
   * Returns an appropriate {@link ComponentMapElement} for each key
   * and value of a {@link java.util.Map}.  If {@link
   * UnsupportedOperationException} is thrown while collecting this
   * information, e.g. because {@link java.util.Map#keySet} is not
   * implemented by {@link java.util.IdentityHashMap}, return a
   * {@link ComponentField} for each relevant field of the map object
   * instead.
   */
  private static Set<Component> getAllMapElements(Map<Object, Object> map) {
    synchronized (map) {
      try {
        Set<Component> keysAndValues = new HashSet<Component>();
        Set<Object> keys = map.keySet();

        for (Object key : keys) {
          keysAndValues.add(new ComponentMapKey(key));
          keysAndValues.add(new ComponentMapValue(key, map.get(key)));
        }
        return keysAndValues;
      } catch (UnsupportedOperationException exception) {
        return getAllFields(map);
      }
    }
  }

  /**
   * Returns a {@link ComponentField} for each non-transient field
   * declared on the object's class and its superclasses.
   */
  private static Set<Component> getAllFields(Object object) {
    return getAllFields(object.getClass(), object, new HashSet<Component>());
  }

  private static Set<Component> getAllFields(
      Class bottomClass, Object object, Set<Component> accumulator) {
    if (bottomClass != null) {
      Field[] fields = bottomClass.getDeclaredFields();

      for (int index = 0; index < fields.length; index++) {
        Field field = fields[index];
        int modifiers = field.getModifiers();

        if (!(Modifier.isStatic(modifiers)
              || Modifier.isTransient(modifiers))) {
          field.setAccessible(true);
          accumulator.add(new ComponentField(field, object));
        }
      }
      getAllFields(bottomClass.getSuperclass(), object, accumulator);
    }
    return accumulator;
  }

  /**
   * Returns true iff object can be serialized without error.
   */
  public static boolean canSerialize(Object object) {
    try {
      ObjectOutputStream stream
          = new ObjectOutputStream(new ByteArrayOutputStream());

      stream.writeObject(object);
      return true;
    } catch (Exception exception) {
      return false;
    } catch (NoClassDefFoundError exception) {
      return false;
    }
  }

  /**
   * Return a copy of path with newElement added to its end.
   */
  private static List<String> extendPath(List<String> path, String newElement) {
    List<String> pathCopy = new LinkedList<String>(path);

    pathCopy.add(newElement);
    return pathCopy;
  }

  /**
   * If object is a string, return it.  Otherwise, return its class's
   * simple name.  This method is used for the labels of keys and
   * values when tracing Maps.
   */
  private static String stringOrClassName(Object object) {
    if (object == null) {
      return null;
    }
    return (object instanceof String)
        ? ((String) object)
        : object.getClass().getSimpleName();
  }
}
