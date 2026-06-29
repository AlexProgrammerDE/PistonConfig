package net.pistonmaster.pistonconfig.migrations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Applies ordered configuration migrations and stores the resulting schema
/// version in the document.
public final class MigrationRegistry {
  private final ConfigPath versionPath;
  private final List<ConfigMigration> migrations;

  private MigrationRegistry(ConfigPath versionPath, List<ConfigMigration> migrations) {
    this.versionPath = Objects.requireNonNull(versionPath, "versionPath");
    this.migrations = migrations.stream()
      .sorted(Comparator.comparingInt(ConfigMigration::version))
      .toList();
  }

  /// Creates a registry from an explicit version path and migration list.
  ///
  /// @param versionPath path that stores the current schema version
  /// @param migrations migrations to apply in version order
  /// @return migration registry
  public static MigrationRegistry of(ConfigPath versionPath, List<ConfigMigration> migrations) {
    return new MigrationRegistry(versionPath, Objects.requireNonNull(migrations, "migrations"));
  }

  /// Creates a builder for a migration registry.
  ///
  /// @return registry builder
  public static Builder builder() {
    return new Builder();
  }

  /// Applies every migration newer than the document's current version.
  ///
  /// @param document document to migrate
  /// @return the same document for chaining
  public ConfigDocument migrate(ConfigDocument document) {
    Objects.requireNonNull(document, "document");

    int currentVersion = document.find(versionPath)
      .flatMap(node -> node.asInt())
      .orElse(0);

    for (ConfigMigration migration : migrations) {
      if (migration.version() <= currentVersion) {
        continue;
      }

      migration.migrate(document);
      currentVersion = migration.version();
      document.set(versionPath, currentVersion);
    }

    return document;
  }

  /// Builder for [MigrationRegistry].
  public static final class Builder {
    private final List<ConfigMigration> migrations = new ArrayList<>();
    private ConfigPath versionPath = ConfigPath.parse("config.version");

    private Builder() {
    }

    /// Sets the path used to store the current schema version.
    ///
    /// @param versionPath dotted path for the schema version
    /// @return this builder
    public Builder versionPath(String versionPath) {
      this.versionPath = ConfigPath.parse(versionPath);
      return this;
    }

    /// Adds a migration.
    ///
    /// @param migration migration to register
    /// @return this builder
    public Builder add(ConfigMigration migration) {
      migrations.add(Objects.requireNonNull(migration, "migration"));
      return this;
    }

    /// Builds the migration registry.
    ///
    /// @return migration registry
    public MigrationRegistry build() {
      return new MigrationRegistry(versionPath, migrations);
    }
  }
}
