---
layout: default
title: Installation
description: Install PistonConfig with Gradle, Maven, Maven Central, and GitHub Packages.
---

# Installation

PistonConfig publishes every module under the `net.pistonmaster` group. Module artifacts use the `pistonconfig-<module>` naming pattern.

## Gradle With the BOM

Use the BOM when an application needs more than one PistonConfig module. It keeps all module versions aligned.

```kotlin
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

## Maven Central

Release artifacts are deployed to Maven Central. Most Gradle and Maven projects already include Maven Central:

```kotlin
repositories {
  mavenCentral()
}
```

## GitHub Packages

Releases are also published to GitHub Packages. GitHub Packages requires authentication even for public packages.

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

In GitHub Actions, use `GITHUB_ACTOR` and `GITHUB_TOKEN`. Locally, use a GitHub token with package read access.

## Pick Modules

Start with `pistonconfig-core` and one format backend. Add higher-level modules only when the application needs that style.

| Need | Modules |
| --- | --- |
| YAML files | `pistonconfig-core`, `pistonconfig-yaml` |
| TOML files | `pistonconfig-core`, `pistonconfig-toml` |
| HOCON files | `pistonconfig-core`, `pistonconfig-hocon` |
| Annotation mapping | `pistonconfig-core`, `pistonconfig-annotations` |
| Static keys | `pistonconfig-core`, `pistonconfig-static-fields` |
| Runtime overrides | `pistonconfig-core`, `pistonconfig-env` |
| Versioned migrations | `pistonconfig-core`, `pistonconfig-migrations` |
