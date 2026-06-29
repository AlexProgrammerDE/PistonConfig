package net.pistonmaster.pistonconfig.staticfields;

import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.PistonStyle;
import org.immutables.value.Value;

/// Static-field-friendly declaration of a typed configuration value.
///
/// @param <T> value type
@PistonStyle
@Value.Immutable
public interface ConfigProperty<T> {
  /// Returns the configuration path.
  ///
  /// @return configuration path
  ConfigPath path();

  /// Returns the Java value type.
  ///
  /// @return Java value type
  Class<T> type();

  /// Returns the default value used when the path is missing.
  ///
  /// @return default value
  T defaultValue();

  /// Returns comments written when defaults are generated.
  ///
  /// @return default comment
  @Value.Default
  default ConfigComment comment() {
    return ConfigComment.none();
  }

  /// Creates an Immutables staged builder for config property declarations.
  ///
  /// @param <T> value type
  /// @return config property builder
  static <T> ImmutableConfigProperty.PathBuildStage<T> builder() {
    return ImmutableConfigProperty.builder();
  }
}
