package net.pistonmaster.pistonconfig.core;

import java.util.LinkedHashMap;

/**
 * Sealed, immutable value model used by codecs and format adapters.
 */
public sealed interface ConfigValue permits ObjectValue, ListValue, ScalarValue, NullValue {
  ConfigValueKind kind();

  static ConfigValue fromNode(ConfigNode node) {
    return switch (node.kind()) {
      case OBJECT -> {
        var children = new LinkedHashMap<String, ConfigValue>();
        node.objectChildren().forEach((key, child) -> children.put(key, fromNode(child)));
        yield new ObjectValue(children);
      }
      case LIST -> new ListValue(node.listChildren().stream().map(ConfigValue::fromNode).toList());
      case SCALAR -> new ScalarValue<>(node.rawValue());
      case NULL -> NullValue.INSTANCE;
    };
  }

  static ConfigNode toNode(ConfigValue value) {
    return switch (value) {
      case ObjectValue objectValue -> {
        var node = ConfigNode.object();
        objectValue.children().forEach((key, child) -> node.setNode(ConfigPath.of(key), toNode(child)));
        yield node;
      }
      case ListValue listValue -> {
        var node = ConfigNode.list();
        listValue.children().forEach(child -> node.addListNode(toNode(child)));
        yield node;
      }
      case ScalarValue<?> scalarValue -> ConfigNode.scalar(scalarValue.value());
      case NullValue ignored -> ConfigNode.nullValue();
    };
  }
}
