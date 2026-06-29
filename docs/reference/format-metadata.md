---
layout: default
title: Format Metadata
description: Metadata and decoration keys used by PistonConfig format modules.
---

# Format Metadata

Core fields handle shared source detail. Format modules expose constants for parser-specific details that still matter for inspection or round-tripping.

## Common Core Fields

<div class="metadata-list">
  <div>
    <strong><code>ConfigNode.comment()</code></strong>
    Leading, inline, and trailing comments attached to the value.
  </div>
  <div>
    <strong><code>ConfigNode.decorations()</code></strong>
    Key comments, scalar style, collection style, source locations, and string attributes.
  </div>
  <div>
    <strong><code>ConfigNode.metadata()</code></strong>
    Backend-specific values that are useful for round-tripping or inspection.
  </div>
</div>

## Core Metadata

| Constant | Key | Meaning |
| --- | --- | --- |
| `ConfigMetadataKeys.RAW_VALUE` | `core.rawValue` | Original backend value when preserving it is useful. |

## YAML

| Constant | Key | Meaning |
| --- | --- | --- |
| `YamlMetadataKeys.TAG` | `yaml.tag` | YAML tag for the value node. |
| `YamlMetadataKeys.ANCHOR` | `yaml.anchor` | YAML anchor name. |
| `YamlMetadataKeys.KEY_TAG` | `yaml.key.tag` | YAML tag for a key node. |
| `YamlMetadataKeys.SCALAR_RAW` | `yaml.scalar.raw` | Raw scalar text, such as `0x10`. |

YAML also maps key comments, scalar style, collection style, key location, and value location into `ConfigNodeDecorations`.

## Properties

| Constant | Key | Meaning |
| --- | --- | --- |
| `PropertiesMetadataKeys.SEPARATOR` | `properties.separator` | Separator used between key and value. |
| `PropertiesMetadataKeys.SINGLE_LINE` | `properties.singleLine` | Whether the value was written on a single logical line. |
| `PropertiesMetadataKeys.BLANK_LINES_BEFORE` | `properties.blankLinesBefore` | Blank lines before the property. |

Repeated keys are represented as list nodes. Dotted keys are represented as paths.

## JSON, JSONC, and JSON5

| Constant | Key | Meaning |
| --- | --- | --- |
| `JsonMetadataKeys.NUMBER_RADIX` | `json5.number.radix` | Numeric radix style exposed by the JSON5 backend. |

The JSON module maps number radix into `ConfigScalarStyle.BINARY`, `OCTAL`, `DECIMAL`, or `HEX` where available.

## HOCON

| Constant | Key | Meaning |
| --- | --- | --- |
| `HoconMetadataKeys.ORIGIN_DESCRIPTION` | `hocon.origin.description` | Origin details from Lightbend Config. |
| `HoconMetadataKeys.RENDERED` | `hocon.rendered` | Rendered source fragment retained from the backend. |

HOCON origin comments are mapped into `ConfigComment`.

## TOML

The TOML backend maps entry comments through `ConfigComment` and table-like structures through `ConfigCollectionStyle.TABLE`. It uses Night Config's commented config model for parser and writer behavior.

## Rule of Thumb

Use core fields for behavior that should work across formats. Use metadata constants only for format-specific behavior.
