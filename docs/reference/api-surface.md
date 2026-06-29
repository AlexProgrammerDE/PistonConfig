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
| `ConfigCodecRegistry` | Registry for built-in scalar codecs and static-field custom codecs. |
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
| `pistonconfig-annotations` | `ConfigStore`, `ConfigStores`, `AnnotatedConfigMapper`, `ConfigMapperOptions`, `ConfigSerializer`, `ConfigSerializeWith`, `ConfigPolymorphic`, `ConfigComment`, `ConfigIgnore`, `ConfigName`, `ConfigPathPrefix` |
| `pistonconfig-static-fields` | `ConfigProperty<T>`, `ConfigType<T>`, `StaticConfigDefinition`, `StaticConfigStore`, `StaticConfigSession`, `StaticConfigStoreOptions`, `StaticConfigDefinitionValidator`, `ConfigComment`, `StaticConfigComments` |
| `pistonconfig-env` | `EnvironmentOverrides` |
| `pistonconfig-migrations` | `MigrationRegistry`, `ConfigMigration`, `Migrations` |

These modules operate on the same `ConfigDocument` model, so they can be mixed in one startup flow.

## Generated Builders

PistonConfig uses Immutables for value objects and option types:

| Interface | Generated implementation |
| --- | --- |
| `ConfigComment` | `ImmutableConfigComment` |
| `ConfigCommentLine` | `ImmutableConfigCommentLine` |
| `ConfigFormatCapabilities` | `ImmutableConfigFormatCapabilities` |
| `MergeOptions` | `ImmutableMergeOptions` |
| `ConfigNodeDecorations` | `ImmutableConfigNodeDecorations` |
| `ConfigSourceLocation` | `ImmutableConfigSourceLocation` |
| `ObjectValue`, `ListValue`, `ScalarValue` | `ImmutableObjectValue`, `ImmutableListValue`, `ImmutableScalarValue` |
| `EnvironmentOverrides` | `ImmutableEnvironmentOverrides` |
| `ConfigMigration`, `MigrationRegistry` | `ImmutableConfigMigration`, `ImmutableMigrationRegistry` |

Use the public `builder()` entry points for new values. Use generated `Immutable*` copy methods when updating an existing immutable value.
