---
layout: default
title: Environment Overrides
description: Override config values from environment variables and system properties.
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

## Apply System Overrides

```java
EnvironmentOverrides.system("myapp").applyTo(document);
```

With the `myapp` prefix:

| Source | Input | Config path |
| --- | --- | --- |
| Environment | `MYAPP_SERVER_PORT=25566` | `server.port` |
| Environment | `MYAPP_DATABASE_POOL_SIZE=12` | `database.pool.size` |
| System property | `-Dmyapp.server.host=127.0.0.1` | `server.host` |

Environment names are normalized to uppercase. Dots and hyphens in the prefix become underscores. Environment variable underscores become path dots.

## Use Explicit Maps in Tests

```java
var overrides = EnvironmentOverrides.of(
  "myapp",
  "myapp",
  Map.of("MYAPP_SERVER_PORT", "25566"),
  Map.of("myapp.server.host", "127.0.0.1")
);

overrides.applyTo(document);
```

This avoids depending on the process environment in tests.

## Override Precedence

Environment variables are applied first. System properties are applied after environment variables, so a system property can override the same path.

## Scalar Conversion

| Raw value | Stored value |
| --- | --- |
| `true`, `false` | `Boolean` |
| Integer-like value | `Long` |
| Decimal value | `Double` |
| Anything else | `String` |

The selected format backend controls how those scalar values render when saved.

## Recommended Order

Apply overrides after migrations and defaults:

```java
registry.migrate(document);
document.mergeDefaults(defaults, MergeOptions.conservative());
EnvironmentOverrides.system("myapp").applyTo(document);
```
