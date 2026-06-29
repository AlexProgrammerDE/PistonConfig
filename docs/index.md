---
layout: default
title: PistonConfig
description: Format-agnostic Java configuration library.
---

# PistonConfig

PistonConfig is a Java 25 library for building configuration systems without coupling your application to YAML, TOML, HOCON, JSON, properties, or one specific mapping style.

The core module owns the document model, comments, source decorations, typed codecs, and merge behavior. Format modules translate established parser libraries into that core model so comments, scalar styles, collection styles, key metadata, source locations, and backend attributes stay inspectable from one API.

## Install

```kotlin
dependencies {
  implementation("net.pistonmaster:pistonconfig-core:0.1.0-SNAPSHOT")
  implementation("net.pistonmaster:pistonconfig-yaml:0.1.0-SNAPSHOT")
}
```

## Use It

```java
var loader = YamlConfigFormat.INSTANCE.loader();
var document = ConfigLoaders.load(Path.of("config.yml"), loader);

document.mergeDefaults(defaults, MergeOptions.conservative());

ConfigLoaders.save(Path.of("config.yml"), loader, document);
```

## Next Steps

- Read the [getting started guide](guides/getting-started.md).
- Compare the [module reference](reference/modules.md).
