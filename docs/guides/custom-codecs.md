---
layout: default
title: Custom Codecs
description: Encode and decode custom Java types with ConfigCodecRegistry.
---

# Custom Codecs

Use a custom codec when a config value should map to an application type instead of a primitive scalar.

## Define the Type

```java
record Endpoint(String host, int port) {
}
```

Records are a good fit because they make config value objects immutable and explicit.

## Register a Codec

```java
var codecs = new ConfigCodecRegistry()
  .register(Endpoint.class, new ConfigCodec<Endpoint>() {
    @Override
    public ConfigNode encode(Endpoint value, ConfigCodecRegistry registry) {
      return ConfigNode.object()
        .set(ConfigPath.of("host"), value.host())
        .set(ConfigPath.of("port"), value.port());
    }

    @Override
    public Endpoint decode(ConfigNode node, ConfigCodecRegistry registry) {
      var host = node.find(ConfigPath.of("host"))
        .flatMap(ConfigNode::asString)
        .orElseThrow();
      var port = node.find(ConfigPath.of("port"))
        .flatMap(ConfigNode::asInt)
        .orElseThrow();
      return new Endpoint(host, port);
    }
  });
```

Codecs receive the registry so they can delegate nested values to other codecs.

## Use the Codec Directly

```java
var endpoint = new Endpoint("localhost", 25565);
var node = codecs.encode(endpoint);
var decoded = codecs.decode(node, Endpoint.class);
```

## Use the Codec With Annotations

```java
final class ServerConfig {
  Endpoint endpoint = new Endpoint("localhost", 25565);
}

var mapper = new AnnotatedConfigMapper(codecs);
var defaults = mapper.writeDefaults(new ServerConfig());
var config = mapper.read(defaults, ServerConfig.class);
```

## Use the Codec With Static Fields

```java
final class ServerOptions {
  static final ConfigProperty<Endpoint> ENDPOINT = ConfigProperty.<Endpoint>builder()
    .path(ConfigPath.parse("endpoint"))
    .type(Endpoint.class)
    .defaultValue(new Endpoint("localhost", 25565))
    .build();
}

var definition = StaticConfigDefinition.from(ServerOptions.class);
var endpoint = definition.get(document, ServerOptions.ENDPOINT, codecs);
```

## Error Handling

The built-in scalar codecs throw `ConfigException` when a value cannot be decoded. Custom codecs should do the same when a node is structurally invalid.
