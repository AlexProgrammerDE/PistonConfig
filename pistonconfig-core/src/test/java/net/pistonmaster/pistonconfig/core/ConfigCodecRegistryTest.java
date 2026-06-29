package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ConfigCodecRegistryTest {
  @Test
  void encodesAndDecodesBuiltInScalarTypes() {
    var registry = new ConfigCodecRegistry();

    assertEquals("value", registry.decode(registry.encode("value"), String.class));
    assertTrue(registry.decode(registry.encode(true), boolean.class));
    assertEquals(10, registry.decode(ConfigNode.scalar("10"), int.class));
    assertEquals(10L, registry.decode(ConfigNode.scalar("10"), long.class));
    assertEquals(10.5D, registry.decode(ConfigNode.scalar("10.5"), double.class));
  }

  @Test
  void encodesNullAsNullNode() {
    var registry = new ConfigCodecRegistry();

    assertEquals(ConfigValueKind.NULL, registry.encode(null).kind());
  }

  @Test
  void throwsWhenCodecIsMissing() {
    var registry = new ConfigCodecRegistry();

    assertThrows(ConfigException.class, () -> registry.codec(Endpoint.class));
    assertThrows(ConfigException.class, () -> registry.encode(new Endpoint("localhost", 25565)));
  }

  @Test
  void customCodecCanEncodeAndDecodeObjectValues() {
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

    var endpoint = new Endpoint("localhost", 25565);
    var encoded = registry.encode(endpoint);

    assertEquals("localhost", encoded.find(ConfigPath.of("host")).flatMap(ConfigNode::asString).orElseThrow());
    assertEquals(endpoint, registry.decode(encoded, Endpoint.class));
  }

  private record Endpoint(String host, int port) {
  }
}
