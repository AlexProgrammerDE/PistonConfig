---
layout: default
title: PistonConfig
description: Format-agnostic Java configuration library.
---

# PistonConfig

PistonConfig is a Java 25 library for building configuration systems without coupling your application to YAML, TOML, HOCON, JSON, properties, or one mapping style.

The core module owns the document model, comments, source decorations, typed codecs, and merge behavior. Format modules translate established parser libraries into that core model so comments, scalar styles, collection styles, key metadata, source locations, and backend attributes stay inspectable from one API.

{: .lead }
Use one document model for hand-edited files, generated defaults, environment overrides, annotation configs, static field declarations, and ordered migrations.

## Install

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
}
```

## Core Workflow

```java
var loader = YamlConfigFormat.INSTANCE.loader();
var document = ConfigLoaders.load(Path.of("config.yml"), loader);

document.mergeDefaults(defaults, MergeOptions.conservative());
EnvironmentOverrides.system("myapp").applyTo(document);

ConfigLoaders.save(Path.of("config.yml"), loader, document);
```

## What It Covers

<div class="home-grid">
  <section class="home-panel">
    <h2>Lossless Core</h2>
    <p>Objects, lists, scalars, null values, comments, key decorations, source locations, scalar styles, collection styles, and backend metadata.</p>
  </section>
  <section class="home-panel">
    <h2>Format Backends</h2>
    <p>YAML, properties, JSON/JSONC/JSON5, TOML, and HOCON modules backed by established Java libraries.</p>
  </section>
  <section class="home-panel">
    <h2>Typed Access</h2>
    <p>Built-in scalar codecs, custom codecs, annotation-based object mapping, and ConfigMe-style static properties.</p>
  </section>
  <section class="home-panel">
    <h2>Operations</h2>
    <p>Default merging, environment and system property overrides, manual edits, and versioned migrations.</p>
  </section>
</div>

## Documentation

<div class="link-grid">
  <a class="link-card" href="guides/getting-started.html">
    <h3>Getting Started</h3>
    <p>Load a file, merge defaults, apply overrides, read values, and save it back.</p>
  </a>
  <a class="link-card" href="guides/installation.html">
    <h3>Installation</h3>
    <p>Use the BOM with Gradle or Maven, including GitHub Packages setup.</p>
  </a>
  <a class="link-card" href="reference/document-model.html">
    <h3>Document Model</h3>
    <p>Understand nodes, comments, decorations, metadata, paths, and codecs.</p>
  </a>
  <a class="link-card" href="reference/modules.html">
    <h3>Module Reference</h3>
    <p>Compare artifact IDs, dependencies, capabilities, and intended usage.</p>
  </a>
</div>
