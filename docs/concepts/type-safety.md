---
layout: default
title: Type Safety
description: How PistonConfig uses codecs, records, static properties, and annotations for type-safe config access.
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

## Codecs Add Domain Types

`ConfigCodec<T>` converts between `ConfigNode` and a Java type.

```java
var endpoint = codecs.decode(node, Endpoint.class);
```

Use codecs for records, value objects, and reusable nested config shapes.

## Static Properties Bind Path and Type

```java
static final ConfigProperty<Integer> PORT = ConfigProperty
  .of("server.port", Integer.class, 25565);
```

The declaration carries the path, Java type, default value, and comments together.

## Annotations Bind Fields and Defaults

Annotation mapping uses field types and Java default values.

```java
final class ServerConfig {
  int port = 25565;
}
```

This is convenient when the application wants a config object rather than scattered property reads.

## Boundary Rule

Keep parser input flexible and application state typed. Convert from `ConfigDocument` into typed application objects before the rest of the application starts.
