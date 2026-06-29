---
layout: default
title: Annotation Configs
description: Map annotated Java objects to PistonConfig documents.
---

# Annotation Configs

Use `pistonconfig-annotations` when a Java object should define default values, names, prefixes, and comments.

## Define a Config Class

```java
@ConfigPathPrefix("server")
final class ServerConfig {
  @ConfigName("bind-address")
  @ConfigComment("Address used by the public listener.")
  String host = "0.0.0.0";

  @ConfigComment("Port used by the public listener.")
  int port = 25565;

  @ConfigIgnore
  transient String runtimeOnly = "not persisted";
}
```

The current mapper works with fields and requires a no-args constructor when reading a new instance.

## Write Defaults

```java
var mapper = new AnnotatedConfigMapper();
var defaults = mapper.writeDefaults(new ServerConfig());
```

Comments from `@ConfigComment` are attached to the generated nodes.

## Read Values

```java
var config = mapper.read(document, ServerConfig.class);
```

Only fields present in the document replace values on the target object. Existing Java defaults remain useful fallback values.

## Reuse a Codec Registry

```java
var codecs = new ConfigCodecRegistry()
  .register(Endpoint.class, endpointCodec);

var mapper = new AnnotatedConfigMapper(codecs);
```

Custom codecs let annotation configs use application-specific value types without making the mapper know about those types.
