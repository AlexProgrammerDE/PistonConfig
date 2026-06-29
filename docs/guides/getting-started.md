---
layout: default
title: Getting Started
description: Load, merge, edit, and save a config document.
---

# Getting Started

This guide shows the normal application workflow: create defaults, load the user's file, merge missing defaults, apply runtime overrides, read typed values, and save the result.

## Add Dependencies

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
  implementation("net.pistonmaster:pistonconfig-env")
}
```

Artifacts are published to Maven Central and GitHub Packages on release. GitHub Packages requires authenticated access through `https://maven.pkg.github.com/AlexProgrammerDE/PistonConfig`.

See the [installation guide](installation.html) for Maven, GitHub Packages, and non-BOM examples.

## Create Defaults

```java
var defaults = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565)
  .set("server.online-mode", true);

defaults.root()
  .getOrCreate(ConfigPath.parse("server.port"))
  .setComment(ConfigComment.lines("Port used by the public listener."));
```

Defaults are normal documents. You can build them by hand, generate them from annotations, generate them from static fields, or load them from a bundled resource.

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

`MergeOptions.conservative()` fills missing values but leaves user changes in place.

## Apply Overrides

```java
EnvironmentOverrides.system("myapp").applyTo(document);
```

With the `myapp` prefix, `MYAPP_SERVER_PORT=25566` maps to `server.port`, and `-Dmyapp.server.host=127.0.0.1` maps to `server.host`.

## Read Values

```java
int port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);
```

The core accessors return `Optional` because config files are external input. Use defaults at the edge of your application rather than assuming a value is present.

## Save Back

```java
ConfigLoaders.save(path, loader, document);
```

Saving writes the current document through the selected backend. Comments and source metadata that the backend can represent are read into core and written back where the backend supports it.

## Next Steps

- Use [manual API](manual-api.html) when code needs direct control over the tree.
- Use [annotation configs](annotation-configs.html) when a class should describe defaults and comments.
- Use [static fields](static-field-configs.html) when you want central typed keys.
- Use [migrations](migrations.html) when config schemas change between releases.
