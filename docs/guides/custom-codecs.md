---
layout: default
title: Custom Serialization
description: Encode and decode custom Java types for typed configs and static fields.
---

# Custom Serialization

Use custom serialization when a config value should map to an application type that is not covered by the built-in scalar and object mapper.

## Typed Config Serializers

For annotation configs, register a `ConfigSerializer<T>` in `ConfigMapperOptions`.

```java
record Endpoint(String host, int port) {
}

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

var mapper = new AnnotatedConfigMapper(options);
```

The serializer receives a context so it can delegate nested values back to the mapper when needed.

## Serializer Annotations

Use `@ConfigSerializeWith` when one member needs a serializer without changing every use of the type.

```java
record ServerConfig(
  @ConfigSerializeWith(EndpointSerializer.class)
  Endpoint endpoint
) {
}
```

For collection elements, set `nesting`.

```java
record ServerConfig(
  @ConfigSerializeWith(value = EndpointSerializer.class, nesting = 1)
  List<Endpoint> endpoints
) {
}
```

## Static Field Codecs

`pistonconfig-static-fields` still uses `ConfigCodecRegistry`. Register a `ConfigCodec<T>` when a static `ConfigProperty<T>` needs an application type.

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
      var host = node.find(ConfigPath.of("host")).flatMap(ConfigNode::asString).orElseThrow();
      var port = node.find(ConfigPath.of("port")).flatMap(ConfigNode::asInt).orElseThrow();
      return new Endpoint(host, port);
    }
  });
```

## Error Handling

Throw `ConfigException` when a node has the wrong shape or a scalar value cannot be parsed. That keeps format errors, mapping errors, and application validation errors under the same exception type.
