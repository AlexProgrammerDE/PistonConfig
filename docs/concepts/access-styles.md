---
layout: default
title: Access Styles
description: Compare PistonConfig manual nodes, annotations, static fields, and custom codecs.
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

Annotation mapping lets Java classes define defaults and comments.

```java
final class ServerConfig {
  @ConfigComment("Public listener port.")
  int port = 25565;
}
```

Choose annotations when your application already has config classes and field defaults are the source of truth.

## Static Fields

Static properties centralize typed keys.

```java
static final ConfigProperty<Integer> PORT = ConfigProperty
  .of("server.port", Integer.class, 25565);
```

Choose static fields when keys are shared across code and you want one declaration for path, type, default, and comment.

## Custom Codecs

Codecs map domain types to nodes.

```java
record Endpoint(String host, int port) {
}
```

Choose codecs when a config value should be a record, value object, enum-like domain type, or other application type.

## Mixing Styles

| Need | Style |
| --- | --- |
| Full tree control | Manual API |
| Config class defaults | Annotations |
| Shared typed keys | Static fields |
| Domain value objects | Custom codecs |
| Deployment overrides | Environment module |
| Release upgrades | Migration module |

The common boundary is always `ConfigDocument`.
