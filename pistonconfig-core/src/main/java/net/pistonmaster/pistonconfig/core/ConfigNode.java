package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/// Mutable node in a format-agnostic configuration tree.
///
/// A node stores one structural kind, optional comments, source decorations, and
/// backend metadata. Mutating a node into another kind clears the old value and
/// child containers.
public final class ConfigNode {
  private ConfigValueKind kind;
  private Map<String, ConfigNode> objectChildren;
  private List<ConfigNode> listChildren;
  private Object value;
  private ConfigComment comment = ConfigComment.none();
  private ConfigNodeDecorations decorations = ConfigNodeDecorations.empty();
  private Map<String, Object> metadata = new LinkedHashMap<>();

  private ConfigNode(ConfigValueKind kind) {
    become(kind);
  }

  /// Creates an empty object node.
  ///
  /// @return object node
  public static ConfigNode object() {
    return new ConfigNode(ConfigValueKind.OBJECT);
  }

  /// Creates an empty list node.
  ///
  /// @return list node
  public static ConfigNode list() {
    return new ConfigNode(ConfigValueKind.LIST);
  }

  /// Creates a scalar node.
  ///
  /// @param value scalar value
  /// @return scalar node
  public static ConfigNode scalar(Object value) {
    var node = new ConfigNode(ConfigValueKind.SCALAR);
    node.value = normalizeScalar(value);
    return node;
  }

  /// Creates a null node.
  ///
  /// @return null node
  public static ConfigNode nullValue() {
    return new ConfigNode(ConfigValueKind.NULL);
  }

  /// Returns the current structural kind.
  ///
  /// @return node kind
  public ConfigValueKind kind() {
    return kind;
  }

  /// Returns the comments attached to this node value.
  ///
  /// @return value comments
  public ConfigComment comment() {
    return comment;
  }

  /// Replaces the comments attached to this node value.
  ///
  /// @param comment value comments
  /// @return this node
  public ConfigNode setComment(ConfigComment comment) {
    this.comment = Objects.requireNonNull(comment, "comment");
    return this;
  }

  /// Returns source decorations for this node.
  ///
  /// @return source decorations
  public ConfigNodeDecorations decorations() {
    return decorations;
  }

  /// Replaces the source decorations for this node.
  ///
  /// @param decorations source decorations
  /// @return this node
  public ConfigNode setDecorations(ConfigNodeDecorations decorations) {
    this.decorations = Objects.requireNonNull(decorations, "decorations");
    return this;
  }

  /// Applies a decoration update function.
  ///
  /// @param decorator update function
  /// @return this node
  public ConfigNode decorate(UnaryOperator<ConfigNodeDecorations> decorator) {
    decorations = Objects.requireNonNull(decorator, "decorator").apply(decorations);
    return this;
  }

  /// Returns whether this node is an object.
  ///
  /// @return `true` for object nodes
  public boolean isObject() {
    return kind == ConfigValueKind.OBJECT;
  }

  /// Returns whether this node is a list.
  ///
  /// @return `true` for list nodes
  public boolean isList() {
    return kind == ConfigValueKind.LIST;
  }

  /// Returns whether this node is a scalar.
  ///
  /// @return `true` for scalar nodes
  public boolean isScalar() {
    return kind == ConfigValueKind.SCALAR;
  }

  /// Returns object children as an immutable snapshot.
  ///
  /// Non-object nodes return an empty map.
  ///
  /// @return object children
  public Map<String, ConfigNode> objectChildren() {
    if (!isObject()) {
      return Map.of();
    }

    return Collections.unmodifiableMap(new LinkedHashMap<>(objectChildren));
  }

  /// Returns list children as an immutable snapshot.
  ///
  /// Non-list nodes return an empty list.
  ///
  /// @return list children
  public List<ConfigNode> listChildren() {
    if (!isList()) {
      return List.of();
    }

    return List.copyOf(listChildren);
  }

  /// Returns the raw scalar value.
  ///
  /// Object, list, and null nodes return `null`.
  ///
  /// @return raw scalar value
  public Object rawValue() {
    return value;
  }

