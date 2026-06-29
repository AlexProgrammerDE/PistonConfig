package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Comments attached to a configuration node.
 *
 * @param leading comments written before the node
 * @param inline comments written on the same logical line as the node
 * @param trailing comments written after the node, such as YAML end comments
 */
public record ConfigComment(
  List<ConfigCommentLine> leading,
  List<ConfigCommentLine> inline,
  List<ConfigCommentLine> trailing
) {
  private static final ConfigComment NONE = new ConfigComment(List.of(), List.of(), List.of());

  public ConfigComment(List<String> leading, String inline) {
    this(
      plainLines(leading, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH),
      inline == null || inline.isBlank()
        ? List.of()
        : List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)),
      List.of()
    );
  }

  public ConfigComment {
    leading = List.copyOf(Objects.requireNonNull(leading, "leading"));
    inline = List.copyOf(Objects.requireNonNull(inline, "inline"));
    trailing = List.copyOf(Objects.requireNonNull(trailing, "trailing"));
  }

  public static ConfigComment none() {
    return NONE;
  }

  public static ConfigComment lines(String... lines) {
    return new ConfigComment(plainLines(List.of(lines), ConfigCommentType.BLOCK, ConfigCommentMarker.HASH), List.of(), List.of());
  }

  public static ConfigComment inline(String inline) {
    return new ConfigComment(List.of(), List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)), List.of());
  }

  public static ConfigComment trailing(String... trailing) {
    return new ConfigComment(List.of(), List.of(), plainLines(List.of(trailing), ConfigCommentType.BLOCK, ConfigCommentMarker.HASH));
  }

  public static ConfigComment ofPlain(List<String> leading, String inline, List<String> trailing) {
    return new ConfigComment(
      plainLines(leading, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH),
      inline == null || inline.isBlank()
        ? List.of()
        : List.of(ConfigCommentLine.inline(inline, ConfigCommentMarker.HASH)),
      plainLines(trailing, ConfigCommentType.BLOCK, ConfigCommentMarker.HASH)
    );
  }

  public List<String> lines() {
    return leadingText();
  }

  public List<String> leadingText() {
    return text(leading);
  }

  public String inlineText() {
    return String.join(" ", text(inline));
  }

  public List<String> trailingText() {
    return text(trailing);
  }

  public List<ConfigCommentLine> all() {
    var lines = new ArrayList<ConfigCommentLine>(leading.size() + inline.size() + trailing.size());
    lines.addAll(leading);
    lines.addAll(inline);
    lines.addAll(trailing);
    return List.copyOf(lines);
  }

  public boolean isEmpty() {
    return leading.isEmpty() && inline.isEmpty() && trailing.isEmpty();
  }

  public boolean hasInline() {
    return !inline.isEmpty();
  }

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
