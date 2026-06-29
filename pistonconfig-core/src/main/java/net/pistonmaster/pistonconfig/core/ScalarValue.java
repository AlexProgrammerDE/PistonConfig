package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Immutable scalar value.
///
/// @param <T> scalar type
@PistonStyle
@Value.Immutable
public non-sealed interface ScalarValue<T> extends ConfigValue {
  /// Returns the scalar value.
  ///
  /// @return scalar value
  T value();

  /// Creates an Immutables staged builder for scalar values.
  ///
  /// @param <T> scalar type
  /// @return scalar value builder
  static <T> ImmutableScalarValue.ValueBuildStage<T> builder() {
    return ImmutableScalarValue.builder();
  }

  /// Returns `ConfigValueKind.SCALAR`.
  ///
  /// @return scalar value kind
  @Override
  default ConfigValueKind kind() {
    return ConfigValueKind.SCALAR;
  }
}
