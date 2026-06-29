package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class ConfigCommentTest {
  @Test
  void createsLeadingInlineAndTrailingTextViews() {
    var comment = ConfigComment.builder()
      .addLeading(line("first", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .addLeading(line("", ConfigCommentType.BLANK, ConfigCommentMarker.NONE))
      .addLeading(line("second", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .addInline(line("inline value", ConfigCommentType.INLINE, ConfigCommentMarker.HASH))
      .addTrailing(line("trailing", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .build();

    assertEquals(List.of("first", "second"), comment.leadingText());
    assertEquals("inline value", comment.inlineText());
    assertEquals(List.of("trailing"), comment.trailingText());
    assertEquals(5, comment.all().size());
    assertTrue(comment.hasInline());
    assertTrue(comment.hasTrailing());
    assertFalse(comment.isEmpty());
  }

  @Test
  void builderSupportsEachCommentPosition() {
    var comment = ConfigComment.builder()
      .addLeading(line("first", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .addLeading(line("second", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .addInline(line("same line", ConfigCommentType.INLINE, ConfigCommentMarker.HASH))
      .addTrailing(line("after", ConfigCommentType.BLOCK, ConfigCommentMarker.HASH))
      .build();

    assertEquals(List.of("first", "second"), comment.leadingText());
    assertEquals("same line", comment.inlineText());
    assertEquals(List.of("after"), comment.trailingText());
  }

  @Test
  void commentLinesCarryTypeAndMarkerInformation() {
    var line = line("note", ConfigCommentType.INLINE, ConfigCommentMarker.DOUBLE_SLASH);
    var blank = line("", ConfigCommentType.BLANK, ConfigCommentMarker.NONE);

    assertEquals("note", line.text());
    assertEquals(ConfigCommentType.INLINE, line.type());
    assertEquals(ConfigCommentMarker.DOUBLE_SLASH, line.marker());
    assertEquals(ConfigCommentType.BLANK, blank.type());
    assertEquals(ConfigCommentMarker.NONE, blank.marker());
  }

  private static ConfigCommentLine line(String text, ConfigCommentType type, ConfigCommentMarker marker) {
    return ConfigCommentLine.builder()
      .text(text)
      .type(type)
      .marker(marker)
      .build();
  }
}
