---
layout: default
title: API Surface
description: Public PistonConfig API grouped by module and responsibility.
---

# API Surface

This page groups the main public API types by module. Use it when deciding where a feature belongs or when scanning what a module exposes.

## Core

| Type | Responsibility |
| --- | --- |
| `ConfigDocument` | Top-level mutable document with a root node. |
| `ConfigNode` | Mutable object, list, scalar, or null node with comments, decorations, and metadata. |
| `ConfigPath` | Immutable path of object keys with dotted parsing and escaping. |
| `ConfigValue` | Sealed immutable value model for adapters and codecs. |
| `ObjectValue`, `ListValue`, `ScalarValue`, `NullValue` | Immutable value implementations. |
| `ConfigComment`, `ConfigCommentLine` | Comment groups and individual comment lines. |
| `ConfigNodeDecorations` | Key comments, style information, source locations, and attributes. |
| `ConfigSourceLocation` | Best-effort parser source location. |
| `ConfigCodec<T>` | Converts between Java values and `ConfigNode`. |
| `ConfigCodecRegistry` | Registry for built-in scalar codecs and custom codecs. |
| `ConfigLoader` | Reads and writes `ConfigDocument` through a format backend. |
| `ConfigFormat` | Backend descriptor with name, extensions, capabilities, and loader. |
| `ConfigMerger`, `MergeOptions`, `MergeListStrategy` | Default merging primitives. |
| `ConfigException` | Runtime exception for load, write, mapping, and config failures. |

## Format Modules

| Module | Main types |
| --- | --- |
| `pistonconfig-yaml` | `YamlConfigFormat`, `YamlConfigLoader`, `YamlMetadataKeys` |
| `pistonconfig-properties` | `PropertiesConfigFormat`, `PropertiesConfigLoader`, `PropertiesMetadataKeys` |
| `pistonconfig-json` | `JsonConfigFormat`, `JsonConfigLoader`, `JsonMetadataKeys` |
| `pistonconfig-toml` | `TomlConfigFormat`, `TomlConfigLoader` |
| `pistonconfig-hocon` | `HoconConfigFormat`, `HoconConfigLoader`, `HoconMetadataKeys` |

Each format module depends on core and exposes a singleton `ConfigFormat` named `INSTANCE`.

## Access Style Modules

| Module | Main types |
| --- | --- |
| `pistonconfig-annotations` | `AnnotatedConfigMapper`, `ConfigComment`, `ConfigIgnore`, `ConfigName`, `ConfigPathPrefix` |
| `pistonconfig-static-fields` | `ConfigProperty<T>`, `StaticConfigDefinition` |
| `pistonconfig-env` | `EnvironmentOverrides` |
| `pistonconfig-migrations` | `MigrationRegistry`, `ConfigMigration`, `Migrations` |

These modules operate on the same `ConfigDocument` model, so they can be mixed in one startup flow.

## Generated Types

The core module uses Immutables for selected value objects:

| Interface | Generated implementation |
| --- | --- |
| `ConfigCommentLine` | `ImmutableConfigCommentLine` |
| `ConfigNodeDecorations` | `ImmutableConfigNodeDecorations` |
| `ConfigSourceLocation` | `ImmutableConfigSourceLocation` |

Most callers use the factory and `with...` methods exposed by the public interfaces instead of referencing generated implementation types directly.
