---
layout: default
title: Installation
description: Install PistonConfig with Gradle, Maven, Maven Central, and GitHub Packages.
---

# Installation

PistonConfig modules are published under the `net.pistonmaster` group. Runtime artifacts use the `pistonconfig-<module>` naming pattern.

## Requirements

| Requirement | Value |
| --- | --- |
| Java | 25 |
| Group ID | `net.pistonmaster` |
| Recommended version alignment | `pistonconfig-bom` |
| Release repositories | Maven Central and GitHub Packages |

## Gradle With the BOM

Use the BOM for applications that depend on more than one module.

```kotlin
repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))
  implementation("net.pistonmaster:pistonconfig-core")
  implementation("net.pistonmaster:pistonconfig-yaml")
  implementation("net.pistonmaster:pistonconfig-annotations")
}
```

## Gradle Without the BOM

```kotlin
dependencies {
  implementation("net.pistonmaster:pistonconfig-core:0.1.0-SNAPSHOT")
  implementation("net.pistonmaster:pistonconfig-yaml:0.1.0-SNAPSHOT")
}
```

Use this form for tiny examples or when a parent build already manages dependency versions.

## Maven With the BOM

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>net.pistonmaster</groupId>
      <artifactId>pistonconfig-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>net.pistonmaster</groupId>
    <artifactId>pistonconfig-core</artifactId>
  </dependency>
  <dependency>
    <groupId>net.pistonmaster</groupId>
    <artifactId>pistonconfig-yaml</artifactId>
  </dependency>
</dependencies>
```

## GitHub Packages

GitHub Packages is configured as a release target. GitHub requires authenticated Maven access even for public packages.

```kotlin
repositories {
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/AlexProgrammerDE/PistonConfig")
    credentials {
      username = providers.gradleProperty("githubPackagesUsername").orNull
      password = providers.gradleProperty("githubPackagesToken").orNull
    }
  }
}
```

In GitHub Actions, `GITHUB_ACTOR` and `GITHUB_TOKEN` are enough for repository workflows. Locally, use a token that can read packages.

## Pick Modules

Start with `pistonconfig-core` and one format backend. Add higher-level modules only for the access style or operation you need.

| Need | Modules |
| --- | --- |
| YAML files | `pistonconfig-core`, `pistonconfig-yaml` |
| TOML files | `pistonconfig-core`, `pistonconfig-toml` |
| HOCON files | `pistonconfig-core`, `pistonconfig-hocon` |
| JSON, JSONC, or JSON5 files | `pistonconfig-core`, `pistonconfig-json` |
| `.properties` files | `pistonconfig-core`, `pistonconfig-properties` |
| Annotation mapping | `pistonconfig-core`, `pistonconfig-annotations` |
| Static typed keys | `pistonconfig-core`, `pistonconfig-static-fields` |
| Environment overrides | `pistonconfig-core`, `pistonconfig-env` |
| Versioned migrations | `pistonconfig-core`, `pistonconfig-migrations` |

## Verify the Dependency Graph

```bash
./gradlew dependencies --configuration runtimeClasspath
```

The BOM should align every `net.pistonmaster:pistonconfig-*` artifact to the same version.
