---
layout: default
title: Diagnostics
description: Report missing values, invalid types, and source-aware configuration errors.
---

# Diagnostics

PistonConfig keeps source locations, comments, paths, and backend metadata available so applications can produce useful configuration errors.

## Report Missing Values

```java
static ConfigNode require(ConfigDocument document, String path) {
  return document.find(path)
    .orElseThrow(() -> new ConfigException("Missing required config path: " + path));
}
```

Use required reads for values that cannot safely fall back to defaults.

## Report Invalid Types

```java
static int requireInt(ConfigDocument document, String path) {
  var node = require(document, path);
  return node.asInt()
    .orElseThrow(() -> new ConfigException("Expected integer at " + path + "."));
}
```

Scalar accessors return `Optional` so callers can decide whether to use defaults or fail.

## Include Source Location

```java
static String location(ConfigNode node) {
  var location = node.decorations().valueLocation();
  if (!location.isKnown()) {
    return "unknown location";
  }

  return location.description() + ":" + (location.line() + 1) + ":" + (location.column() + 1);
}
```

Backends expose source locations on a best-effort basis. Use them when present, but keep errors useful when they are unknown.

## Keep Parser Errors Separate

```java
try {
  return ConfigLoaders.load(path, loader);
} catch (ConfigException exception) {
  throw new ConfigException("Could not load " + path + ". Check the file syntax.", exception);
}
```

Syntax errors, missing required paths, invalid types, and failed custom codecs are different problems. Keep their messages distinct.

## Validate After Migrations and Defaults

Recommended validation order:

1. Load the file.
2. Run migrations.
3. Merge defaults.
4. Apply deployment overrides.
5. Validate required values and ranges.
6. Decode into application objects.

Validation after overrides catches the values the application will actually use.

## Validate Ranges

```java
static int port(ConfigDocument document) {
  var value = requireInt(document, "server.port");
  if (value < 1 || value > 65535) {
    throw new ConfigException("server.port must be between 1 and 65535.");
  }
  return value;
}
```

Keep validation close to typed reads so invalid configuration fails before the application starts partially.
