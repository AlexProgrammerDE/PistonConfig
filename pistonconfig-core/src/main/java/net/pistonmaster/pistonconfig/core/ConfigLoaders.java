package net.pistonmaster.pistonconfig.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Convenience methods for loading and saving documents from paths.
 */
public final class ConfigLoaders {
  private ConfigLoaders() {
  }

  public static ConfigDocument load(Path path, ConfigLoader loader) {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(loader, "loader");

    try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      return loader.load(reader);
    } catch (IOException exception) {
      throw new ConfigException("Could not load configuration from " + path + ".", exception);
    }
  }

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
