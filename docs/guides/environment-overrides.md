---
layout: default
title: Environment Overrides
description: Override config values from environment variables and system properties.
---

# Environment Overrides

Use `pistonconfig-env` when deployment systems need to override file values without editing the config file.

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

Environment names are normalized to uppercase, `_` is converted to `.`, and the prefix may be written with dots or hyphens.

## Test With Explicit Maps

```java
var overrides = EnvironmentOverrides.of(
  "myapp",
  "myapp",
  Map.of("MYAPP_SERVER_PORT", "25566"),
  Map.of("myapp.server.host", "127.0.0.1")
);

overrides.applyTo(document);
```

Using explicit maps keeps tests deterministic and avoids depending on the process environment.

## Scalar Conversion

Override values are parsed as:

| Raw value | Stored value |
| --- | --- |
| `true`, `false` | `Boolean` |
| Integer-like values | `Long` |
| Decimal values | `Double` |
| Other values | `String` |

Format loaders still decide how those Java scalar values are rendered when the document is saved.
