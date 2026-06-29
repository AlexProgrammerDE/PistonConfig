package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void representsRootPath() {
    var root = ConfigPath.root();

    assertTrue(root.isRoot());
    assertTrue(root.segments().isEmpty());
    assertEquals("", root.toString());
    assertTrue(root.parent().isEmpty());
    assertThrows(ConfigException.class, root::lastSegment);
  }

  @Test
  void buildsChildPathsAndEscapesSpecialCharacters() {
    var path = ConfigPath.of("database").child("primary.host").child("port\\tcp");

    assertFalse(path.isRoot());
    assertEquals("port\\tcp", path.lastSegment());
    assertEquals(ConfigPath.of("database", "primary.host"), path.parent().orElseThrow());
    assertEquals("database.primary\\.host.port\\\\tcp", path.toString());
    assertEquals(path, ConfigPath.parse(path.toString()));
  }

  @Test
  void preservesTrailingEscapeAsLiteralBackslash() {
    var path = ConfigPath.parse("server.path\\");

    assertEquals("path\\", path.lastSegment());
    assertEquals("server.path\\\\", path.toString());
  }
}
