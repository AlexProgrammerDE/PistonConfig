package net.pistonmaster.pistonconfig.migrations;

import java.util.function.Consumer;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.PistonStyle;
import org.immutables.value.Value;

/// One ordered migration that changes a configuration document in place.
@PistonStyle
@Value.Immutable
public interface ConfigMigration {
  /// Returns the schema version produced by this migration.
  ///
  /// @return migration version
  int version();

  /// Returns the action that mutates the document.
  ///
  /// @return migration action
  Consumer<ConfigDocument> action();

  /// Creates an Immutables staged builder for migrations.
  ///
  /// @return migration builder
  static ImmutableConfigMigration.VersionBuildStage builder() {
    return ImmutableConfigMigration.builder();
  }

  /// Applies this migration to a document.
  ///
  /// @param document document to mutate
  default void migrate(ConfigDocument document) {
    action().accept(document);
  }
}
