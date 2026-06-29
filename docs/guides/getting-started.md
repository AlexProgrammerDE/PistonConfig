---
layout: default
title: Getting Started
description: Load, merge, edit, and save a config document.
---

# Getting Started

This guide shows the core workflow: load a document, merge defaults, read values, and save the result.

## Add Dependencies

```kotlin
dependencies {
  implementation("net.pistonmaster:pistonconfig-core:0.1.0-SNAPSHOT")
  implementation("net.pistonmaster:pistonconfig-yaml:0.1.0-SNAPSHOT")
}
```

## Create Defaults

```java
var defaults = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565);
```

## Load and Merge

```java
var path = Path.of("config.yml");
var loader = YamlConfigFormat.INSTANCE.loader();

var document = Files.exists(path)
  ? ConfigLoaders.load(path, loader)
  : ConfigDocument.empty();

document.mergeDefaults(defaults, MergeOptions.conservative());
ConfigLoaders.save(path, loader, document);
```

## Read Values

```java
int port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);
```

Use `ConfigCodecRegistry` when a value should map to a custom Java type.
