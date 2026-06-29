package net.pistonmaster.pistonconfig.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
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
}
