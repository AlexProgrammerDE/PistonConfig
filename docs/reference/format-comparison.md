---
layout: default
title: Format Comparison
description: Compare PistonConfig format backends by syntax, preservation behavior, and best fit.
---

# Format Comparison

PistonConfig format modules translate established parser libraries into the core document model. Pick the backend based on the files your users want to edit and the source detail your application needs.

## Summary

| Format | Module | Extensions | Best fit |
| --- | --- | --- | --- |
| YAML | `pistonconfig-yaml` | `.yaml`, `.yml` | Human-edited config with comments, nesting, and flexible syntax. |
| TOML | `pistonconfig-toml` | `.toml` | Application config with tables and predictable scalar syntax. |
| HOCON | `pistonconfig-hocon` | `.conf`, `.hocon` | Typesafe Config ecosystems and HOCON object syntax. |
| JSON family | `pistonconfig-json` | `.json`, `.jsonc`, `.json5` | JSON-shaped config with optional comments and JSON5 features. |
| Properties | `pistonconfig-properties` | `.properties` | Legacy Java key-value files and flat config. |

## Preservation Matrix

| Capability | YAML | TOML | HOCON | JSON family | Properties |
| --- | --- | --- | --- | --- | --- |
| Object nodes | yes | yes | yes | yes | dotted paths |
| List nodes | yes | yes | yes | yes | repeated keys |
| Leading comments | yes | yes | yes | yes | yes |
| Inline comments | yes | limited | limited | backend comment model | no |
| Trailing comments | yes | limited | limited | limited | root footer comments |
| Scalar style | rich | limited | limited | numeric radix, timestamps | string layout |
| Source location | value and key | limited | origin line | limited | limited |
| Backend metadata | tags, anchors, raw scalars | table style | origin, rendered value | number radix | separator, blank lines |

## Selection Rules

Choose YAML when users expect hand-edited config files with rich comments and nested structures.

Choose TOML when the config shape is mostly tables, arrays, and scalar values with a stricter syntax than YAML.

Choose HOCON when integrating with software that already uses Lightbend Config or `.conf` files.

Choose the JSON module when files are exchanged with tools that expect JSON but users still need JSONC or JSON5 comments.

Choose properties when compatibility with Java properties files matters more than nested syntax.

## Cross-Format Rule

The core model can represent more detail than some target formats can render. Use metadata for inspection and best-effort round trips, but avoid promising that converting one format to another preserves every source feature.
