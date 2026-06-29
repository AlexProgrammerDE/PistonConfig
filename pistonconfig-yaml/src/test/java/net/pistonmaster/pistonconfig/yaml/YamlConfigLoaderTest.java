package net.pistonmaster.pistonconfig.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigCollectionStyle;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ConfigScalarStyle;
import net.pistonmaster.pistonconfig.core.ConfigValueKind;
import net.pistonmaster.pistonconfig.core.ImmutableConfigNodeDecorations;
import org.junit.jupiter.api.Test;

final class YamlConfigLoaderTest {
  @Test
  void preservesValueComments() {
    var loader = new YamlConfigLoader();
    var document = loader.load(new StringReader("""
      # port comment
      port: 25565 # inline port
      """));

    var port = document.find("port").orElseThrow();

    assertEquals("port comment", port.decorations().keyComment().leadingText().getFirst());
    assertEquals("inline port", port.comment().inlineText());

    var writer = new StringWriter();
    loader.save(document, writer);

    assertTrue(writer.toString().contains("port comment"));
    assertTrue(writer.toString().contains("inline port"));
  }

  @Test
  void exposesFormatDescriptor() {
    var format = YamlConfigFormat.INSTANCE;

    assertEquals("yaml", format.name());
    assertTrue(format.extensions().contains("yaml"));
    assertTrue(format.extensions().contains("yml"));
    assertTrue(format.capabilities().inlineComments());
    assertNotNull(format.loader());
  }

  @Test
  void emptyYamlLoadsAsEmptyDocument() {
    var document = new YamlConfigLoader().load(new StringReader(""));

    assertTrue(document.root().isObject());
    assertTrue(document.root().objectChildren().isEmpty());
  }

  @Test
  void parsesScalarsCollectionsStylesLocationsAndMetadata() {
    var document = new YamlConfigLoader().load(new StringReader("""
      defaults: &defaults
        enabled: true
      numbers: [0x10, 1_000, 3.5]
      literal: |
        first
        second
      single: 'quoted'
      flow: {one: 1, two: 2}
      nullValue: null
      huge: 123456789012345678901234567890
      """));

    var defaults = document.find("defaults").orElseThrow();
    var numbers = document.find("numbers").orElseThrow();
    var numberItems = numbers.listChildren();
    var literal = document.find("literal").orElseThrow();
    var single = document.find("single").orElseThrow();
    var flow = document.find("flow").orElseThrow();

    assertEquals(ConfigCollectionStyle.BLOCK, defaults.decorations().collectionStyle());
    assertEquals("defaults", defaults.decorations().attributes().get(YamlMetadataKeys.ANCHOR));
    assertEquals(ConfigCollectionStyle.FLOW, numbers.decorations().collectionStyle());
    assertEquals(16, numberItems.getFirst().asInt().orElseThrow());
    assertEquals("0x10", numberItems.getFirst().metadata(YamlMetadataKeys.SCALAR_RAW).orElseThrow());
    assertEquals(1000, numberItems.get(1).asInt().orElseThrow());
    assertInstanceOf(BigDecimal.class, numberItems.get(2).rawValue());
    assertEquals(ConfigScalarStyle.LITERAL, literal.decorations().scalarStyle());
    assertEquals(ConfigScalarStyle.SINGLE_QUOTED, single.decorations().scalarStyle());
    assertEquals(ConfigCollectionStyle.FLOW, flow.decorations().collectionStyle());
    assertEquals(ConfigValueKind.NULL, document.find("nullValue").orElseThrow().kind());
    assertInstanceOf(BigInteger.class, document.find("huge").orElseThrow().rawValue());
    assertTrue(single.decorations().valueLocation().isKnown());
    assertFalse(single.decorations().attributes().get(YamlMetadataKeys.TAG).isBlank());
  }

  @Test
  void savesScalarStylesRawNumericValuesAndAnchors() {
    var document = net.pistonmaster.pistonconfig.core.ConfigDocument.empty()
      .setNode(ConfigPath.of("name"), ConfigNode.scalar("quoted")
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withScalarStyle(ConfigScalarStyle.SINGLE_QUOTED))
        .setComment(ConfigComment.builder()
          .addInline(commentLine("name comment", ConfigCommentType.INLINE))
          .build()))
      .setNode(ConfigPath.of("answer"), ConfigNode.scalar(16)
        .setMetadata(YamlMetadataKeys.SCALAR_RAW, "0x10"))
      .setNode(ConfigPath.of("settings"), ConfigNode.object()
        .set(ConfigPath.of("enabled"), true)
        .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
          .withAttributes(Map.of(YamlMetadataKeys.ANCHOR, "settings"))));

    var writer = new StringWriter();
    new YamlConfigLoader().save(document, writer);
    var yaml = writer.toString();

    assertTrue(yaml.contains("name: 'quoted'"));
    assertTrue(yaml.contains("name comment"));
    assertTrue(yaml.contains("answer: 0x10"));
    assertTrue(yaml.contains("&settings"));
  }

  @Test
  void wrapsInvalidYamlAsConfigException() {
    assertThrows(ConfigException.class, () -> new YamlConfigLoader().load(new StringReader("broken: [")));
  }

  private static ConfigCommentLine commentLine(String text, ConfigCommentType type) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(type)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }
}
