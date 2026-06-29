# PistonConfig

PistonConfig is a Java 25 configuration library for projects that need one abstraction across multiple config formats. It combines a lossless core document model, comment-aware backends, type-aware object configs, scalar codecs, static field declarations, environment overrides, and ordered migrations.

The Maven group is `net.pistonmaster`. Module artifacts use the format `pistonconfig-<module>`.

## Modules

| Module | Purpose |
| --- | --- |
| `pistonconfig-core` | Format-agnostic config tree, comments, decorations, loaders, codecs, and default merging. |
| `pistonconfig-yaml` | SnakeYAML-backed YAML backend with key/value comments, scalar style, collection style, tags, anchors, and source locations mapped into core. |
| `pistonconfig-properties` | Apache Commons Configuration-backed `.properties` backend with comments, separators, key order, and layout attributes. |
| `pistonconfig-json` | `json5-java`-backed JSON, JSONC, and JSON5 backend with comment and numeric style support. |
| `pistonconfig-toml` | Night Config-backed TOML backend with commented config support. |
| `pistonconfig-hocon` | Lightbend Config-backed HOCON backend. |
| `pistonconfig-annotations` | Type-aware object config mapper and generic config store API. |
| `pistonconfig-static-fields` | ConfigMe-style static `ConfigProperty<T>` declarations, typed static stores, and holder validation. |
| `pistonconfig-env` | System property and environment variable overrides. |
| `pistonconfig-migrations` | Flyway-style ordered config migrations. |

## Install

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
}
```

GitHub Packages is also configured as a release target. Consumers need an authenticated Maven repository:

```kotlin
repositories {
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/AlexProgrammerDE/PistonConfig")
    credentials {
      username = providers.gradleProperty("githubPackagesUsername").orNull
      password = providers.gradleProperty("githubPackagesToken").orNull
    }
  }
}
```

## Manual Config API

```java
var document = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565);

var port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElseThrow();
```

## Merge Defaults

```java
var current = ConfigDocument.empty()
  .set("server.port", 25566);

var defaults = ConfigDocument.empty()
  .set("server.port", 25565)
  .set("server.host", "0.0.0.0");

current.mergeDefaults(defaults, MergeOptions.conservative());
```

## Annotation Configs

```java
@ConfigPathPrefix("server")
record ServerConfig(
  @ConfigComment("Port used by the server.")
  int port
) {
  ServerConfig() {
    this(25565);
  }
}

var mapper = new AnnotatedConfigMapper();
var defaults = mapper.writeDefaults(ServerConfig.class);
var config = mapper.read(defaults, ServerConfig.class);
```

## Typed Store

```java
var store = ConfigStores.forType(ServerConfig.class)
  .format(YamlConfigFormat.INSTANCE)
  .build();

var config = store.update(Path.of("config.yml"));
```

The typed mapper supports records, POJOs with no-args constructors, nested objects, inherited fields, enums, common Java scalar value types, arrays, lists, sets, maps, custom serializers, and polymorphic members.

## Static Field Configs

```java
final class ServerOptions {
  @ConfigComment("Port used by the server.")
  static final ConfigProperty<Integer> PORT =
    ConfigProperty.of("server.port", Integer.class, 25565);
}

var session = StaticConfigStore.builder()
  .holders(ServerOptions.class)
  .format(YamlConfigFormat.INSTANCE)
  .build()
  .update(Path.of("config.yml"));

int port = session.get(ServerOptions.PORT);
```

## Migrations

```java
var registry = MigrationRegistry.builder()
  .addMigration(ConfigMigration.builder()
    .version(1)
    .action(config -> Migrations.rename(config, "old.port", "server.port"))
    .build())
  .build();

registry.migrate(document);
```

## Build

```bash
./gradlew test
./gradlew build
```

The project publishes sources and Javadocs JARs for every module.
