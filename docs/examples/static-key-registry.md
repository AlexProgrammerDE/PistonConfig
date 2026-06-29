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
  static final ConfigProperty<String> HOST = ConfigProperty.<String>builder()
    .path(ConfigPath.parse("server.host"))
    .type(String.class)
    .defaultValue("0.0.0.0")
    .comment(comment("Address used by the public listener."))
    .build();

  static final ConfigProperty<Integer> PORT = ConfigProperty.<Integer>builder()
    .path(ConfigPath.parse("server.port"))
    .type(Integer.class)
    .defaultValue(25565)
    .comment(comment("Port used by the public listener."))
    .build();

  static final ConfigProperty<Boolean> METRICS = ConfigProperty.<Boolean>builder()
    .path(ConfigPath.parse("features.metrics"))
    .type(Boolean.class)
    .defaultValue(true)
    .comment(comment("Whether metrics should be collected."))
    .build();

  private ServerOptions() {
  }

  private static ConfigComment comment(String text) {
    return ConfigComment.builder()
      .addLeading(ConfigCommentLine.builder()
        .text(text)
        .type(ConfigCommentType.BLOCK)
        .marker(ConfigCommentMarker.HASH)
        .build())
      .build();
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
