package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.List;
import org.immutables.value.Value;

/// Comments attached to a configuration node.
///
/// Comments are split by placement so backends can preserve block comments,
/// inline comments, and trailing or end comments when their parser exposes that
/// distinction.
@PistonStyle
@Value.Immutable
public interface ConfigComment {
  /// Returns comments written before the node.
  ///
  /// @return leading comments
  List<ConfigCommentLine> leading();

  /// Returns comments written on the same logical line as the node.
  ///
  /// @return inline comments
  List<ConfigCommentLine> inline();

  /// Returns comments written after the node, such as YAML end comments.
  ///
  /// @return trailing comments
  List<ConfigCommentLine> trailing();

  /// Creates an Immutables builder for comments.
  ///
  /// @return comment builder
  static ImmutableConfigComment.Builder builder() {
    return ImmutableConfigComment.builder();
  }

  /// Returns an empty comment value.
  ///
  /// @return empty comment value
  static ConfigComment none() {
    return builder().build();
  }

  /// Returns the leading comment text.
  ///
  /// @return leading comment text
  default List<String> lines() {
    return leadingText();
  }

  /// Returns leading comment text without blank comment entries.
  ///
  /// @return leading comment text
  default List<String> leadingText() {
    return text(leading());
  }

  /// Returns inline comment text joined with spaces.
  ///
  /// @return inline comment text
  default String inlineText() {
    return String.join(" ", text(inline()));
  }

  /// Returns trailing comment text without blank comment entries.
  ///
  /// @return trailing comment text
  default List<String> trailingText() {
    return text(trailing());
  }

  /// Returns all comment lines in leading, inline, trailing order.
  ///
  /// @return immutable combined comment lines
  default List<ConfigCommentLine> all() {
    var lines = new ArrayList<ConfigCommentLine>(leading().size() + inline().size() + trailing().size());
    lines.addAll(leading());
    lines.addAll(inline());
    lines.addAll(trailing());
    return List.copyOf(lines);
  }

  /// Returns whether this comment has no lines in any placement.
  ///
  /// @return `true` when the comment is empty
  default boolean isEmpty() {
    return leading().isEmpty() && inline().isEmpty() && trailing().isEmpty();
  }

  /// Returns whether this comment has inline content.
  ///
  /// @return `true` when inline comment lines exist
  default boolean hasInline() {
    return !inline().isEmpty();
  }

  /// Returns whether this comment has trailing content.
  ///
  /// @return `true` when trailing comment lines exist
  default boolean hasTrailing() {
    return !trailing().isEmpty();
  }

  private static List<String> text(List<ConfigCommentLine> lines) {
    return lines.stream()
      .filter(line -> line.type() != ConfigCommentType.BLANK)
      .map(ConfigCommentLine::text)
      .toList();
  }
}
