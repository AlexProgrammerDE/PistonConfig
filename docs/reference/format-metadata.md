---
layout: default
title: Format Metadata
description: Metadata and decoration keys used by PistonConfig format modules.
---

# Format Metadata

Core fields handle the metadata that is shared across many formats. Format modules use constants for data that is specific to one backend.

## Common Core Fields

<div class="metadata-list">
  <div>
    <strong><code>ConfigNode.comment()</code></strong>
    Leading, inline, and trailing comments attached to the value.
  </div>
  <div>
    <strong><code>ConfigNode.decorations()</code></strong>
    Key comments, scalar style, collection style, key source location, value source location, and string attributes.
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
| `YamlMetadataKeys.SCALAR_RAW` | `yaml.scalar.raw` | Raw scalar text. |

## Properties

| Constant | Key | Meaning |
| --- | --- | --- |
| `PropertiesMetadataKeys.SEPARATOR` | `properties.separator` | Separator used between key and value. |
| `PropertiesMetadataKeys.SINGLE_LINE` | `properties.singleLine` | Whether the value was written on a single logical line. |
| `PropertiesMetadataKeys.BLANK_LINES_BEFORE` | `properties.blankLinesBefore` | Blank lines before the property. |

## JSON, JSONC, and JSON5

| Constant | Key | Meaning |
| --- | --- | --- |
| `JsonMetadataKeys.NUMBER_RADIX` | `json5.number.radix` | Numeric radix style exposed by the JSON5 backend. |

## HOCON

| Constant | Key | Meaning |
| --- | --- | --- |
| `HoconMetadataKeys.ORIGIN_DESCRIPTION` | `hocon.origin.description` | Origin details from Lightbend Config. |
| `HoconMetadataKeys.RENDERED` | `hocon.rendered` | Rendered source fragment when retained by the backend. |

## TOML

The TOML backend maps comments through `ConfigComment` and table-like structures through `ConfigCollectionStyle.TABLE`. It uses Night Config's commented config model for parser and writer behavior.
