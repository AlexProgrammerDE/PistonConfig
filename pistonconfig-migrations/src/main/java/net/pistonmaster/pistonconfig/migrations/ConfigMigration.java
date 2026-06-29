package net.pistonmaster.pistonconfig.migrations;

import net.pistonmaster.pistonconfig.core.ConfigDocument;

/// One ordered migration that changes a configuration document in place.
public interface ConfigMigration {
  /// Returns the schema version produced by this migration.
  ///
  /// @return migration version
  int version();

  /// Applies this migration to a document.
  ///
  /// @param document document to mutate
  void migrate(ConfigDocument document);
}
