package net.pistonmaster.pistonconfig.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import org.junit.jupiter.api.Test;

final class EnvironmentOverridesTest {
  @Test
  void appliesEnvironmentAndSystemPropertyOverridesWithScalarParsing() {
    var document = ConfigDocument.empty()
      .set("server.port", 25565)
      .set("server.host", "0.0.0.0");

    EnvironmentOverrides.of(
      "my-app",
      "my.app",
      Map.of(
        "MY_APP_SERVER_PORT", "25566",
        "MY_APP_SERVER_ENABLED", "true",
        "OTHER_SERVER_PORT", "1"
      ),
      Map.of(
        "my.app.server.host", "127.0.0.1",
        "my.app.server.ratio", "0.75"
      )
    ).applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("127.0.0.1", document.find("server.host").orElseThrow().asString().orElseThrow());
    assertEquals(0.75D, document.find("server.ratio").orElseThrow().asDouble().orElseThrow());
    assertTrue(document.find("other.server.port").isEmpty());
  }

  @Test
  void systemPropertiesOverrideEnvironmentWhenTheyTargetSamePath() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.of(
      "app",
      "app",
      Map.of("APP_SERVER_PORT", "25565"),
      Map.of("app.server.port", "25566")
    ).applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
  }

  @Test
  void emptyPrefixesApplyEveryNonBlankKey() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.of(
      "",
      null,
      Map.of("SERVER_PORT", "25565"),
      Map.of("server.host", "localhost")
    ).applyTo(document);

    assertEquals(25565, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
    assertEquals("localhost", document.find("server.host").orElseThrow().asString().orElseThrow());
  }

  @Test
  void copiesInputMapsDefensively() {
    var environment = new LinkedHashMap<String, String>();
    environment.put("APP_SERVER_PORT", "25565");
    var overrides = EnvironmentOverrides.of("app", "app", environment, Map.of());

    environment.put("APP_SERVER_PORT", "1");

    var document = ConfigDocument.empty();
    overrides.applyTo(document);

    assertEquals(25565, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
  }

  @Test
  void leavesNonNumericStringsAsStrings() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.of("app", "app", Map.of("APP_SERVER_MODE", "production"), Map.of()).applyTo(document);

    var value = document.find("server.mode").orElseThrow().rawValue();
    assertInstanceOf(String.class, value);
    assertEquals("production", value);
  }

  @Test
  void rejectsNullInputs() {
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.of("app", "app", null, Map.of()));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.of("app", "app", Map.of(), null));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.of("app", "app", Map.of(), Map.of()).applyTo(null));
  }
}
