package net.pistonmaster.pistonconfig.staticfields;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.pistonmaster.pistonconfig.core.ConfigCodec;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class StaticConfigDefinitionTest {
  @Test
  void readsStaticPropertiesAndSortsByPath() {
    var definition = StaticConfigDefinition.from(Options.class);

    assertIterableEquals(
      List.of(ConfigPath.parse("server.host"), ConfigPath.parse("server.port")),
      definition.properties().stream().map(ConfigProperty::path).toList()
    );
  }

  @Test
  void createsDefaultsWithComments() {
    var defaults = StaticConfigDefinition.from(Options.class).defaults(new ConfigCodecRegistry());

    var host = defaults.find("server.host").orElseThrow();
    var port = defaults.find("server.port").orElseThrow();

    assertEquals("localhost", host.asString().orElseThrow());
    assertEquals("Server host.", host.comment().leadingText().getFirst());
    assertEquals(25565, port.asInt().orElseThrow());
  }

  @Test
  void appliesDefaultsWithoutReplacingUserValues() {
    var document = ConfigDocument.empty()
      .set("server.port", 25566);

    StaticConfigDefinition.from(Options.class).applyDefaults(document, new ConfigCodecRegistry());

    assertEquals(25566, document.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals("localhost", document.find("server.host").flatMap(ConfigNode::asString).orElseThrow());
  }

  @Test
  void getsTypedValuesAndFallsBackToDefaults() {
    var definition = StaticConfigDefinition.from(Options.class);
    var document = ConfigDocument.empty()
      .set("server.port", 25566);

    assertEquals(25566, definition.get(document, Options.PORT, new ConfigCodecRegistry()));
    assertEquals("localhost", definition.get(document, Options.HOST, new ConfigCodecRegistry()));
  }

  @Test
  void supportsCustomCodecs() {
    var codecRegistry = new ConfigCodecRegistry()
      .register(Endpoint.class, new ConfigCodec<Endpoint>() {
        @Override
        public ConfigNode encode(Endpoint value, ConfigCodecRegistry registry) {
          return ConfigNode.object()
            .set(ConfigPath.of("host"), value.host())
            .set(ConfigPath.of("port"), value.port());
        }

        @Override
        public Endpoint decode(ConfigNode node, ConfigCodecRegistry registry) {
          return new Endpoint(
            node.find(ConfigPath.of("host")).flatMap(ConfigNode::asString).orElseThrow(),
            node.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow()
          );
        }
      });
    var definition = StaticConfigDefinition.from(EndpointOptions.class);
    var defaults = definition.defaults(codecRegistry);

    defaults.set("endpoint.port", 8080);

    assertEquals(new Endpoint("localhost", 8080), definition.get(defaults, EndpointOptions.ENDPOINT, codecRegistry));
  }

  @Test
  void rejectsNullInputs() {
    assertThrows(NullPointerException.class, () -> StaticConfigDefinition.builder().addProperty(null));
    assertThrows(NullPointerException.class, () -> StaticConfigDefinition.from(null));
    assertThrows(NullPointerException.class, () -> ConfigProperty.<String>builder()
      .path(ConfigPath.parse("path"))
      .type(null)
      .defaultValue("value")
      .build());
    assertThrows(NullPointerException.class, () -> ConfigProperty.<String>builder()
      .path(null)
      .type(String.class)
      .defaultValue("value")
      .build());
    assertTrue(ConfigProperty.<String>builder()
      .path(ConfigPath.parse("path"))
      .type(String.class)
      .defaultValue("value")
      .build()
      .comment()
      .isEmpty());
  }

  private static final class Options {
    static final String IGNORED = "ignored";
    static final ConfigProperty<Integer> PORT = ConfigProperty.<Integer>builder()
      .path(ConfigPath.parse("server.port"))
      .type(Integer.class)
      .defaultValue(25565)
      .comment(comment("Server port."))
      .build();
    private static final ConfigProperty<String> HOST = ConfigProperty.<String>builder()
      .path(ConfigPath.parse("server.host"))
      .type(String.class)
      .defaultValue("localhost")
      .comment(comment("Server host."))
      .build();

    ConfigProperty<String> instanceProperty = ConfigProperty.<String>builder()
      .path(ConfigPath.parse("ignored"))
      .type(String.class)
      .defaultValue("ignored")
      .build();
  }

  private static final class EndpointOptions {
    static final ConfigProperty<Endpoint> ENDPOINT = ConfigProperty.<Endpoint>builder()
      .path(ConfigPath.parse("endpoint"))
      .type(Endpoint.class)
      .defaultValue(new Endpoint("localhost", 25565))
      .build();
  }

  private record Endpoint(String host, int port) {
  }

  private static ConfigComment comment(String text) {
    return ConfigComment.builder()
      .addLeading(ConfigCommentLine.builder()
        .text(text)
        .type(ConfigCommentType.BLOCK)
        .marker(ConfigCommentMarker.HASH)
        .build())
      .build();
  }
}
