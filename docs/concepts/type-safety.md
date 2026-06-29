---
layout: default
title: Type Safety
description: How PistonConfig uses typed stores, records, static properties, and codecs for type-safe config access.
---

# Type Safety

PistonConfig keeps the core document flexible, then adds type safety at the access boundary.

## The Core Is Structural

`ConfigNode` can hold objects, lists, scalars, and nulls. That is necessary because format backends need to represent arbitrary files.

```java
var value = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);
```

Manual reads are explicit and safe because conversions return `Optional`.

## Typed Stores Add Object Configs

`ConfigStore<T>` converts between a file and a Java config type.

```java
var store = ConfigStores.forType(ServerConfig.class)
  .format(YamlConfigFormat.INSTANCE)
  .build();

var config = store.update(Path.of("config.yml"));
```

Use typed stores for records, object configs, nested collections, maps, and validation.

## Static Properties Bind Path and Type

```java
static final ConfigProperty<Integer> PORT = ConfigProperty.<Integer>builder()
  .path(ConfigPath.parse("server.port"))
  .type(Integer.class)
  .defaultValue(25565)
  .build();
```

The declaration carries the path, Java type, default value, and comments together.

## Annotations Bind Members and Defaults

Annotation mapping uses field or record component types and Java default values.

```java
record ServerConfig(int port) {
  ServerConfig() {
    this(25565);
  }
}
```

This is convenient when the application wants a config object rather than scattered property reads.

## Codecs Support Scalar and Static-Field Access

`ConfigCodec<T>` converts between `ConfigNode` and a Java type for APIs that work directly with codecs.

```java
var endpoint = codecs.decode(node, Endpoint.class);
```

Use codecs for static properties or direct document integrations. Use typed serializers for annotation configs.

## Boundary Rule

Keep parser input flexible and application state typed. Convert from `ConfigDocument` into typed application objects before the rest of the application starts.
