package net.pistonmaster.pistonconfig.core;

import java.util.List;

/// Immutable list value.
///
/// @param children ordered child values
public record ListValue(List<ConfigValue> children) implements ConfigValue {
  /// Normalizes child values to an immutable list.
  public ListValue {
    children = List.copyOf(children);
  }

  /// Returns `ConfigValueKind.LIST`.
  ///
  /// @return list value kind
  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.LIST;
  }
}
