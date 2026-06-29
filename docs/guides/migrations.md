---
layout: default
title: Migrations
description: Apply ordered schema migrations to configuration documents.
---

# Migrations

Use `pistonconfig-migrations` when an application release changes config paths, defaults, or schema expectations.

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

## Common Operations

| Helper | Purpose |
| --- | --- |
| `Migrations.rename(document, from, to)` | Move a node to a new path. |
| `Migrations.copy(document, from, to)` | Copy a node while leaving the source in place. |
| `Migrations.remove(document, path)` | Delete a node. |
| `Migrations.setIfMissing(document, path, value)` | Add a scalar default only when no value exists. |

## Workflow

1. Load the user's file.
2. Apply migrations.
3. Merge current defaults.
4. Apply environment and system property overrides.
5. Save the migrated document.

This keeps old files readable while preserving user edits.
