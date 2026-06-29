---
layout: default
title: Environment Overrides
description: Override existing config values from environment variables and system properties.
---

# Environment Overrides

Use `pistonconfig-env` when deployment systems need to override file values without editing the config file.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-env")
}
```

## Apply Overrides After Defaults

```java
document.mergeDefaults(defaults, MergeOptions.conservative());
EnvironmentOverrides.system("myapp").applyTo(document);
```

Apply overrides after migrations and defaults. The existing document shape defines which paths can be overridden and which scalar type each value should use.

## Path Mapping

With the `myapp` prefix:

| Source | Input | Config path |
| --- | --- | --- |
| Environment | `MYAPP_SERVER_PORT=25566` | `server.port` |
| Environment | `MYAPP_DATABASE_POOL_SIZE=12` | `database.pool.size` |
| System property | `-Dmyapp.server.host=127.0.0.1` | `server.host` |

Environment names are case-insensitive by default. Dots and hyphens in the prefix become underscores. Environment variable underscores become path dots.

## Use Explicit Maps in Tests

```java
var overrides = EnvironmentOverrides.builder()
  .environmentPrefix("myapp")
  .propertyPrefix("myapp")
  .putAllEnvironment(Map.of("MYAPP_SERVER_PORT", "25566"))
  .putAllProperties(Map.of("myapp.server.host", "127.0.0.1"))
  .build();

overrides.applyTo(document);
```

This avoids depending on the process environment in tests.

## Type-Aware Conversion

Overrides only update existing scalar paths by default. The existing value controls parsing:

| Existing value | Override parsing |
| --- | --- |
| `String` | raw string |
| `Boolean` | `true` or `false` |
| integer number | matching integer type |
| decimal number | matching decimal type |
| object or list | rejected |
| null | rejected |

Missing paths are ignored unless `allowNewPaths(true)` is set. New paths are stored as strings because there is no existing type to infer from.

## Case-Sensitive Environments

```java
var overrides = EnvironmentOverrides.builder()
  .caseSensitiveEnvironment(true)
  .environmentPrefix("MyApp")
  .putAllEnvironment(System.getenv())
  .build();
```

Use case-sensitive matching when your deployment environment and config paths intentionally depend on case.

## Override Precedence

Environment variables are applied first. System properties are applied after environment variables, so a system property can override the same path.
