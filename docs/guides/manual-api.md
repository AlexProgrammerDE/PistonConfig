---
layout: default
title: Manual API
description: Work directly with configuration documents and nodes.
---

# Manual API

Use the manual API when code needs direct control over a configuration tree. It is the lowest-level application API above individual format loaders.

## Create a Document

```java
var document = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565)
  .set("features.whitelist", true);
```

`ConfigDocument` always has an object root. Dotted strings are parsed as `ConfigPath` values.

## Address Paths Safely

```java
var simple = ConfigPath.parse("server.port");
var literalDot = ConfigPath.of("database.url");
var escapedDot = ConfigPath.parse("database\\.url");
```

Use `ConfigPath.of(...)` when a single key contains a dot. Use `ConfigPath.parse(...)` for normal dotted paths.

## Read Values

```java
var port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);

var host = document.find("server.host")
  .flatMap(ConfigNode::asString)
  .orElse("0.0.0.0");
```

Scalar accessors convert simple string values where that conversion is safe, such as `"25565"` to an integer or `"true"` to a boolean.

## Add Comments

```java
document.root()
  .getOrCreate(ConfigPath.parse("server.port"))
  .setComment(ConfigComment.ofPlain(
    List.of("Port used by the public listener."),
    "Restart required",
    List.of()
  ));
```

Core comments distinguish leading, inline, trailing, blank, block, and marker information. Format modules keep the parts they can express.

## Preserve Decorations

```java
document.root()
  .getOrCreate(ConfigPath.parse("server"))
  .decorate(decorations -> decorations.withCollectionStyle(ConfigCollectionStyle.BLOCK));
```

Decorations are source-level details that are not part of the typed value, such as key comments, scalar style, collection style, and source locations.

## Load and Save

```java
var loader = YamlConfigFormat.INSTANCE.loader();
var path = Path.of("config.yml");

var document = ConfigLoaders.load(path, loader);
ConfigLoaders.save(path, loader, document);
```

Loaders work with `Reader` and `Writer`. `ConfigLoaders` is the path-oriented convenience API.

## Custom Codecs

```java
record Endpoint(String host, int port) {
}

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
      var host = node.find(ConfigPath.of("host"))
        .flatMap(ConfigNode::asString)
        .orElseThrow();
      var port = node.find(ConfigPath.of("port"))
        .flatMap(ConfigNode::asInt)
        .orElseThrow();
      return new Endpoint(host, port);
    }
  });
```

The same registry can be passed to annotation and static-field APIs.
