package net.pistonmaster.pistonconfig.core;

/// Common metadata keys shared by format modules.
public final class ConfigMetadataKeys {
  /// Stores the backend-specific raw value when it cannot be represented as a
  /// regular scalar without losing information.
  public static final String RAW_VALUE = "core.rawValue";

  private ConfigMetadataKeys() {
  }
}
