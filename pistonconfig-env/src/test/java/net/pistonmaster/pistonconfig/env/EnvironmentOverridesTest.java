package net.pistonmaster.pistonconfig.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    EnvironmentOverrides.builder()
      .environmentPrefix("my-app")
      .propertyPrefix("my.app")
      .putAllEnvironment(Map.of(
        "MY_APP_SERVER_PORT", "25566",
        "MY_APP_SERVER_ENABLED", "true",
        "OTHER_SERVER_PORT", "1"
      ))
      .putAllProperties(Map.of(
        "my.app.server.host", "127.0.0.1",
        "my.app.server.ratio", "0.75"
      ))
      .build()
      .applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("127.0.0.1", document.find("server.host").orElseThrow().asString().orElseThrow());
    assertEquals(0.75D, document.find("server.ratio").orElseThrow().asDouble().orElseThrow());
    assertTrue(document.find("other.server.port").isEmpty());
  }

  @Test
  void systemPropertiesOverrideEnvironmentWhenTheyTargetSamePath() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.builder()
      .environmentPrefix("app")
      .propertyPrefix("app")
      .putAllEnvironment(Map.of("APP_SERVER_PORT", "25565"))
      .putAllProperties(Map.of("app.server.port", "25566"))
      .build()
      .applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
  }

  @Test
  void emptyPrefixesApplyEveryNonBlankKey() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.builder()
      .putAllEnvironment(Map.of("SERVER_PORT", "25565"))
      .putAllProperties(Map.of("server.host", "localhost"))
      .build()
      .applyTo(document);

    assertEquals(25565, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
    assertEquals("localhost", document.find("server.host").orElseThrow().asString().orElseThrow());
  }

  @Test
  void copiesInputMapsDefensively() {
    var environment = new LinkedHashMap<String, String>();
    environment.put("APP_SERVER_PORT", "25565");
    var overrides = EnvironmentOverrides.builder()
      .environmentPrefix("app")
      .propertyPrefix("app")
      .putAllEnvironment(environment)
      .build();

    environment.put("APP_SERVER_PORT", "1");

    var document = ConfigDocument.empty();
    overrides.applyTo(document);

    assertEquals(25565, document.find("server.port").flatMap(node -> node.asInt()).orElseThrow());
  }

  @Test
  void leavesNonNumericStringsAsStrings() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.builder()
      .environmentPrefix("app")
      .propertyPrefix("app")
      .putAllEnvironment(Map.of("APP_SERVER_MODE", "production"))
      .build()
      .applyTo(document);

    var value = document.find("server.mode").orElseThrow().rawValue();
    assertInstanceOf(String.class, value);
    assertEquals("production", value);
  }

  @Test
  void rejectsNullInputs() {
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().putAllEnvironment(null));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().putAllProperties(null));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().build().applyTo(null));
  }
}
