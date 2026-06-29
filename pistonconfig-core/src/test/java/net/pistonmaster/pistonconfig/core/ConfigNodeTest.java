package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class ConfigNodeTest {
  @Test
  void scalarAccessorsConvertCompatibleValues() {
    var integer = ConfigNode.scalar("42");
    var decimal = ConfigNode.scalar("3.5");
    var bool = ConfigNode.scalar("true");

    assertEquals(42, integer.asInt().orElseThrow());
    assertEquals(42L, integer.asLong().orElseThrow());
    assertEquals(3.5D, decimal.asDouble().orElseThrow());
    assertTrue(bool.asBoolean().orElseThrow());
    assertFalse(ConfigNode.scalar("not-a-number").asInt().isPresent());
  }

  @Test
  void objectAndListViewsAreDefensive() {
    var object = ConfigNode.object()
      .set(ConfigPath.of("server"), "localhost");
    var list = ConfigNode.list()
      .addListValue("first");

    assertThrows(UnsupportedOperationException.class, () -> object.objectChildren().put("other", ConfigNode.scalar("value")));
    assertThrows(UnsupportedOperationException.class, () -> list.listChildren().add(ConfigNode.scalar("second")));
  }

  @Test
  void setNodeCopiesReplacement() {
    var replacement = ConfigNode.scalar("initial")
      .setComment(ConfigComment.builder()
        .addLeading(commentLine("comment"))
        .build());
    var object = ConfigNode.object().setNode(ConfigPath.of("value"), replacement);

    replacement.set(ConfigPath.root(), "changed");

    var stored = object.find(ConfigPath.of("value")).orElseThrow();
    assertEquals("initial", stored.asString().orElseThrow());
    assertEquals("comment", stored.comment().leadingText().getFirst());
  }

  @Test
  void replacementOptionsCanPreserveExistingSourceAndMetadata() {
    var root = ConfigNode.object()
      .setNode(ConfigPath.of("value"), ConfigNode.scalar("old")
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("existing comment"))
          .build())
        .setDecorations(ConfigNodeDecorations.builder()
          .putAttribute("style", "existing")
          .build())
        .setMetadata("raw", "old"));

    root.setNode(ConfigPath.of("value"), ConfigNode.scalar("new")
      .setMetadata("raw", "new"), ConfigReplacementOptions.sourceAndMetadata());

    var value = root.find(ConfigPath.of("value")).orElseThrow();
    assertEquals("new", value.asString().orElseThrow());
    assertEquals(List.of("existing comment"), value.comment().leadingText());
    assertEquals("existing", value.decorations().attributes().get("style"));
    assertEquals("old", value.metadata("raw").orElseThrow());
  }

  @Test
  void preservingSourceDoesNotPreserveBackendMetadataByDefault() {
    var root = ConfigNode.object()
      .setNode(ConfigPath.of("value"), ConfigNode.scalar("old")
        .setMetadata("raw", "old"));

    root.setNodePreservingSource(ConfigPath.of("value"), ConfigNode.scalar("new")
      .setMetadata("raw", "new"));

    var value = root.find(ConfigPath.of("value")).orElseThrow();
    assertEquals("new", value.asString().orElseThrow());
    assertEquals("new", value.metadata("raw").orElseThrow());
  }

  @Test
  void removeChildAndRootNodes() {
    var root = ConfigNode.object()
      .set(ConfigPath.parse("server.host"), "localhost")
      .set(ConfigPath.parse("server.port"), 25565);

    var removed = root.remove(ConfigPath.parse("server.host")).orElseThrow();
    assertEquals("localhost", removed.asString().orElseThrow());
    assertTrue(root.find(ConfigPath.parse("server.host")).isEmpty());
    assertTrue(root.find(ConfigPath.parse("server.port")).isPresent());

    var previousRoot = root.remove(ConfigPath.root()).orElseThrow();
    assertTrue(previousRoot.find(ConfigPath.parse("server.port")).isPresent());
    assertTrue(root.isObject());
    assertTrue(root.objectChildren().isEmpty());
  }

  @Test
  void copyDoesNotShareMutableChildrenOrMetadata() {
    var original = ConfigNode.object()
      .set(ConfigPath.of("port"), 25565)
      .setMetadata("source", "default")
      .setDecorations(ConfigNodeDecorations.builder()
        .putAttribute("style", "block")
        .build());

    var copy = original.copy();
    copy.set(ConfigPath.of("port"), 25566);
    copy.setMetadata("source", "copy");
    copy.decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
      .withAttributes(Map.of("style", "flow")));

    assertEquals(25565, original.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals("default", original.metadata("source").orElseThrow());
    assertEquals("block", original.decorations().attributes().get("style"));
    assertEquals(25566, copy.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void metadataCanBeRemovedWithNullValue() {
    var node = ConfigNode.scalar("value")
      .setMetadata("key", "value");

    node.setMetadata("key", null);

    assertTrue(node.metadata("key").isEmpty());
    assertThrows(UnsupportedOperationException.class, () -> node.metadata().put("key", "new"));
  }

  @Test
  void nullValueHasNoStringRepresentation() {
    var node = ConfigNode.nullValue();

    assertTrue(node.asString().isEmpty());
    assertEquals(ConfigValueKind.NULL, node.kind());
    assertInstanceOf(Map.class, node.metadata());
  }

  private static ConfigCommentLine commentLine(String text) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(ConfigCommentType.BLOCK)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }
}
