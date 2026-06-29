package net.pistonmaster.pistonconfig.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import net.pistonmaster.pistonconfig.core.ConfigNode;
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
}
