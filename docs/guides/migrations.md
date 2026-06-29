---
layout: default
title: Migrations
description: Apply ordered schema migrations to configuration documents.
---

# Migrations

Use `pistonconfig-migrations` when a release changes config paths, defaults, or schema expectations.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-migrations")
}
```

## Create a Registry

```java
var registry = MigrationRegistry.builder()
  .versionPath("config.version")
  .add(Migrations.migration(1, config -> {
    Migrations.rename(config, "server.bind", "server.host");
    Migrations.setIfMissing(config, "server.port", 25565);
  }))
  .add(Migrations.migration(2, config -> {
    Migrations.copy(config, "server.host", "network.host");
    Migrations.remove(config, "legacy");
  }))
  .build();
```

Migrations run in ascending version order. The registry stores the latest applied version in the configured version path.

## Apply Migrations

```java
registry.migrate(document);
```

Run migrations after loading a file and before reading application values. Migrations mutate the document in place.

## Built-In Operations

| Helper | Purpose |
| --- | --- |
| `Migrations.rename(document, from, to)` | Move a node to a new path. |
| `Migrations.copy(document, from, to)` | Copy a node while leaving the source in place. |
| `Migrations.remove(document, path)` | Delete a node. |
| `Migrations.setIfMissing(document, path, value)` | Add a scalar default only when no value exists. |

## Versioning Rules

- Use monotonically increasing integer versions.
- Keep migrations deterministic and idempotent.
- Avoid reading runtime environment state inside migrations.
- Keep migrations focused on document shape, not application startup side effects.

## Full Startup Order

1. Load the user's file.
2. Run migrations.
3. Merge current defaults.
4. Apply environment and system property overrides.
5. Save the migrated document.

This order keeps older files readable while preserving user edits.
