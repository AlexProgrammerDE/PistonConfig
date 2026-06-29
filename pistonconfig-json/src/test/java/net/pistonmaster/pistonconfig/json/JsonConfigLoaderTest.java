package net.pistonmaster.pistonconfig.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import net.pistonmaster.pistonconfig.core.ConfigScalarStyle;
import net.pistonmaster.pistonconfig.core.ConfigValueKind;
import org.junit.jupiter.api.Test;

final class JsonConfigLoaderTest {
  @Test
  void exposesFormatDescriptor() {
    var format = JsonConfigFormat.INSTANCE;

    assertEquals("json", format.name());
    assertTrue(format.extensions().contains("json"));
    assertTrue(format.extensions().contains("jsonc"));
    assertTrue(format.extensions().contains("json5"));
    assertTrue(format.capabilities().typedScalars());
    assertNotNull(format.loader());
  }

  @Test
  void parsesJson5ObjectsListsCommentsAndNumberRadix() {
    var document = new JsonConfigLoader().load(new StringReader("""
      {
        server: {
          // Port comment
          port: 0x10,
          enabled: true,
          modules: ["core", null, 2],
        },
      }
      """));

    var port = document.find("server.port").orElseThrow();
    var modules = document.find("server.modules").orElseThrow();

    assertEquals(16, port.asInt().orElseThrow());
    assertEquals(ConfigScalarStyle.HEX, port.decorations().scalarStyle());
    assertEquals(16, port.metadata(JsonMetadataKeys.NUMBER_RADIX).orElseThrow());
    assertEquals("Port comment", port.comment().leadingText().getFirst());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("core", modules.listChildren().getFirst().asString().orElseThrow());
    assertEquals(ConfigValueKind.NULL, modules.listChildren().get(1).kind());
    assertEquals(2, modules.listChildren().get(2).asInt().orElseThrow());
  }

  @Test
  void savesCommentsRadixAndScalarTypes() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.of("answer"), ConfigNode.scalar(16)
        .setMetadata(JsonMetadataKeys.NUMBER_RADIX, 16)
        .setComment(ConfigComment.builder()
          .addLeading(ConfigCommentLine.builder()
            .text("Hex value.")
            .type(ConfigCommentType.BLOCK)
            .marker(ConfigCommentMarker.DOUBLE_SLASH)
            .build())
          .build()))
      .setNode(ConfigPath.of("enabled"), ConfigNode.scalar(true))
      .setNode(ConfigPath.of("nothing"), ConfigNode.nullValue());

    var writer = new StringWriter();
    new JsonConfigLoader().save(document, writer);
    var json = writer.toString();

    assertTrue(json.contains("Hex value."));
    assertTrue(json.contains("0x10"));
    assertTrue(json.contains("true"));
    assertTrue(json.contains("null"));
  }

  @Test
  void wrapsInvalidJsonAsConfigException() {
    assertThrows(ConfigException.class, () -> new JsonConfigLoader().load(new StringReader("{ broken: [ }")));
  }

  @Test
  void convertsUnsupportedScalarsToStringsWhenSaving() {
    var document = ConfigDocument.empty()
      .set("character", 'x');

    var writer = new StringWriter();
    new JsonConfigLoader().save(document, writer);

    assertInstanceOf(String.class, new JsonConfigLoader()
      .load(new StringReader(writer.toString()))
      .find("character")
      .orElseThrow()
      .rawValue());
  }

  @Test
  void roundTripsMixedJson5StructuresWithLiteralKeysCommentsNullsAndRadices() {
    var loader = new JsonConfigLoader();
    var document = loader.load(new StringReader("""
      {
        server: {
          // Route table.
          routes: [
            { name: "dev", limits: [0b1010, 0o12, 0x10], enabled: true },
            null,
            { name: "prod", limits: [], enabled: false },
          ],
          "literal.key": {
            value: "kept",
          },
        },
      }
      """));
    document.setNode(ConfigPath.parse("server.maxConnections"), ConfigNode.scalar(255)
      .setMetadata(JsonMetadataKeys.NUMBER_RADIX, 16)
      .setComment(ConfigComment.builder()
        .addLeading(ConfigCommentLine.builder()
          .text("Maximum connections.")
          .type(ConfigCommentType.BLOCK)
          .marker(ConfigCommentMarker.DOUBLE_SLASH)
          .build())
        .build()));

    var writer = new StringWriter();
    loader.save(document, writer);
    var roundTripped = loader.load(new StringReader(writer.toString()));
    var routes = roundTripped.find("server.routes").orElseThrow().listChildren();
    var limits = routes.getFirst().find(ConfigPath.of("limits")).orElseThrow().listChildren();

    assertEquals("Route table.", roundTripped.find("server.routes").orElseThrow().comment().leadingText().getFirst());
    assertEquals(10, limits.getFirst().asInt().orElseThrow());
    assertEquals(ConfigScalarStyle.BINARY, limits.getFirst().decorations().scalarStyle());
    assertEquals(8, limits.get(1).metadata(JsonMetadataKeys.NUMBER_RADIX).orElseThrow());
    assertEquals(16, limits.get(2).asInt().orElseThrow());
    assertEquals(ConfigValueKind.NULL, routes.get(1).kind());
    assertEquals("kept", roundTripped.find(ConfigPath.of("server").child("literal.key").child("value")).flatMap(ConfigNode::asString).orElseThrow());
    assertEquals(255, roundTripped.find("server.maxConnections").flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals(16, roundTripped.find("server.maxConnections").orElseThrow().metadata(JsonMetadataKeys.NUMBER_RADIX).orElseThrow());
  }
}
