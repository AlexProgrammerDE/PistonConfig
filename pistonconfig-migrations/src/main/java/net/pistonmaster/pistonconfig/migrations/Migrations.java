package net.pistonmaster.pistonconfig.migrations;

import java.util.function.Consumer;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/**
 * Helpers for common migration operations.
 */
public final class Migrations {
  private Migrations() {
  }

  public static ConfigMigration migration(int version, Consumer<ConfigDocument> action) {
    return new ConfigMigration() {
      @Override
      public int version() {
        return version;
      }

      @Override
      public void migrate(ConfigDocument document) {
        action.accept(document);
      }
    };
  }

  public static void rename(ConfigDocument document, String from, String to) {
    var source = ConfigPath.parse(from);
    document.root().remove(source).ifPresent(node -> document.setNode(ConfigPath.parse(to), node));
  }

  public static void setIfMissing(ConfigDocument document, String path, Object value) {
    var parsedPath = ConfigPath.parse(path);
    if (document.find(parsedPath).isEmpty()) {
      document.set(parsedPath, value);
    }
  }

  public static void remove(ConfigDocument document, String path) {
    document.root().remove(ConfigPath.parse(path));
  }

  public static void copy(ConfigDocument document, String from, String to) {
    document.find(ConfigPath.parse(from))
      .map(ConfigNode::copy)
      .ifPresent(node -> document.setNode(ConfigPath.parse(to), node));
  }
}
