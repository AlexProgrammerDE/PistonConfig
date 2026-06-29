package net.pistonmaster.pistonconfig.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.pistonmaster.pistonconfig.core.ConfigCodec;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import org.junit.jupiter.api.Test;

final class AnnotatedConfigMapperTest {
  @Test
  void writesAndReadsAnnotatedFields() {
    var mapper = new AnnotatedConfigMapper();
    var defaults = mapper.writeDefaults(new ExampleConfig());

    defaults.set("server.port", 25566);
    var mapped = mapper.read(defaults, ExampleConfig.class);

    assertEquals(25566, mapped.port);
  }

  @Test
  void writesFieldNamesPrefixesCommentsAndIgnoresRuntimeFields() {
    var mapper = new AnnotatedConfigMapper();
    var defaults = mapper.writeDefaults(new NamedConfig());

    var bindAddress = defaults.find("server.bind-address").orElseThrow();

    assertEquals("127.0.0.1", bindAddress.asString().orElseThrow());
    assertEquals("Address used by the server.", bindAddress.comment().leadingText().getFirst());
    assertTrue(defaults.find("server.runtimeOnly").isEmpty());
    assertTrue(defaults.find("server.ignored").isEmpty());
    assertTrue(defaults.find("server.STATIC_VALUE").isEmpty());
  }

  @Test
  void readsInheritedFieldsAndPreservesMissingDefaultsOnReadInto() {
    var mapper = new AnnotatedConfigMapper();
    var target = new ChildConfig();
    var document = ConfigDocument.empty()
      .set("server.child", "changed");

    mapper.readInto(document, target);

    assertEquals("base-default", target.base);
    assertEquals("changed", target.child);
  }

  @Test
  void customCodecSupportsApplicationTypes() {
    var registry = new ConfigCodecRegistry()
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
    var mapper = new AnnotatedConfigMapper(registry);

    var defaults = mapper.writeDefaults(new EndpointConfig());
    defaults.set("endpoint.port", 8080);

    assertEquals(new Endpoint("localhost", 8080), mapper.read(defaults, EndpointConfig.class).endpoint);
  }

  @Test
  void throwsWhenTargetCannotBeInstantiated() {
    var mapper = new AnnotatedConfigMapper();

    assertThrows(ConfigException.class, () -> mapper.read(ConfigDocument.empty(), NoNoArgsConfig.class));
  }

  @ConfigPathPrefix("server")
  static final class ExampleConfig {
    @ConfigComment("Port used by the server.")
    int port = 25565;
  }

  @ConfigPathPrefix("server")
  static final class NamedConfig {
    static final String STATIC_VALUE = "ignored";

    @ConfigName("bind-address")
    @ConfigComment("Address used by the server.")
    String host = "127.0.0.1";

    transient String runtimeOnly = "runtime";

    @ConfigIgnore
    String ignored = "ignored";
  }

  static class BaseConfig {
    String base = "base-default";
  }

  @ConfigPathPrefix("server")
  static final class ChildConfig extends BaseConfig {
    String child = "child-default";
  }

  static final class EndpointConfig {
    Endpoint endpoint = new Endpoint("localhost", 25565);
  }

  static final class NoNoArgsConfig {
    NoNoArgsConfig(String ignored) {
    }
  }

  private record Endpoint(String host, int port) {
  }
}
