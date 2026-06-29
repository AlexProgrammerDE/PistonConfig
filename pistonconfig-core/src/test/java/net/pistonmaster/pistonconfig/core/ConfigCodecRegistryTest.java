package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
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
    assertEquals((byte) 10, registry.decode(ConfigNode.scalar("10"), byte.class));
    assertEquals('x', registry.decode(ConfigNode.scalar("x"), char.class));
    assertEquals(new BigInteger("100000000000000000000"), registry.decode(ConfigNode.scalar("100000000000000000000"), BigInteger.class));
    assertEquals(new BigDecimal("10.5"), registry.decode(ConfigNode.scalar("10.5"), BigDecimal.class));
    assertEquals(LocalDate.of(2026, 1, 1), registry.decode(registry.encode(LocalDate.of(2026, 1, 1)), LocalDate.class));
    assertEquals(Duration.ofSeconds(5), registry.decode(registry.encode(Duration.ofSeconds(5)), Duration.class));
    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), registry.decode(ConfigNode.scalar("00000000-0000-0000-0000-000000000001"), UUID.class));
    assertEquals(Path.of("config.yml"), registry.decode(ConfigNode.scalar("config.yml"), Path.class));
    assertEquals(URI.create("https://example.com"), registry.decode(ConfigNode.scalar("https://example.com"), URI.class));
    assertEquals(Mode.PROD, registry.decode(registry.encode(Mode.PROD), Mode.class));
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

  private enum Mode {
    PROD
  }
}
