package net.pistonmaster.pistonconfig.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable object value.
 */
public record ObjectValue(Map<String, ConfigValue> children) implements ConfigValue {
  public ObjectValue {
    children = Collections.unmodifiableMap(new LinkedHashMap<>(children));
  }

  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.OBJECT;
  }
}
