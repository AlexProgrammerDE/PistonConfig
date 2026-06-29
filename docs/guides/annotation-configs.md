---
layout: default
title: Annotation Configs
description: Map annotated Java objects to PistonConfig documents.
---

# Annotation Configs

Use `pistonconfig-annotations` when a Java class should define default values, paths, field names, and comments.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-annotations")
}
```

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

The mapper works with fields. Reading into a new instance requires a no-args constructor.

## Generate Defaults

```java
var mapper = new AnnotatedConfigMapper();
var defaults = mapper.writeDefaults(new ServerConfig());
```

`@ConfigComment` values become node comments. `@ConfigName` changes the final path segment. `@ConfigPathPrefix` prefixes every mapped field.

## Read a Config Object

```java
var document = ConfigLoaders.load(Path.of("config.yml"), YamlConfigFormat.INSTANCE.loader());
var config = mapper.read(document, ServerConfig.class);
```

Missing fields keep the Java default values from the newly constructed instance.

## Read Into an Existing Object

```java
var config = new ServerConfig();
mapper.readInto(document, config);
```

Use this form when the target object needs constructor setup or values supplied by the application before file values are applied.

## Use Custom Types

```java
var mapper = new AnnotatedConfigMapper(codecRegistry);
```

The mapper delegates field encoding and decoding to `ConfigCodecRegistry`. Register a [custom codec](custom-codecs.html) before using application-specific value types.

## Field Selection Rules

| Field kind | Mapped |
| --- | --- |
| Instance field | yes |
| Static field | no |
| Transient field | no |
| Field annotated with `@ConfigIgnore` | no |
| Inherited instance field | yes |
