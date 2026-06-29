---
layout: default
title: Access Styles
description: Compare PistonConfig manual nodes, typed stores, static fields, and custom serialization.
---

# Access Styles

PistonConfig does not force one declaration style. Every access style reads or writes the same `ConfigDocument`, so projects can mix styles where they make sense.

## Manual API

Manual document edits are the lowest-level application API.

```java
document.set("server.port", 25565);
var port = document.find("server.port").flatMap(ConfigNode::asInt).orElse(25565);
```

Choose manual access when building tools, converters, migration helpers, or workflows that need direct control over comments and metadata.

## Annotation Configs

Annotation mapping lets Java records and classes define defaults, comments, and typed reads.

```java
record ServerConfig(
  @ConfigComment("Public listener port.")
  int port
) {
  ServerConfig() {
    this(25565);
  }
}
```

Choose annotations when your application wants a config object as the startup boundary.

## Static Fields

Static properties centralize typed keys.

```java
@ConfigComment("Public listener port.")
static final ConfigProperty<Integer> PORT =
  ConfigProperty.of("server.port", Integer.class, 25565);

var session = StaticConfigStore.builder()
  .holders(ServerOptions.class)
  .format(YamlConfigFormat.INSTANCE)
  .build()
  .update(Path.of("config.yml"));

int port = session.get(PORT);
```

Choose static fields when keys are shared across code and you want one declaration for path, type, default, comments, and store behavior.

## Custom Serialization

Serializers and codecs map domain types to nodes.

```java
record Endpoint(String host, int port) {
}
```

Choose typed serializers for annotation configs. Choose core codecs for static fields and direct codec use.

## Mixing Styles

| Need | Style |
| --- | --- |
| Full tree control | Manual API |
| Typed config object | Annotation store |
| Shared typed keys | Static field store |
| Domain value objects | Custom serialization |
| Deployment overrides | Environment module |
| Release upgrades | Migration module |

The common boundary is always `ConfigDocument`.
