package net.pistonmaster.pistonconfig.staticfields;

import java.nio.file.Path;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigDocument;

/// Stateful static config session bound to one path.
public final class StaticConfigSession {
  private final StaticConfigStore store;
  private final Path path;
  private ConfigDocument document;

  StaticConfigSession(StaticConfigStore store, Path path, ConfigDocument document) {
    this.store = Objects.requireNonNull(store, "store");
    this.path = Objects.requireNonNull(path, "path");
    this.document = Objects.requireNonNull(document, "document");
  }

  /// Returns the backing store.
  ///
  /// @return static config store
  public StaticConfigStore store() {
    return store;
  }

  /// Returns the session path.
  ///
  /// @return config path
  public Path path() {
    return path;
  }

  /// Returns the mutable document.
  ///
  /// @return current document
  public ConfigDocument document() {
    return document;
  }

  /// Reads a property.
  ///
  /// @param property property declaration
  /// @param <T> value type
  /// @return resolved value
  public <T> T get(ConfigProperty<T> property) {
    return store.get(document, property);
  }

  /// Reads a property with source metadata.
  ///
  /// @param property property declaration
  /// @param <T> value type
  /// @return read result
  public <T> StaticConfigValue<T> resolve(ConfigProperty<T> property) {
    return store.resolve(document, property);
  }

  /// Sets a property value in the current document.
  ///
  /// @param property property declaration
  /// @param value value to write
  /// @param <T> value type
  public <T> void set(ConfigProperty<T> property, T value) {
    store.set(document, property, value);
  }

  /// Reloads the current document from disk and reruns validators.
  public void reload() {
    document = store.reloadDocument(path);
    store.validate(this);
  }

  /// Saves the current document to disk.
  public void save() {
    store.saveDocument(path, document);
  }
}
