package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Comments attached to a configuration node.
///
/// Comments are split by placement so backends can preserve block comments,
/// inline comments, and trailing or end comments when their parser exposes that
/// distinction.
///
/// @param leading comments written before the node
/// @param inline comments written on the same logical line as the node
/// @param trailing comments written after the node, such as YAML end comments
public record ConfigComment(
  List<ConfigCommentLine> leading,
  List<ConfigCommentLine> inline,
  List<ConfigCommentLine> trailing
) {
  private static final ConfigComment NONE = new ConfigComment(List.of(), List.of(), List.of());

  /// Creates a comment from plain leading text and one optional inline comment.
  ///
  /// @param leading leading block comment text
  /// @param inline inline comment text, or `null` for no inline comment
  public ConfigComment(List<String> leading, String inline) {
    this(
      plainLines(leading, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH),
      inline == null || inline.isBlank()
        ? List.of()
        : List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)),
      List.of()
    );
  }

  /// Normalizes comment line lists to immutable copies.
  public ConfigComment {
    leading = List.copyOf(Objects.requireNonNull(leading, "leading"));
    inline = List.copyOf(Objects.requireNonNull(inline, "inline"));
    trailing = List.copyOf(Objects.requireNonNull(trailing, "trailing"));
  }

  /// Returns the shared empty comment value.
  ///
  /// @return empty comment value
  public static ConfigComment none() {
    return NONE;
  }

  /// Creates a leading block comment from plain text lines.
  ///
  /// @param lines leading comment lines
  /// @return comment with leading lines
  public static ConfigComment lines(String... lines) {
    return new ConfigComment(plainLines(List.of(lines), ConfigCommentType.BLOCK, ConfigCommentMarker.HASH), List.of(), List.of());
  }

  /// Creates an inline comment from plain text.
  ///
  /// @param inline inline comment text
  /// @return comment with one inline line
  public static ConfigComment inline(String inline) {
    return new ConfigComment(List.of(), List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)), List.of());
  }

  /// Creates a trailing block comment from plain text lines.
  ///
  /// @param trailing trailing comment lines
  /// @return comment with trailing lines
  public static ConfigComment trailing(String... trailing) {
    return new ConfigComment(List.of(), List.of(), plainLines(List.of(trailing), ConfigCommentType.BLOCK, ConfigCommentMarker.HASH));
  }

  /// Creates a comment from plain text while preserving placement.
  ///
  /// @param leading leading block comment text
  /// @param inline inline comment text, or `null` for no inline comment
  /// @param trailing trailing block comment text
  /// @return comment with plain text converted into core comment lines
  public static ConfigComment ofPlain(List<String> leading, String inline, List<String> trailing) {
    return new ConfigComment(
      plainLines(leading, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH),
      inline == null || inline.isBlank()
        ? List.of()
        : List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)),
      plainLines(trailing, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH)
    );
  }

  /// Returns the leading comment text.
  ///
  /// @return leading comment text
  public List<String> lines() {
    return leadingText();
  }

  /// Returns leading comment text without blank comment entries.
  ///
  /// @return leading comment text
  public List<String> leadingText() {
    return text(leading);
  }

  /// Returns inline comment text joined with spaces.
  ///
  /// @return inline comment text
  public String inlineText() {
    return String.join(" ", text(inline));
  }

  /// Returns trailing comment text without blank comment entries.
  ///
  /// @return trailing comment text
  public List<String> trailingText() {
    return text(trailing);
  }

  /// Returns all comment lines in leading, inline, trailing order.
  ///
  /// @return immutable combined comment lines
  public List<ConfigCommentLine> all() {
    var lines = new ArrayList<ConfigCommentLine>(leading.size() + inline.size() + trailing.size());
    lines.addAll(leading);
    lines.addAll(inline);
    lines.addAll(trailing);
    return List.copyOf(lines);
  }

  /// Returns whether this comment has no lines in any placement.
  ///
  /// @return `true` when the comment is empty
  public boolean isEmpty() {
    return leading.isEmpty() && inline.isEmpty() && trailing.isEmpty();
  }

  /// Returns whether this comment has inline content.
  ///
  /// @return `true` when inline comment lines exist
  public boolean hasInline() {
    return !inline.isEmpty();
  }

  /// Returns whether this comment has trailing content.
  ///
  /// @return `true` when trailing comment lines exist
  public boolean hasTrailing() {
    return !trailing.isEmpty();
  }

  private static List<ConfigCommentLine> plainLines(List<String> lines, ConfigCommentType type, ConfigCommentMarker marker) {
    if (lines == null || lines.isEmpty()) {
      return List.of();
    }

    return lines.stream()
      .map(line -> line == null || line.isEmpty()
        ? ConfigCommentLine.blank()
        : ConfigCommentLine.builder()
          .text(line)
          .type(type)
          .marker(marker)
          .build())
      .toList();
  }

  private static List<String> text(List<ConfigCommentLine> lines) {
    return lines.stream()
      .filter(line -> line.type() != ConfigCommentType.BLANK)
      .map(ConfigCommentLine::text)
      .toList();
  }
}
