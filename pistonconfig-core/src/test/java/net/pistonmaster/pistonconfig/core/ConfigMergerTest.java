package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
  }

  @Test
  void updatesCommentsWithoutReplacingScalarValues() {
    var current = ConfigDocument.empty()
      .set("server.port", 25566);
    var defaults = ConfigDocument.empty()
      .set("server.port", 25565);
    defaults.root()
      .getOrCreate(ConfigPath.parse("server.port"))
      .setComment(ConfigComment.lines("Default port."));

    current.mergeDefaults(defaults, MergeOptions.conservative());

    var port = current.find("server.port").orElseThrow();
    assertEquals(25566, port.asInt().orElseThrow());
    assertEquals(List.of("Default port."), port.comment().leadingText());
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
    preserved.mergeDefaults(defaults, new MergeOptions(false, false, MergeListStrategy.PRESERVE_EXISTING));

    var replaced = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list().addListValue("custom"));
    replaced.mergeDefaults(defaults, new MergeOptions(false, false, MergeListStrategy.REPLACE));

    var appended = ConfigDocument.empty()
      .setNode(ConfigPath.of("modules"), ConfigNode.list().addListValue("custom"));
    appended.mergeDefaults(defaults, new MergeOptions(false, false, MergeListStrategy.APPEND_MISSING));

    assertIterableEquals(List.of("custom"), scalarList(preserved));
    assertIterableEquals(List.of("core", "yaml", "toml"), scalarList(replaced));
    assertIterableEquals(List.of("custom", "yaml", "toml"), scalarList(appended));
  }

  @Test
  void nullListStrategyFallsBackToPreserveExisting() {
    var options = new MergeOptions(false, false, null);

    assertEquals(MergeListStrategy.PRESERVE_EXISTING, options.listStrategy());
  }

  private static List<String> scalarList(ConfigDocument document) {
    return document.find("modules")
      .orElseThrow()
      .listChildren()
      .stream()
      .map(node -> node.asString().orElseThrow())
      .toList();
  }
}
