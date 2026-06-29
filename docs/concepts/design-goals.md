---
layout: default
title: Design Goals
description: Why PistonConfig separates format backends, core documents, and access styles.
---

# Design Goals

PistonConfig is built around one principle: configuration files are both data structures and human-authored documents.

## One Model, Many Sources

Applications should not need separate business logic for YAML defaults, TOML user files, JSON imports, and properties overrides. Format modules load parser-specific input into the same `ConfigDocument` model.

That makes these operations format-neutral:

- Merge default files.
- Apply environment overrides.
- Run migrations.
- Decode custom value types.
- Generate defaults from annotations or static properties.

## Preserve Source Detail

Many config libraries reduce files to plain maps. That is easy for applications but rough for users because comments, styles, ordering, and source hints disappear.

PistonConfig keeps four layers separate:

| Layer | Example |
| --- | --- |
| Value | `25565` |
| Comment | `Port used by the public listener.` |
| Decoration | YAML scalar style, key location, collection style |
| Metadata | YAML anchor, JSON5 radix, properties separator |

## Use Established Parsers

The format modules delegate parsing and rendering to libraries that already know the file format:

- SnakeYAML for YAML.
- Apache Commons Configuration for properties.
- json5-java for JSON, JSONC, and JSON5.
- Night Config for TOML.
- Lightbend Config for HOCON.

PistonConfig's job is to translate those structures into a consistent core model.

## Keep Access Styles Separate

Projects disagree on how config should be declared. PistonConfig keeps those styles as modules:

| Style | Module |
| --- | --- |
| Direct document edits | `pistonconfig-core` |
| Annotation-based config classes | `pistonconfig-annotations` |
| Static typed keys | `pistonconfig-static-fields` |
| Runtime overrides | `pistonconfig-env` |
| Ordered schema upgrades | `pistonconfig-migrations` |

You can mix these styles because they all operate on `ConfigDocument`.
