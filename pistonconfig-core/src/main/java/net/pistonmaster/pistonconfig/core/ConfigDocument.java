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

  /// Sets a scalar value at a dotted path, creating parent objects when needed.
  ///
  /// @param path dotted path to write
  /// @param value scalar value
  /// @return this document
  public ConfigDocument set(String path, Object value) {
    return set(ConfigPath.parse(path), value);
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
