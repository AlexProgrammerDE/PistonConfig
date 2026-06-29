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

  /// Creates a block comment line.
  ///
  /// @param text comment text
  /// @param marker source marker, or `null` for `ConfigCommentMarker.UNKNOWN`
  /// @return block comment line
  static ConfigCommentLine block(String text, ConfigCommentMarker marker) {
    return builder()
      .text(text == null ? "" : text)
      .type(ConfigCommentType.BLOCK)
      .marker(marker == null ? ConfigCommentMarker.UNKNOWN : marker)
      .build();
  }

  /// Creates an inline comment line.
  ///
  /// @param text comment text
  /// @param marker source marker, or `null` for `ConfigCommentMarker.UNKNOWN`
  /// @return inline comment line
  static ConfigCommentLine inline(String text, ConfigCommentMarker marker) {
    return builder()
      .text(text == null ? "" : text)
      .type(ConfigCommentType.INLINE)
      .marker(marker == null ? ConfigCommentMarker.UNKNOWN : marker)
      .build();
  }

  /// Creates a blank comment line.
  ///
  /// @return blank comment line
  static ConfigCommentLine blank() {
    return builder()
      .text("")
      .type(ConfigCommentType.BLANK)
      .marker(ConfigCommentMarker.NONE)
      .build();
  }
}
