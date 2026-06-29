---
layout: default
title: Concepts
description: Explanation-oriented documentation for PistonConfig design decisions.
---

# Concepts

These pages explain why PistonConfig separates format backends, the core model, typed access styles, and operational workflows.

## Architecture

<div class="doc-grid">
  <a class="link-card" href="design-goals.html">
    <h3>Design Goals</h3>
    <p>Why configuration files are treated as both data and documents.</p>
  </a>
  <a class="link-card" href="lossless-configuration.html">
    <h3>Lossless Configuration</h3>
    <p>How values, comments, decorations, and metadata stay separate.</p>
  </a>
  <a class="link-card" href="round-tripping.html">
    <h3>Round Tripping</h3>
    <p>What preservation means when different formats expose different detail.</p>
  </a>
  <a class="link-card" href="type-safety.html">
    <h3>Type Safety</h3>
    <p>How codecs, records, annotations, and static properties keep call sites typed.</p>
  </a>
</div>

## Application Design

<div class="doc-grid">
  <a class="link-card" href="access-styles.html">
    <h3>Access Styles</h3>
    <p>Choose between manual nodes, annotated classes, static keys, and codecs.</p>
  </a>
  <a class="link-card" href="migration-strategy.html">
    <h3>Migration Strategy</h3>
    <p>Plan schema versions, defaults, overrides, and user edits across releases.</p>
  </a>
</div>
