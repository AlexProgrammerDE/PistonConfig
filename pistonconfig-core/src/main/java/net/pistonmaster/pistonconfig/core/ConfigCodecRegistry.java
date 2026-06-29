package net.pistonmaster.pistonconfig.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for custom and built-in type codecs.
 */
public final class ConfigCodecRegistry {
  private final Map<Class<?>, ConfigCodec<?>> codecs = new LinkedHashMap<>();

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

  public <T> ConfigCodecRegistry register(Class<T> type, ConfigCodec<? extends T> codec) {
    codecs.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(codec, "codec"));
    return this;
  }

  public <T> ConfigNode encode(T value) {
    if (value == null) {
      return ConfigNode.nullValue();
    }

    @SuppressWarnings("unchecked")
    var valueType = (Class<T>) value.getClass();
    return codec(valueType).encode(value, this);
  }

  public <T> T decode(ConfigNode node, Class<T> type) {
    return codec(type).decode(node, this);
  }

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
