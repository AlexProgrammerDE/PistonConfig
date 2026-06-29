package net.pistonmaster.pistonconfig.core;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
    register(Byte.class, new ScalarCodec<>(node -> integer(node).byteValueExact()));
    register(byte.class, codec(Byte.class));
    register(Short.class, new ScalarCodec<>(node -> integer(node).shortValueExact()));
    register(short.class, codec(Short.class));
    register(Integer.class, new ScalarCodec<>(node -> node.asInt()
      .orElseThrow(() -> new ConfigException("Expected an integer value."))));
    register(int.class, codec(Integer.class));
    register(Long.class, new ScalarCodec<>(node -> node.asLong()
      .orElseThrow(() -> new ConfigException("Expected a long value."))));
    register(long.class, codec(Long.class));
    register(Double.class, new ScalarCodec<>(node -> node.asDouble()
      .orElseThrow(() -> new ConfigException("Expected a double value."))));
    register(double.class, codec(Double.class));
    register(Float.class, new ScalarCodec<>(node -> node.asDouble()
      .map(Double::floatValue)
      .orElseThrow(() -> new ConfigException("Expected a float value."))));
    register(float.class, codec(Float.class));
    register(Character.class, new ScalarCodec<>(node -> {
      var value = node.asString().orElseThrow(() -> new ConfigException("Expected a character value."));
      if (value.length() != 1) {
        throw new ConfigException("Expected a single character value.");
      }
      return value.charAt(0);
    }));
    register(char.class, codec(Character.class));
    register(BigInteger.class, new ScalarCodec<>(ConfigCodecRegistry::integer));
    register(BigDecimal.class, new ScalarCodec<>(ConfigCodecRegistry::decimal));
    register(LocalDate.class, stringValue(LocalDate::parse, "local date"));
    register(LocalTime.class, stringValue(LocalTime::parse, "local time"));
    register(LocalDateTime.class, stringValue(LocalDateTime::parse, "local date-time"));
    register(Instant.class, stringValue(Instant::parse, "instant"));
    register(Duration.class, stringValue(Duration::parse, "duration"));
    register(Period.class, stringValue(Period::parse, "period"));
    register(UUID.class, stringValue(UUID::fromString, "UUID"));
    register(File.class, stringValue(File::new, "file"));
    register(Path.class, stringValue(Path::of, "path"));
    register(URI.class, stringValue(URI::create, "URI"));
    register(URL.class, stringValue(value -> {
      try {
        return URI.create(value).toURL();
      } catch (MalformedURLException exception) {
        throw new ConfigException("Expected a URL value.", exception);
      }
    }, "URL"));
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

    if (value instanceof Enum<?> enumValue) {
      return ConfigNode.scalar(enumValue.name());
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
    Objects.requireNonNull(type, "type");
    if (type.isEnum()) {
      return decodeEnum(node, type);
    }
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

  private static BigInteger integer(ConfigNode node) {
    var value = node.rawValue();
    try {
      if (value instanceof BigInteger integer) {
        return integer;
      }
      if (value instanceof BigDecimal decimal) {
        return decimal.toBigIntegerExact();
      }
      if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
        return BigInteger.valueOf(((Number) value).longValue());
      }
      if (value instanceof Float || value instanceof Double) {
        return BigDecimal.valueOf(((Number) value).doubleValue()).toBigIntegerExact();
      }
      if (value instanceof String stringValue) {
        return new BigInteger(stringValue);
      }
    } catch (ArithmeticException | NumberFormatException exception) {
      throw new ConfigException("Expected an integer value.", exception);
    }

    throw new ConfigException("Expected an integer value.");
  }

  private static BigDecimal decimal(ConfigNode node) {
    var value = node.rawValue();
    try {
      if (value instanceof BigDecimal decimal) {
        return decimal;
      }
      if (value instanceof BigInteger integer) {
        return new BigDecimal(integer);
      }
      if (value instanceof Number number) {
        return BigDecimal.valueOf(number.doubleValue());
      }
      if (value instanceof String stringValue) {
        return new BigDecimal(stringValue);
      }
    } catch (NumberFormatException exception) {
      throw new ConfigException("Expected a decimal value.", exception);
    }

    throw new ConfigException("Expected a decimal value.");
  }

  private static <T> ConfigCodec<T> stringValue(StringParser<T> parser, String label) {
    return new ScalarCodec<>(node -> {
      var value = node.asString()
        .orElseThrow(() -> new ConfigException("Expected a " + label + " value."));
      try {
        return parser.parse(value);
      } catch (RuntimeException exception) {
        if (exception instanceof ConfigException configException) {
          throw configException;
        }
        throw new ConfigException("Expected a " + label + " value.", exception);
      }
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> T decodeEnum(ConfigNode node, Class<T> type) {
    var value = node.asString()
      .orElseThrow(() -> new ConfigException("Expected an enum value."));
    try {
      return (T) Enum.valueOf((Class<? extends Enum>) type, value);
    } catch (IllegalArgumentException exception) {
      throw new ConfigException("Unknown enum value " + value + " for " + type.getName() + ".", exception);
    }
  }

  @FunctionalInterface
  private interface StringParser<T> {
    T parse(String value);
  }
}
