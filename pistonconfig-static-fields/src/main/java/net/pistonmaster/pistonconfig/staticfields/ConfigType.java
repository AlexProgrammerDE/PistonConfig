package net.pistonmaster.pistonconfig.staticfields;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;

/// Runtime type token used by static config properties.
///
/// [ConfigType] keeps enough structure to encode and decode parameterized
/// values such as lists, maps, optionals, and arrays.
///
/// @param <T> Java value type
public sealed interface ConfigType<T> permits
  ConfigType.SimpleType,
  ConfigType.ListType,
  ConfigType.SetType,
  ConfigType.MapType,
  ConfigType.OptionalType,
  ConfigType.ArrayType {
  /// Encodes a Java value into a config node.
  ///
  /// @param value Java value
  /// @param registry codec registry for scalar and custom simple values
  /// @return encoded node
  ConfigNode encode(T value, ConfigCodecRegistry registry);

  /// Decodes a Java value from a config node.
  ///
  /// @param node source node
  /// @param registry codec registry for scalar and custom simple values
  /// @return decoded value
  T decode(ConfigNode node, ConfigCodecRegistry registry);

  /// Describes this type for diagnostics.
  ///
  /// @return diagnostic type name
  String describe();

  /// Returns enum constants if this type directly represents an enum.
  ///
  /// @return enum constant names
  default List<String> enumConstantNames() {
    return List.of();
  }

  /// Creates a simple type backed by [ConfigCodecRegistry].
  ///
  /// @param type Java type
  /// @param <T> Java value type
  /// @return config type
  static <T> ConfigType<T> of(Class<T> type) {
    return new SimpleType<>(type);
  }

  /// Creates a list type.
  ///
  /// @param elementType element type
  /// @param <E> element value type
  /// @return config type
  static <E> ConfigType<List<E>> listOf(ConfigType<E> elementType) {
    return new ListType<>(elementType);
  }

  /// Creates a set type.
  ///
  /// @param elementType element type
  /// @param <E> element value type
  /// @return config type
  static <E> ConfigType<Set<E>> setOf(ConfigType<E> elementType) {
    return new SetType<>(elementType);
  }

  /// Creates a map type.
  ///
  /// Map keys are stored as object keys. The key type must encode to a scalar
  /// string representation and be able to decode from that same string.
  ///
  /// @param keyType map key type
  /// @param valueType map value type
  /// @param <K> key value type
  /// @param <V> mapped value type
  /// @return config type
  static <K, V> ConfigType<Map<K, V>> mapOf(ConfigType<K> keyType, ConfigType<V> valueType) {
    return new MapType<>(keyType, valueType);
  }

  /// Creates an optional type.
  ///
  /// Empty optionals are encoded as config null values.
  ///
  /// @param valueType wrapped value type
  /// @param <E> wrapped value type
  /// @return config type
  static <E> ConfigType<Optional<E>> optionalOf(ConfigType<E> valueType) {
    return new OptionalType<>(valueType);
  }

  /// Creates an object array type.
  ///
  /// @param elementType element type
  /// @param arrayFactory creates arrays of the requested size
  /// @param <E> element value type
  /// @return config type
  static <E> ConfigType<E[]> arrayOf(ConfigType<E> elementType, IntFunction<E[]> arrayFactory) {
    return new ArrayType<>(elementType, arrayFactory);
  }

  /// Simple codec-backed value type.
  ///
  /// @param type Java type
  /// @param <T> Java value type
  record SimpleType<T>(Class<T> type) implements ConfigType<T> {
    /// Creates a simple codec-backed value type.
    ///
    /// @param type Java type
    public SimpleType {
      Objects.requireNonNull(type, "type");
    }

    @Override
    public ConfigNode encode(T value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(registry, "registry");
      return registry.encode(value);
    }

    @Override
    public T decode(ConfigNode node, ConfigCodecRegistry registry) {
      Objects.requireNonNull(node, "node");
      Objects.requireNonNull(registry, "registry");
      return registry.decode(node, type);
    }

    @Override
    public String describe() {
      return type.getTypeName();
    }

    @Override
    public List<String> enumConstantNames() {
      if (!type.isEnum()) {
        return List.of();
      }

      var constants = type.getEnumConstants();
      var names = new ArrayList<String>(constants.length);
      for (T constant : constants) {
        names.add(((Enum<?>) constant).name());
      }
      return List.copyOf(names);
    }
  }

  /// List value type.
  ///
  /// @param elementType element type
  /// @param <E> element value type
  record ListType<E>(ConfigType<E> elementType) implements ConfigType<List<E>> {
    /// Creates a list value type.
    ///
    /// @param elementType element type
    public ListType {
      Objects.requireNonNull(elementType, "elementType");
    }

    @Override
    public ConfigNode encode(List<E> value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(value, "value");
      var node = ConfigNode.list();
      for (E element : value) {
        node.addListNode(elementType.encode(element, registry));
      }
      return node;
    }

    @Override
    public List<E> decode(ConfigNode node, ConfigCodecRegistry registry) {
      requireList(node, describe());
      var values = new ArrayList<E>();
      for (ConfigNode child : node.listChildren()) {
        values.add(elementType.decode(child, registry));
      }
      return Collections.unmodifiableList(values);
    }

    @Override
    public String describe() {
      return "List<" + elementType.describe() + ">";
    }
  }

  /// Set value type.
  ///
  /// @param elementType element type
  /// @param <E> element value type
  record SetType<E>(ConfigType<E> elementType) implements ConfigType<Set<E>> {
    /// Creates a set value type.
    ///
    /// @param elementType element type
    public SetType {
      Objects.requireNonNull(elementType, "elementType");
    }

    @Override
    public ConfigNode encode(Set<E> value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(value, "value");
      var node = ConfigNode.list();
      for (E element : value) {
        node.addListNode(elementType.encode(element, registry));
      }
      return node;
    }

    @Override
    public Set<E> decode(ConfigNode node, ConfigCodecRegistry registry) {
      requireList(node, describe());
      var values = new LinkedHashSet<E>();
      for (ConfigNode child : node.listChildren()) {
        values.add(elementType.decode(child, registry));
      }
      return Collections.unmodifiableSet(values);
    }

    @Override
    public String describe() {
      return "Set<" + elementType.describe() + ">";
    }
  }

  /// Map value type.
  ///
  /// @param keyType key type
  /// @param valueType value type
  /// @param <K> key value type
  /// @param <V> mapped value type
  record MapType<K, V>(ConfigType<K> keyType, ConfigType<V> valueType) implements ConfigType<Map<K, V>> {
    /// Creates a map value type.
    ///
    /// @param keyType key type
    /// @param valueType value type
    public MapType {
      Objects.requireNonNull(keyType, "keyType");
      Objects.requireNonNull(valueType, "valueType");
    }

    @Override
    public ConfigNode encode(Map<K, V> value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(value, "value");
      var node = ConfigNode.object();
      for (var entry : value.entrySet()) {
        var key = encodeKey(entry.getKey(), registry);
        node.setNode(net.pistonmaster.pistonconfig.core.ConfigPath.of(key), valueType.encode(entry.getValue(), registry));
      }
      return node;
    }

    @Override
    public Map<K, V> decode(ConfigNode node, ConfigCodecRegistry registry) {
      if (!node.isObject()) {
        throw new ConfigException("Expected " + describe() + " object value.");
      }

      var values = new LinkedHashMap<K, V>();
      for (var entry : node.objectChildren().entrySet()) {
        var key = keyType.decode(ConfigNode.scalar(entry.getKey()), registry);
        var value = valueType.decode(entry.getValue(), registry);
        values.put(key, value);
      }
      return Collections.unmodifiableMap(values);
    }

    @Override
    public String describe() {
      return "Map<" + keyType.describe() + ", " + valueType.describe() + ">";
    }

    private String encodeKey(K key, ConfigCodecRegistry registry) {
      var keyNode = keyType.encode(key, registry);
      return keyNode.asString()
        .orElseThrow(() -> new ConfigException("Map key type " + keyType.describe() + " must encode to a scalar string."));
    }
  }

  /// Optional value type.
  ///
  /// @param valueType wrapped value type
  /// @param <E> wrapped value type
  record OptionalType<E>(ConfigType<E> valueType) implements ConfigType<Optional<E>> {
    /// Creates an optional value type.
    ///
    /// @param valueType wrapped value type
    public OptionalType {
      Objects.requireNonNull(valueType, "valueType");
    }

    @Override
    public ConfigNode encode(Optional<E> value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(value, "value");
      return value.map(element -> valueType.encode(element, registry)).orElseGet(ConfigNode::nullValue);
    }

    @Override
    public Optional<E> decode(ConfigNode node, ConfigCodecRegistry registry) {
      Objects.requireNonNull(node, "node");
      if (node.kind() == net.pistonmaster.pistonconfig.core.ConfigValueKind.NULL) {
        return Optional.empty();
      }
      return Optional.of(valueType.decode(node, registry));
    }

    @Override
    public String describe() {
      return "Optional<" + valueType.describe() + ">";
    }

    @Override
    public List<String> enumConstantNames() {
      return valueType.enumConstantNames();
    }
  }

  /// Object array value type.
  ///
  /// @param elementType element type
  /// @param arrayFactory creates arrays of the requested size
  /// @param <E> element value type
  record ArrayType<E>(ConfigType<E> elementType, IntFunction<E[]> arrayFactory) implements ConfigType<E[]> {
    /// Creates an object array value type.
    ///
    /// @param elementType element type
    /// @param arrayFactory creates arrays of the requested size
    public ArrayType {
      Objects.requireNonNull(elementType, "elementType");
      Objects.requireNonNull(arrayFactory, "arrayFactory");
    }

    @Override
    public ConfigNode encode(E[] value, ConfigCodecRegistry registry) {
      Objects.requireNonNull(value, "value");
      var node = ConfigNode.list();
      for (E element : value) {
        node.addListNode(elementType.encode(element, registry));
      }
      return node;
    }

    @Override
    public E[] decode(ConfigNode node, ConfigCodecRegistry registry) {
      requireList(node, describe());
      var array = arrayFactory.apply(node.listChildren().size());
      for (int index = 0; index < node.listChildren().size(); index++) {
        Array.set(array, index, elementType.decode(node.listChildren().get(index), registry));
      }
      return array;
    }

    @Override
    public String describe() {
      return elementType.describe() + "[]";
    }
  }

  private static void requireList(ConfigNode node, String description) {
    Objects.requireNonNull(node, "node");
    if (!node.isList()) {
      throw new ConfigException("Expected " + description + " list value.");
    }
  }
}
