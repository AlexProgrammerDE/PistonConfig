package net.pistonmaster.pistonconfig.hocon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class HoconConfigLoaderTest {
  @Test
  void exposesFormatDescriptor() {
    var format = HoconConfigFormat.INSTANCE;

    assertEquals("hocon", format.name());
    assertTrue(format.extensions().contains("conf"));
    assertTrue(format.extensions().contains("hocon"));
    assertTrue(format.capabilities().ordering());
    assertNotNull(format.loader());
  }

  @Test
  void parsesObjectsListsCommentsAndOriginMetadata() {
    var document = new HoconConfigLoader().load(new StringReader("""
      server {
        # Host comment.
        host = "localhost"
        ports = [25565, 25566]
        enabled = true
      }
      """));

    var host = document.find("server.host").orElseThrow();
    var ports = document.find("server.ports").orElseThrow();

    assertEquals("localhost", host.asString().orElseThrow());
    assertEquals("Host comment.", host.comment().leadingText().getFirst());
    assertEquals(25565, ports.listChildren().getFirst().asInt().orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertTrue(host.decorations().valueLocation().isKnown());
    assertFalse(host.decorations().attributes().get(HoconMetadataKeys.ORIGIN_DESCRIPTION).isBlank());
    assertFalse(host.decorations().attributes().get(HoconMetadataKeys.RENDERED).isBlank());
  }

  @Test
  void savesNestedObjectsListsAndComments() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.host"), ConfigNode.scalar("localhost")
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Host comment."))
          .build()))
      .setNode(ConfigPath.parse("server.ports"), ConfigNode.list()
        .addListValue(25565)
        .addListValue(25566));

    var writer = new StringWriter();
    new HoconConfigLoader().save(document, writer);
    var hocon = writer.toString();

    assertTrue(hocon.contains("Host comment."));
    assertTrue(hocon.contains("localhost"));
    assertTrue(hocon.contains("25565"));
  }

  @Test
  void wrapsInvalidHoconAsConfigException() {
    assertThrows(ConfigException.class, () -> new HoconConfigLoader().load(new StringReader("server = [")));
  }

  private static ConfigCommentLine commentLine(String text) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(ConfigCommentType.BLOCK)
      .marker(ConfigCommentMarker.DOUBLE_SLASH)
      .build();
  }
}
