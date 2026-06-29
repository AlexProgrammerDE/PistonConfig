package net.pistonmaster.pistonconfig.core;

import java.util.Objects;
import java.util.Optional;

/**
 * A complete configuration document with an object root.
 */
public final class ConfigDocument {
  private final ConfigNode root;

  private ConfigDocument(ConfigNode root) {
    this.root = Objects.requireNonNull(root, "root");
  }

  public static ConfigDocument empty() {
    return new ConfigDocument(ConfigNode.object());
  }

  public static ConfigDocument of(ConfigNode root) {
    return new ConfigDocument(root.copy());
  }

  public ConfigNode root() {
    return root;
  }

  public Optional<ConfigNode> find(ConfigPath path) {
    return root.find(path);
  }

  public Optional<ConfigNode> find(String path) {
    return find(ConfigPath.parse(path));
  }

  public ConfigDocument set(ConfigPath path, Object value) {
    root.set(path, value);
    return this;
  }

  public ConfigDocument set(String path, Object value) {
    return set(ConfigPath.parse(path), value);
  }

  public ConfigDocument setNode(ConfigPath path, ConfigNode node) {
    root.setNode(path, node);
    return this;
  }

  public ConfigDocument mergeDefaults(ConfigDocument defaults, MergeOptions options) {
    ConfigMerger.merge(root, defaults.root, options);
    return this;
  }

  public ConfigDocument copy() {
    return new ConfigDocument(root.copy());
  }
}
