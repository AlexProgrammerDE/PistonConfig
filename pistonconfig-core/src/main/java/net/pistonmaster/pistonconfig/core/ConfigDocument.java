package net.pistonmaster.pistonconfig.core;

import java.util.Objects;
import java.util.Optional;

/// A complete configuration document with a mutable root node.
///
/// Documents are the top-level values passed to loaders, format backends,
/// mergers, migrations, and mapping APIs.
public final class ConfigDocument {
  private final ConfigNode root;

  private ConfigDocument(ConfigNode root) {
    this.root = Objects.requireNonNull(root, "root");
  }

  /// Creates an empty document with an object root.
  ///
  /// @return empty document
  public static ConfigDocument empty() {
    return new ConfigDocument(ConfigNode.object());
  }

  /// Creates a document by copying an existing root node.
  ///
  /// @param root source root node
  /// @return document containing a copy of `root`
  public static ConfigDocument of(ConfigNode root) {
    return new ConfigDocument(root.copy());
  }

  /// Returns the mutable root node.
  ///
  /// @return document root
  public ConfigNode root() {
    return root;
  }

  /// Finds a node by path.
  ///
  /// @param path path to read
  /// @return node when the path exists
  public Optional<ConfigNode> find(ConfigPath path) {
    return root.find(path);
  }

  /// Finds a node by dotted path.
  ///
  /// @param path dotted path to read
  /// @return node when the path exists
  public Optional<ConfigNode> find(String path) {
    return find(ConfigPath.parse(path));
  }

  /// Sets a scalar value at a path, creating parent objects when needed.
  ///
  /// @param path path to write
  /// @param value scalar value
  /// @return this document
  public ConfigDocument set(ConfigPath path, Object value) {
    root.set(path, value);
    return this;
  }

  /// Sets a scalar value at a path with replacement options.
  ///
  /// @param path path to write
  /// @param value scalar value
  /// @param options replacement behavior
  /// @return this document
  public ConfigDocument set(ConfigPath path, Object value, ConfigReplacementOptions options) {
    root.set(path, value, options);
    return this;
  }

  /// Sets a scalar value at a dotted path, creating parent objects when needed.
  ///
  /// @param path dotted path to write
  /// @param value scalar value
  /// @return this document
  public ConfigDocument set(String path, Object value) {
    return set(ConfigPath.parse(path), value);
  }

  /// Sets a scalar value at a dotted path with replacement options.
  ///
  /// @param path dotted path to write
  /// @param value scalar value
  /// @param options replacement behavior
  /// @return this document
  public ConfigDocument set(String path, Object value, ConfigReplacementOptions options) {
    return set(ConfigPath.parse(path), value, options);
  }

  /// Sets a scalar value while preserving comments and source decorations from the existing node.
  ///
  /// @param path path to write
  /// @param value scalar value
  /// @return this document
  public ConfigDocument setPreservingSource(ConfigPath path, Object value) {
    root.setPreservingSource(path, value);
    return this;
  }

  /// Sets a scalar value at a dotted path while preserving comments and source decorations from the existing node.
  ///
  /// @param path dotted path to write
  /// @param value scalar value
  /// @return this document
  public ConfigDocument setPreservingSource(String path, Object value) {
    return setPreservingSource(ConfigPath.parse(path), value);
  }

  /// Sets a node at a path, copying the supplied node into the document.
  ///
  /// @param path path to write
  /// @param node node to copy into the document
  /// @return this document
  public ConfigDocument setNode(ConfigPath path, ConfigNode node) {
    root.setNode(path, node);
    return this;
  }

  /// Sets a node at a path with replacement options.
  ///
  /// @param path path to write
  /// @param node node to copy into the document
  /// @param options replacement behavior
  /// @return this document
  public ConfigDocument setNode(ConfigPath path, ConfigNode node, ConfigReplacementOptions options) {
    root.setNode(path, node, options);
    return this;
  }

  /// Sets a node while preserving comments and source decorations from the existing node.
  ///
  /// @param path path to write
  /// @param node node to copy into the document
  /// @return this document
  public ConfigDocument setNodePreservingSource(ConfigPath path, ConfigNode node) {
    root.setNodePreservingSource(path, node);
    return this;
  }

  /// Removes a node by path.
  ///
  /// Removing the root resets this document to an empty object and returns the previous root.
  ///
  /// @param path path to remove
  /// @return removed node when one existed
  public Optional<ConfigNode> remove(ConfigPath path) {
    return root.remove(path);
  }

  /// Removes a node by dotted path.
  ///
  /// Removing the root resets this document to an empty object and returns the previous root.
  ///
  /// @param path dotted path to remove
  /// @return removed node when one existed
  public Optional<ConfigNode> remove(String path) {
    return remove(ConfigPath.parse(path));
  }

  /// Merges a default document into this document.
  ///
  /// @param defaults default document
  /// @param options merge behavior
  /// @return this document
  public ConfigDocument mergeDefaults(ConfigDocument defaults, MergeOptions options) {
    ConfigMerger.merge(root, defaults.root, options);
    return this;
  }

  /// Creates a deep copy of this document.
  ///
  /// @return copied document
  public ConfigDocument copy() {
    return new ConfigDocument(root.copy());
  }
}
