package net.pistonmaster.pistonconfig.staticfields;

import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Static-field-friendly declaration of a typed configuration value.
///
/// @param path configuration path
/// @param type typed value shape
/// @param defaultValue default value used when the path is missing or rewritten
/// @param comment comments written when defaults are generated
/// @param <T> value type
public record ConfigProperty<T>(
  ConfigPath path,
  ConfigType<T> type,
  T defaultValue,
  net.pistonmaster.pistonconfig.core.ConfigComment comment
) {
  /// Creates a property declaration.
  public ConfigProperty {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(defaultValue, "defaultValue");
    Objects.requireNonNull(comment, "comment");
  }

  /// Creates a property for a simple Java type handled by [ConfigCodecRegistry].
  ///
  /// @param path dotted path
  /// @param type Java type
  /// @param defaultValue default value
  /// @param <T> value type
  /// @return property declaration
  public static <T> ConfigProperty<T> of(String path, Class<T> type, T defaultValue) {
    return of(ConfigPath.parse(path), ConfigType.of(type), defaultValue);
  }

  /// Creates a property for a simple Java type handled by [ConfigCodecRegistry].
  ///
  /// @param path config path
  /// @param type Java type
  /// @param defaultValue default value
  /// @param <T> value type
  /// @return property declaration
  public static <T> ConfigProperty<T> of(ConfigPath path, Class<T> type, T defaultValue) {
    return of(path, ConfigType.of(type), defaultValue);
  }

  /// Creates a property for a full [ConfigType].
  ///
  /// @param path dotted path
  /// @param type config type
  /// @param defaultValue default value
  /// @param <T> value type
  /// @return property declaration
  public static <T> ConfigProperty<T> of(String path, ConfigType<T> type, T defaultValue) {
    return of(ConfigPath.parse(path), type, defaultValue);
  }

  /// Creates a property for a full [ConfigType].
  ///
  /// @param path config path
  /// @param type config type
  /// @param defaultValue default value
  /// @param <T> value type
  /// @return property declaration
  public static <T> ConfigProperty<T> of(ConfigPath path, ConfigType<T> type, T defaultValue) {
    return new ConfigProperty<>(path, type, defaultValue, net.pistonmaster.pistonconfig.core.ConfigComment.none());
  }

  /// Returns a copy with the supplied comment.
  ///
  /// @param comment property comment
  /// @return copied property
  public ConfigProperty<T> withComment(net.pistonmaster.pistonconfig.core.ConfigComment comment) {
    return new ConfigProperty<>(path, type, defaultValue, comment);
  }

  /// Returns a copy with a leading block comment built from text lines.
  ///
  /// @param lines comment lines
  /// @return copied property
  public ConfigProperty<T> withComment(String... lines) {
    return withComment(comment(lines));
  }

  static net.pistonmaster.pistonconfig.core.ConfigComment comment(String... lines) {
    Objects.requireNonNull(lines, "lines");
    var builder = net.pistonmaster.pistonconfig.core.ConfigComment.builder();
    for (String line : lines) {
      builder.addLeading(commentLine(line));
    }
    return builder.build();
  }

  static ConfigCommentLine commentLine(String line) {
    Objects.requireNonNull(line, "line");
    if (line.isEmpty()) {
      return ConfigCommentLine.builder()
        .text("")
        .type(ConfigCommentType.BLANK)
        .marker(ConfigCommentMarker.HASH)
        .build();
    }

    return ConfigCommentLine.builder()
      .text(line)
      .type(ConfigCommentType.BLOCK)
      .marker(ConfigCommentMarker.HASH)
      .build();
  }
}
