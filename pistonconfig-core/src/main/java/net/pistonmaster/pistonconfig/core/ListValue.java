package net.pistonmaster.pistonconfig.core;

import java.util.List;
import org.immutables.value.Value;

/// Immutable list value.
@PistonStyle
@Value.Immutable
public non-sealed interface ListValue extends ConfigValue {
  /// Returns ordered child values.
  ///
  /// @return child values
  List<ConfigValue> children();

  /// Creates an Immutables builder for list values.
  ///
  /// @return list value builder
  static ImmutableListValue.Builder builder() {
    return ImmutableListValue.builder();
  }

  /// Returns `ConfigValueKind.LIST`.
  ///
  /// @return list value kind
  @Override
  default ConfigValueKind kind() {
    return ConfigValueKind.LIST;
  }
}
