package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Mutable node in a format-agnostic configuration tree.
 */
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

  public static ConfigNode object() {
    return new ConfigNode(ConfigValueKind.OBJECT);
  }

  public static ConfigNode list() {
    return new ConfigNode(ConfigValueKind.LIST);
  }

  public static ConfigNode scalar(Object value) {
    var node = new ConfigNode(ConfigValueKind.SCALAR);
    node.value = normalizeScalar(value);
    return node;
  }

  public static ConfigNode nullValue() {
    return new ConfigNode(ConfigValueKind.NULL);
  }

  public ConfigValueKind kind() {
    return kind;
  }

  public ConfigComment comment() {
    return comment;
  }

  public ConfigNode setComment(ConfigComment comment) {
    this.comment = Objects.requireNonNull(comment, "comment");
    return this;
  }

  public ConfigNodeDecorations decorations() {
    return decorations;
  }

  public ConfigNode setDecorations(ConfigNodeDecorations decorations) {
    this.decorations = Objects.requireNonNull(decorations, "decorations");
    return this;
  }

  public ConfigNode decorate(UnaryOperator<ConfigNodeDecorations> decorator) {
    decorations = Objects.requireNonNull(decorator, "decorator").apply(decorations);
    return this;
  }

  public boolean isObject() {
    return kind == ConfigValueKind.OBJECT;
  }

  public boolean isList() {
    return kind == ConfigValueKind.LIST;
  }

  public boolean isScalar() {
    return kind == ConfigValueKind.SCALAR;
  }

  public Map<String, ConfigNode> objectChildren() {
    if (!isObject()) {
      return Map.of();
    }

    return Collections.unmodifiableMap(new LinkedHashMap<>(objectChildren));
  }

  public List<ConfigNode> listChildren() {
    if (!isList()) {
      return List.of();
    }

    return List.copyOf(listChildren);
  }

  public Object rawValue() {
    return value;
  }

  public Map<String, Object> metadata() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
  }

  public Optional<Object> metadata(String key) {
    return Optional.ofNullable(metadata.get(Objects.requireNonNull(key, "key")));
  }

  public ConfigNode setMetadata(String key, Object value) {
    Objects.requireNonNull(key, "key");
    if (value == null) {
      metadata.remove(key);
    } else {
      metadata.put(key, value);
    }
    return this;
  }

  public Optional<String> asString() {
    if (kind == ConfigValueKind.NULL) {
      return Optional.empty();
    }

    return Optional.ofNullable(value).map(Object::toString);
  }

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

  public Optional<Integer> asInt() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.intValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Integer.parseInt(stringValue));
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  public Optional<Long> asLong() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.longValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Long.parseLong(stringValue));
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  public Optional<Double> asDouble() {
    if (value instanceof Number numberValue) {
      return Optional.of(numberValue.doubleValue());
    }

    if (value instanceof String stringValue) {
      try {
        return Optional.of(Double.parseDouble(stringValue));
      } catch (NumberFormatException ignored) {
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

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

  public ConfigNode getOrCreate(ConfigPath path) {
    Objects.requireNonNull(path, "path");
    ConfigNode current = this;

    for (String segment : path.segments()) {
      current.ensureObject();
      current = current.objectChildren.computeIfAbsent(segment, ignored -> ConfigNode.object());
    }

    return current;
  }

  public ConfigNode set(ConfigPath path, Object value) {
    return setNode(path, ConfigNode.scalar(value));
  }

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

  public ConfigNode addListValue(Object value) {
    ensureList();
    listChildren.add(ConfigNode.scalar(value));
    return this;
  }

  public ConfigNode addListNode(ConfigNode node) {
    ensureList();
    listChildren.add(Objects.requireNonNull(node, "node").copy());
    return this;
  }

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

  Map<String, ConfigNode> mutableObjectChildren() {
    ensureObject();
    return objectChildren;
  }

  List<ConfigNode> mutableListChildren() {
    ensureList();
    return listChildren;
  }

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
