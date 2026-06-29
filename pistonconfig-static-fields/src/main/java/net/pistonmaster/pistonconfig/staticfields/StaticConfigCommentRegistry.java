package net.pistonmaster.pistonconfig.staticfields;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigPath;

/// Registry used by [StaticConfigComments] holders.
public final class StaticConfigCommentRegistry {
  private final Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments = new LinkedHashMap<>();
  private net.pistonmaster.pistonconfig.core.ConfigComment rootComment = net.pistonmaster.pistonconfig.core.ConfigComment.none();

  /// Creates an empty comment registry.
  public StaticConfigCommentRegistry() {
  }

  /// Sets a generated comment for a property or section path.
  ///
  /// @param path dotted path
  /// @param lines comment lines
  public void setComment(String path, String... lines) {
    setComment(ConfigPath.parse(path), lines);
  }

  /// Sets a generated comment for a property or section path.
  ///
  /// @param path config path
  /// @param lines comment lines
  public void setComment(ConfigPath path, String... lines) {
    comments.put(Objects.requireNonNull(path, "path"), leadingComment(lines));
  }

  /// Sets generated root comments.
  ///
  /// @param lines comment lines
  public void setRootComment(String... lines) {
    rootComment = net.pistonmaster.pistonconfig.core.ConfigComment.builder()
      .addAllLeading(commentLines(lines))
      .addAllTrailing(rootComment.trailing())
      .build();
  }

  /// Sets generated footer comments.
  ///
  /// Backends that support trailing root comments can render these near the end
  /// of the file.
  ///
  /// @param lines comment lines
  public void setFooterComment(String... lines) {
    rootComment = net.pistonmaster.pistonconfig.core.ConfigComment.builder()
      .addAllLeading(rootComment.leading())
      .addAllTrailing(commentLines(lines))
      .build();
  }

  Map<ConfigPath, net.pistonmaster.pistonconfig.core.ConfigComment> comments() {
    return Map.copyOf(comments);
  }

  net.pistonmaster.pistonconfig.core.ConfigComment rootComment() {
    return rootComment;
  }

  private static net.pistonmaster.pistonconfig.core.ConfigComment leadingComment(String... lines) {
    return net.pistonmaster.pistonconfig.core.ConfigComment.builder()
      .addAllLeading(commentLines(lines))
      .build();
  }

  private static java.util.List<ConfigCommentLine> commentLines(String... lines) {
    Objects.requireNonNull(lines, "lines");
    return java.util.Arrays.stream(lines)
      .map(StaticConfigCommentRegistry::commentLine)
      .toList();
  }

  private static ConfigCommentLine commentLine(String line) {
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
