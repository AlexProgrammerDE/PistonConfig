package net.pistonmaster.pistonconfig.migrations;

import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Helpers for common migration operations.
public final class Migrations {
  private Migrations() {
  }

  /// Renames a path when the source path exists.
  ///
  /// @param document document to mutate
  /// @param from source dotted path
  /// @param to target dotted path
  public static void rename(ConfigDocument document, String from, String to) {
    var source = ConfigPath.parse(from);
    document.remove(source).ifPresent(node -> document.setNode(ConfigPath.parse(to), node));
  }

  /// Sets a value only when the path is currently missing.
  ///
  /// @param document document to mutate
  /// @param path dotted path to write
  /// @param value value to set
  public static void setIfMissing(ConfigDocument document, String path, Object value) {
    var parsedPath = ConfigPath.parse(path);
    if (document.find(parsedPath).isEmpty()) {
      document.set(parsedPath, value);
    }
  }

  /// Removes a path when it exists.
  ///
  /// @param document document to mutate
  /// @param path dotted path to remove
  public static void remove(ConfigDocument document, String path) {
    document.remove(path);
  }

  /// Copies a node from one path to another when the source path exists.
  ///
  /// @param document document to mutate
  /// @param from source dotted path
  /// @param to target dotted path
  public static void copy(ConfigDocument document, String from, String to) {
    document.find(ConfigPath.parse(from))
      .map(ConfigNode::copy)
      .ifPresent(node -> document.setNode(ConfigPath.parse(to), node));
  }
}
