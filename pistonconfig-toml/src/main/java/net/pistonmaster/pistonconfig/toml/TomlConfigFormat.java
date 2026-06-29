package net.pistonmaster.pistonconfig.toml;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/// TOML format backend.
public final class TomlConfigFormat implements ConfigFormat {
  /// Shared TOML format descriptor.
  public static final TomlConfigFormat INSTANCE = new TomlConfigFormat();

  private final ConfigLoader loader = new TomlConfigLoader();

  private TomlConfigFormat() {
  }

  /// Returns `toml`.
  ///
  /// @return backend name
  @Override
  public String name() {
    return "toml";
  }

  /// Returns the TOML file extension.
  ///
  /// @return supported extensions
  @Override
  public Set<String> extensions() {
    return Set.of("toml");
  }

  /// Returns the preservation capabilities of the TOML backend.
  ///
  /// @return backend capabilities
  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.full();
  }

  /// Returns the shared TOML loader.
  ///
  /// @return TOML loader
  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
