package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class ConfigCommentTest {
  @Test
  void createsLeadingInlineAndTrailingTextViews() {
    var comment = ConfigComment.ofPlain(
      List.of("first", "", "second"),
      "inline value",
      List.of("trailing")
    );

    assertEquals(List.of("first", "second"), comment.leadingText());
    assertEquals("inline value", comment.inlineText());
    assertEquals(List.of("trailing"), comment.trailingText());
    assertEquals(5, comment.all().size());
    assertTrue(comment.hasInline());
    assertTrue(comment.hasTrailing());
    assertFalse(comment.isEmpty());
  }

  @Test
  void helperFactoriesUseExpectedCommentPositions() {
    var leading = ConfigComment.lines("first", "second");
    var inline = ConfigComment.inline("same line");
    var trailing = ConfigComment.trailing("after");

    assertEquals(List.of("first", "second"), leading.leadingText());
    assertEquals("same line", inline.inlineText());
    assertEquals(List.of("after"), trailing.trailingText());
  }

  @Test
  void commentLinesCarryTypeAndMarkerInformation() {
    var line = ConfigCommentLine.inline("note", ConfigCommentMarker.DOUBLE_SLASH);
    var blank = ConfigCommentLine.blank();

    assertEquals("note", line.text());
    assertEquals(ConfigCommentType.INLINE, line.type());
    assertEquals(ConfigCommentMarker.DOUBLE_SLASH, line.marker());
    assertEquals(ConfigCommentType.BLANK, blank.type());
    assertEquals(ConfigCommentMarker.NONE, blank.marker());
  }
}
