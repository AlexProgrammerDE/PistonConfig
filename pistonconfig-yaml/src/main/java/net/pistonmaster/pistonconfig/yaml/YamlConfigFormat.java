package net.pistonmaster.pistonconfig.yaml;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/**
 * YAML format backend.
 */
public final class YamlConfigFormat implements ConfigFormat {
  public static final YamlConfigFormat INSTANCE = new YamlConfigFormat();

  private final ConfigLoader loader = new YamlConfigLoader();

  private YamlConfigFormat() {
  }

  @Override
  public String name() {
    return "yaml";
  }

  @Override
  public Set<String> extensions() {
    return Set.of("yaml", "yml");
  }

  @Override
  public ConfigFormatCapabilities capabilities() {
    return ConfigFormatCapabilities.full();
  }

  @Override
  public ConfigLoader loader() {
    return loader;
  }
}
