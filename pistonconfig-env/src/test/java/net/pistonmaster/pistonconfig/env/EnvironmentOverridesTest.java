package net.pistonmaster.pistonconfig.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigNodeDecorations;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class EnvironmentOverridesTest {
  @Test
  void appliesEnvironmentAndSystemPropertyOverridesUsingExistingScalarTypes() {
    var document = ConfigDocument.empty()
      .set("server.port", 25565)
      .set("server.host", "0.0.0.0")
      .set("server.enabled", false)
      .set("server.ratio", 0.5D);

    EnvironmentOverrides.builder()
      .environmentPrefix("my-app")
      .propertyPrefix("my.app")
      .putAllEnvironment(Map.of(
        "MY_APP_SERVER_PORT", "25566",
        "MY_APP_SERVER_ENABLED", "true",
        "MY_APP_SERVER_MISSING", "ignored",
        "OTHER_SERVER_PORT", "1"
      ))
      .putAllProperties(Map.of(
        "my.app.server.host", "127.0.0.1",
        "my.app.server.ratio", "0.75"
      ))
      .build()
      .applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
    assertTrue(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals("127.0.0.1", document.find("server.host").orElseThrow().asString().orElseThrow());
    assertEquals(0.75D, document.find("server.ratio").orElseThrow().asDouble().orElseThrow());
    assertTrue(document.find("server.missing").isEmpty());
    assertTrue(document.find("other.server.port").isEmpty());
  }

  @Test
  void systemPropertiesOverrideEnvironmentWhenTheyTargetSamePath() {
    var document = ConfigDocument.empty()
      .set("server.port", 25565);

    EnvironmentOverrides.builder()
      .environmentPrefix("app")
      .propertyPrefix("app")
      .putAllEnvironment(Map.of("APP_SERVER_PORT", "25565"))
      .putAllProperties(Map.of("app.server.port", "25566"))
      .build()
      .applyTo(document);

    assertEquals(25566, document.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void mixedOverridesPreserveSourceDecorationsAndRejectUninferableShapes() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.parse("server.port"), ConfigNode.scalar(25565)
        .setComment(ConfigComment.builder()
          .addLeading(ConfigCommentLine.builder()
            .text("Port comment.")
            .type(ConfigCommentType.BLOCK)
            .marker(ConfigCommentMarker.HASH)
            .build())
          .build())
        .setDecorations(ConfigNodeDecorations.builder()
          .putAttribute("format", "yaml")
          .build())
        .setMetadata("raw", "0x63"))
      .set("server.enabled", true)
      .set("server.ratio", 0.5D)
      .setNode(ConfigPath.parse("server.alias"), ConfigNode.nullValue());

    EnvironmentOverrides.builder()
      .environmentPrefix("app")
      .propertyPrefix("app")
      .putAllEnvironment(Map.of(
        "APP_SERVER_PORT", "30000",
        "APP_SERVER_ENABLED", "false",
        "APP_SERVER_RATIO", "0.75",
        "APP_SERVER_MISSING", "ignored"
      ))
      .putAllProperties(Map.of("app.server.port", "31000"))
      .build()
      .applyTo(document);
    var port = document.find("server.port").orElseThrow();

    assertEquals(31000, port.asInt().orElseThrow());
    assertEquals("Port comment.", port.comment().leadingText().getFirst());
    assertEquals("yaml", port.decorations().attributes().get("format"));
    assertTrue(port.metadata("raw").isEmpty());
    assertFalse(document.find("server.enabled").orElseThrow().asBoolean().orElseThrow());
    assertEquals(0.75D, document.find("server.ratio").orElseThrow().asDouble().orElseThrow());
    assertTrue(document.find("server.missing").isEmpty());

    var nullOverride = EnvironmentOverrides.builder()
      .putAllEnvironment(Map.of("SERVER_ALIAS", "primary"))
      .build();
    assertThrows(ConfigException.class, () -> nullOverride.applyTo(document));
  }

  @Test
  void allowNewPathsCreatesStringValuesOnly() {
    var document = ConfigDocument.empty();

    EnvironmentOverrides.builder()
      .allowNewPaths(true)
      .putAllEnvironment(Map.of("SERVER_PORT", "25565"))
      .putAllProperties(Map.of("server.host", "localhost"))
      .build()
      .applyTo(document);

    assertInstanceOf(String.class, document.find("server.port").orElseThrow().rawValue());
    assertEquals("25565", document.find("server.port").orElseThrow().rawValue());
    assertEquals("localhost", document.find("server.host").orElseThrow().rawValue());
  }

  @Test
  void caseSensitiveEnvironmentMatchingPreservesPathCase() {
    var document = ConfigDocument.empty()
      .set("Server.Port", 25565);

    EnvironmentOverrides.builder()
      .caseSensitiveEnvironment(true)
      .environmentPrefix("App")
      .putAllEnvironment(Map.of(
        "App_Server_Port", "25566",
        "APP_SERVER_PORT", "1"
      ))
      .build()
      .applyTo(document);

    assertEquals(25566, document.find("Server.Port").flatMap(ConfigNode::asInt).orElseThrow());
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

    var document = ConfigDocument.empty()
      .set("server.port", 0);
    overrides.applyTo(document);

    assertEquals(25565, document.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @Test
  void rejectsCollectionReplacement() {
    var document = ConfigDocument.empty()
      .setNode(ConfigPath.of("servers"), ConfigNode.list().addListValue("one"));

    var overrides = EnvironmentOverrides.builder()
      .putAllEnvironment(Map.of("SERVERS", "two"))
      .build();

    assertThrows(ConfigException.class, () -> overrides.applyTo(document));
  }

  @Test
  void rejectsInvalidTypedScalars() {
    var document = ConfigDocument.empty()
      .set("server.enabled", false);

    var overrides = EnvironmentOverrides.builder()
      .putAllEnvironment(Map.of("SERVER_ENABLED", "yes"))
      .build();

    assertThrows(ConfigException.class, () -> overrides.applyTo(document));
  }

  @Test
  void rejectsNullInputs() {
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().putAllEnvironment(null));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().putAllProperties(null));
    assertThrows(NullPointerException.class, () -> EnvironmentOverrides.builder().build().applyTo(null));
  }
}
