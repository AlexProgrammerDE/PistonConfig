---
layout: default
title: Annotation Configs
description: Map Java objects and records to PistonConfig documents.
---

# Annotation Configs

Use `pistonconfig-annotations` when a Java type should define defaults, names, comments, and typed access for a config file.

## Add the Module

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-annotations")
  implementation("net.pistonmaster:pistonconfig-yaml")
}
```

## Define a Config Type

```java
@ConfigPathPrefix("server")
record ServerConfig(
  @ConfigName("bind-address")
  @ConfigComment("Address used by the public listener.")
  String host,
  int port,
  List<User> users,
  Map<Mode, Endpoint> endpoints
) {
  ServerConfig() {
    this(
      "0.0.0.0",
      25565,
      List.of(new User("root", true)),
      Map.of(Mode.PROD, new Endpoint("example.com", 443))
    );
  }
}

record User(String name, boolean admin) {
}

record Endpoint(String host, int port) {
}

enum Mode {
  PROD
}
```

Records use a no-args constructor for defaults when one exists. Classes need a no-args constructor and mutable fields.

## Load and Update a File

```java
var store = ConfigStores.forType(ServerConfig.class)
  .format(YamlConfigFormat.INSTANCE)
  .build();

var config = store.update(Path.of("config.yml"));
```

`update` creates the file when it is missing, merges missing defaults when it exists, refreshes generated comments, saves the document, and returns the typed config object.

## Use the Mapper Directly

```java
var mapper = new AnnotatedConfigMapper();
var defaults = mapper.writeDefaults(ServerConfig.class);
var config = mapper.read(defaults, ServerConfig.class);
```

Use the mapper directly when your application already controls loading, migrations, merging, or saving.

## Configure Mapping

```java
var options = ConfigMapperOptions.builder()
  .nameFormatter(ConfigNameFormatters.KEBAB_CASE)
  .unknownKeyPolicy(ConfigUnknownKeyPolicy.DROP)
  .outputNulls(false)
  .inputNulls(false)
  .build();

var mapper = new AnnotatedConfigMapper(options);
```

Options control name formatting, explicit null handling, stale key behavior during store updates, list merge behavior, scalar coercion, and custom serializers.

## Supported Shapes

The mapper supports:

- records and POJOs with no-args constructors
- inherited instance fields
- nested config objects
- primitive types, wrappers, strings, characters, and enums
- `BigInteger`, `BigDecimal`, `LocalDate`, `LocalTime`, `LocalDateTime`, `Instant`, `Duration`, `Period`, `UUID`, `File`, `Path`, `URL`, and `URI`
- arrays, `List<T>`, `Set<T>`, and `Map<K, V>`
- scalar, string-value, and enum map keys

Raw generics, wildcard generics, type variables, and collection or config-object map keys are rejected with `ConfigException`.

## Field Selection Rules

| Member kind | Mapped |
| --- | --- |
| Record component | yes |
| Instance field | yes |
| Inherited instance field | yes |
| Static field | no |
| Transient field | no |
| Final field | no |
| Member annotated with `@ConfigIgnore` | no |
