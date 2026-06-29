package net.pistonmaster.pistonconfig.migrations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/**
 * Applies ordered configuration migrations and stores the resulting schema version in the document.
 */
public final class MigrationRegistry {
  private final ConfigPath versionPath;
  private final List<ConfigMigration> migrations;

  private MigrationRegistry(ConfigPath versionPath, List<ConfigMigration> migrations) {
    this.versionPath = Objects.requireNonNull(versionPath, "versionPath");
    this.migrations = migrations.stream()
      .sorted(Comparator.comparingInt(ConfigMigration::version))
      .toList();
  }

  public static MigrationRegistry of(ConfigPath versionPath, List<ConfigMigration> migrations) {
    return new MigrationRegistry(versionPath, Objects.requireNonNull(migrations, "migrations"));
  }

  public static Builder builder() {
    return new Builder();
  }

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

  public static final class Builder {
    private final List<ConfigMigration> migrations = new ArrayList<>();
    private ConfigPath versionPath = ConfigPath.parse("config.version");

    private Builder() {
    }

    public Builder versionPath(String versionPath) {
      this.versionPath = ConfigPath.parse(versionPath);
      return this;
    }

    public Builder add(ConfigMigration migration) {
      migrations.add(Objects.requireNonNull(migration, "migration"));
      return this;
    }

    public MigrationRegistry build() {
      return new MigrationRegistry(versionPath, migrations);
    }
  }
}
