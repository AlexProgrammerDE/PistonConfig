---
layout: default
title: Manual API
description: Work directly with configuration documents, nodes, paths, comments, and decorations.
---

# Manual API

Use the manual API when code needs direct control over the configuration tree. It is the lowest-level application API above a format loader.

## Create a Document

```java
var document = ConfigDocument.empty()
  .set("server.host", "0.0.0.0")
  .set("server.port", 25565)
  .set("features.whitelist", true);
```

`ConfigDocument` always has an object root. Setting a dotted path creates missing object nodes along the way.

## Work With Paths

```java
var dotted = ConfigPath.parse("server.port");
var literalDot = ConfigPath.of("database.url");
var escapedDot = ConfigPath.parse("database\\.url");
```

Use `ConfigPath.parse(...)` for normal dotted paths. Use `ConfigPath.of(...)` when a key segment contains punctuation that should not be interpreted as a path separator.

## Read Values

```java
var port = document.find("server.port")
  .flatMap(ConfigNode::asInt)
  .orElse(25565);

var enabled = document.find("server.online-mode")
  .flatMap(ConfigNode::asBoolean)
  .orElse(true);
```

Scalar accessors convert compatible string values, such as `"25565"` to an integer or `"true"` to a boolean.

## Replace Nodes

```java
document.setNode(
  ConfigPath.of("modules"),
  ConfigNode.list()
    .addListValue("core")
    .addListValue("yaml")
);
```

`setNode` copies the replacement node. Later mutations on the original replacement object do not mutate the stored document.

## Remove Nodes

```java
document.root().remove(ConfigPath.parse("legacy.enabled"));
```

Removing the root resets it to an empty object and returns the previous root as a copy.

## Add Comments

```java
document.root()
  .getOrCreate(ConfigPath.parse("server.port"))
  .setComment(ConfigComment.builder()
    .addLeading(ConfigCommentLine.builder()
      .text("Port used by the public listener.")
      .type(ConfigCommentType.BLOCK)
      .marker(ConfigCommentMarker.HASH)
      .build())
    .addInline(ConfigCommentLine.builder()
      .text("Restart required")
      .type(ConfigCommentType.INLINE)
      .marker(ConfigCommentMarker.HASH)
      .build())
    .build());
```

`ConfigComment` separates leading, inline, and trailing comments. `ConfigCommentLine` keeps the logical type and source marker when the backend exposes them.

## Add Decorations

```java
document.root()
  .getOrCreate(ConfigPath.parse("server"))
  .decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
    .withCollectionStyle(ConfigCollectionStyle.BLOCK));
```

Decorations store source-level details that are not part of the typed value. Examples include key comments, scalar style, collection style, source locations, and string attributes.

## Load and Save

```java
var loader = YamlConfigFormat.INSTANCE.loader();
var path = Path.of("config.yml");

var document = ConfigLoaders.load(path, loader);
ConfigLoaders.save(path, loader, document);
```

Use `Reader` and `Writer` directly when your application owns storage. Use `ConfigLoaders` for path-based file IO.

## Convert to Immutable Values

```java
ConfigValue value = ConfigValue.fromNode(document.root());
ConfigNode node = ConfigValue.toNode(value);
```

`ConfigValue` is a sealed immutable value model. It is useful for codecs and adapters that should not expose a mutable `ConfigNode`.
