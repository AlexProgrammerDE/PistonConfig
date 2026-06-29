package net.pistonmaster.pistonconfig.migrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pistonmaster.pistonconfig.core.ConfigDocument;
import org.junit.jupiter.api.Test;

final class MigrationRegistryTest {
  @Test
  void appliesPendingMigrationsInOrder() {
    var document = ConfigDocument.empty()
      .set("old.enabled", true);

    var registry = MigrationRegistry.builder()
      .add(Migrations.migration(1, config -> Migrations.rename(config, "old.enabled", "new.enabled")))
      .add(Migrations.migration(2, config -> Migrations.setIfMissing(config, "new.mode", "auto")))
      .build();

    registry.migrate(document);

    assertTrue(document.find("new.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("auto", document.find("new.mode").orElseThrow().asString().orElseThrow());
    assertEquals(2, document.find("config.version").orElseThrow().asInt().orElseThrow());
  }
}
