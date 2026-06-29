---
layout: default
title: Testing Configs
description: Test PistonConfig codecs, mappings, migrations, and backend round trips.
---

# Testing Configs

Test config code at the same layer where it can break: codecs, mappings, migrations, merge behavior, and backend round trips.

## Test Codecs Directly

```java
@Test
void endpointCodecRoundTrips() {
  var codecs = new ConfigCodecRegistry()
    .register(Endpoint.class, endpointCodec());

  var node = codecs.encode(new Endpoint("localhost", 25565));
  var decoded = codecs.decode(node, Endpoint.class);

  assertEquals(new Endpoint("localhost", 25565), decoded);
}
```

Codec tests should check structure and failure behavior. Avoid tying tests to incidental wording in comments unless comments are the feature under test.

## Test Defaults

```java
@Test
void staticDefaultsIncludeComments() {
  var definition = StaticConfigDefinition.from(ServerOptions.class);
  var defaults = definition.defaults(new ConfigCodecRegistry());

  var port = defaults.find("server.port").orElseThrow();

  assertEquals(25565, port.asInt().orElseThrow());
  assertFalse(port.comment().isEmpty());
}
```

Default tests are useful when annotations or static properties are the source of truth for generated files.

## Test Migrations

```java
@Test
void migrationRenamesBindToHost() {
  var document = ConfigDocument.empty()
    .set("server.bind", "127.0.0.1");

  MigrationRegistry.builder()
    .add(Migrations.migration(1, config ->
      Migrations.rename(config, "server.bind", "server.host")))
    .build()
    .migrate(document);

  assertEquals("127.0.0.1", document.find("server.host").flatMap(ConfigNode::asString).orElseThrow());
  assertTrue(document.find("server.bind").isEmpty());
}
```

Migration tests should start from old document shapes and assert the new shape.

## Test Format Round Trips

```java
@Test
void yamlRoundTripKeepsComment() {
  var loader = YamlConfigFormat.INSTANCE.loader();
  var document = ConfigDocument.empty()
    .set("server.port", 25565);

  document.root()
    .getOrCreate(ConfigPath.parse("server.port"))
    .setComment(ConfigComment.lines("Public listener port."));

  var output = new StringWriter();
  loader.save(document, output);

  var loaded = loader.load(new StringReader(output.toString()));
  assertEquals(List.of("Public listener port."), loaded.find("server.port").orElseThrow().comment().leadingText());
}
```

Round-trip tests should be scoped to backend features that matter to your application.

## Test Environment Overrides With Explicit Maps

```java
var overrides = EnvironmentOverrides.of(
  "app",
  "app",
  Map.of("APP_SERVER_PORT", "25566"),
  Map.of()
);

overrides.applyTo(document);
```

Explicit maps avoid depending on the test process environment.
