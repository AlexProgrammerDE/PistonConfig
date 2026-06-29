package net.pistonmaster.pistonconfig.migrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ImmutableConfigNodeDecorations;
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

  @Test
  void complexMigrationChainPreservesCopiedNodesAndPrunesLegacyBranches() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.endpoints.prod"), ConfigNode.object()
        .set(ConfigPath.of("host"), "prod.example.com")
        .set(ConfigPath.of("port"), 443)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Production endpoint."))
          .build())
        .setMetadata("source", "v0")
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withAttributes(Map.of("style", "table"))))
      .set("server.legacy.enabled", true);

    var registry = MigrationRegistry.builder()
      .versionPath(ConfigPath.parse("schema.version"))
      .addMigration(ConfigMigration.builder()
        .version(3)
        .action(config -> {
          Migrations.remove(config, "server.legacy");
          Migrations.setIfMissing(config, "server.enabled", true);
          Migrations.setIfMissing(config, "server.endpoints.production.port", 25565);
        })
        .build())
      .addMigration(ConfigMigration.builder()
        .version(1)
        .action(config -> Migrations.copy(config, "server.endpoints.prod", "server.endpoints.default"))
        .build())
      .addMigration(ConfigMigration.builder()
        .version(2)
        .action(config -> Migrations.rename(config, "server.endpoints.prod", "server.endpoints.production"))
        .build())
      .build();

    registry.migrate(document);
    document.set("server.endpoints.production.port", 8443);

    var copied = document.find("server.endpoints.default").orElseThrow();
    var renamed = document.find("server.endpoints.production").orElseThrow();
    assertEquals(3, document.find("schema.version").orElseThrow().asInt().orElseThrow());
    assertTrue(document.find("server.endpoints.prod").isEmpty());
    assertTrue(document.find("server.legacy").isEmpty());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("Production endpoint.", copied.comment().leadingText().getFirst());
    assertEquals("table", copied.decorations().attributes().get("style"));
    assertEquals("v0", copied.metadata("source").orElseThrow());
    assertEquals(443, copied.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals(8443, renamed.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow());
  }

  private static ConfigCommentLine commentLine(String text) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(ConfigCommentType.BLOCK)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }
}
