package net.pistonmaster.pistonconfig.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/// Convenience methods for loading and saving documents from paths.
public final class ConfigLoaders {
  private ConfigLoaders() {
  }

  /// Loads a UTF-8 configuration file through a loader.
  ///
  /// @param path file path to read
  /// @param loader format loader
  /// @return loaded document
  /// @throws ConfigException when the file cannot be read
  public static ConfigDocument load(Path path, ConfigLoader loader) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(loader, "loader");

    try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return loader.load(reader);
    } catch (IOException exception) {
      throw new ConfigException("Could not load configuration from " + path + ".", exception);
    }
  }

  /// Saves a document as UTF-8 through a loader, creating parent directories.
  ///
  /// @param path file path to write
  /// @param loader format loader
  /// @param document document to save
  /// @throws ConfigException when the file cannot be written
  public static void save(Path path, ConfigLoader loader, ConfigDocument document) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(loader, "loader");
    Objects.requireNonNull(document, "document");

    try {
      var parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }

      try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        loader.save(document, writer);
      }
    } catch (IOException exception) {
      throw new ConfigException("Could not save configuration to " + path + ".", exception);
    }
  }
}
