---
layout: default
title: Document Model
description: Reference for core document, node, comment, path, decoration, metadata, and codec types.
---

# Document Model

The core module defines a format-neutral mutable tree with separate layers for values, human comments, common source decorations, and backend-specific metadata.

## Main Types

| Type | Role |
| --- | --- |
| `ConfigDocument` | Complete config with an object root. |
| `ConfigNode` | Mutable tree node. A node is an object, list, scalar, or null. |
| `ConfigPath` | Immutable path of object keys. |
| `ConfigComment` | Leading, inline, and trailing comment groups. |
| `ConfigCommentLine` | One comment line with text, marker, and logical type. |
| `ConfigNodeDecorations` | Source details shared across formats. |
| `ConfigValue` | Sealed immutable value model for codecs and adapters. |
| `ConfigCodec<T>` | Encoder and decoder for Java value types used by codec-based APIs. |
| `ConfigCodecRegistry` | Registry of built-in scalar codecs and custom static-field codecs. |
| `ConfigLoader` | Reader and writer for a concrete format. |
| `ConfigFormat` | Named format descriptor with extensions, capabilities, and loader. |

## Node Kinds

| Kind | Shape |
| --- | --- |
| `OBJECT` | Ordered map of `String` keys to child nodes. |
| `LIST` | Ordered list of child nodes. |
| `SCALAR` | Java scalar value such as `String`, `Boolean`, `Number`, or backend scalar. |
| `NULL` | Explicit null value. |

## Comments

`ConfigComment` stores source comments in three positions:

| Position | Meaning |
| --- | --- |
| Leading | Lines before the node. |
| Inline | Comment on the same logical line as the node. |
| Trailing | Lines after the node, such as YAML end comments. |

Each `ConfigCommentLine` stores:

| Field | Examples |
| --- | --- |
| `text()` | `Port used by the public listener.` |
| `type()` | `BLOCK`, `INLINE`, `BLANK` |
| `marker()` | `HASH`, `DOUBLE_SLASH`, `EXCLAMATION`, `UNKNOWN` |

## Decorations

`ConfigNodeDecorations` stores source detail that is common enough to model directly:

| Decoration | Purpose |
| --- | --- |
| `keyComment()` | Comment attached to the key rather than the value. |
| `keyStyle()` | Scalar style for a key. |
| `scalarStyle()` | Scalar style for a scalar value. |
| `collectionStyle()` | Block, flow, table, inline, array-table, or unspecified style. |
| `keyLocation()` | Best-effort source location for the key. |
| `valueLocation()` | Best-effort source location for the value. |
| `attributes()` | String attributes that are useful but not common enough for a dedicated field. |

## Metadata

`ConfigNode.metadata()` stores backend-specific values. Use constants from the relevant module instead of raw strings.

```java
node.metadata(JsonMetadataKeys.NUMBER_RADIX)
  .ifPresent(radix -> log.debug("JSON5 radix {}", radix));
```

## Paths

```java
ConfigPath.parse("server.port");
ConfigPath.of("server", "port");
ConfigPath.parse("database\\.url");
```

`ConfigPath.parse(...)` uses dots as separators and backslash as the escape character. `ConfigPath.of(...)` accepts literal path segments.

## Built-In Codecs

The default registry supports:

| Java type | Notes |
| --- | --- |
| `String` | Uses string value or scalar `toString()`. |
| `Boolean` and `boolean` | Accepts booleans and compatible strings. |
| Primitive wrappers | Boolean, byte, short, int, long, float, double, char. |
| Big numbers | `BigInteger`, `BigDecimal`. |
| Time values | `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `Duration`, `Period`. |
| Utility values | `UUID`, `File`, `Path`, `URL`, `URI`. |
| Enums | Encoded by enum constant name. |

Register custom codecs for static-field application records and value objects.
