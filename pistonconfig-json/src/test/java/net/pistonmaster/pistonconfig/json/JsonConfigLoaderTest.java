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
}
