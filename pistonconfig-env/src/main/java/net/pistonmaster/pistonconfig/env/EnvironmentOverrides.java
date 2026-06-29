package net.pistonmaster.pistonconfig.env;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.PistonStyle;
import org.immutables.value.Value;

/// Applies environment variable and system property overrides to a configuration
/// document.
///
/// Environment keys use uppercase underscore-separated names. System property
/// keys use dotted names. Both are written into a [ConfigDocument] as parsed
/// scalar values.
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

    var normalizedEnvironmentPrefix = normalizeEnvironmentPrefix(environmentPrefix());
    var normalizedPropertyPrefix = normalizePropertyPrefix(propertyPrefix());

    for (Map.Entry<String, String> entry : environment().entrySet()) {
      toEnvironmentPath(entry.getKey(), normalizedEnvironmentPrefix)
        .ifPresent(path -> document.set(path, parseScalar(entry.getValue())));
    }

    for (Map.Entry<String, String> entry : properties().entrySet()) {
      toPropertyPath(entry.getKey(), normalizedPropertyPrefix)
        .ifPresent(path -> document.set(path, parseScalar(entry.getValue())));
    }

    return document;
  }

  private static java.util.Optional<String> toEnvironmentPath(String key, String environmentPrefix) {
    var normalized = key.toUpperCase(Locale.ROOT);
    if (!environmentPrefix.isEmpty()) {
      var prefix = environmentPrefix + "_";
      if (!normalized.startsWith(prefix)) {
        return java.util.Optional.empty();
      }
      normalized = normalized.substring(prefix.length());
    }

    if (normalized.isBlank()) {
      return java.util.Optional.empty();
    }

    return java.util.Optional.of(normalized.toLowerCase(Locale.ROOT).replace('_', '.'));
  }

  private static java.util.Optional<String> toPropertyPath(String key, String propertyPrefix) {
    if (!propertyPrefix.isEmpty()) {
      var prefix = propertyPrefix + ".";
      if (!key.startsWith(prefix)) {
        return java.util.Optional.empty();
      }
      key = key.substring(prefix.length());
    }

    if (key.isBlank()) {
      return java.util.Optional.empty();
    }

    return java.util.Optional.of(key);
  }

  private static Object parseScalar(String raw) {
    if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
      return Boolean.parseBoolean(raw);
    }

    try {
      return Long.parseLong(raw);
    } catch (NumberFormatException ignored) {
      // Keep checking scalar types.
    }

    try {
      return Double.parseDouble(raw);
    } catch (NumberFormatException ignored) {
      return raw;
    }
  }

  private static String normalizeEnvironmentPrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "";
    }

    return prefix.strip().replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
  }

  private static String normalizePropertyPrefix(String prefix) {
    if (prefix == null || prefix.isBlank()) {
      return "";
    }

    return prefix.strip();
  }
}