  /// Returns backend metadata as an immutable snapshot.
  ///
  /// @return metadata map
  public Map<String, Object> metadata() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
  }

  /// Looks up one metadata value.
  ///
  /// @param key metadata key
  /// @return metadata value when present
  public Optional<Object> metadata(String key) {
    return Optional.ofNullable(metadata.get(Objects.requireNonNull(key, "key")));
  }

  /// Sets or removes a metadata value.
  ///
  /// Passing `null` removes the key.
  ///
  /// @param key metadata key
  /// @param value metadata value, or `null` to remove it
  /// @return this node
  public ConfigNode setMetadata(String key, Object value) {
    Objects.requireNonNull(key, "key");
    if (value == null) {
      metadata.remove(key);
    } else {
      metadata.put(key, value);
    }
    return this;
  }

  /// Reads this node as a string when possible.
  ///
  /// Null nodes return an empty optional. Other values use `toString()`.
  ///
  /// @return string value when present
  public Optional<String> asString() {
    if (kind == ConfigValueKind.NULL) {
      return Optional.empty();
    }

    return Optional.ofNullable(value).map(Object::toString);
  }

  /// Reads this node as a boolean when possible.
  ///
  /// Boolean scalars are returned directly. String scalars accept `true` and
  /// `false` ignoring case.
  ///
  /// @return boolean value when conversion succeeds
  public Optional<Boolean> asBoolean() {
    if (value instanceof Boolean booleanValue) {
      return Optional.of(booleanValue);
    }

    if (value instanceof String stringValue) {
      if ("true".equalsIgnoreCase(stringValue) || "false".equalsIgnoreCase(stringValue)) {
        return Optional.of(Boolean.parseBoolean(stringValue));
      }
    }

    return Optional.empty();
  }

  /// Reads this node as an integer when possible.
  ///
  /// Number scalars use `Number.intValue()`. String scalars are parsed as base-10
  /// integers.
  ///
  /// @return integer value when conversion succeeds
  public Optional<Integer> asInt() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.intValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Integer.parseInt(stringValue));
      } catch (NumberFormatException _) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  /// Reads this node as a long when possible.
  ///
  /// Number scalars use `Number.longValue()`. String scalars are parsed as base-10
  /// long values.
  ///
  /// @return long value when conversion succeeds
  public Optional<Long> asLong() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.longValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Long.parseLong(stringValue));
      } catch (NumberFormatException _) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  /// Reads this node as a double when possible.
  ///
  /// Number scalars use `Number.doubleValue()`. String scalars are parsed as
  /// base-10 doubles.
  ///
  /// @return double value when conversion succeeds
  public Optional<Double> asDouble() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.doubleValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Double.parseDouble(stringValue));
      } catch (NumberFormatException _) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  /// Finds a descendant node by path.
  ///
  /// @param path descendant path
  /// @return node when every path segment exists through object nodes
  public Optional<ConfigNode> find(ConfigPath path) {
    Objects.requireNonNull(path, "path");
    ConfigNode current = this;

    for (String segment : path.segments()) {
      if (!current.isObject()) {
        return Optional.empty();
      }

      current = current.objectChildren.get(segment);
      if (current == null) {
        return Optional.empty();
      }
    }

    return Optional.of(current);
  }

  /// Finds or creates a descendant object path.
  ///
  /// Missing path segments are created as object nodes. Non-object nodes along
  /// the path are converted to objects.
  ///
  /// @param path descendant path
  /// @return node at the requested path
  public ConfigNode getOrCreate(ConfigPath path) {
    Objects.requireNonNull(path, "path");
    ConfigNode current = this;

    for (String segment : path.segments()) {
      current.ensureObject();
      current = current.objectChildren.computeIfAbsent(segment, _ -> ConfigNode.object());
    }

    return current;
  }

  /// Sets a scalar value at a descendant path.
  ///
  /// @param path descendant path
  /// @param value scalar value
  /// @return this node
  public ConfigNode set(ConfigPath path, Object value) {
    return setNode(path, ConfigNode.scalar(value));
  }

  /// Sets a node at a descendant path.
  ///
  /// The replacement is copied before insertion.
  ///
  /// @param path descendant path
  /// @param replacement replacement node
  /// @return this node
  public ConfigNode setNode(ConfigPath path, ConfigNode replacement) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(replacement, "replacement");

    if (path.isRoot()) {
      copyFrom(replacement);
      return this;
    }

    var parent = getOrCreate(path.parent().orElse(ConfigPath.root()));
    parent.ensureObject();
    parent.objectChildren.put(path.lastSegment(), replacement.copy());
    return this;
  }

  /// Removes a descendant node.
  ///
  /// Removing the root resets this node to an empty object and returns the
  /// previous value.
  ///
  /// @param path descendant path
  /// @return removed node when one existed
  public Optional<ConfigNode> remove(ConfigPath path) {
    Objects.requireNonNull(path, "path");
    if (path.isRoot()) {
      var previous = copy();
      become(ConfigValueKind.OBJECT);
      return Optional.of(previous);
    }

    return find(path.parent().orElse(ConfigPath.root()))
      .filter(ConfigNode::isObject)
      .map(parent -> parent.objectChildren.remove(path.lastSegment()));
  }

  /// Appends a scalar value to this node as a list item.
  ///
  /// Non-list nodes are converted to lists.
  ///
  /// @param value scalar value
  /// @return this node
  public ConfigNode addListValue(Object value) {
    ensureList();
    listChildren.add(ConfigNode.scalar(value));
    return this;
  }

  /// Appends a node to this node as a list item.
  ///
  /// Non-list nodes are converted to lists. The supplied node is copied before
  /// insertion.
  ///
  /// @param node node to append
  /// @return this node
  public ConfigNode addListNode(ConfigNode node) {
    ensureList();
    listChildren.add(Objects.requireNonNull(node, "node").copy());
    return this;
  }

  /// Creates a deep copy of this node.
  ///
  /// @return copied node
  public ConfigNode copy() {
    var copy = new ConfigNode(kind);
    copy.value = value;
    copy.comment = comment;
    copy.decorations = decorations;
    copy.metadata.putAll(metadata);

    if (isObject()) {
      for (var entry : objectChildren.entrySet()) {
        copy.objectChildren.put(entry.getKey(), entry.getValue().copy());
      }
    }

    if (isList()) {
      for (ConfigNode child : listChildren) {
        copy.listChildren.add(child.copy());
      }
    }

    return copy;
  }

  /// Returns mutable object children for package collaborators.
  ///
  /// @return mutable object children
  Map<String, ConfigNode> mutableObjectChildren() {
    ensureObject();
    return objectChildren;
  }

  /// Returns mutable list children for package collaborators.
  ///
  /// @return mutable list children
  List<ConfigNode> mutableListChildren() {
    ensureList();
    return listChildren;
  }

  /// Replaces this node with a deep copy of another node.
  ///
  /// @param other source node
  void copyFrom(ConfigNode other) {
    become(other.kind);
    value = other.value;
    comment = other.comment;
    decorations = other.decorations;
    metadata.putAll(other.metadata);

    if (other.isObject()) {
      for (var entry : other.objectChildren.entrySet()) {
        objectChildren.put(entry.getKey(), entry.getValue().copy());
      }
    }

    if (other.isList()) {
      for (ConfigNode child : other.listChildren) {
        listChildren.add(child.copy());
      }
    }
  }

  private void ensureObject() {
    if (!isObject()) {
      become(ConfigValueKind.OBJECT);
    }
  }

  private void ensureList() {
    if (!isList()) {
      become(ConfigValueKind.LIST);
    }
  }

  private void become(ConfigValueKind nextKind) {
    kind = Objects.requireNonNull(nextKind, "nextKind");
    value = null;
    objectChildren = new LinkedHashMap<>();
    listChildren = new ArrayList<>();
    decorations = ConfigNodeDecorations.empty();
    metadata = new LinkedHashMap<>();
  }

  private static Object normalizeScalar(Object value) {
    return value;
  }
}
