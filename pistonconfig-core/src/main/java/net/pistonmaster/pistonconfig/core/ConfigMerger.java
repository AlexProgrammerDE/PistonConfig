package net.pistonmaster.pistonconfig.core;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Merges default configuration documents into current user configuration documents.
 */
public final class ConfigMerger {
  private ConfigMerger() {
  }

  public static void merge(ConfigNode target, ConfigNode defaults, MergeOptions options) {
    Objects.requireNonNull(target, "target");
    Objects.requireNonNull(defaults, "defaults");
    Objects.requireNonNull(options, "options");

    if (options.updateComments()) {
      target.setComment(defaults.comment());
    }

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
}
