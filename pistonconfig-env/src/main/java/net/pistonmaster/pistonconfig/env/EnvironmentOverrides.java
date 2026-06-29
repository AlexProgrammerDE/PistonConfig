package net.pistonmaster.pistonconfig.env;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;

/**
 * Applies environment variable and system property overrides to a configuration document.
 */
public final class EnvironmentOverrides {
  private final String environmentPrefix;
  private final String propertyPrefix;
  private final Map<String, String> environment;
  private final Map<String, String> properties;

  private EnvironmentOverrides(
    String environmentPrefix,
    String propertyPrefix,
    Map<String, String> environment,
    Map<String, String> properties
  ) {
    this.environmentPrefix = normalizeEnvironmentPrefix(environmentPrefix);
    this.propertyPrefix = normalizePropertyPrefix(propertyPrefix);
    this.environment = Map.copyOf(environment);
    this.properties = Map.copyOf(properties);
  }

  public static EnvironmentOverrides system(String prefix) {
    var properties = new LinkedHashMap<String, String>();
    System.getProperties().forEach((key, value) -> properties.put(key.toString(), value.toString()));
    return new EnvironmentOverrides(prefix, prefix, System.getenv(), properties);
  }

  public static EnvironmentOverrides of(
    String environmentPrefix,
    String propertyPrefix,
    Map<String, String> environment,
    Map<String, String> properties
  ) {
    return new EnvironmentOverrides(
      environmentPrefix,
      propertyPrefix,
      Objects.requireNonNull(environment, "environment"),
      Objects.requireNonNull(properties, "properties")
    );
  }

  public ConfigDocument applyTo(ConfigDocument document) {
    Objects.requireNonNull(document, "document");

    for (Map.Entry<String, String> entry : environment.entrySet()) {
      toEnvironmentPath(entry.getKey()).ifPresent(path -> document.set(path, parseScalar(entry.getValue())));
    }

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      toPropertyPath(entry.getKey()).ifPresent(path -> document.set(path, parseScalar(entry.getValue())));
    }

    return document;
  }

  private java.util.Optional<String> toEnvironmentPath(String key) {
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

  private java.util.Optional<String> toPropertyPath(String key) {
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
