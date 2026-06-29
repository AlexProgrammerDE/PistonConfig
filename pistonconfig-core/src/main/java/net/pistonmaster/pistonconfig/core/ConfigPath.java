package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// Immutable dotted path used to address nodes inside a configuration document.
///
/// Dot characters and backslashes inside a segment are escaped by [toString]
/// and understood by [parse].
public final class ConfigPath {
  private static final ConfigPath ROOT = new ConfigPath(List.of());

  private final List<String> segments;

  private ConfigPath(List<String> segments) {
    this.segments = List.copyOf(segments);
  }

  /// Returns the root path.
  ///
  /// @return root path
  public static ConfigPath root() {
    return ROOT;
  }

  /// Creates a path from individual segments.
  ///
  /// @param first first path segment
  /// @param rest remaining path segments
  /// @return path containing all supplied segments
  public static ConfigPath of(String first, String... rest) {
    Objects.requireNonNull(first, "first");
    Objects.requireNonNull(rest, "rest");

    var segments = new ArrayList<String>(rest.length + 1);
    segments.add(first);
    segments.addAll(Arrays.asList(rest));
    return new ConfigPath(validate(segments));
  }

  /// Parses a dotted path string.
  ///
  /// Dots separate path segments. A backslash escapes the next character, so
  /// `server\.host` addresses a single segment named `server.host`.
  ///
  /// @param path dotted path string
  /// @return parsed path
  public static ConfigPath parse(String path) {
    Objects.requireNonNull(path, "path");
    if (path.isEmpty()) {
      return root();
    }

    var segments = new ArrayList<String>();
    var current = new StringBuilder();
    boolean escaping = false;

    for (int index = 0; index < path.length(); index++) {
      char character = path.charAt(index);

      if (escaping) {
        current.append(character);
        escaping = false;
        continue;
      }

      if (character == '\\') {
        escaping = true;
        continue;
      }

      if (character == '.') {
        segments.add(current.toString());
        current.setLength(0);
        continue;
      }

      current.append(character);
    }

    if (escaping) {
      current.append('\\');
    }

    segments.add(current.toString());
    return new ConfigPath(validate(segments));
  }

  /// Returns path segments.
  ///
  /// @return immutable path segments
  public List<String> segments() {
    return segments;
  }

  /// Returns whether this path addresses the root node.
  ///
  /// @return `true` for the root path
  public boolean isRoot() {
    return segments.isEmpty();
  }

  /// Returns the final path segment.
  ///
  /// @return final segment
  /// @throws ConfigException when this is the root path
  public String lastSegment() {
    if (segments.isEmpty()) {
      throw new ConfigException("The root path has no last segment.");
    }

    return segments.getLast();
  }

  /// Returns the parent path.
  ///
  /// @return parent path, or empty for the root path
  public Optional<ConfigPath> parent() {
    if (segments.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ConfigPath(segments.subList(0, segments.size() - 1)));
  }

  /// Creates a child path by appending one segment.
  ///
  /// @param segment child segment
  /// @return child path
  public ConfigPath child(String segment) {
    validateSegment(segment);

    var childSegments = new ArrayList<>(segments);
    childSegments.add(segment);
    return new ConfigPath(childSegments);
  }

  /// Renders this path as an escaped dotted string.
  ///
  /// @return dotted path string
  @Override
  public String toString() {
    if (segments.isEmpty()) {
      return "";
    }

    var escaped = new ArrayList<String>(segments.size());
    for (String segment : segments) {
      escaped.add(segment.replace("\\", "\\\\").replace(".", "\\."));
    }

    return String.join(".", escaped);
  }

  /// Compares this path by segment value.
  ///
  /// @param other value to compare
  /// @return `true` when both paths have the same segments
  @Override
  public boolean equals(Object other) {
    return other instanceof ConfigPath path && segments.equals(path.segments);
  }

  /// Returns the segment list hash code.
  ///
  /// @return hash code
  @Override
  public int hashCode() {
    return segments.hashCode();
  }

  private static List<String> validate(List<String> segments) {
    for (String segment : segments) {
      validateSegment(segment);
    }

    return Collections.unmodifiableList(new ArrayList<>(segments));
  }

  private static void validateSegment(String segment) {
    Objects.requireNonNull(segment, "segment");
    if (segment.isEmpty()) {
      throw new ConfigException("Configuration path segments cannot be empty.");
    }
  }
}
