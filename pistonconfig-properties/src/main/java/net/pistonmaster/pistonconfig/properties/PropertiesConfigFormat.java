package net.pistonmaster.pistonconfig.properties;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/// Java properties format backend.
public final class PropertiesConfigFormat implements ConfigFormat {
  /// Shared properties format descriptor.
  public static final PropertiesConfigFormat INSTANCE = new PropertiesConfigFormat();

  private final ConfigLoader loader = new PropertiesConfigLoader();

  private PropertiesConfigFormat() {
  }

  /// Returns `properties`.
  ///
  /// @return backend name
  @Override
  public String name() {
    return "properties";
  }

  /// Returns the properties file extension.
  ///
  /// @return supported extensions
  @Override
  public Set<String> extensions() {
    return Set.of("properties");
  }

  /// Returns the preservation capabilities of the properties backend.
  ///
  /// @return backend capabilities
  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.builder()
      .blockComments(true)
      .inlineComments(false)
      .ordering(true)
      .typedScalars(false)
      .lists(false)
      .build();
  }

  /// Returns the shared properties loader.
  ///
  /// @return properties loader
  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
