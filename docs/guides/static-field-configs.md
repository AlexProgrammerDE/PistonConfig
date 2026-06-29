---
layout: default
title: Static Field Configs
description: Declare typed static config keys with defaults, comments, stores, and validation.
---

# Static Field Configs

Use `pistonconfig-static-fields` when config keys need to be shared across application code. Each property keeps its path, type, default value, and comment in one declaration.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-static-fields")
  implementation("net.pistonmaster:pistonconfig-yaml")
}
```

## Declare Properties

```java
final class ServerOptions implements StaticConfigComments {
  @ConfigComment("Address used by the public listener.")
  static final ConfigProperty<String> HOST =
    ConfigProperty.of("server.host", String.class, "0.0.0.0");

  @ConfigComment("Port used by the public listener.")
  static final ConfigProperty<Integer> PORT =
    ConfigProperty.of("server.port", Integer.class, 25565);

  static final ConfigProperty<List<String>> FLAGS = ConfigProperty.of(
    "server.flags",
    ConfigType.listOf(ConfigType.of(String.class)),
    List.of("default")
  ).withComment("Feature flags enabled at startup.");

  private ServerOptions() {
  }

  @Override
  public void registerComments(StaticConfigCommentRegistry comments) {
    comments.setRootComment("Server configuration.");
    comments.setComment("server", "Network listener settings.");
  }
}
```

`ConfigProperty.of` covers simple codec-backed types. Use `ConfigType` for parameterized values such as lists, sets, maps, optionals, and arrays.

## Update a File

```java
var store = StaticConfigStore.builder()
  .holders(ServerOptions.class)
  .format(YamlConfigFormat.INSTANCE)
  .options(StaticConfigStoreOptions.builder()
    .unknownKeyPolicy(StaticUnknownKeyPolicy.PRESERVE)
    .invalidValuePolicy(StaticInvalidValuePolicy.FALLBACK_AND_REWRITE)
    .build())
  .build();

var session = store.update(Path.of("config.yml"));
```

`update` creates the file when it is missing, runs configured document migrations, merges defaults, refreshes generated comments, rewrites invalid declared values when configured, saves the document, and returns a stateful session.

## Read and Write Values

```java
int port = session.get(ServerOptions.PORT);

session.set(ServerOptions.PORT, 25566);
session.save();
session.reload();
```

Use `resolve` when code needs to know whether a value came from the source document or from a fallback:

```java
var value = session.resolve(ServerOptions.PORT);

if (value.requiresRewrite()) {
  logger.warn("Using default for {}", value.property().path());
}
```

Direct `StaticConfigDefinition.get` stays strict for present values. It returns the default only when the path is missing.

## Use Parameterized Types

```java
enum Mode {
  DEV,
  PROD
}

record Endpoint(String host, int port) {
}

static final ConfigProperty<Map<Mode, Endpoint>> ENDPOINTS = ConfigProperty.of(
  "server.endpoints",
  ConfigType.mapOf(ConfigType.of(Mode.class), ConfigType.of(Endpoint.class)),
  Map.of(Mode.DEV, new Endpoint("localhost", 8080))
).withComment("Endpoint per runtime mode.");
```

Register a codec for custom simple values:

```java
var codecs = new ConfigCodecRegistry()
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
```

Then pass the registry to the store:

```java
var store = StaticConfigStore.builder()
  .holders(ServerOptions.class)
  .format(YamlConfigFormat.INSTANCE)
  .codecRegistry(codecs)
  .build();
```

## Validate Holder Classes

Use `StaticConfigDefinitionValidator` in unit tests:

```java
@Test
void staticConfigDeclarationsAreValid() {
  new StaticConfigDefinitionValidator().validate(ServerOptions.class);
}
```

The default validation checks that property fields are `static final`, holder classes are final with one private no-args constructor, every property has a comment, comment lines fit the default length limit, enum comments list every enum constant, and defaults can encode and decode.

## When to Choose Static Fields

Choose static fields when config keys are used in multiple services, commands, or libraries. Choose annotation configs when the application wants one typed config object as its startup boundary.
