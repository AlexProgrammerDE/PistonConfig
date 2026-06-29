package net.pistonmaster.pistonconfig.migrations;

import net.pistonmaster.pistonconfig.core.ConfigDocument;

/**
 * One ordered migration that changes a configuration document in place.
 */
public interface ConfigMigration {
  int version();

  void migrate(ConfigDocument document);
}
