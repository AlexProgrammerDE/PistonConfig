package net.pistonmaster.pistonconfig.core;

/**
 * Runtime exception used when a configuration document cannot be read, written, or mapped.
 */
public class ConfigException extends RuntimeException {
  public ConfigException(String message) {
    super(message);
  }

  public ConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
