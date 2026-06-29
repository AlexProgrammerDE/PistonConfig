---
layout: default
title: Multi-Format Apps
description: Build applications that support more than one PistonConfig backend.
---

# Multi-Format Apps

Use this guide when one application should accept several file formats while keeping business logic independent from the chosen backend.

## Add the Backends

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
  implementation("net.pistonmaster:pistonconfig-toml")
  implementation("net.pistonmaster:pistonconfig-json")
  implementation("net.pistonmaster:pistonconfig-hocon")
  implementation("net.pistonmaster:pistonconfig-properties")
}
```

Install only the backends you actually support. The core model stays the same after a file is loaded.

## Register Formats

```java
static final List<ConfigFormat> FORMATS = List.of(
  YamlConfigFormat.INSTANCE,
  TomlConfigFormat.INSTANCE,
  JsonConfigFormat.INSTANCE,
  HoconConfigFormat.INSTANCE,
  PropertiesConfigFormat.INSTANCE
);
```

Keep format selection near file I/O. Application code should receive `ConfigDocument`, not a parser-specific object.

## Select a Loader

```java
static ConfigLoader loaderFor(Path path) {
  var extension = extension(path.getFileName().toString());
  return FORMATS.stream()
    .filter(format -> format.extensions().contains(extension))
    .findFirst()
    .orElseThrow(() -> new ConfigException("Unsupported config extension: " + extension))
    .loader();
}

static String extension(String fileName) {
  var index = fileName.lastIndexOf('.');
  return index < 0 ? "" : fileName.substring(index + 1).toLowerCase(Locale.ROOT);
}
```

Use `ConfigFormat.extensions()` instead of duplicating extension lists across your application.

## Keep Defaults Format-Neutral

```java
var defaults = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565)
  .set("features.metrics", true);
```

Defaults can come from manual construction, annotations, static fields, or another loaded file. They merge through the same API.

## Convert Between Formats

```java
var sourceLoader = loaderFor(Path.of("config.yml"));
var targetLoader = loaderFor(Path.of("config.toml"));
var document = ConfigLoaders.load(Path.of("config.yml"), sourceLoader);

ConfigLoaders.save(Path.of("config.toml"), targetLoader, document);
```

Format-specific metadata remains inspectable in the document, but a target backend can only write details it supports.

## Recommended Boundary

| Layer | Owns |
| --- | --- |
| File boundary | loader selection, path validation, save location |
| Config workflow | migrations, default merging, overrides |
| Application mapping | codecs, annotations, static properties, manual reads |
| Business logic | typed application values only |

This keeps parser differences from leaking into application behavior.
