package net.pistonmaster.pistonconfig.core;

/// Immutable null value.
public enum NullValue implements ConfigValue {
  /// Singleton null value.
  INSTANCE;

  /// Returns `ConfigValueKind.NULL`.
  ///
  /// @return null value kind
  @Override
  public ConfigValueKind kind() {
    return ConfigValueKind.NULL;
  }
}
