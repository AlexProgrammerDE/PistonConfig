package net.pistonmaster.pistonconfig.annotations;

import java.util.Objects;

/// Entry points for typed config stores.
public final class ConfigStores {
  private ConfigStores() {
  }

  /// Starts building a typed config store.
  ///
  /// @param type config type
  /// @param <T> config type
  /// @return store builder
  public static <T> ConfigStore.Builder<T> forType(Class<T> type) {
    return ConfigStore.builder(Objects.requireNonNull(type, "type"));
  }
}
