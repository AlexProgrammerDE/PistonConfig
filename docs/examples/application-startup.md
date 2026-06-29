---
layout: default
title: Application Startup
description: Complete startup flow with YAML, defaults, migrations, overrides, typed reads, and save.
---

# Application Startup

This example shows the common production flow for one YAML config file.

## Dependencies

```kotlin
dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
  implementation("net.pistonmaster:pistonconfig-env")
  implementation("net.pistonmaster:pistonconfig-migrations")
}
```

## Startup Function

```java
final class ConfigBootstrap {
  private static final Path CONFIG_PATH = Path.of("config.yml");
  private static final ConfigLoader LOADER = YamlConfigFormat.INSTANCE.loader();

  ConfigDocument load() {
    var document = Files.exists(CONFIG_PATH)
      ? ConfigLoaders.load(CONFIG_PATH, LOADER)
      : ConfigDocument.empty();

    migrations().migrate(document);
    document.mergeDefaults(defaults(), MergeOptions.conservative());
    EnvironmentOverrides.system("piston").applyTo(document);
    validate(document);
    ConfigLoaders.save(CONFIG_PATH, LOADER, document);
    return document;
  }

  private static ConfigDocument defaults() {
    var document = ConfigDocument.empty()
      .set("server.host", "0.0.0.0")
      .set("server.port", 25565)
      .set("features.metrics", true);

    document.root().getOrCreate(ConfigPath.parse("server.port"))
      .setComment(ConfigComment.builder()
        .addLeading(ConfigCommentLine.builder()
          .text("Port used by the public listener.")
          .type(ConfigCommentType.BLOCK)
          .marker(ConfigCommentMarker.HASH)
          .build())
        .build());
    return document;
  }

  private static MigrationRegistry migrations() {
    return MigrationRegistry.builder()
      .versionPath(ConfigPath.parse("config.version"))
      .addMigration(ConfigMigration.builder()
        .version(1)
        .action(config -> Migrations.rename(config, "server.bind", "server.host"))
        .build())
      .build();
  }

  private static void validate(ConfigDocument document) {
    var port = document.find("server.port")
      .flatMap(ConfigNode::asInt)
      .orElseThrow(() -> new ConfigException("Missing server.port."));

    if (port < 1 || port > 65535) {
      throw new ConfigException("server.port must be between 1 and 65535.");
    }
  }
}
```

## Read Application Values

```java
var document = new ConfigBootstrap().load();
var host = document.find("server.host").flatMap(ConfigNode::asString).orElse("0.0.0.0");
var port = document.find("server.port").flatMap(ConfigNode::asInt).orElse(25565);
```

This keeps startup behavior explicit and makes the saved file reflect migrations and new defaults.
