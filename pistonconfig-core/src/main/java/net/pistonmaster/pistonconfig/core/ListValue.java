package net.pistonmaster.pistonconfig.core;

import java.util.List;

/**
 * Immutable list value.
 */
public record ListValue(List<ConfigValue> children) implements ConfigValue {
  public ListValue {
    children = List.copyOf(children);
  }

  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.LIST;
  }
}
