---
layout: default
title: Typed Record Config
description: Use a Java record with the generic typed store API.
---

# Typed Record Config

Records are the most direct way to define an immutable typed config.

## Config Type

```java
@ConfigPathPrefix("server")
record ServerConfig(
  @ConfigName("bind-address")
  String host,
  int port,
  Endpoint endpoint
) {
  ServerConfig() {
    this("0.0.0.0", 25565, new Endpoint("localhost", 25565));
  }
}

record Endpoint(String host, int port) {
}
```

The no-args constructor supplies defaults for `ConfigStore.update`.

## Store

```java
var store = ConfigStores.forType(ServerConfig.class)
  .format(YamlConfigFormat.INSTANCE)
  .build();

var config = store.update(Path.of("config.yml"));
```

The store is format-agnostic. Swap `YamlConfigFormat.INSTANCE` for another backend when the file format changes.

## Custom Serializer

Nested records are mapped as objects by default. Use a serializer only when you want another representation.

```java
final class EndpointSerializer implements ConfigSerializer<Endpoint> {
  @Override
  public ConfigNode encode(Endpoint value, ConfigSerializationContext context) {
    return ConfigNode.scalar(value.host() + ":" + value.port());
  }

  @Override
  public Endpoint decode(ConfigNode node, ConfigSerializationContext context) {
    var parts = node.asString().orElseThrow().split(":", 2);
    return new Endpoint(parts[0], Integer.parseInt(parts[1]));
  }
}
```

```java
var options = ConfigMapperOptions.builder()
  .serializer(Endpoint.class, new EndpointSerializer())
  .build();

var store = ConfigStores.forType(ServerConfig.class)
  .format(YamlConfigFormat.INSTANCE)
  .options(options)
  .build();
```
