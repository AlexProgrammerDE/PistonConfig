package net.pistonmaster.pistonconfig.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class PropertiesConfigLoaderTest {
  @Test
  void preservesPropertyComments() {
    var loader = new PropertiesConfigLoader();
    var document = loader.load(new StringReader("""
      # host comment
      server.host=localhost
      """));

    assertEquals("host comment", document.find("server.host").orElseThrow().comment().leadingText().getFirst());

    var writer = new StringWriter();
    loader.save(document, writer);

    assertTrue(writer.toString().contains("host comment"));
  }

  @Test
  void exposesFormatDescriptor() {
    var format = PropertiesConfigFormat.INSTANCE;

    assertEquals("properties", format.name());
    assertTrue(format.extensions().contains("properties"));
    assertTrue(format.capabilities().blockComments());
    assertFalse(format.capabilities().lists());
    assertNotNull(format.loader());
  }

  @Test
  void parsesRepeatedKeysCommentsAndLayoutMetadata() {
    var loader = new PropertiesConfigLoader();
    var document = loader.load(new StringReader("""
      # File header.

      ! Port comment.
      server.port : 25565
      modules=core
      modules=yaml
      """));

    var port = document.find("server.port").orElseThrow();
    var modules = document.find("modules").orElseThrow();

    assertEquals("File header.", document.root().comment().leadingText().getFirst());
    assertEquals("Port comment.", port.comment().leadingText().getFirst());
    assertEquals(ConfigCommentMarker.EXCLAMATION, port.comment().leading().getFirst().marker());
    assertEquals(25565, port.asInt().orElseThrow());
    assertEquals(2, modules.listChildren().size());
    assertEquals("core", modules.listChildren().getFirst().asString().orElseThrow());
    assertEquals("yaml", modules.listChildren().get(1).asString().orElseThrow());
    assertTrue(port.decorations().attributes().containsKey(PropertiesMetadataKeys.SEPARATOR));
    assertEquals("true", port.decorations().attributes().get(PropertiesMetadataKeys.SINGLE_LINE));
  }

  @Test
  void savesRootCommentsListsPropertyCommentsAndSeparators() {
    var document = ConfigDocument.empty();
    document.root().setComment(ConfigComment.ofPlain(
      java.util.List.of("File header."),
      "",
      java.util.List.of("File footer.")
    ));
    document
      .setNode(ConfigPath.parse("server.port"), ConfigNode.scalar(25565)
        .setComment(ConfigComment.lines("Port comment."))
        .decorate(decorations -> decorations.withAttribute(PropertiesMetadataKeys.SEPARATOR, " : ")))
      .setNode(ConfigPath.of("modules"), ConfigNode.list()
        .addListValue("core")
        .addListValue("yaml"));

    var writer = new StringWriter();
    new PropertiesConfigLoader().save(document, writer);
    var properties = writer.toString();

    assertTrue(properties.contains("File header."));
    assertTrue(properties.contains("File footer."));
    assertTrue(properties.contains("Port comment."));
    assertTrue(properties.contains("server.port"));
    assertTrue(properties.contains("modules"));
    assertTrue(properties.contains("core"));
    assertTrue(properties.contains("yaml"));
  }

  @Test
  void wrapsInvalidPropertiesAsConfigException() {
    assertThrows(ConfigException.class, () -> new PropertiesConfigLoader().load(new ThrowingReader()));
  }

  private static final class ThrowingReader extends java.io.Reader {
    @Override
    public int read(char[] buffer, int offset, int length) throws java.io.IOException {
      throw new java.io.IOException("boom");
    }

    @Override
    public void close() {
    }
  }
}
