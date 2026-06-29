package net.pistonmaster.pistonconfig.env;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ConfigValueKind;
import net.pistonmaster.pistonconfig.core.PistonStyle;
import org.immutables.value.Value;

/// Applies environment variable and system property overrides to a configuration
/// document.
///
/// Environment keys use underscore-separated names. System property keys use
/// dotted names. Overrides are coerced from the existing node type so typed
/// defaults can define the shape before process-level values are applied.
@PistonStyle
@Value.Immutable
public interface EnvironmentOverrides {
  /// Returns the environment variable prefix.
  ///
  /// @return environment variable prefix
  @Value.Default
  default String environmentPrefix() {
    return "";
  }

  /// Returns the system property prefix.
  ///
  /// @return system property prefix
  @Value.Default
  default String propertyPrefix() {
    return "";
  }

  /// Returns the environment variable source map.
  ///
  /// @return environment variables
  Map<String, String> environment();

  /// Returns the system property source map.
  ///
  /// @return system properties
  Map<String, String> properties();

  /// Returns whether environment variable names are matched case-sensitively.
  ///
  /// @return `true` for case-sensitive matching
  @Value.Default
  default boolean caseSensitiveEnvironment() {
    return false;
  }

  /// Returns whether overrides may create paths that are not already present.
  ///
  /// Keep this disabled for typed configs so accidental environment variables do
  /// not silently expand the schema.
  ///
  /// @return `true` when missing paths may be created
  @Value.Default
  default boolean allowNewPaths() {
    return false;
  }

  /// Creates an Immutables builder for environment overrides.
  ///
  /// @return environment override builder
  static ImmutableEnvironmentOverrides.Builder builder() {
    return ImmutableEnvironmentOverrides.builder();
  }

  /// Creates overrides from the current process environment and system properties.
  ///
  /// @param prefix shared prefix used for both environment variables and system properties
  /// @return process-backed overrides
  static EnvironmentOverrides system(String prefix) {
    var properties = new LinkedHashMap<String, String>();
    System.getProperties().forEach((key, value) -> properties.put(key.toString(), value.toString()));
    return builder()
      .environmentPrefix(prefix)
      .propertyPrefix(prefix)
      .putAllEnvironment(System.getenv())
      .putAllProperties(properties)
      .build();
  }

  /// Applies all matching overrides to a document in place.
  ///
  /// @param document target document
  /// @return the same document for chaining
  default ConfigDocument applyTo(ConfigDocument document) {
    Objects.requireNonNull(document, "document");

    var normalizedEnvironmentPrefix = normalizeEnvironmentPrefix(environmentPrefix(), caseSensitiveEnvironment());
    var normalizedPropertyPrefix = normalizePropertyPrefix(propertyPrefix());

    for (Map.Entry<String, String> entry : environment().entrySet()) {
      toEnvironmentPath(entry.getKey(), normalizedEnvironmentPrefix, caseSensitiveEnvironment())
        .ifPresent(path -> applyOverride(document, path, entry.getValue()));
    }

    for (Map.Entry<String, String> entry : properties().entrySet()) {
      toPropertyPath(entry.getKey(), normalizedPropertyPrefix)
        .ifPresent(path -> applyOverride(document, path, entry.getValue()));
    }

    return document;
  }

  private void applyOverride(ConfigDocument document, String path, String rawValue) {
    var existing = document.find(path);
    if (existing.isEmpty()) {
      if (allowNewPaths()) {
        document.set(path, rawValue);
      }
      return;
    }

    var replacement = coerce(existing.get(), rawValue);
    document.setNodePreservingSource(ConfigPath.parse(path), replacement);
  }

  private static ConfigNode coerce(ConfigNode existing, String rawValue) {
    if (existing.isObject() || existing.isList()) {
      throw new ConfigException("Environment overrides cannot replace object or list nodes.");
    }
    if (existing.kind() == ConfigValueKind.NULL) {
      throw new ConfigException("Environment overrides cannot infer a value type from null nodes.");
    }

    try {
      var current = existing.rawValue();
      if (current instanceof String) {
        return ConfigNode.scalar(rawValue);
      }
      if (current instanceof Boolean) {
        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
          return ConfigNode.scalar(Boolean.parseBoolean(rawValue));
        }
        throw new ConfigException("Environment override value " + rawValue + " is not a boolean.");
      }
      if (current instanceof Byte) {
        return ConfigNode.scalar(Byte.parseByte(rawValue));
      }
      if (current instanceof Short) {
        return ConfigNode.scalar(Short.parseShort(rawValue));
      }
      if (current instanceof Integer) {
        return ConfigNode.scalar(Integer.parseInt(rawValue));
      }
      if (current instanceof Long) {
        return ConfigNode.scalar(Long.parseLong(rawValue));
      }
      if (current instanceof java.math.BigInteger) {
        return ConfigNode.scalar(new java.math.BigInteger(rawValue));
      }
      if (current instanceof Float) {
        return ConfigNode.scalar(Float.parseFloat(rawValue));
      }
      if (current instanceof Double) {
        return ConfigNode.scalar(Double.parseDouble(rawValue));
      }
      if (current instanceof java.math.BigDecimal) {
        return ConfigNode.scalar(new java.math.BigDecimal(rawValue));
      }
      return ConfigNode.scalar(rawValue);
    } catch (NumberFormatException exception) {
      throw new ConfigException("Environment override value " + rawValue + " does not match the existing scalar type.", exception);
    }
  }

  private static Optional<String> toEnvironmentPath(String key, String environmentPrefix, boolean caseSensitive) {
    var normalized = caseSensitive ? key : key.toUpperCase(Locale.ROOT);
    if (!environmentPrefix.isEmpty()) {
      var prefix = environmentPrefix + "_";
      if (!normalized.startsWith(prefix)) {
        return Optional.empty();
      }
      normalized = normalized.substring(prefix.length());
    }

    if (normalized.isBlank()) {
      return Optional.empty();
    }

    var path = caseSensitive ? normalized : normalized.toLowerCase(Locale.ROOT);
    return Optional.of(path.replace('_', '.'));
  }

  private static Optional<String> toPropertyPath(String key, String propertyPrefix) {
    if (!propertyPrefix.isEmpty()) {
      var prefix = propertyPrefix + ".";
      if (!key.startsWith(prefix)) {
        return Optional.empty();
      }
      key = key.substring(prefix.length());
    }

    if (key.isBlank()) {
      return Optional.empty();
    }

    return Optional.of(key);
  }

  private static String normalizeEnvironmentPrefix(String prefix, boolean caseSensitive) {
    if (prefix == null || prefix.isBlank()) {
      return "";
    }

    var normalized = prefix.strip().replace('.', '_').replace('-', '_');
    return caseSensitive ? normalized : normalized.toUpperCase(Locale.ROOT);
  }

  private static String normalizePropertyPrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "";
    }

    return prefix.strip();
  }
}
