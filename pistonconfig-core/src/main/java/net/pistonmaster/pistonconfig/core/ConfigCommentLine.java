package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/// One comment line, including its logical shape and source marker.
@PistonStyle
@Value.Immutable
public interface ConfigCommentLine {
  /// Returns the comment text without the source marker.
  ///
  /// @return comment text
  String text();

  /// Returns the logical placement or shape of this comment line.
  ///
  /// @return comment type
  ConfigCommentType type();

  /// Returns the marker used by the source format.
  ///
  /// @return comment marker
  ConfigCommentMarker marker();

  /// Creates an Immutables staged builder for a comment line.
  ///
  /// @return staged builder
  static ImmutableConfigCommentLine.TextBuildStage builder() {
    return ImmutableConfigCommentLine.builder();
  }
}
