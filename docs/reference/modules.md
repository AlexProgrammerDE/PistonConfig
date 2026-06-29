---
layout: default
title: Modules
description: PistonConfig module reference.
---

# Modules

| Module | Backend | Notes |
| --- | --- | --- |
| `pistonconfig-core` | none | Common tree, comments, decorations, codecs, loaders, and merging. |
| `pistonconfig-yaml` | SnakeYAML | Maps key comments, value comments, YAML tags, anchors, scalar styles, collection styles, and source locations into core. |
| `pistonconfig-properties` | Apache Commons Configuration | Preserves layout comments, separators, blank lines, and single-line settings as core decorations. |
| `pistonconfig-json` | json5-java | Supports JSON, JSONC, and JSON5 with comments and number radix metadata. |
| `pistonconfig-toml` | Night Config | Uses `CommentedConfig` for comments and TOML parser/writer support. |
| `pistonconfig-hocon` | Lightbend Config | Uses HOCON parsing, origin comments, and renderable source details. |
| `pistonconfig-annotations` | core | Maps annotated Java objects to config documents. |
| `pistonconfig-static-fields` | core | Declares typed static config properties. |
| `pistonconfig-env` | core | Applies environment and system property overrides. |
| `pistonconfig-migrations` | core | Applies ordered schema migrations. |

## Lossless Metadata

Each `ConfigNode` has:

- `comment()` for leading, inline, and trailing comments.
- `decorations()` for key comments, scalar style, collection style, key/value locations, and string attributes.
- `metadata()` for values that are still useful but do not belong in the common decoration model.

Format modules should prefer constants such as `YamlMetadataKeys.TAG` over raw strings.
