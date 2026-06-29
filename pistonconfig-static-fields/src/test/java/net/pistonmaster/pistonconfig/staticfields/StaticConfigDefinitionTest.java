package net.pistonmaster.pistonconfig.staticfields;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigCodec;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ConfigValueKind;
import net.pistonmaster.pistonconfig.core.MergeListStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings("UnusedVariable")
final class StaticConfigDefinitionTest {
  @TempDir
  private Path tempDir;

  @Test
  void scansMultipleHoldersInheritedFieldsCommentsAndGroupedOrder() {
    var definition = StaticConfigDefinition.from(ExtraOptions.class, ServerOptions.class);

    assertIterableEquals(
      List.of(
        ConfigPath.parse("alpha.one"),
        ConfigPath.parse("server.host"),
        ConfigPath.parse("server.port"),
        ConfigPath.parse("server.mode"),
        ConfigPath.parse("server.flags"),
        ConfigPath.parse("server.limits"),
        ConfigPath.parse("server.endpoints"),
        ConfigPath.parse("server.timeout"),
        ConfigPath.parse("server.alias"),
        ConfigPath.parse("server.tags")
      ),
      definition.properties().stream().map(ConfigProperty::path).toList()
    );

    var defaults = definition.defaults(endpointRegistry());

    assertEquals("Root comment.", defaults.root().comment().leadingText().getFirst());
    assertEquals("Server settings.", defaults.find("server").orElseThrow().comment().leadingText().getFirst());
    assertEquals("Server port.", defaults.find("server.port").orElseThrow().comment().leadingText().getFirst());
    assertEquals(25565, defaults.find("server.port").flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals(Duration.ofSeconds(30), definition.get(defaults, ServerOptions.TIMEOUT, endpointRegistry()));
  }

  @Test
  void rejectsDuplicateAndOverlappingPaths() {
    assertThrows(ConfigException.class, () -> StaticConfigDefinition.from(ServerOptions.class, DuplicateOptions.class));
    assertThrows(ConfigException.class, () -> StaticConfigDefinition.from(ParentPathOptions.class));
  }

  @Test
  void supportsParameterizedTypesAndCustomCodecs() {
    var registry = endpointRegistry();
    var definition = StaticConfigDefinition.from(ServerOptions.class);
    var document = definition.defaults(registry);

    document.setNode(ConfigPath.parse("server.flags"), ConfigNode.list()
      .addListValue("fast")
      .addListValue("safe"));
    document.setNode(ConfigPath.parse("server.limits"), ConfigNode.list()
      .addListValue(10)
      .addListValue(20)
      .addListValue(10));
    document.setNode(ConfigPath.parse("server.endpoints"), ConfigNode.object()
      .setNode(ConfigPath.of("PROD"), endpointNode("prod.example.com", 443)));
    document.set("server.alias", "primary");
    document.setNode(ConfigPath.parse("server.tags"), ConfigNode.list()
      .addListValue("a")
      .addListValue("b"));

    assertEquals(List.of("fast", "safe"), definition.get(document, ServerOptions.FLAGS, registry));
    assertEquals(Set.of(10, 20), definition.get(document, ServerOptions.LIMITS, registry));
    assertEquals(Map.of(Mode.PROD, new Endpoint("prod.example.com", 443)), definition.get(document, ServerOptions.ENDPOINTS, registry));
    assertEquals(Optional.of("primary"), definition.get(document, ServerOptions.ALIAS, registry));
    assertArrayEquals(new String[] {"a", "b"}, definition.get(document, ServerOptions.TAGS, registry));
  }

  @Test
  void storeUpdatesDropsUnknownsRewritesInvalidValuesAndSupportsSessionLifecycle() throws IOException {
    var path = tempDir.resolve("server.conf");
    Files.writeString(path, """
      server.port=bad
      server.stale=true
      """);

    var store = StaticConfigStore.builder()
      .holders(ServerOptions.class)
      .loader(new LinesLoader())
      .codecRegistry(endpointRegistry())
      .options(StaticConfigStoreOptions.builder()
        .unknownKeyPolicy(StaticUnknownKeyPolicy.DROP)
        .invalidValuePolicy(StaticInvalidValuePolicy.FALLBACK_AND_REWRITE)
        .build())
      .validator(session -> {
        if (session.get(ServerOptions.PORT) <= 0) {
          throw new ConfigException("Port must be positive.");
        }
      })
      .build();

    var session = store.update(path);
    var written = Files.readString(path);

    assertEquals(25565, session.get(ServerOptions.PORT));
    assertFalse(written.contains("stale"));
    assertTrue(written.contains("server.port=25565"));

    session.set(ServerOptions.PORT, 25566);
    session.save();
    session.set(ServerOptions.PORT, 1);
    session.reload();

    assertEquals(25566, session.get(ServerOptions.PORT));
  }

  @Test
  void storeReadOverridesAreNotPersisted() throws IOException {
    var path = tempDir.resolve("override.conf");
    var store = StaticConfigStore.builder()
      .holders(ServerOptions.class)
      .loader(new LinesLoader())
      .codecRegistry(endpointRegistry())
      .readOverride(document -> document.set("server.port", 30000))
      .build();

    var session = store.update(path);

    assertEquals(30000, session.get(ServerOptions.PORT));
    assertTrue(Files.readString(path).contains("server.port=25565"));
  }

  @Test
  void directResolveReportsMissingAndInvalidSources() {
    var definition = StaticConfigDefinition.from(ServerOptions.class);
    var missing = definition.resolve(ConfigDocument.empty(), ServerOptions.PORT, endpointRegistry(), StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);
    var invalidDocument = ConfigDocument.empty().set("server.port", "bad");
    var invalid = definition.resolve(invalidDocument, ServerOptions.PORT, endpointRegistry(), StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);

    assertTrue(missing.requiresRewrite());
    assertFalse(missing.sourcePresent());
    assertEquals(25565, missing.value());
    assertTrue(invalid.requiresRewrite());
    assertTrue(invalid.sourcePresent());
    assertEquals(25565, invalid.value());
    assertThrows(ConfigException.class, () -> definition.resolve(invalidDocument, ServerOptions.PORT, endpointRegistry(), StaticInvalidValuePolicy.STRICT));
  }

  @Test
  void fallbackRewriteRepairsMixedInvalidParameterizedValuesWithoutTouchingValidNullOptionals() {
    var registry = endpointRegistry();
    var definition = StaticConfigDefinition.from(ServerOptions.class);
    var document = definition.defaults(registry);
    document.setNode(ConfigPath.parse("server.flags"), ConfigNode.scalar("not-a-list"));
    document.setNode(ConfigPath.parse("server.limits"), ConfigNode.list()
      .addListValue(10)
      .addListValue("bad"));
    document.setNode(ConfigPath.parse("server.endpoints"), ConfigNode.object()
      .setNode(ConfigPath.of("DEV"), endpointNode("dev.example.com", 443))
      .setNode(ConfigPath.of("PROD"), endpointNodeWithRawPort("prod.example.com", "bad")));
    document.setNode(ConfigPath.parse("server.alias"), ConfigNode.nullValue());

    var flags = definition.resolve(document, ServerOptions.FLAGS, registry, StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);
    var limits = definition.resolve(document, ServerOptions.LIMITS, registry, StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);
    var endpoints = definition.resolve(document, ServerOptions.ENDPOINTS, registry, StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);
    var alias = definition.resolve(document, ServerOptions.ALIAS, registry, StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);

    assertTrue(flags.requiresRewrite());
    assertTrue(limits.requiresRewrite());
    assertTrue(endpoints.requiresRewrite());
    assertEquals(Optional.empty(), alias.value());
    assertThrows(ConfigException.class, () -> definition.resolve(document, ServerOptions.FLAGS, registry, StaticInvalidValuePolicy.STRICT));

    definition.rewriteInvalidValues(document, registry, StaticInvalidValuePolicy.FALLBACK_AND_REWRITE);

    assertEquals(List.of("default"), definition.get(document, ServerOptions.FLAGS, registry));
    assertEquals(Set.of(10), definition.get(document, ServerOptions.LIMITS, registry));
    assertEquals(Map.of(Mode.DEV, new Endpoint("localhost", 8080)), definition.get(document, ServerOptions.ENDPOINTS, registry));
    assertEquals(Optional.empty(), definition.get(document, ServerOptions.ALIAS, registry));
    assertEquals(ConfigValueKind.NULL, document.find("server.alias").orElseThrow().kind());
  }

  @Test
  void validatorCatchesHolderAndCommentProblems() {
    var validator = new StaticConfigDefinitionValidator();

    assertThrows(ConfigException.class, () -> validator.validateAllPropertiesAreStaticFinal(List.of(InvalidConstantOptions.class)));
    assertThrows(ConfigException.class, () -> validator.validateHolderClassesFinal(List.of(NonFinalOptions.class)));
    assertThrows(ConfigException.class, () -> validator.validateHolderClassesHaveHiddenNoArgConstructor(List.of(PublicConstructorOptions.class)));
    assertThrows(ConfigException.class, () -> validator.validateHasCommentOnEveryProperty(
      StaticConfigDefinition.from(MissingCommentOptions.class),
      _ -> true
    ));
    assertThrows(ConfigException.class, () -> validator.validateHasAllEnumEntriesInComment(
      StaticConfigDefinition.from(MissingEnumCommentOptions.class),
      _ -> true
    ));

    validator.validate(GoodValidationOptions.class);
  }

  private static ConfigCodecRegistry endpointRegistry() {
    return new ConfigCodecRegistry()
      .register(Endpoint.class, new ConfigCodec<Endpoint>() {
        @Override
        public ConfigNode encode(Endpoint value, ConfigCodecRegistry registry) {
          return endpointNode(value.host(), value.port());
        }

        @Override
        public Endpoint decode(ConfigNode node, ConfigCodecRegistry registry) {
          return new Endpoint(
            node.find(ConfigPath.of("host")).flatMap(ConfigNode::asString).orElseThrow(),
            node.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow()
          );
        }
      });
  }

  private static ConfigNode endpointNode(String host, int port) {
    return ConfigNode.object()
      .set(ConfigPath.of("host"), host)
      .set(ConfigPath.of("port"), port);
  }

  private static ConfigNode endpointNodeWithRawPort(String host, Object port) {
    return ConfigNode.object()
      .set(ConfigPath.of("host"), host)
      .set(ConfigPath.of("port"), port);
  }

  private static class BaseOptions {
    static final ConfigProperty<String> HOST = ConfigProperty.of("server.host", String.class, "localhost")
      .withComment("Server host.");

    private BaseOptions() {
    }
  }

  private static final class ExtraOptions {
    static final ConfigProperty<Integer> ALPHA = ConfigProperty.of("alpha.one", Integer.class, 1)
      .withComment("Alpha value.");

    private ExtraOptions() {
    }
  }

  private static final class ServerOptions extends BaseOptions implements StaticConfigComments {
    @ConfigComment("Server port.")
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("server.port", Integer.class, 25565);
    static final ConfigProperty<Mode> MODE = ConfigProperty.of("server.mode", Mode.class, Mode.DEV)
      .withComment("Allowed values: DEV, PROD.");
    static final ConfigProperty<List<String>> FLAGS = ConfigProperty.of(
      "server.flags",
      ConfigType.listOf(ConfigType.of(String.class)),
      List.of("default")
    ).withComment("Feature flags.");
    static final ConfigProperty<Set<Integer>> LIMITS = ConfigProperty.of(
      "server.limits",
      ConfigType.setOf(ConfigType.of(Integer.class)),
      Set.of(10)
    ).withComment("Numeric limits.");
    static final ConfigProperty<Map<Mode, Endpoint>> ENDPOINTS = ConfigProperty.of(
      "server.endpoints",
      ConfigType.mapOf(ConfigType.of(Mode.class), ConfigType.of(Endpoint.class)),
      Map.of(Mode.DEV, new Endpoint("localhost", 8080))
    ).withComment("Mode endpoints.");
    static final ConfigProperty<Duration> TIMEOUT = ConfigProperty.of("server.timeout", Duration.class, Duration.ofSeconds(30))
      .withComment("Request timeout.");
    static final ConfigProperty<Optional<String>> ALIAS = ConfigProperty.of(
      "server.alias",
      ConfigType.optionalOf(ConfigType.of(String.class)),
      Optional.empty()
    ).withComment("Optional alias.");
    static final ConfigProperty<String[]> TAGS = ConfigProperty.of(
      "server.tags",
      ConfigType.arrayOf(ConfigType.of(String.class), String[]::new),
      new String[] {"default"}
    ).withComment("Tag list.");

    private ServerOptions() {
    }

    @Override
    public void registerComments(StaticConfigCommentRegistry comments) {
      comments.setRootComment("Root comment.");
      comments.setComment("server", "Server settings.");
      comments.setFooterComment("Footer comment.");
    }
  }

  private static final class DuplicateOptions {
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("server.port", Integer.class, 1234)
      .withComment("Duplicate port.");

    private DuplicateOptions() {
    }
  }

  private static final class ParentPathOptions {
    static final ConfigProperty<String> SERVER = ConfigProperty.of("server", String.class, "root")
      .withComment("Server root.");
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("server.port", Integer.class, 25565)
      .withComment("Server port.");

    private ParentPathOptions() {
    }
  }

  private static final class InvalidConstantOptions {
    static ConfigProperty<Integer> PORT = ConfigProperty.of("port", Integer.class, 25565);

    private InvalidConstantOptions() {
    }
  }

  private static class NonFinalOptions {
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("port", Integer.class, 25565)
      .withComment("Port.");

    private NonFinalOptions() {
    }
  }

  private static final class PublicConstructorOptions {
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("port", Integer.class, 25565)
      .withComment("Port.");

    PublicConstructorOptions() {
    }
  }

  private static final class MissingCommentOptions {
    static final ConfigProperty<Integer> PORT = ConfigProperty.of("port", Integer.class, 25565);

    private MissingCommentOptions() {
    }
  }

  private static final class MissingEnumCommentOptions {
    static final ConfigProperty<Mode> MODE = ConfigProperty.of("mode", Mode.class, Mode.DEV)
      .withComment("Allowed values: DEV.");

    private MissingEnumCommentOptions() {
    }
  }

  private static final class GoodValidationOptions {
    static final ConfigProperty<Mode> MODE = ConfigProperty.of("mode", Mode.class, Mode.DEV)
      .withComment("Allowed values: DEV, PROD.");

    private GoodValidationOptions() {
    }
  }

  private record Endpoint(String host, int port) {
  }

  private enum Mode {
    DEV,
    PROD
  }

  private static final class LinesLoader implements ConfigLoader {
    @Override
    public ConfigDocument load(Reader reader) {
      var document = ConfigDocument.empty();
      try (var buffered = new BufferedReader(reader)) {
        String line;
        while ((line = buffered.readLine()) != null) {
          if (line.isBlank() || !line.contains("=")) {
            continue;
          }

          var separator = line.indexOf('=');
          document.set(line.substring(0, separator), parse(line.substring(separator + 1)));
        }
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
      return document;
    }

    @Override
    public void save(ConfigDocument document, Writer writer) {
      try {
        for (var entry : flatten(document).entrySet()) {
          writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
        }
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }

    private static Object parse(String value) {
      if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
        return Boolean.parseBoolean(value);
      }
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException _) {
        return value;
      }
    }

    private static Map<String, Object> flatten(ConfigDocument document) {
      var values = new LinkedHashMap<String, Object>();
      flatten(ConfigPath.root(), document.root(), values);
      return values;
    }

    private static void flatten(ConfigPath path, ConfigNode node, Map<String, Object> values) {
      if (node.isObject()) {
        for (var entry : node.objectChildren().entrySet()) {
          var child = path.isRoot() ? ConfigPath.of(entry.getKey()) : path.child(entry.getKey());
          flatten(child, entry.getValue(), values);
        }
        return;
      }

      if (node.isList()) {
        values.put(path.toString(), node.listChildren().stream()
          .map(child -> child.asString().orElse(""))
          .toList());
        return;
      }

      values.put(path.toString(), node.rawValue());
    }
  }
}
