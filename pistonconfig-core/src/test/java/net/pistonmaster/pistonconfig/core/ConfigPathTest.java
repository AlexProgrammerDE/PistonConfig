package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class ConfigPathTest {
  @Test
  void parsesEscapedSegments() {
    var path = ConfigPath.parse("database.primary\\.host.port");

    assertEquals(3, path.segments().size());
    assertEquals("primary.host", path.segments().get(1));
    assertEquals("database.primary\\.host.port", path.toString());
  }

  @Test
  void rejectsEmptySegments() {
    assertThrows(ConfigException.class, () -> ConfigPath.parse("server..port"));
  }
}
