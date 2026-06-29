---
layout: default
title: Document Model
description: Reference for core document, node, comment, path, decoration, and codec types.
---

# Document Model

The core module defines a format-neutral tree that can represent normal configuration values plus source information from richer file formats.

## Main Types

| Type | Role |
| --- | --- |
| `ConfigDocument` | Complete config with an object root. |
| `ConfigNode` | Mutable tree node. A node is an object, list, scalar, or null. |
| `ConfigValueKind` | Enum for the node kind. |
| `ConfigPath` | Immutable path of object keys. |
| `ConfigComment` | Leading, inline, and trailing comment groups. |
| `ConfigCommentLine` | One comment line with text, marker, and logical type. |
| `ConfigNodeDecorations` | Source details shared across formats. |
| `ConfigCodec<T>` | Encoder and decoder for Java value types. |
| `ConfigCodecRegistry` | Registry of built-in and custom codecs. |
| `ConfigLoader` | Reader and writer for a concrete format. |
| `ConfigFormat` | Named format descriptor with extensions, capabilities, and loader. |

## Node Kinds

| Kind | Java shape |
| --- | --- |
| Object | Ordered map of `String` keys to `ConfigNode` children. |
| List | Ordered list of `ConfigNode` children. |
| Scalar | Java scalar value such as `String`, `Boolean`, `Number`, or backend scalar. |
| Null | Explicit null value. |

## Comments

`ConfigComment` stores comments in three positions:

| Position | Meaning |
| --- | --- |
| Leading | Lines before the node. |
| Inline | Comment on the same logical line as the node. |
| Trailing | Lines after the node, such as YAML end comments. |

`ConfigCommentLine` stores the text, the marker, and the type. This lets format modules distinguish hash comments, slash comments, blank lines, block comments, and unknown markers where the backend exposes them.

## Decorations

`ConfigNodeDecorations` stores information that is common enough to be represented directly:

| Decoration | Purpose |
| --- | --- |
| `keyComment()` | Comment attached to the key rather than the value. |
| `keyStyle()` | Scalar style for a key. |
| `scalarStyle()` | Scalar style for a scalar value. |
| `collectionStyle()` | Block, flow, table, or unspecified collection style. |
| `keyLocation()` | Source location for the key. |
| `valueLocation()` | Source location for the value. |
| `attributes()` | String attributes that do not deserve a dedicated core field. |

Backend-specific information belongs in `metadata()` or decoration attributes with constants from the format module.

## Paths

```java
ConfigPath.parse("server.port");
ConfigPath.of("server", "port");
ConfigPath.parse("database\\.url");
```

`ConfigPath.parse(...)` uses dots as separators and backslash as the escape character. `ConfigPath.of(...)` accepts literal segments.

## Codecs

The default registry supports strings, booleans, integers, longs, doubles, and primitive equivalents. Register a custom `ConfigCodec<T>` to map application value objects.
