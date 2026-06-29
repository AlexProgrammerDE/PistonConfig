---
layout: default
title: Publishing
description: Release publishing targets, artifacts, and validation commands.
---

# Publishing

PistonConfig publishes release artifacts to Maven Central and GitHub Packages.

## Coordinates

| Artifact | Coordinate |
| --- | --- |
| BOM | `net.pistonmaster:pistonconfig-bom` |
| Core | `net.pistonmaster:pistonconfig-core` |
| YAML | `net.pistonmaster:pistonconfig-yaml` |
| Properties | `net.pistonmaster:pistonconfig-properties` |
| JSON | `net.pistonmaster:pistonconfig-json` |
| TOML | `net.pistonmaster:pistonconfig-toml` |
| HOCON | `net.pistonmaster:pistonconfig-hocon` |
| Annotations | `net.pistonmaster:pistonconfig-annotations` |
| Environment overrides | `net.pistonmaster:pistonconfig-env` |
| Static fields | `net.pistonmaster:pistonconfig-static-fields` |
| Migrations | `net.pistonmaster:pistonconfig-migrations` |

## Release Workflow

The release workflow:

1. Checks out the repository.
2. Sets up Java 25.
3. Runs tests.
4. Publishes all publications to the local staging repository used by JReleaser.
5. Publishes all publications to GitHub Packages.
6. Lets JReleaser deploy the staged artifacts to Maven Central.

The workflow grants `packages: write` so the repository `GITHUB_TOKEN` can publish to GitHub Packages.

## Published Attachments

Java library modules publish:

- Main JAR.
- Sources JAR.
- Javadocs JAR.
- Maven metadata.
- Signatures for release publishing.

The BOM is a Java Platform publication. It publishes a POM that constrains all PistonConfig module versions.

## Local Checks

```bash
./gradlew build
./gradlew publishAllPublicationsToStagingRepository
./gradlew publishAllPublicationsToGitHubPackagesRepository --dry-run
```

The GitHub Packages publish task needs credentials when it is run without `--dry-run`.

## Release Inputs

| Secret | Used by |
| --- | --- |
| `SIGNING_IN_MEMORY_KEY` | Gradle signing and JReleaser signing |
| `SIGNING_IN_MEMORY_KEY_PASSWORD` | Gradle signing and JReleaser signing |
| `SIGNING_PUBLIC_KEY` | JReleaser |
| `MAVEN_CENTRAL_USERNAME` | JReleaser Maven Central deploy |
| `MAVEN_CENTRAL_PASSWORD` | JReleaser Maven Central deploy |
| `GITHUB_TOKEN` | GitHub release metadata and GitHub Packages |
