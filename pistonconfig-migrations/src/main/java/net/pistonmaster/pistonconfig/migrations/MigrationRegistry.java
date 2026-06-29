package net.pistonmaster.pistonconfig.migrations;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.PistonStyle;
import org.immutables.value.Value;

/// Applies ordered configuration migrations and stores the resulting schema
/// version in the document.
@PistonStyle
@Value.Immutable
public interface MigrationRegistry {
  /// Returns the path that stores the current schema version.
  ///
  /// @return schema version path
  @Value.Default
  default ConfigPath versionPath() {
    return ConfigPath.parse("config.version");
  }

  /// Returns migrations to apply.
  ///
  /// @return configured migrations
  List<ConfigMigration> migrations();

  /// Creates an Immutables builder for migration registries.
  ///
  /// @return migration registry builder
  static ImmutableMigrationRegistry.Builder builder() {
    return ImmutableMigrationRegistry.builder();
  }

  /// Applies every migration newer than the document's current version.
  ///
  /// @param document document to migrate
  /// @return the same document for chaining
  default ConfigDocument migrate(ConfigDocument document) {
    Objects.requireNonNull(document, "document");

    int currentVersion = document.find(versionPath())
      .flatMap(node -> node.asInt())
      .orElse(0);

    for (ConfigMigration migration : migrations().stream()
      .sorted(Comparator.comparingInt(ConfigMigration::version))
      .toList()) {
      if (migration.version() <= currentVersion) {
        continue;
      }

      migration.migrate(document);
      currentVersion = migration.version();
      document.set(versionPath(), currentVersion);
    }

    return document;
  }
}
