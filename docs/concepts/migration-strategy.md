---
layout: default
title: Migration Strategy
description: Plan configuration schema migrations, defaults, overrides, and validation across releases.
---

# Migration Strategy

Configuration migrations keep older user files compatible with newer application releases.

## Treat Config as a Schema

Even when a file is informal YAML or TOML, the application expects a schema: paths, types, defaults, and constraints. Migrations are the upgrade path for that schema.

## Version the Document

Store the schema version inside the document:

```java
MigrationRegistry.builder()
  .versionPath(ConfigPath.parse("config.version"))
  .addMigration(ConfigMigration.builder()
    .version(1)
    .action(config -> Migrations.rename(config, "server.bind", "server.host"))
    .build())
  .build();
```

Use monotonically increasing integers. Do not reuse a version number for a different migration.

## Keep Migrations Focused

Good migrations:

- Rename paths.
- Copy values.
- Remove obsolete sections.
- Set a value when missing.
- Convert one simple representation into another.

Avoid migrations that depend on network calls, random values, current time, or application services.

## Merge Defaults After Migrations

Run migrations before default merging so old paths are upgraded first. Then defaults can fill missing values using the current schema.

## Validate After Overrides

Validation should happen after migrations, defaults, and environment overrides. That catches the values the application will use in production.

## Keep Old Migrations

Keep migrations for every released schema version while users may still upgrade from those versions. Removing old migrations can strand old config files.
