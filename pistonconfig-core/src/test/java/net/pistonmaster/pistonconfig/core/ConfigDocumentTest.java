package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ConfigDocumentTest {
  @Test
  void removeDeletesNodesExplicitly() {
    var document = ConfigDocument.empty()
      .set("server.host", "localhost")
      .set("server.port", 25565);

    var removed = document.remove("server.host").orElseThrow();

    assertEquals("localhost", removed.asString().orElseThrow());
    assertTrue(document.find("server.host").isEmpty());
    assertEquals(25565, document.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void settingNullCreatesNullNodeInsteadOfDeleting() {
    var document = ConfigDocument.empty()
      .set("server.host", "localhost")
      .set("server.host", null);

    var node = document.find("server.host").orElseThrow();

    assertEquals(ConfigValueKind.NULL, node.kind());
  }

  @Test
  void removingRootResetsDocumentToEmptyObject() {
    var document = ConfigDocument.empty()
      .set("server.port", 25565);

    var removed = document.remove(ConfigPath.root()).orElseThrow();

    assertEquals(25565, removed.find(ConfigPath.parse("server.port")).flatMap(ConfigNode::asInt).orElseThrow());
    assertTrue(document.root().isObject());
    assertTrue(document.root().objectChildren().isEmpty());
  }
}
