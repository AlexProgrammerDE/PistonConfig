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

`MergeOptions` uses an Immutables builder, so custom behavior is named at the call site instead of hidden behind positional booleans.

| Field | Purpose |
| --- | --- |
| `commentStrategy` | How comments and presentation decorations from defaults merge into existing nodes. |
| `removeUnknown` | Remove object keys that the defaults do not declare. |
| `listStrategy` | How to merge when both the user value and the default are lists. |
| `valueStrategy` | When an existing value should be replaced by the default value. |

The presets cover the common cases. `MergeOptions.conservative()` fills missing comments, repairs invalid node shapes, keeps unknown keys, and preserves existing lists. `MergeOptions.exactDefaults()` replaces comments, removes unknown keys, replaces lists, and replaces existing values declared by the defaults.

## Exact Defaults

```java
current.mergeDefaults(defaults, MergeOptions.exactDefaults());
```

Exact defaults are useful for generated files where unknown keys should be removed and lists should match the defaults. Avoid this mode for user-authored files unless that behavior is explicit in your application.

## List Strategies

```java
var options = MergeOptions.builder()
  .removeUnknown(false)
  .listStrategy(MergeListStrategy.APPEND_MISSING)
  .build();
```

| Strategy | Behavior |
| --- | --- |
| `PRESERVE_EXISTING` | Keep the user's list exactly as it is. |
| `REPLACE` | Replace the user's list with the default list. |
| `APPEND_MISSING` | Append default items by index when the user's list is shorter. |

## Value Strategies

```java
var options = MergeOptions.builder()
  .valueStrategy(MergeValueStrategy.REPLACE_INVALID)
  .build();
```

| Strategy | Behavior |
| --- | --- |
| `PRESERVE_EXISTING` | Keep existing values, even when their node kind differs from the default. |
| `REPLACE_INVALID` | Replace existing values only when their node kind differs from the default. |
| `REPLACE_EXISTING` | Replace values declared by the defaults. |

`REPLACE_INVALID` is useful for user-authored files because it fixes old scalar/object/list shape mistakes without replacing compatible values.

## Comment Strategies

Comment strategies apply to node comments and presentation-oriented decorations such as key comments, scalar style, collection style, and backend attributes. Source locations are kept on existing nodes.

```java
var options = MergeOptions.builder()
  .commentStrategy(MergeCommentStrategy.FILL_MISSING)
  .build();
```

| Strategy | Behavior |
| --- | --- |
| `KEEP_EXISTING` | Keep existing comments and presentation decorations. |
| `FILL_MISSING` | Copy default comments and presentation decorations only where the target has none. |
| `REPLACE` | Replace existing comments and presentation decorations with the defaults. |

`FILL_MISSING` is the default for conservative merging. It lets new comments appear without overwriting comments users already edited.

## Recommended Order

1. Load the user's file.
2. Run migrations.
3. Merge current defaults.
4. Apply environment and system property overrides.
5. Save the file.

This order lets migrations reshape old paths before defaults fill gaps, then lets deployment overrides win.
