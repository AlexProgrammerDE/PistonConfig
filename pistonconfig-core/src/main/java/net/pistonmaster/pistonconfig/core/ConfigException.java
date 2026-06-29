package net.pistonmaster.pistonconfig.core;

/// Runtime exception used when a configuration document cannot be read, written,
/// merged, migrated, or mapped.
public class ConfigException extends RuntimeException {
  /// Creates an exception with a message.
  ///
  /// @param message failure message
  public ConfigException(String message) {
    super(message);
  }

  /// Creates an exception with a message and cause.
  ///
  /// @param message failure message
  /// @param cause original cause
  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
