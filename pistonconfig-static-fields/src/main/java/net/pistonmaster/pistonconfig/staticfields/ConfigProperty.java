package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/**
 * Static field friendly declaration of a typed configuration value.
 *
 * @param <T> value type
 */
public record ConfigProperty<T>(
  ConfigPath path,
  Class<T> type,
  T defaultValue,
  ConfigComment comment
) {
  public ConfigProperty {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(type, "type");
    comment = comment == null ? ConfigComment.none() : comment;
  }

  public static <T> ConfigProperty<T> of(String path, Class<T> type, T defaultValue) {
    return new ConfigProperty<>(ConfigPath.parse(path), type, defaultValue, ConfigComment.none());
  }

  public ConfigProperty<T> withComment(String... lines) {
    return new ConfigProperty<>(path, type, defaultValue, ConfigComment.lines(lines));
  }
}
