---
layout: default
title: Format Conversion
description: Convert configuration documents between PistonConfig format backends.
---

# Format Conversion

This example loads one supported format and saves another format through the core document model.

## Converter

```java
final class ConfigConverter {
  private final List<ConfigFormat> formats = List.of(
    YamlConfigFormat.INSTANCE,
    TomlConfigFormat.INSTANCE,
    JsonConfigFormat.INSTANCE,
    HoconConfigFormat.INSTANCE,
    PropertiesConfigFormat.INSTANCE
  );

  void convert(Path source, Path target) {
    var sourceLoader = loaderFor(source);
    var targetLoader = loaderFor(target);
    var document = ConfigLoaders.load(source, sourceLoader);
    ConfigLoaders.save(target, targetLoader, document);
  }

  private ConfigLoader loaderFor(Path path) {
    var extension = extension(path.getFileName().toString());
    return formats.stream()
      .filter(format -> format.extensions().contains(extension))
      .findFirst()
      .orElseThrow(() -> new ConfigException("Unsupported extension: " + extension))
      .loader();
  }

  private static String extension(String fileName) {
    var index = fileName.lastIndexOf('.');
    return index < 0 ? "" : fileName.substring(index + 1).toLowerCase(Locale.ROOT);
  }
}
```

## Use It

```java
new ConfigConverter().convert(
  Path.of("config.yml"),
  Path.of("config.toml")
);
```

## What Carries Across

Core values, comments, ordering, and decorations stay available in memory. The target backend decides what can be written to the target file.

| Source detail | Conversion behavior |
| --- | --- |
| Object and list values | Written when the target format supports the shape. |
| Scalar values | Written through the target backend's scalar rules. |
| Leading comments | Written when the target backend supports comments. |
| YAML anchors or JSON5 radix | Kept as metadata, but not all target formats can render it. |

Use conversion for import/export tools, not as a promise that every byte of source formatting will be identical in another format.
