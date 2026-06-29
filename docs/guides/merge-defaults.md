---
layout: default
title: Merge Defaults
description: Merge generated defaults into existing user configuration documents.
---

# Merge Defaults

Default merging keeps a user's current file useful while letting your application ship new defaults, new comments, and new sections.

## Conservative Merge

```java
var current = ConfigDocument.empty()
  .set("server.port", 25566);

var defaults = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565);

current.mergeDefaults(defaults, MergeOptions.conservative());
```

After this merge:

| Path | Value |
| --- | --- |
| `server.host` | `0.0.0.0` |
| `server.port` | `25566` |

The user value stays in place. The missing default is added.

## Merge Options

`MergeOptions` uses an Immutables staged builder, so custom behavior is named at the call site instead of hidden behind positional booleans.

| Field | Purpose |
| --- | --- |
| `updateComments` | Refresh comments from the defaults without touching user values. |
| `removeUnknown` | Remove object keys that the defaults do not declare. |
| `listStrategy` | How to merge when both the user value and the default are lists. |

The presets cover the common cases. `MergeOptions.conservative()` refreshes comments, keeps unknown keys, and preserves existing lists. `MergeOptions.exactDefaults()` refreshes comments, removes unknown keys, and replaces lists.

## Exact Defaults

```java
current.mergeDefaults(defaults, MergeOptions.exactDefaults());
```

Exact defaults are useful for generated files where unknown keys should be removed and lists should match the defaults. Avoid this mode for user-authored files unless that behavior is explicit in your application.

## List Strategies

```java
var options = MergeOptions.builder()
  .updateComments(true)
  .removeUnknown(false)
  .listStrategy(MergeListStrategy.APPEND_MISSING)
  .build();
```

| Strategy | Behavior |
| --- | --- |
| `PRESERVE_EXISTING` | Keep the user's list exactly as it is. |
| `REPLACE` | Replace the user's list with the default list. |
| `APPEND_MISSING` | Append default items by index when the user's list is shorter. |

## Comment Refresh

When `updateComments` is true, defaults can refresh comments without replacing user values.

```java
var options = MergeOptions.builder()
  .updateComments(true)
  .removeUnknown(false)
  .listStrategy(MergeListStrategy.PRESERVE_EXISTING)
  .build();
```

This is useful when your shipped defaults improve wording or add comments for new versions.

## Recommended Order

1. Load the user's file.
2. Run migrations.
3. Merge current defaults.
4. Apply environment and system property overrides.
5. Save the file.

This order lets migrations reshape old paths before defaults fill gaps, then lets deployment overrides win.
