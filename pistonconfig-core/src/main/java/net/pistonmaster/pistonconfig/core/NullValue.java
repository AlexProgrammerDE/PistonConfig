package net.pistonmaster.pistonconfig.core;

/**
 * Immutable null value.
 */
public enum NullValue implements ConfigValue {
  INSTANCE;

  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.NULL;
  }
}
