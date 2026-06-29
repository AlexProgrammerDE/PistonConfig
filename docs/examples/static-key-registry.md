---
layout: default
title: Static Key Registry
description: Centralize typed config keys, defaults, comments, and reads with static fields.
---

# Static Key Registry

This example uses static properties when many classes need to read the same keys without repeating string paths.

## Registry

```java
final class ServerOptions {
  static final ConfigProperty<String> HOST = ConfigProperty
    .of("server.host", String.class, "0.0.0.0")
    .withComment("Address used by the public listener.");

  static final ConfigProperty<Integer> PORT = ConfigProperty
    .of("server.port", Integer.class, 25565)
    .withComment("Port used by the public listener.");

  static final ConfigProperty<Boolean> METRICS = ConfigProperty
    .of("features.metrics", Boolean.class, true)
    .withComment("Whether metrics should be collected.");

  private ServerOptions() {
  }
}
```

## Defaults and Reads

```java
var codecs = new ConfigCodecRegistry();
var definition = StaticConfigDefinition.from(ServerOptions.class);

definition.applyDefaults(document, codecs);

var host = definition.get(document, ServerOptions.HOST, codecs);
var port = definition.get(document, ServerOptions.PORT, codecs);
var metrics = definition.get(document, ServerOptions.METRICS, codecs);
```

## Why It Helps

| Problem | Static property behavior |
| --- | --- |
| Repeated string paths | Paths live in one declaration. |
| Missing defaults | Each key carries its own default value. |
| Comments drift | Comments stay near the key they describe. |
| Type mistakes | Reads use the declared `Class<T>`. |

Static properties are a good fit for libraries, plugins, and larger applications where config keys are used in multiple places.
