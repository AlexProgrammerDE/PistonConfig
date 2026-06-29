package net.pistonmaster.pistonconfig.properties;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/**
 * Java properties format backend.
 */
public final class PropertiesConfigFormat implements ConfigFormat {
  public static final PropertiesConfigFormat INSTANCE = new PropertiesConfigFormat();

  private final ConfigLoader loader = new PropertiesConfigLoader();

  private PropertiesConfigFormat() {
  }

  @Override
  public String name() {
    return "properties";
  }

  @Override
  public Set<String> extensions() {
    return Set.of("properties");
  }

  @Override
  public ConfigFormatCapabilities capabilities() {
    return new ConfigFormatCapabilities(true, false, true, false, false);
  }

  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
