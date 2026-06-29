---
layout: default
title: Getting Started
description: Load, merge, override, read, and save a PistonConfig document.
---

# Getting Started

This guide builds the common application flow: install the core and a backend, create defaults, load a file, merge missing defaults, apply runtime overrides, read typed values, and save the document.

## Add Dependencies

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
  implementation("net.pistonmaster:pistonconfig-env")
}
```

Use [installation](installation.html) for Maven, GitHub Packages, and non-BOM examples.

## Create Defaults

Defaults are just normal documents. That keeps manual defaults, annotation-generated defaults, static-field defaults, and resource-loaded defaults compatible with the same merge API.

```java
var defaults = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565)
  .set("server.online-mode", true);

defaults.root()
  .getOrCreate(ConfigPath.parse("server.port"))
  .setComment(ConfigComment.lines("Port used by the public listener."));
```

## Load the User File

```java
var path = Path.of("config.yml");
var loader = YamlConfigFormat.INSTANCE.loader();

var document = Files.exists(path)
  ? ConfigLoaders.load(path, loader)
  : ConfigDocument.empty();
```

`ConfigLoaders` handles UTF-8 readers and writers. Format modules provide the actual `ConfigLoader`.

## Merge Missing Defaults

```java
document.mergeDefaults(defaults, MergeOptions.conservative());
```

`MergeOptions.conservative()` adds missing defaults and refreshes comments from the defaults. It does not replace user values or remove unknown user keys.

Use [merge defaults](merge-defaults.html) when you need exact-default behavior or list strategies.

## Apply Deployment Overrides

```java
EnvironmentOverrides.system("myapp").applyTo(document);
```

With the `myapp` prefix:

| Source | Example | Config path |
| --- | --- | --- |
| Environment | `MYAPP_SERVER_PORT=25566` | `server.port` |
| System property | `-Dmyapp.server.host=127.0.0.1` | `server.host` |

Apply overrides after merging defaults and migrations so deployment values win.

## Read Values

```java
int port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);

String host = document.find("server.host")
  .flatMap(ConfigNode::asString)
  .orElse("0.0.0.0");
```

Accessors return `Optional` because files are external input. Decode near the boundary where you can choose a fallback or report an error.

## Save the Result

```java
ConfigLoaders.save(path, loader, document);
```

The backend writes the source detail it can represent. For example, YAML can write inline comments, scalar styles, anchors, and collection styles; properties files keep a flatter model and layout attributes.

## Next Choices

<div class="link-grid">
  <a class="link-card" href="format-backends.html">
    <h3>Pick a backend</h3>
    <p>Choose the file format whose source model matches your users.</p>
  </a>
  <a class="link-card" href="annotation-configs.html">
    <h3>Use annotations</h3>
    <p>Generate defaults and comments from Java classes.</p>
  </a>
  <a class="link-card" href="static-field-configs.html">
    <h3>Use static properties</h3>
    <p>Centralize typed keys and defaults.</p>
  </a>
  <a class="link-card" href="migrations.html">
    <h3>Add migrations</h3>
    <p>Keep old user files compatible as your schema changes.</p>
  </a>
</div>
