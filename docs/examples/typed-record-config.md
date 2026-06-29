---
layout: default
title: Typed Record Config
description: Use a Java record with a custom codec, annotation mapping, and static properties.
---

# Typed Record Config

Records work well for config value objects because they make the shape immutable and explicit.

## Record Type

```java
record Endpoint(String host, int port) {
}
```

## Codec

```java
static ConfigCodec<Endpoint> endpointCodec() {
  return new ConfigCodec<>() {
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
  };
}
```

## Annotation Mapping

```java
final class ServerConfig {
  Endpoint endpoint = new Endpoint("localhost", 25565);
}

var codecs = new ConfigCodecRegistry()
  .register(Endpoint.class, endpointCodec());

var mapper = new AnnotatedConfigMapper(codecs);
var defaults = mapper.writeDefaults(new ServerConfig());
var config = mapper.read(defaults, ServerConfig.class);
```

## Static Property

```java
final class ServerOptions {
  static final ConfigProperty<Endpoint> ENDPOINT = ConfigProperty
    .of("server.endpoint", Endpoint.class, new Endpoint("localhost", 25565))
    .withComment("Public listener endpoint.");
}

var definition = StaticConfigDefinition.from(ServerOptions.class);
var endpoint = definition.get(document, ServerOptions.ENDPOINT, codecs);
```

Use the same codec no matter which access style reads the value.
