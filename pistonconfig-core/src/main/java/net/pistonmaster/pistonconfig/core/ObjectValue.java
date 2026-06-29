package net.pistonmaster.pistonconfig.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/// Immutable object value.
///
/// @param children named child values in source order
public record ObjectValue(Map<String, ConfigValue> children) implements ConfigValue {
  /// Normalizes child values to an immutable insertion-ordered map.
  public ObjectValue {
    children = Collections.unmodifiableMap(new LinkedHashMap<>(children));
  }

  /// Returns `ConfigValueKind.OBJECT`.
  ///
  /// @return object value kind
  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.OBJECT;
  }
}
