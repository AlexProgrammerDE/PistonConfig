package net.pistonmaster.pistonconfig.staticfields;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.pistonmaster.pistonconfig.core.ConfigCodecRegistry;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.MergeOptions;

/**
 * Collection of static {@link ConfigProperty} declarations.
 */
public final class StaticConfigDefinition {
  private final List<ConfigProperty<?>> properties;

  private StaticConfigDefinition(List<ConfigProperty<?>> properties) {
    this.properties = List.copyOf(properties);
  }

  public static StaticConfigDefinition of(List<ConfigProperty<?>> properties) {
    return new StaticConfigDefinition(Objects.requireNonNull(properties, "properties"));
  }

  public static StaticConfigDefinition from(Class<?> holderType) {
    Objects.requireNonNull(holderType, "holderType");

    var properties = new ArrayList<ConfigProperty<?>>();
    for (Field field : holderType.getDeclaredFields()) {
      if (!Modifier.isStatic(field.getModifiers()) || !ConfigProperty.class.isAssignableFrom(field.getType())) {
        continue;
      }

      try {
        field.setAccessible(true);
        properties.add((ConfigProperty<?>) field.get(null));
      } catch (IllegalAccessException exception) {
        throw new ConfigException("Could not read static config property " + field.getName() + ".", exception);
      }
    }

    properties.sort(Comparator.comparing(property -> property.path().toString()));
    return new StaticConfigDefinition(properties);
  }

  public List<ConfigProperty<?>> properties() {
    return properties;
  }

  public ConfigDocument defaults(ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(codecRegistry, "codecRegistry");

    var document = ConfigDocument.empty();
    for (ConfigProperty<?> property : properties) {
      var node = codecRegistry.encode(property.defaultValue());
      node.setComment(property.comment());
      document.setNode(property.path(), node);
    }
    return document;
  }

  public ConfigDocument applyDefaults(ConfigDocument document, ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(document, "document");
    document.mergeDefaults(defaults(codecRegistry), MergeOptions.conservative());
    return document;
  }

  public <T> T get(ConfigDocument document, ConfigProperty<T> property, ConfigCodecRegistry codecRegistry) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(property, "property");
    Objects.requireNonNull(codecRegistry, "codecRegistry");

    return document.find(property.path())
      .map(node -> decode(codecRegistry, node, property))
      .orElse(property.defaultValue());
  }

  private static <T> T decode(ConfigCodecRegistry codecRegistry, ConfigNode node, ConfigProperty<T> property) {
    return codecRegistry.decode(node, property.type());
  }
}
