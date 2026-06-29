package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/// Merges default configuration documents into current user configuration documents.
public final class ConfigMerger {
  private ConfigMerger() {
  }

  /// Merges default values into a target node in place.
  ///
  /// Object nodes are merged key by key. List nodes follow the strategy in
  /// `MergeOptions.listStrategy()`. Existing scalar and mismatched values follow
  /// `MergeOptions.valueStrategy()`.
  ///
  /// @param target current user node to update
  /// @param defaults default node to merge from
  /// @param options merge behavior
  public static void merge(ConfigNode target, ConfigNode defaults, MergeOptions options) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(defaults, "defaults");
    Objects.requireNonNull(options, "options");

    if (!(target.isObject() && defaults.isObject()) && shouldReplace(target, defaults, options.valueStrategy())) {
      target.copyFrom(defaults);
      return;
    }

    mergeSource(target, defaults, options.commentStrategy());

    if (target.isObject() && defaults.isObject()) {
      mergeObjects(target, defaults, options);
      return;
    }

    if (target.isList() && defaults.isList()) {
      mergeLists(target, defaults, options);
    }
  }

  private static void mergeObjects(ConfigNode target, ConfigNode defaults, MergeOptions options) {
    var targetChildren = target.mutableObjectChildren();

    for (var defaultEntry : defaults.objectChildren().entrySet()) {
      var existing = targetChildren.get(defaultEntry.getKey());
      if (existing == null) {
        targetChildren.put(defaultEntry.getKey(), defaultEntry.getValue().copy());
        continue;
      }

      if (shouldReplace(existing, defaultEntry.getValue(), options.valueStrategy())) {
        targetChildren.put(defaultEntry.getKey(), defaultEntry.getValue().copy());
        continue;
      }

      merge(existing, defaultEntry.getValue(), options);
    }

    if (options.removeUnknown()) {
      var allowedKeys = defaults.objectChildren().keySet();
      targetChildren.keySet().removeIf(key -> !allowedKeys.contains(key));
    }
  }

  private static void mergeLists(ConfigNode target, ConfigNode defaults, MergeOptions options) {
    var targetChildren = target.mutableListChildren();
    var defaultChildren = defaults.listChildren();

    switch (options.listStrategy()) {
      case PRESERVE_EXISTING -> {
      }
      case REPLACE -> {
        targetChildren.clear();
        for (ConfigNode child : defaultChildren) {
          targetChildren.add(child.copy());
        }
      }
      case APPEND_MISSING -> {
        var additions = new ArrayList<ConfigNode>();
        for (int index = targetChildren.size(); index < defaultChildren.size(); index++) {
          additions.add(defaultChildren.get(index).copy());
        }
        targetChildren.addAll(additions);
      }
    }
  }

  private static boolean shouldReplace(ConfigNode target, ConfigNode defaults, MergeValueStrategy strategy) {
    return switch (strategy) {
      case PRESERVE_EXISTING -> false;
      case REPLACE_INVALID -> target.kind() != defaults.kind();
      case REPLACE_EXISTING -> true;
    };
  }

  private static void mergeSource(ConfigNode target, ConfigNode defaults, MergeCommentStrategy strategy) {
    switch (strategy) {
      case KEEP_EXISTING -> {
      }
      case FILL_MISSING -> {
        target.setComment(fillComment(target.comment(), defaults.comment()));
        target.setDecorations(fillDecorations(target.decorations(), defaults.decorations()));
      }
      case REPLACE -> {
        target.setComment(defaults.comment());
        target.setDecorations(replacePresentationDecorations(target.decorations(), defaults.decorations()));
      }
    }
  }

  private static ConfigComment fillComment(ConfigComment current, ConfigComment defaults) {
    if (current.isEmpty()) {
      return defaults;
    }

    return ConfigComment.builder()
      .addAllLeading(selectLines(current.leading(), defaults.leading()))
      .addAllInline(selectLines(current.inline(), defaults.inline()))
      .addAllTrailing(selectLines(current.trailing(), defaults.trailing()))
      .build();
  }

  private static List<ConfigCommentLine> selectLines(List<ConfigCommentLine> current, List<ConfigCommentLine> defaults) {
    return current.isEmpty() ? defaults : current;
  }

  private static ConfigNodeDecorations fillDecorations(ConfigNodeDecorations current, ConfigNodeDecorations defaults) {
    var attributes = new LinkedHashMap<>(defaults.attributes());
    attributes.putAll(current.attributes());

    return ImmutableConfigNodeDecorations.copyOf(current)
      .withKeyComment(current.keyComment().isEmpty() ? defaults.keyComment() : fillComment(current.keyComment(), defaults.keyComment()))
      .withKeyStyle(current.keyStyle() == ConfigScalarStyle.UNSPECIFIED ? defaults.keyStyle() : current.keyStyle())
      .withScalarStyle(current.scalarStyle() == ConfigScalarStyle.UNSPECIFIED ? defaults.scalarStyle() : current.scalarStyle())
      .withCollectionStyle(current.collectionStyle() == ConfigCollectionStyle.UNSPECIFIED ? defaults.collectionStyle() : current.collectionStyle())
      .withAttributes(attributes);
  }

  private static ConfigNodeDecorations replacePresentationDecorations(
    ConfigNodeDecorations current,
    ConfigNodeDecorations defaults
  ) {
    return ImmutableConfigNodeDecorations.copyOf(current)
      .withKeyComment(defaults.keyComment())
      .withKeyStyle(defaults.keyStyle())
      .withScalarStyle(defaults.scalarStyle())
      .withCollectionStyle(defaults.collectionStyle())
      .withAttributes(defaults.attributes());
  }
}
