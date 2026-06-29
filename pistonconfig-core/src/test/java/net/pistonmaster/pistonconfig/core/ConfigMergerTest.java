package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class ConfigMergerTest {
  @Test
  void mergesMissingDefaultsWithoutReplacingUserValues() {
    var current = ConfigDocument.empty()
      .set("server.port", 25566);
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565)
      .set("server.host", "0.0.0.0");

    current.mergeDefaults(defaults, MergeOptions.conservative());

    assertEquals(25566, current.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals("0.0.0.0", current.find("server.host").flatMap(ConfigNode::asString).orElseThrow());
  }

  @Test
  void canRemoveUnknownKeysForExactDefaultMode() {
    var current = ConfigDocument.empty()
      .set("server.port", 25566)
      .set("legacy.enabled", true);
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565);

    current.mergeDefaults(defaults, MergeOptions.exactDefaults());

    assertTrue(current.find("legacy.enabled").isEmpty());
    assertEquals(25565, current.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void conservativeMergeFillsMissingCommentsWithoutReplacingScalarValues() {
    var current = ConfigDocument.empty()
      .set("server.port", 25566);
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565);
    defaults.root()
      .getOrCreate(ConfigPath.parse("server.port"))
      .setComment(ConfigComment.builder()
        .addLeading(ConfigCommentLine.builder()
          .text("Default port.")
          .type(ConfigCommentType.BLOCK)
          .marker(ConfigCommentMarker.HASH)
          .build())
        .build());

    current.mergeDefaults(defaults, MergeOptions.conservative());

    var port = current.find("server.port").orElseThrow();
    assertEquals(25566, port.asInt().orElseThrow());
    assertEquals(List.of("Default port."), port.comment().leadingText());
  }

  @Test
  void conservativeMergeRepairsInvalidNodeShapes() {
    var current = ConfigDocument.empty()
      .set("server", "invalid");
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565);

    current.mergeDefaults(defaults, MergeOptions.conservative());

    assertEquals(25565, current.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void valueStrategyCanPreserveInvalidNodeShapes() {
    var current = ConfigDocument.empty()
      .set("server", "invalid");
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565);

    current.mergeDefaults(defaults, MergeOptions.builder()
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    assertEquals("invalid", current.find("server").flatMap(ConfigNode::asString).orElseThrow());
    assertTrue(current.find("server.port").isEmpty());
  }

  @Test
  void commentStrategyCanKeepFillOrReplaceComments() {
    var defaults = ConfigDocument.empty()
      .setNode(ConfigPath.of("port"), ConfigNode.scalar(25565)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Default port."))
          .addInline(commentLine("default inline", ConfigCommentType.INLINE))
          .build()));

    var kept = ConfigDocument.empty()
      .setNode(ConfigPath.of("port"), ConfigNode.scalar(25566)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("User port."))
          .build()));
    kept.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.KEEP_EXISTING)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    var filled = ConfigDocument.empty()
      .setNode(ConfigPath.of("port"), ConfigNode.scalar(25566)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("User port."))
          .build()));
    filled.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.FILL_MISSING)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    var replaced = ConfigDocument.empty()
      .setNode(ConfigPath.of("port"), ConfigNode.scalar(25566)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("User port."))
          .build()));
    replaced.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.REPLACE)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    assertEquals(List.of("User port."), kept.find("port").orElseThrow().comment().leadingText());
    assertEquals("", kept.find("port").orElseThrow().comment().inlineText());
    assertEquals(List.of("User port."), filled.find("port").orElseThrow().comment().leadingText());
    assertEquals("default inline", filled.find("port").orElseThrow().comment().inlineText());
    assertEquals(List.of("Default port."), replaced.find("port").orElseThrow().comment().leadingText());
    assertEquals("default inline", replaced.find("port").orElseThrow().comment().inlineText());
  }

  @Test
  void commentStrategyMergesPresentationDecorationsWithoutReplacingLocations() {
    var defaults = ConfigDocument.empty()
      .setNode(ConfigPath.of("name"), ConfigNode.scalar("default")
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withKeyComment(ConfigComment.builder()
            .addLeading(commentLine("Default key."))
            .build())
          .withScalarStyle(ConfigScalarStyle.SINGLE_QUOTED)
          .withAttributes(Map.of("default", "yes"))
          .withValueLocation(ConfigSourceLocation.builder()
            .description("defaults.yml")
            .line(10)
            .column(3)
            .build())));

    var current = ConfigDocument.empty()
      .setNode(ConfigPath.of("name"), ConfigNode.scalar("current")
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withValueLocation(ConfigSourceLocation.builder()
            .description("current.yml")
            .line(2)
            .column(1)
            .build())));

    current.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.FILL_MISSING)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    var decorations = current.find("name").orElseThrow().decorations();
    assertEquals(List.of("Default key."), decorations.keyComment().leadingText());
    assertEquals(ConfigScalarStyle.SINGLE_QUOTED, decorations.scalarStyle());
    assertEquals("yes", decorations.attributes().get("default"));
    assertEquals("current.yml", decorations.valueLocation().description());
  }

  @Test
  void canPreserveReplaceOrAppendListValues() {
    var defaults = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list()
        .addListValue("core")
        .addListValue("yaml")
        .addListValue("toml"));

    var preserved = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list().addListValue("custom"));
    preserved.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.KEEP_EXISTING)
      .listStrategy(MergeListStrategy.PRESERVE_EXISTING)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    var replaced = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list().addListValue("custom"));
    replaced.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.KEEP_EXISTING)
      .listStrategy(MergeListStrategy.REPLACE)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    var appended = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list().addListValue("custom"));
    appended.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.KEEP_EXISTING)
      .listStrategy(MergeListStrategy.APPEND_MISSING)
      .valueStrategy(MergeValueStrategy.PRESERVE_EXISTING)
      .build());

    assertIterableEquals(List.of("custom"), scalarList(preserved));
    assertIterableEquals(List.of("core", "yaml", "toml"), scalarList(replaced));
    assertIterableEquals(List.of("custom", "yaml", "toml"), scalarList(appended));
  }

  @Test
  void mergeCanRepairShapesDropUnknownsReplaceListsAndRefreshPresentationTogether() {
    var defaults = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.host"), ConfigNode.scalar("0.0.0.0")
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Default host."))
          .build())
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withScalarStyle(ConfigScalarStyle.DOUBLE_QUOTED)
          .withAttributes(Map.of("source", "defaults"))))
      .setNode(ConfigPath.parse("server.modules"), ConfigNode.list()
        .addListValue("core")
        .addListValue("yaml"))
      .setNode(ConfigPath.parse("server.database"), ConfigNode.object()
        .set(ConfigPath.of("host"), "db.internal")
        .set(ConfigPath.of("port"), 5432));
    var current = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.host"), ConfigNode.scalar("custom.example.com")
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("User host."))
          .build())
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withScalarStyle(ConfigScalarStyle.SINGLE_QUOTED)
          .withAttributes(Map.of("source", "current"))
          .withValueLocation(ConfigSourceLocation.builder()
            .description("current.yml")
            .line(4)
            .column(2)
            .build())))
      .setNode(ConfigPath.parse("server.modules"), ConfigNode.list()
        .addListValue("custom"))
      .set("server.database", "broken")
      .set("server.legacy", true)
      .set("debug", true);

    current.mergeDefaults(defaults, MergeOptions.builder()
      .commentStrategy(MergeCommentStrategy.REPLACE)
      .listStrategy(MergeListStrategy.REPLACE)
      .removeUnknown(true)
      .valueStrategy(MergeValueStrategy.REPLACE_INVALID)
      .build());

    var host = current.find("server.host").orElseThrow();
    assertEquals("custom.example.com", host.asString().orElseThrow());
    assertEquals(List.of("Default host."), host.comment().leadingText());
    assertEquals(ConfigScalarStyle.DOUBLE_QUOTED, host.decorations().scalarStyle());
    assertEquals("defaults", host.decorations().attributes().get("source"));
    assertEquals("current.yml", host.decorations().valueLocation().description());
    assertIterableEquals(List.of("core", "yaml"), scalarList(current, "server.modules"));
    assertEquals("db.internal", current.find("server.database.host").flatMap(ConfigNode::asString).orElseThrow());
    assertEquals(5432, current.find("server.database.port").flatMap(ConfigNode::asInt).orElseThrow());
    assertTrue(current.find("server.legacy").isEmpty());
    assertTrue(current.find("debug").isEmpty());
  }

  @Test
  void builderDefaultsAreConservative() {
    var options = MergeOptions.builder().build();

    assertEquals(MergeCommentStrategy.FILL_MISSING, options.commentStrategy());
    assertFalse(options.removeUnknown());
    assertEquals(MergeListStrategy.PRESERVE_EXISTING, options.listStrategy());
    assertEquals(MergeValueStrategy.REPLACE_INVALID, options.valueStrategy());
  }

  private static List<String> scalarList(ConfigDocument document) {
    return scalarList(document, "modules");
  }

  private static List<String> scalarList(ConfigDocument document, String path) {
    return document.find(path)
      .orElseThrow()
      .listChildren()
      .stream()
      .map(node -> node.asString().orElseThrow())
      .toList();
  }

  private static ConfigCommentLine commentLine(String text) {
    return commentLine(text, ConfigCommentType.BLOCK);
  }

  private static ConfigCommentLine commentLine(String text, ConfigCommentType type) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(type)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }
}
