package net.pistonmaster.pistonconfig.migrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class MigrationRegistryTest {
  @Test
  void appliesPendingMigrationsInOrder() {
    var document = ConfigDocument.empty()
      .set("old.enabled", true);

    var registry = MigrationRegistry.builder()
      .addMigration(ConfigMigration.builder()
        .version(1)
        .action(config -> Migrations.rename(config, "old.enabled", "new.enabled"))
        .build())
      .addMigration(ConfigMigration.builder()
        .version(2)
        .action(config -> Migrations.setIfMissing(config, "new.mode", "auto"))
        .build())
      .build();

    registry.migrate(document);

    assertTrue(document.find("new.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("auto", document.find("new.mode").orElseThrow().asString().orElseThrow());
    assertEquals(2, document.find("config.version").orElseThrow().asInt().orElseThrow());
  }

  @Test
  void skipsAlreadyAppliedMigrationsAndUsesCustomVersionPath() {
    var calls = new ArrayList<Integer>();
    var document = ConfigDocument.empty()
      .set("schema", 1)
      .set("old.value", "kept");

    var registry = MigrationRegistry.builder()
      .versionPath(ConfigPath.parse("schema"))
      .addMigration(ConfigMigration.builder()
        .version(1)
        .action(_ -> calls.add(1))
        .build())
      .addMigration(ConfigMigration.builder()
        .version(2)
        .action(config -> {
          calls.add(2);
          Migrations.rename(config, "old.value", "new.value");
        })
        .build())
      .build();

    registry.migrate(document);

    assertIterableEquals(List.of(2), calls);
    assertEquals(2, document.find("schema").orElseThrow().asInt().orElseThrow());
    assertEquals("kept", document.find("new.value").orElseThrow().asString().orElseThrow());
    assertTrue(document.find("old.value").isEmpty());
  }

  @Test
  void sortsMigrationsByVersionBeforeApplying() {
    var calls = new ArrayList<Integer>();

    MigrationRegistry.builder()
      .addMigration(ConfigMigration.builder()
        .version(3)
        .action(_ -> calls.add(3))
        .build())
      .addMigration(ConfigMigration.builder()
        .version(1)
        .action(_ -> calls.add(1))
        .build())
      .addMigration(ConfigMigration.builder()
        .version(2)
        .action(_ -> calls.add(2))
        .build())
      .build()
      .migrate(ConfigDocument.empty());

    assertIterableEquals(List.of(1, 2, 3), calls);
  }

  @Test
  void copyRemoveAndSetIfMissingOperateOnDocuments() {
    var document = ConfigDocument.empty()
      .set("server.host", "localhost")
      .set("server.port", 25565)
      .set("server.mode", "existing");

    Migrations.copy(document, "server.host", "network.host");
    Migrations.remove(document, "server.port");
    Migrations.setIfMissing(document, "server.mode", "default");
    Migrations.setIfMissing(document, "server.enabled", true);

    assertEquals("localhost", document.find("network.host").orElseThrow().asString().orElseThrow());
    assertTrue(document.find("server.port").isEmpty());
    assertEquals("existing", document.find("server.mode").orElseThrow().asString().orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
  }
}
