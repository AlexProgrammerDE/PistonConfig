---
layout: default
title: PistonConfig
description: Format-agnostic Java configuration library.
---

# PistonConfig

PistonConfig is a Java 25 configuration library for projects that need one document model across YAML, TOML, HOCON, JSON, JSONC, JSON5, and `.properties`.

{: .lead }
Load a human-edited file, preserve comments and source detail where the backend exposes it, merge defaults, apply overrides, run migrations, and read values through the access style that fits your project.

<div class="quickstart" markdown="1">
  <div markdown="1">

## Install

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
}
```

  </div>
  <div markdown="1">

## Load and Save

```java
var path = Path.of("config.yml");
var loader = YamlConfigFormat.INSTANCE.loader();
var document = ConfigLoaders.load(path, loader);

document.mergeDefaults(defaults, MergeOptions.conservative());
ConfigLoaders.save(path, loader, document);
```

  </div>
</div>

## Start With the Job You Have

<div class="path-grid">
  <a class="link-card" href="guides/getting-started.html">
    <h3>Set up a config file</h3>
    <p>Build defaults, load a user file, merge missing values, apply overrides, and save the result.</p>
  </a>
  <a class="link-card" href="guides/format-backends.html">
    <h3>Choose a format backend</h3>
    <p>Compare YAML, TOML, HOCON, JSON, JSONC, JSON5, and properties behavior.</p>
  </a>
  <a class="link-card" href="guides/annotation-configs.html">
    <h3>Map object configs</h3>
    <p>Use annotated Java classes for defaults, comments, names, and path prefixes.</p>
  </a>
  <a class="link-card" href="guides/static-field-configs.html">
    <h3>Centralize typed keys</h3>
    <p>Declare ConfigMe-style static properties with defaults, comments, and typed reads.</p>
  </a>
</div>

## How the Pieces Fit

<div class="diagram">
  <img src="assets/img/document-model.svg" alt="Format backends feed the core document model, then application APIs consume that model.">
</div>

## Main Capabilities

<div class="home-grid">
  <section class="home-panel">
    <h2>Lossless Core</h2>
    <p>Objects, lists, scalars, nulls, comments, key decorations, source locations, scalar style, collection style, and backend metadata.</p>
  </section>
  <section class="home-panel">
    <h2>Established Backends</h2>
    <p>SnakeYAML, Apache Commons Configuration, json5-java, Night Config, and Lightbend Config do the parser-specific work.</p>
  </section>
  <section class="home-panel">
    <h2>Typed Access</h2>
    <p>Use built-in scalar codecs, custom codecs, annotations, static fields, or direct document edits from the same model.</p>
  </section>
  <section class="home-panel">
    <h2>Operational Tools</h2>
    <p>Merge defaults, apply environment and system property overrides, and run ordered schema migrations.</p>
  </section>
</div>

## Documentation Map

<div class="link-grid">
  <a class="link-card" href="guides/installation.html">
    <h3>Installation</h3>
    <p>Gradle, Maven, BOM usage, Maven Central, and GitHub Packages.</p>
  </a>
  <a class="link-card" href="guides/manual-api.html">
    <h3>Manual API</h3>
    <p>Work directly with <code>ConfigDocument</code>, <code>ConfigNode</code>, comments, decorations, and paths.</p>
  </a>
  <a class="link-card" href="guides/custom-codecs.html">
    <h3>Custom Codecs</h3>
    <p>Map records and value objects to config nodes while keeping call sites type-safe.</p>
  </a>
  <a class="link-card" href="reference/document-model.html">
    <h3>Reference</h3>
    <p>Use the type, module, metadata, and publishing reference when integrating deeper.</p>
  </a>
</div>

<p class="page-footer">PistonConfig targets Java 25 and publishes sources and Javadocs jars for library modules.</p>
