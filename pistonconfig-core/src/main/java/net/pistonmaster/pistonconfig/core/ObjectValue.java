package net.pistonmaster.pistonconfig.core;

import java.util.Map;
import org.immutables.value.Value;

/// Immutable object value.
@PistonStyle
@Value.Immutable
public non-sealed interface ObjectValue extends ConfigValue {
  /// Returns named child values in source order.
  ///
  /// @return child values
  Map<String, ConfigValue> children();

  /// Creates an Immutables builder for object values.
  ///
  /// @return object value builder
  static ImmutableObjectValue.Builder builder() {
    return ImmutableObjectValue.builder();
  }

  /// Returns `ConfigValueKind.OBJECT`.
  ///
  /// @return object value kind
  @Override
  default ConfigValueKind kind() {
    return ConfigValueKind.OBJECT;
  }
}
