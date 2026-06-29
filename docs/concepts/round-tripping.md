---
layout: default
title: Round Tripping
description: What round-tripping means across PistonConfig's format-neutral core model.
---

# Round Tripping

Round-tripping means loading a file into a document and saving it again while preserving as much useful information as the backend exposes.

## Not Every Format Exposes the Same Detail

YAML can expose key comments, value comments, anchors, tags, scalar styles, and source marks. Properties files expose separators and layout. HOCON exposes origin information. TOML and JSON-family parsers expose different subsets.

PistonConfig stores shared details in core fields and format-specific details in metadata.

## Three Levels of Preservation

| Level | Meaning |
| --- | --- |
| Value preservation | The application value survives load and save. |
| Document preservation | Comments, ordering, styles, and metadata remain available in `ConfigDocument`. |
| Format preservation | The same backend can write useful source detail back out. |

A format conversion can preserve values and document metadata while still losing source syntax that the target format cannot express.

## Metadata Is Inspectable

```java
node.decorations().attributes().forEach((key, value) ->
  log.debug("{} = {}", key, value));

node.metadata().forEach((key, value) ->
  log.debug("{} = {}", key, value));
```

Use metadata for diagnostics, conversion tools, and backend-aware behavior. Avoid depending on metadata when the same code should behave identically across every format.

## Practical Expectation

PistonConfig aims to avoid throwing away information that parser libraries make available. It does not promise byte-for-byte formatting preservation across every backend or across different target formats.
