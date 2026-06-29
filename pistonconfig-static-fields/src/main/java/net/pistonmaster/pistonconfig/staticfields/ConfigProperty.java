package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Static-field-friendly declaration of a typed configuration value.
///
/// @param path configuration path
/// @param type Java value type
/// @param defaultValue default value used when the path is missing
/// @param comment comments written when defaults are generated
/// @param <T> value type
public record ConfigProperty<T>(
  ConfigPath path,
  Class<T> type,
  T defaultValue,
  ConfigComment comment
) {
  /// Normalizes required components and replaces a missing comment with an empty comment.
  public ConfigProperty {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(type, "type");
    comment = comment == null ? ConfigComment.none() : comment;
  }

  /// Creates a property declaration from a dotted path.
  ///
  /// @param path dotted configuration path
  /// @param type Java value type
  /// @param defaultValue default value
  /// @param <T> value type
  /// @return config property declaration
  public static <T> ConfigProperty<T> of(String path, Class<T> type, T defaultValue) {
    return new ConfigProperty<>(ConfigPath.parse(path), type, defaultValue, ConfigComment.none());
  }

  /// Returns a copy with leading comment lines.
  ///
  /// @param lines comment lines
  /// @return property declaration with comments
  public ConfigProperty<T> withComment(String... lines) {
    return new ConfigProperty<>(path, type, defaultValue, ConfigComment.lines(lines));
  }
}
