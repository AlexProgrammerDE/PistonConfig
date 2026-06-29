package net.pistonmaster.pistonconfig.json;

import java.util.Set;
import net.pistonmaster.pistonconfig.core.ConfigFormat;
import net.pistonmaster.pistonconfig.core.ConfigFormatCapabilities;
import net.pistonmaster.pistonconfig.core.ConfigLoader;

/**
 * Standard JSON format backend.
 */
public final class JsonConfigFormat implements ConfigFormat {
  public static final JsonConfigFormat INSTANCE = new JsonConfigFormat();

  private final ConfigLoader loader = new JsonConfigLoader();

  private JsonConfigFormat() {
  }

  @Override
  public String name() {
    return "json";
  }

  @Override
  public Set<String> extensions() {
    return Set.of("json", "jsonc", "json5");
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
