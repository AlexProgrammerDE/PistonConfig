---
layout: default
title: Startup Order
description: Recommended order for loading, migrating, merging, overriding, validating, reading, and saving configuration.
---

# Startup Order

Most applications should follow the same order every time they load a config file.

## Recommended Flow

1. Choose the format loader.
2. Load the user document, or start with an empty document if the file does not exist.
3. Run migrations.
4. Merge current defaults.
5. Apply environment variables and system properties.
6. Validate required values and ranges.
7. Decode into typed application values.
8. Save the document.

## Why This Order Works

| Step | Why it happens there |
| --- | --- |
| Load | The application needs the user's current file before any operation can run. |
| Migrate | Old paths should be reshaped before defaults fill gaps. |
| Merge defaults | New defaults and comments should be added after old shapes are upgraded. |
| Apply overrides | Deployment values should win over file values and defaults. |
| Validate | Validation should inspect the values the application will actually use. |
| Decode | Typed application objects should be built only after validation succeeds. |
| Save | Saving persists migrations and new defaults. |

## Code Skeleton

```java
var loader = YamlConfigFormat.INSTANCE.loader();
var path = Path.of("config.yml");

var document = Files.exists(path)
  ? ConfigLoaders.load(path, loader)
  : ConfigDocument.empty();

migrations.migrate(document);
document.mergeDefaults(defaults, MergeOptions.conservative());
EnvironmentOverrides.system("myapp").applyTo(document);
validate(document);

var config = mapper.read(document, AppConfig.class);
ConfigLoaders.save(path, loader, document);
```

## When to Save

Save after migrations and default merging when you want user files to stay current. Skip saving only for read-only workflows, temporary conversions, or tooling that should not mutate source files.

## When to Apply Overrides

Apply overrides before typed reads. Do not save deployment-only overrides back into the file unless that is an explicit product decision.
