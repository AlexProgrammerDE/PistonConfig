package net.pistonmaster.pistonconfig.hocon;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/// HOCON format backend.
public final class HoconConfigFormat implements ConfigFormat {
  /// Shared HOCON format descriptor.
  public static final HoconConfigFormat INSTANCE = new HoconConfigFormat();

  private final ConfigLoader loader = new HoconConfigLoader();

  private HoconConfigFormat() {
  }

  /// Returns `hocon`.
  ///
  /// @return backend name
  @Override
  public String name() {
    return "hocon";
  }

  /// Returns HOCON file extensions.
  ///
  /// @return supported extensions
  @Override
  public Set<String> extensions() {
    return Set.of("conf", "hocon");
  }

  /// Returns the preservation capabilities of the HOCON backend.
  ///
  /// @return backend capabilities
  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.full();
  }

  /// Returns the shared HOCON loader.
  ///
  /// @return HOCON loader
  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
