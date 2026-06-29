package net.pistonmaster.pistonconfig.toml;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/**
 * TOML format backend.
 */
public final class TomlConfigFormat implements ConfigFormat {
  public static final TomlConfigFormat INSTANCE = new TomlConfigFormat();

  private final ConfigLoader loader = new TomlConfigLoader();

  private TomlConfigFormat() {
  }

  @Override
  public String name() {
    return "toml";
  }

  @Override
  public Set<String> extensions() {
    return Set.of("toml");
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
