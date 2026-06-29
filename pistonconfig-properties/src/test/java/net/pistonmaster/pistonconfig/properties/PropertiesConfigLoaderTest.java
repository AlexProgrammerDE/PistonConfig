package net.pistonmaster.pistonconfig.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ImmutableConfigNodeDecorations;
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
    document.root().setComment(ConfigComment.builder()
      .addLeading(commentLine("File header.", ConfigCommentMarker.HASH))
      .addTrailing(commentLine("File footer.", ConfigCommentMarker.HASH))
      .build());
    document
      .setNode(ConfigPath.parse("server.port"), ConfigNode.scalar(25565)
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Port comment.", ConfigCommentMarker.HASH))
          .build())
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withAttributes(Map.of(PropertiesMetadataKeys.SEPARATOR, " : "))))
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
  void roundTripsFlattenedLiteralDotKeysListsNullsAndLayoutAttributes() {
    var literalPath = ConfigPath.of("server").child("literal.key").child("value");
    var document = ConfigDocument.empty();
    document.root().setComment(ConfigComment.builder()
      .addLeading(commentLine("Header.", ConfigCommentMarker.HASH))
      .build());
    document
      .setNode(literalPath, ConfigNode.scalar("kept")
        .setComment(ConfigComment.builder()
          .addLeading(commentLine("Literal key comment.", ConfigCommentMarker.EXCLAMATION))
          .build())
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withAttributes(Map.of(
            PropertiesMetadataKeys.SEPARATOR, " : ",
            PropertiesMetadataKeys.BLANK_LINES_BEFORE, "2"
          ))))
      .setNode(ConfigPath.of("modules"), ConfigNode.list()
        .addListValue("core")
        .addListValue("yaml"))
      .setNode(ConfigPath.of("empty"), ConfigNode.nullValue());

    var writer = new StringWriter();
    new PropertiesConfigLoader().save(document, writer);
    var roundTripped = new PropertiesConfigLoader().load(new StringReader(writer.toString()));
    var literal = roundTripped.find(literalPath).orElseThrow();
    var modules = roundTripped.find("modules").orElseThrow().listChildren();

    assertEquals("Header.", roundTripped.root().comment().leadingText().getFirst());
    assertEquals("kept", literal.asString().orElseThrow());
    assertEquals("Literal key comment.", literal.comment().leadingText().getFirst());
    assertEquals(2, Integer.parseInt(literal.decorations().attributes().get(PropertiesMetadataKeys.BLANK_LINES_BEFORE)));
    assertEquals("core", modules.getFirst().asString().orElseThrow());
    assertEquals("yaml", modules.get(1).asString().orElseThrow());
    assertEquals("", roundTripped.find("empty").flatMap(ConfigNode::asString).orElseThrow());
  }

  @Test
  void wrapsInvalidPropertiesAsConfigException() {
    assertThrows(ConfigException.class, () -> new PropertiesConfigLoader().load(new ThrowingReader()));
  }

  private static ConfigCommentLine commentLine(String text, ConfigCommentMarker marker) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(ConfigCommentType.BLOCK)
      .marker(marker)
      .build();
  }

  private static final class ThrowingReader extends Reader {
    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
      throw new IOException("boom");
    }

    @Override
    public void close() {
    }
  }
}
