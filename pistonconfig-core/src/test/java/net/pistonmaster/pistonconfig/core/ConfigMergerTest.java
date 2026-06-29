package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
