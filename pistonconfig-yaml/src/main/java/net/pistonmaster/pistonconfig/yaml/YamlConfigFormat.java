package net.pistonmaster.pistonconfig.yaml;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/// YAML format backend.
public final class YamlConfigFormat implements ConfigFormat {
  /// Shared YAML format descriptor.
  public static final YamlConfigFormat INSTANCE = new YamlConfigFormat();

  private final ConfigLoader loader = new YamlConfigLoader();

  private YamlConfigFormat() {
  }

  /// Returns `yaml`.
  ///
  /// @return backend name
  @Override
  public String name() {
    return "yaml";
  }

  /// Returns YAML file extensions.
  ///
  /// @return supported extensions
  @Override
  public Set<String> extensions() {
    return Set.of("yaml", "yml");
  }

  /// Returns the preservation capabilities of the YAML backend.
  ///
  /// @return backend capabilities
  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.full();
  }

  /// Returns the shared YAML loader.
  ///
  /// @return YAML loader
  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
