package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// Declares what a backend can preserve when reading and writing a document.
@PistonStyle
@Value.Immutable
public interface ConfigFormatCapabilities {
  /// Returns whether the backend preserves standalone block comments.
  ///
  /// @return `true` when block comments are preserved
  boolean blockComments();

  /// Returns whether the backend preserves inline comments.
  ///
  /// @return `true` when inline comments are preserved
  boolean inlineComments();

  /// Returns whether the backend preserves key and list ordering.
  ///
  /// @return `true` when ordering is preserved
  boolean ordering();

  /// Returns whether the backend preserves scalar types beyond strings.
  ///
  /// @return `true` when typed scalars are preserved
  boolean typedScalars();

  /// Returns whether the backend preserves list values.
  ///
  /// @return `true` when lists are preserved
  boolean lists();

  /// Creates an Immutables staged builder for format capabilities.
  ///
  /// @return capability builder
  static ImmutableConfigFormatCapabilities.BlockCommentsBuildStage builder() {
    return ImmutableConfigFormatCapabilities.builder();
  }

  /// Returns capabilities for a backend that preserves every core feature.
  ///
  /// @return full capability set
  static ConfigFormatCapabilities full() {
    return builder()
      .blockComments(true)
      .inlineComments(true)
      .ordering(true)
      .typedScalars(true)
      .lists(true)
      .build();
  }
}
