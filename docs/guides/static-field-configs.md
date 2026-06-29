---
layout: default
title: Static Field Configs
description: Declare typed static config keys with defaults and comments.
---

# Static Field Configs

Use `pistonconfig-static-fields` when you want a central registry of typed keys, similar to ConfigMe-style property declarations.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-static-fields")
}
```

## Declare Properties

```java
final class ServerOptions {
  static final ConfigProperty<String> HOST = ConfigProperty
    .of("server.host", String.class, "0.0.0.0")
    .withComment("Address used by the public listener.");

  static final ConfigProperty<Integer> PORT = ConfigProperty
    .of("server.port", Integer.class, 25565)
    .withComment("Port used by the public listener.");
}
```

Each property carries a path, Java type, default value, and comment.

## Build a Definition

```java
var codecs = new ConfigCodecRegistry();
var definition = StaticConfigDefinition.from(ServerOptions.class);
```

The definition reads static `ConfigProperty<?>` fields and sorts them by path for stable output.

## Generate Defaults

```java
var defaults = definition.defaults(codecs);
```

The default document can be merged into a loaded user document:

```java
definition.applyDefaults(document, codecs);
```

## Read Values

```java
int port = definition.get(document, ServerOptions.PORT, codecs);
```

If the document is missing the path, `get` returns the property's default value. If the node exists, it decodes through the registry.

## Use Custom Property Types

```java
static final ConfigProperty<Endpoint> ENDPOINT = ConfigProperty
  .of("endpoint", Endpoint.class, new Endpoint("localhost", 25565));
```

Register a matching codec before generating defaults or reading values.

## When to Choose This Style

<div class="decision">
  <h3>Choose static fields when keys are shared across code.</h3>
  <p>They make call sites explicit, avoid repeated string paths, and keep defaults close to the key definition.</p>
</div>
