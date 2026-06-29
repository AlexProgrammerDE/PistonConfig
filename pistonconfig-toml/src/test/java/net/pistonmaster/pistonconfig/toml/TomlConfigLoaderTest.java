package net.pistonmaster.pistonconfig.toml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import net.pistonmaster.pistonconfig.core.ConfigCollectionStyle;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class TomlConfigLoaderTest {
  @Test
  void exposesFormatDescriptor() {
    var format = TomlConfigFormat.INSTANCE;

    assertEquals("toml", format.name());
    assertTrue(format.extensions().contains("toml"));
    assertTrue(format.capabilities().lists());
    assertNotNull(format.loader());
  }

  @Test
  void parsesTablesArraysAndComments() {
    var document = new TomlConfigLoader().load(new StringReader("""
      # Server table.
      [server]
      # Host comment.
      host = "localhost"
      ports = [25565, 25566]
      enabled = true
      """));

    var server = document.find("server").orElseThrow();
    var host = document.find("server.host").orElseThrow();
    var ports = document.find("server.ports").orElseThrow();

    assertEquals(ConfigCollectionStyle.TABLE, server.decorations().collectionStyle());
    assertEquals("Host comment.", host.comment().leadingText().getFirst());
    assertEquals("localhost", host.asString().orElseThrow());
    assertEquals(25565, ports.listChildren().getFirst().asInt().orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
  }

  @Test
  void savesNestedObjectsArraysAndComments() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.host"), ConfigNode.scalar("localhost")
        .setComment(ConfigComment.lines("Host comment.")))
      .setNode(ConfigPath.parse("server.ports"), ConfigNode.list()
        .addListValue(25565)
        .addListValue(25566));

    var writer = new StringWriter();
    new TomlConfigLoader().save(document, writer);
    var toml = writer.toString();

    assertTrue(toml.contains("Host comment."));
    assertTrue(toml.contains("host"));
    assertTrue(toml.contains("ports"));
    assertTrue(toml.contains("25565"));
  }

  @Test
  void wrapsInvalidTomlAsConfigException() {
    assertThrows(ConfigException.class, () -> new TomlConfigLoader().load(new StringReader("server = [")));
  }
}
