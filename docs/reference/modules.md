---
layout: default
title: Modules
description: PistonConfig module and capability reference.
---

# Modules

PistonConfig is split into small modules so applications can choose only the config formats and access styles they need.

## Version Alignment

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
}
```

Use the BOM unless a parent build already manages PistonConfig versions.

## Module Catalog

| Module | Depends on | Backend | Purpose |
| --- | --- | --- | --- |
| `pistonconfig-bom` | none | Gradle Java Platform | Aligns all PistonConfig module versions. |
| `pistonconfig-core` | none | none | Document tree, comments, decorations, metadata, loaders, codecs, and merging. |
| `pistonconfig-yaml` | core | SnakeYAML | YAML and `.yml` files with rich comment and style support. |
| `pistonconfig-properties` | core | Apache Commons Configuration | Java properties with comments, separators, repeated keys, and layout attributes. |
| `pistonconfig-json` | core | json5-java | JSON, JSONC, and JSON5 with comments and numeric style metadata. |
| `pistonconfig-toml` | core | Night Config | TOML parser and writer support with commented configs. |
| `pistonconfig-hocon` | core | Lightbend Config | HOCON parsing, origin comments, and rendered source attributes. |
| `pistonconfig-annotations` | core | none | Object mapping through field annotations. |
| `pistonconfig-static-fields` | core | none | Static `ConfigProperty<T>` declarations. |
| `pistonconfig-env` | core | none | Environment variable and system property overrides. |
| `pistonconfig-migrations` | core | none | Ordered document migrations with stored schema version. |

## Format Capabilities

| Format | Extensions | Comments | Nested objects | Lists | Notable metadata |
| --- | --- | --- | --- | --- | --- |
| YAML | `.yaml`, `.yml` | leading, inline, trailing | yes | yes | tags, anchors, raw scalars, key tags |
| Properties | `.properties` | leading and root comments | dotted paths | repeated keys | separators, blank lines, single-line layout |
| JSON | `.json`, `.jsonc`, `.json5` | JSON5 comment model | yes | yes | number radix, timestamp style |
| TOML | `.toml` | leading comments | yes | yes | table collection style |
| HOCON | `.conf`, `.hocon` | origin comments | yes | yes | origin description, rendered value |

## Access Style Modules

<div class="module-grid">
  <section class="module-card">
    <h3>Manual API</h3>
    <p>Use only `pistonconfig-core` when code should edit the tree directly.</p>
  </section>
  <section class="module-card">
    <h3>Annotations</h3>
    <p>Use `pistonconfig-annotations` when Java fields should define defaults and comments.</p>
  </section>
  <section class="module-card">
    <h3>Static Fields</h3>
    <p>Use `pistonconfig-static-fields` when keys should be centralized and type-safe.</p>
  </section>
  <section class="module-card">
    <h3>Operations</h3>
    <p>Use `pistonconfig-env` and `pistonconfig-migrations` for deployment and upgrade workflows.</p>
  </section>
</div>

## Recommended Combinations

| Project shape | Modules |
| --- | --- |
| Plugin or app with one YAML file | core, yaml, migrations |
| CLI app with environment overrides | core, toml or yaml, env |
| Library exposing typed config keys | core, static-fields |
| Application with object config classes | core, annotations, chosen backend |
| Config conversion tool | core plus every needed format backend |
