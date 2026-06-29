package net.pistonmaster.pistonconfig.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/// Registry for custom and built-in type codecs.
///
/// The registry starts with scalar codecs for common Java primitives and wrapper
/// types. Applications can register additional codecs for records, value
/// objects, or domain types.
public final class ConfigCodecRegistry {
  private final Map<Class<?>, ConfigCodec<?>> codecs = new LinkedHashMap<>();

  /// Creates a registry with the built-in scalar codecs installed.
  public ConfigCodecRegistry() {
    register(String.class, new ScalarCodec<>(node -> node.asString()
      .orElseThrow(() -> new ConfigException("Expected a string value."))));
    register(Boolean.class, new ScalarCodec<>(node -> node.asBoolean()
      .orElseThrow(() -> new ConfigException("Expected a boolean value."))));
    register(boolean.class, codec(Boolean.class));
    register(Integer.class, new ScalarCodec<>(node -> node.asInt()
      .orElseThrow(() -> new ConfigException("Expected an integer value."))));
    register(int.class, codec(Integer.class));
    register(Long.class, new ScalarCodec<>(node -> node.asLong()
      .orElseThrow(() -> new ConfigException("Expected a long value."))));
    register(long.class, codec(Long.class));
    register(Double.class, new ScalarCodec<>(node -> node.asDouble()
      .orElseThrow(() -> new ConfigException("Expected a double value."))));
    register(double.class, codec(Double.class));
  }

  /// Registers or replaces the codec for a Java type.
  ///
  /// @param type Java type handled by the codec
  /// @param codec codec implementation
  /// @param <T> Java type handled by the codec
  /// @return this registry
  public <T> ConfigCodecRegistry register(Class<T> type, ConfigCodec<? extends T> codec) {
    codecs.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(codec, "codec"));
    return this;
  }

  /// Encodes a Java value using the codec registered for its runtime class.
  ///
  /// `null` values are encoded as `ConfigNode.nullValue()`.
  ///
  /// @param value value to encode
  /// @param <T> Java value type
  /// @return encoded node
  public <T> ConfigNode encode(T value) {
    if (value == null) {
      return ConfigNode.nullValue();
    }

    @SuppressWarnings("unchecked")
    var valueType = (Class<T>) value.getClass();
    return codec(valueType).encode(value, this);
  }

  /// Decodes a configuration node into the requested Java type.
  ///
  /// @param node source node
  /// @param type Java type to decode
  /// @param <T> Java type to decode
  /// @return decoded value
  public <T> T decode(ConfigNode node, Class<T> type) {
    return codec(type).decode(node, this);
  }

  /// Looks up the codec for a Java type.
  ///
  /// @param type Java type to resolve
  /// @param <T> Java type handled by the codec
  /// @return registered codec
  /// @throws ConfigException if no codec is registered for `type`
  @SuppressWarnings("unchecked")
  public <T> ConfigCodec<T> codec(Class<T> type) {
    var codec = codecs.get(Objects.requireNonNull(type, "type"));
    if (codec == null) {
      throw new ConfigException("No configuration codec registered for " + type.getName() + ".");
    }

    return (ConfigCodec<T>) codec;
  }

  private record ScalarCodec<T>(ScalarDecoder<T> decoder) implements ConfigCodec<T> {
    @Override
    public ConfigNode encode(T value, ConfigCodecRegistry registry) {
      return ConfigNode.scalar(value);
    }

    @Override
    public T decode(ConfigNode node, ConfigCodecRegistry registry) {
      return decoder.decode(node);
    }
  }

  @FunctionalInterface
  private interface ScalarDecoder<T> {
    T decode(ConfigNode node);
  }
}
