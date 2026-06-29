---
layout: default
title: Format Backends
description: Choose and use YAML, TOML, HOCON, JSON, JSONC, JSON5, and properties backends.
---

# Format Backends

Format modules translate parser-specific documents into the core `ConfigDocument` model. Choose a backend based on what your users edit and what source detail you need to preserve.

## Backend Matrix

| Format | Module | Parser library | Best fit |
| --- | --- | --- | --- |
| YAML | `pistonconfig-yaml` | SnakeYAML | Human-edited config with rich comments and styles. |
| TOML | `pistonconfig-toml` | Night Config | Structured app config with tables and predictable syntax. |
| HOCON | `pistonconfig-hocon` | Lightbend Config | Config with substitutions and HOCON-style object syntax. |
| JSON / JSONC / JSON5 | `pistonconfig-json` | json5-java | JSON-shaped config with optional comments and JSON5 features. |
| Properties | `pistonconfig-properties` | Apache Commons Configuration | Flat key-value files and legacy Java ecosystem config. |

## Create a Loader

```java
ConfigLoader loader = YamlConfigFormat.INSTANCE.loader();
var document = ConfigLoaders.load(Path.of("config.yml"), loader);
```

Every format module exposes a singleton `ConfigFormat` with a name, file extensions, capabilities, and loader.

## Switch by Extension

```java
static ConfigLoader loaderFor(Path path) {
  var fileName = path.getFileName().toString();
  if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
    return YamlConfigFormat.INSTANCE.loader();
  }
  if (fileName.endsWith(".toml")) {
    return TomlConfigFormat.INSTANCE.loader();
  }
  if (fileName.endsWith(".conf") || fileName.endsWith(".hocon")) {
    return HoconConfigFormat.INSTANCE.loader();
  }
  if (fileName.endsWith(".json") || fileName.endsWith(".jsonc") || fileName.endsWith(".json5")) {
    return JsonConfigFormat.INSTANCE.loader();
  }
  if (fileName.endsWith(".properties")) {
    return PropertiesConfigFormat.INSTANCE.loader();
  }
  throw new IllegalArgumentException("Unsupported config format: " + fileName);
}
```

Keep backend selection at your boundary. After loading, most operations should work against `ConfigDocument`.

## What Gets Preserved

| Source detail | YAML | TOML | HOCON | JSON module | Properties |
| --- | --- | --- | --- | --- | --- |
| Leading comments | yes | yes | yes | yes | yes |
| Inline comments | yes | backend-dependent | backend-dependent | comment model | no |
| Lists | yes | yes | yes | yes | repeated keys |
| Nested objects | yes | yes | yes | yes | dotted paths |
| Scalar style | yes | limited | limited | number radix and timestamps | separator/layout attributes |
| Source locations | yes | limited | origin line | limited | limited |

## Backend-Specific Metadata

Use backend metadata only when code genuinely needs format-specific detail:

```java
var node = document.find("server.port").orElseThrow();
node.metadata(JsonMetadataKeys.NUMBER_RADIX)
  .ifPresent(radix -> log.debug("JSON5 radix: {}", radix));
```

For behavior that should work across formats, prefer core fields such as `comment()`, `decorations()`, `kind()`, and `rawValue()`.

## Saving

```java
ConfigLoaders.save(Path.of("config.yml"), loader, document);
```

Saving is format-aware. A backend writes the source details it can represent and ignores the rest. That is why the core model separates common decorations from backend-specific metadata.
