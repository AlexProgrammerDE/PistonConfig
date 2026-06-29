---
layout: default
title: Guides
description: Task-focused guides for building configuration workflows with PistonConfig.
---

# Guides

Use these guides when you are wiring PistonConfig into an application, choosing a backend, defining typed access, or planning upgrade behavior.

{: .lead }
Start with the full startup flow, then branch into the access style or operational feature your project needs.

## Core Workflow

<div class="doc-grid">
  <a class="link-card" href="getting-started.html">
    <h3>Getting Started</h3>
    <p>Load, merge, override, read, and save a document.</p>
  </a>
  <a class="link-card" href="installation.html">
    <h3>Installation</h3>
    <p>Install modules with Gradle, Maven, the BOM, Maven Central, and GitHub Packages.</p>
  </a>
  <a class="link-card" href="multi-format-apps.html">
    <h3>Multi-Format Apps</h3>
    <p>Dispatch loaders by extension and keep application behavior format-neutral.</p>
  </a>
  <a class="link-card" href="manual-api.html">
    <h3>Manual API</h3>
    <p>Edit documents, nodes, comments, paths, and decorations directly.</p>
  </a>
</div>

## Typed Access

<div class="doc-grid">
  <a class="link-card" href="custom-codecs.html">
    <h3>Custom Codecs</h3>
    <p>Encode records and domain value objects through the codec registry.</p>
  </a>
  <a class="link-card" href="annotation-configs.html">
    <h3>Annotation Configs</h3>
    <p>Map Java fields to defaults, comments, names, and path prefixes.</p>
  </a>
  <a class="link-card" href="static-field-configs.html">
    <h3>Static Fields</h3>
    <p>Declare typed keys once and read values through shared property declarations.</p>
  </a>
  <a class="link-card" href="../concepts/access-styles.html">
    <h3>Choose an Access Style</h3>
    <p>Compare manual nodes, annotations, static fields, and custom codecs.</p>
  </a>
</div>

## Operations

<div class="doc-grid">
  <a class="link-card" href="format-backends.html">
    <h3>Format Backends</h3>
    <p>Choose YAML, TOML, HOCON, JSON-family, or properties support.</p>
  </a>
  <a class="link-card" href="merge-defaults.html">
    <h3>Merge Defaults</h3>
    <p>Merge shipped defaults into existing user documents safely.</p>
  </a>
  <a class="link-card" href="environment-overrides.html">
    <h3>Environment Overrides</h3>
    <p>Apply process environment and system property values on top of files.</p>
  </a>
  <a class="link-card" href="migrations.html">
    <h3>Migrations</h3>
    <p>Run ordered schema migrations and store the applied version.</p>
  </a>
  <a class="link-card" href="testing-configs.html">
    <h3>Testing Configs</h3>
    <p>Test codecs, mappings, migrations, and round-trip behavior.</p>
  </a>
  <a class="link-card" href="validation-and-diagnostics.html">
    <h3>Diagnostics</h3>
    <p>Report missing values, invalid types, and source-aware config errors.</p>
  </a>
</div>
