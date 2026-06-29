package net.pistonmaster.pistonconfig.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.AnnotatedType;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.MergeListStrategy;
import net.pistonmaster.pistonconfig.yaml.YamlConfigFormat;
import net.pistonmaster.pistonconfig.yaml.YamlConfigLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AnnotatedConfigMapperTest {
  @TempDir
  private java.nio.file.Path tempDir;

  @Test
  void writesAndReadsRecordsNestedCollectionsMapsAndValueTypes() {
    var mapper = new AnnotatedConfigMapper();
    var defaults = mapper.writeDefaults(ServerConfig.class);

    assertEquals("127.0.0.1", defaults.find("server.bind-address").orElseThrow().asString().orElseThrow());
    assertEquals("Address used by the server.", defaults.find("server.bind-address").orElseThrow().comment().leadingText().getFirst());
    assertEquals("PT30S", defaults.find("server.timeout").orElseThrow().asString().orElseThrow());

    defaults.set("server.port", 25566);
    defaults.set("server.endpoints.PROD.port", 443);

    var config = mapper.read(defaults, ServerConfig.class);

    assertEquals("127.0.0.1", config.host());
    assertEquals(25566, config.port());
    assertEquals(Mode.DEV, config.mode());
    assertEquals(List.of(new User("root", true), new User("guest", false)), config.users());
    assertEquals(Set.of(10, 20), config.limits());
    assertEquals(new Endpoint("prod.example.com", 443), config.endpoints().get(Mode.PROD));
    assertEquals(LocalDate.of(2026, 1, 1), config.releaseDates().getFirst().get(Mode.DEV));
    assertEquals(Duration.ofSeconds(30), config.timeout());
  }

  @Test
  void readsInheritedFieldsIntoClassesAndPreservesMissingDefaults() {
    var mapper = new AnnotatedConfigMapper();
    var target = new ChildConfig();
    var document = ConfigDocument.empty()
      .set("server.child-name", "changed");

    mapper.readInto(document, target);

    assertEquals("base-default", target.base);
    assertEquals("changed", target.childName);
  }

  @Test
  void nameFormatterAppliesWhenNameAnnotationIsAbsent() {
    var mapper = new AnnotatedConfigMapper(ConfigMapperOptions.builder()
      .nameFormatter(ConfigNameFormatters.KEBAB_CASE)
      .build());

    var document = mapper.write(new FormattedConfig());

    assertTrue(document.find("request-timeout").isPresent());
    assertEquals(15, mapper.read(document, FormattedConfig.class).requestTimeout);
  }

  @Test
  void scalarCoercionHandlesStringBackedNestedCollectionsMapsAndRecords() {
    var document = ConfigDocument.empty()
      .set("enabled", "true")
      .set("port", "25566")
      .setNode(ConfigPath.of("limits"), ConfigNode.list()
        .addListValue("10")
        .addListValue(20))
      .set("grade", "A")
      .set("timeout", "PT5S")
      .set("endpoints.DEV.1.host", "dev.example.com")
      .set("endpoints.DEV.1.port", "8080")
      .set("endpoints.PROD.2.host", "prod.example.com")
      .set("endpoints.PROD.2.port", 443);

    assertThrows(ConfigException.class, () -> new AnnotatedConfigMapper().read(document, CoercedConfig.class));

    var mapper = new AnnotatedConfigMapper(ConfigMapperOptions.builder()
      .scalarCoercion(ConfigScalarCoercion.STRING)
      .build());
    var config = mapper.read(document, CoercedConfig.class);

    assertTrue(config.enabled());
    assertEquals(25566, config.port());
    assertEquals(List.of(10, 20), config.limits());
    assertEquals('A', config.grade());
    assertEquals(Duration.ofSeconds(5), config.timeout());
    assertEquals(new Endpoint("dev.example.com", 8080), config.endpoints().get(Mode.DEV).get(1));
    assertEquals(new Endpoint("prod.example.com", 443), config.endpoints().get(Mode.PROD).get(2));
  }

  @Test
  void nullInputPolicyControlsExplicitNulls() {
    var document = ConfigDocument.empty()
      .setNode(net.pistonmaster.pistonconfig.core.ConfigPath.of("name"), ConfigNode.nullValue());

    assertEquals("default", new AnnotatedConfigMapper().read(document, NullableConfig.class).name());

    var mapper = new AnnotatedConfigMapper(ConfigMapperOptions.builder()
      .inputNulls(true)
      .build());

    assertNull(mapper.read(document, NullableConfig.class).name());
  }

  @Test
  void customSerializerCanBeRegisteredByType() {
    var mapper = new AnnotatedConfigMapper(ConfigMapperOptions.builder()
      .serializer(Endpoint.class, new EndpointSerializer())
      .build());

    var document = mapper.write(new EndpointHolder(new Endpoint("localhost", 25565)));
    assertEquals("localhost:25565", document.find("endpoint").orElseThrow().asString().orElseThrow());
    assertEquals(new Endpoint("localhost", 8080), mapper.read(ConfigDocument.empty().set("endpoint", "localhost:8080"), EndpointHolder.class).endpoint());
  }

  @Test
  void serializerAnnotationCanTargetNestedCollectionElements() {
    var mapper = new AnnotatedConfigMapper();
    var document = mapper.write(new AnnotatedEndpointHolder(List.of(new Endpoint("one", 1), new Endpoint("two", 2))));

    assertEquals("one:1", document.find("endpoints").orElseThrow().listChildren().getFirst().asString().orElseThrow());
    assertEquals(new Endpoint("two", 2), mapper.read(document, AnnotatedEndpointHolder.class).endpoints().get(1));
  }

  @Test
  void polymorphicMembersRoundTripWithAliases() {
    var mapper = new AnnotatedConfigMapper();
    var document = mapper.write(new ShapeConfig(new Circle(3)));

    assertEquals("circle", document.find("shape.type").orElseThrow().asString().orElseThrow());
    assertEquals(new ShapeConfig(new Circle(3)), mapper.read(document, ShapeConfig.class));
  }

  @Test
  void rejectsUnsupportedGenericShapes() {
    var mapper = new AnnotatedConfigMapper();

    assertThrows(ConfigException.class, () -> mapper.write(new RawListConfig()));
    assertThrows(ConfigException.class, () -> mapper.write(new WildcardConfig()));
  }

  @Test
  void configStoreUpdatesFilesThroughAnyFormat() throws IOException {
    var path = tempDir.resolve("server.yml");
    Files.writeString(path, """
      server:
        port: 25566
        stale: true
      """);

    var store = ConfigStores.forType(ServerConfig.class)
      .format(YamlConfigFormat.INSTANCE)
      .options(ConfigMapperOptions.builder()
        .unknownKeyPolicy(ConfigUnknownKeyPolicy.DROP)
        .listStrategy(MergeListStrategy.PRESERVE_EXISTING)
        .build())
      .validator(config -> {
        if (config.port() <= 0) {
          throw new ConfigException("Port must be positive.");
        }
      })
      .build();

    var config = store.update(path);
    var written = Files.readString(path);

    assertEquals(25566, config.port());
    assertFalse(written.contains("stale"));
    assertTrue(written.contains("bind-address"));
  }

  @Test
  void configStoreReadOverridesAreNotPersistedDuringUpdate() throws IOException {
    var path = tempDir.resolve("server-overrides.yml");
    var store = ConfigStores.forType(SimplePortConfig.class)
      .format(YamlConfigFormat.INSTANCE)
      .readOverride(document -> document.set("port", 25566))
      .build();

    var config = store.update(path);

    assertEquals(25566, config.port());
    assertTrue(Files.readString(path).contains("25565"));
  }

  @Test
  void configStoreRewritePreservesExistingSourceComments() throws IOException {
    var path = tempDir.resolve("server-rewrite.yml");
    Files.writeString(path, """
      # Existing port comment
      port: 25565
      """);
    var store = ConfigStores.forType(SimplePortConfig.class)
      .format(YamlConfigFormat.INSTANCE)
      .build();

    store.rewrite(path, new SimplePortConfig(25566));
    var written = Files.readString(path);
    ConfigDocument document;
    try (var reader = Files.newBufferedReader(path)) {
      document = new YamlConfigLoader().load(reader);
    }

    assertTrue(written.contains("Existing port comment"));
    assertEquals(25566, document.find("port").flatMap(ConfigNode::asInt).orElseThrow());
  }

  @ConfigPathPrefix("server")
  record ServerConfig(
    @ConfigName("bind-address")
    @ConfigComment("Address used by the server.")
    String host,
    int port,
    Mode mode,
    List<User> users,
    Set<Integer> limits,
    Map<Mode, Endpoint> endpoints,
    List<Map<Mode, LocalDate>> releaseDates,
    UUID id,
    Duration timeout
  ) {
    ServerConfig() {
      this(
        "127.0.0.1",
        25565,
        Mode.DEV,
        List.of(new User("root", true), new User("guest", false)),
        Set.of(10, 20),
        Map.of(
          Mode.DEV, new Endpoint("dev.example.com", 80),
          Mode.PROD, new Endpoint("prod.example.com", 8443)
        ),
        List.of(Map.of(Mode.DEV, LocalDate.of(2026, 1, 1))),
        UUID.fromString("00000000-0000-0000-0000-000000000001"),
        Duration.ofSeconds(30)
      );
    }
  }

  record SimplePortConfig(int port) {
    SimplePortConfig() {
      this(25565);
    }
  }

  record CoercedConfig(
    boolean enabled,
    int port,
    List<Integer> limits,
    Map<Mode, Map<Integer, Endpoint>> endpoints,
    char grade,
    Duration timeout
  ) {
  }

  record User(String name, boolean admin) {
  }

  record Endpoint(String host, int port) {
  }

  enum Mode {
    DEV,
    PROD
  }

  static class BaseConfig {
    String base = "base-default";
  }

  @ConfigPathPrefix("server")
  static final class ChildConfig extends BaseConfig {
    @ConfigName("child-name")
    String childName = "child-default";
  }

  static final class FormattedConfig {
    int requestTimeout = 15;
  }

  record NullableConfig(String name) {
    NullableConfig() {
      this("default");
    }
  }

  record EndpointHolder(Endpoint endpoint) {
  }

  record AnnotatedEndpointHolder(
    @ConfigSerializeWith(value = EndpointSerializer.class, nesting = 1)
    List<Endpoint> endpoints
  ) {
  }

  static final class EndpointSerializer implements ConfigSerializer<Endpoint> {
    @Override
    public ConfigNode encode(Endpoint value, ConfigSerializationContext context) {
      return ConfigNode.scalar(value.host() + ":" + value.port());
    }

    @Override
    public Endpoint decode(ConfigNode node, ConfigSerializationContext context) {
      var parts = node.asString().orElseThrow().split(":", 2);
      return new Endpoint(parts[0], Integer.parseInt(parts[1]));
    }
  }

  @ConfigPolymorphic
  @ConfigPolymorphicTypes({
    @ConfigPolymorphicTypes.Type(type = Circle.class, alias = "circle"),
    @ConfigPolymorphicTypes.Type(type = Square.class, alias = "square")
  })
  sealed interface Shape permits Circle, Square {
  }

  record Circle(int radius) implements Shape {
  }

  record Square(int size) implements Shape {
  }

  record ShapeConfig(Shape shape) {
  }

  @SuppressWarnings("rawtypes")
  static final class RawListConfig {
    List values = List.of("bad");
  }

  static final class WildcardConfig {
    List<? extends String> values = List.of("bad");
  }
}
