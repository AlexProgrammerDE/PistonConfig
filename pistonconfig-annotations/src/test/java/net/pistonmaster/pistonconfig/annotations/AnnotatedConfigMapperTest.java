package net.pistonmaster.pistonconfig.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

  @ConfigPathPrefix("server")
  static final class ExampleConfig {
    @ConfigComment("Port used by the server.")
    int port = 25565;
  }
}
