package net.pistonmaster.pistonconfig.core;

/// Immutable scalar value.
///
/// @param value scalar value
/// @param <T> scalar type
public record ScalarValue<T>(T value) implements ConfigValue {
  /// Returns `ConfigValueKind.SCALAR`.
  ///
  /// @return scalar value kind
  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.SCALAR;
  }
}
