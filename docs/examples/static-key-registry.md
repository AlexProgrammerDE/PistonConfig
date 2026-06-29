---
layout: default
title: Static Key Registry
description: Centralize typed config keys, defaults, comments, and reads with static fields.
---

# Static Key Registry

This example uses static properties when many classes need to read the same keys without repeating string paths.

## Registry

```java
final class ServerOptions implements StaticConfigComments {
  @ConfigComment("Address used by the public listener.")
  static final ConfigProperty<String> HOST =
    ConfigProperty.of("server.host", String.class, "0.0.0.0");

  @ConfigComment("Port used by the public listener.")
  static final ConfigProperty<Integer> PORT =
    ConfigProperty.of("server.port", Integer.class, 25565);

  @ConfigComment("Whether metrics should be collected.")
  static final ConfigProperty<Boolean> METRICS =
    ConfigProperty.of("features.metrics", Boolean.class, true);

  private ServerOptions() {
  }

  @Override
  public void registerComments(StaticConfigCommentRegistry comments) {
    comments.setRootComment("Application configuration.");
    comments.setComment("server", "Network listener settings.");
    comments.setComment("features", "Feature toggles.");
  }
}
```

## Store and Reads

```java
var store = StaticConfigStore.builder()
  .holders(ServerOptions.class)
  .format(YamlConfigFormat.INSTANCE)
  .build();

var session = store.update(Path.of("config.yml"));

var host = session.get(ServerOptions.HOST);
var port = session.get(ServerOptions.PORT);
var metrics = session.get(ServerOptions.METRICS);
```

## Why It Helps

| Problem | Static property behavior |
| --- | --- |
| Repeated string paths | Paths live in one declaration. |
| Missing defaults | Each key carries its own default value. |
| Comments drift | Comments stay near the key they describe. |
| Type mistakes | Reads use the declared `ConfigType<T>`. |
| File lifecycle | `StaticConfigStore` handles update, save, reload, and typed access. |

Static properties are a good fit for libraries, plugins, and larger applications where config keys are used in multiple places.
