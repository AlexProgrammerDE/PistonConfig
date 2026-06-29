package net.pistonmaster.pistonconfig.core;

import org.immutables.value.Value;

/**
 * One comment line, including its logical shape and source marker.
 */
@PistonStyle
@Value.Immutable
public interface ConfigCommentLine {
  String text();

  ConfigCommentType type();

  ConfigCommentMarker marker();

  static ImmutableConfigCommentLine.TextBuildStage builder() {
    return ImmutableConfigCommentLine.builder();
  }

  static ConfigCommentLine block(String text, ConfigCommentMarker marker) {
    return builder()
      .text(text == null ? "" : text)
      .type(ConfigCommentType.BLOCK)
      .marker(marker == null ? ConfigCommentMarker.UNKNOWN : marker)
      .build();
  }

  static ConfigCommentLine inline(String text, ConfigCommentMarker marker) {
    return builder()
      .text(text == null ? "" : text)
      .type(ConfigCommentType.INLINE)
      .marker(marker == null ? ConfigCommentMarker.UNKNOWN : marker)
      .build();
  }

  static ConfigCommentLine blank() {
    return builder()
      .text("")
      .type(ConfigCommentType.BLANK)
      .marker(ConfigCommentMarker.NONE)
      .build();
  }
}
