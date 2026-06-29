package net.pistonmaster.pistonconfig.json;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/// JSON, JSONC, and JSON5 format backend.
public final class JsonConfigFormat implements ConfigFormat {
  /// Shared JSON format descriptor.
  public static final JsonConfigFormat INSTANCE = new JsonConfigFormat();

  private final ConfigLoader loader = new JsonConfigLoader();

  private JsonConfigFormat() {
  }

  /// Returns `json`.
  ///
  /// @return backend name
  @Override
  public String name() {
    return "json";
  }

  /// Returns JSON-family file extensions.
  ///
  /// @return supported extensions
  @Override
  public Set<String> extensions() {
    return Set.of("json", "jsonc", "json5");
  }

  /// Returns the preservation capabilities of the JSON backend.
  ///
  /// @return backend capabilities
  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.full();
  }

  /// Returns the shared JSON loader.
  ///
  /// @return JSON loader
  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
