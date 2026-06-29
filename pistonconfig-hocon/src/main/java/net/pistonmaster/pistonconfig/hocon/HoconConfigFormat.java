package net.pistonmaster.pistonconfig.hocon;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/**
 * HOCON format backend.
 */
public final class HoconConfigFormat implements ConfigFormat {
  public static final HoconConfigFormat INSTANCE = new HoconConfigFormat();

  private final ConfigLoader loader = new HoconConfigLoader();

  private HoconConfigFormat() {
  }

  @Override
  public String name() {
    return "hocon";
  }

  @Override
  public Set<String> extensions() {
    return Set.of("conf", "hocon");
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
