package net.pistonmaster.pistonconfig.core;

/**
 * Immutable scalar value.
 *
 * @param <T> scalar type
 */
public record ScalarValue<T>(T value) implements ConfigValue {
  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.SCALAR;
  }
}
