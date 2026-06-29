package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;

/// Result of resolving a static config property from a document.
///
/// @param property property declaration
/// @param value resolved value
/// @param sourcePresent whether a source node existed at the property path
/// @param validSource whether the source node was valid for the property type
/// @param diagnostic diagnostic message when the source was invalid
/// @param <T> value type
public record StaticConfigValue<T>(
  ConfigProperty<T> property,
  T value,
  boolean sourcePresent,
  boolean validSource,
  String diagnostic
) {
  /// Creates a valid source result.
  ///
  /// @param property property declaration
  /// @param value decoded source value
  /// @param <T> value type
  /// @return result
  public static <T> StaticConfigValue<T> valid(ConfigProperty<T> property, T value) {
    return new StaticConfigValue<>(property, value, true, true, "");
  }

  /// Creates a missing source result using the property default.
  ///
  /// @param property property declaration
  /// @param <T> value type
  /// @return result
  public static <T> StaticConfigValue<T> missing(ConfigProperty<T> property) {
    return new StaticConfigValue<>(property, property.defaultValue(), false, false, "Missing value.");
  }

  /// Creates an invalid source result using the property default.
  ///
  /// @param property property declaration
  /// @param diagnostic diagnostic message
  /// @param <T> value type
  /// @return result
  public static <T> StaticConfigValue<T> invalid(ConfigProperty<T> property, String diagnostic) {
    return new StaticConfigValue<>(property, property.defaultValue(), true, false, diagnostic == null ? "Invalid value." : diagnostic);
  }

  /// Creates a result.
  public StaticConfigValue {
    Objects.requireNonNull(property, "property");
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(diagnostic, "diagnostic");
  }

  /// Returns whether the stored representation should be rewritten.
  ///
  /// @return `true` when the source was missing or invalid
  public boolean requiresRewrite() {
    return !validSource;
  }
}
