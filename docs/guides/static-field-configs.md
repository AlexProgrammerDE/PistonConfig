---
layout: default
title: Static Field Configs
description: Declare typed static config keys with defaults and comments.
---

# Static Field Configs

Use `pistonconfig-static-fields` when you want a central set of typed keys, similar to ConfigMe-style configuration declarations.

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

Properties carry the path, Java type, default value, and comment together.

## Generate Defaults

```java
var codecs = new ConfigCodecRegistry();
var definition = StaticConfigDefinition.from(ServerOptions.class);
var defaults = definition.defaults(codecs);
```

The definition reads static `ConfigProperty<?>` fields and sorts them by path for stable output.

## Apply Defaults

```java
definition.applyDefaults(document, codecs);
```

This merges the generated defaults with the current document using conservative merge behavior.

## Read a Value

```java
int port = definition.get(document, ServerOptions.PORT, codecs);
```

The property type drives decoding through `ConfigCodecRegistry`, so custom property types work once a codec is registered.
