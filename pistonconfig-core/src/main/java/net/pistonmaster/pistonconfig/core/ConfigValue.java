package net.pistonmaster.pistonconfig.core;

import java.util.LinkedHashMap;

/// Sealed, immutable value model used by codecs and format adapters.
public sealed interface ConfigValue permits ObjectValue, ListValue, ScalarValue, NullValue {
  /// Returns the structural kind of this value.
  ///
  /// @return value kind
  ConfigValueKind kind();

  /// Converts a mutable node tree into immutable values.
  ///
  /// @param node source node
  /// @return immutable value
  static ConfigValue fromNode(ConfigNode node) {
    return switch (node.kind()) {
      case OBJECT -> {
        var children = new LinkedHashMap<String, ConfigValue>();
        node.objectChildren().forEach((key, child) -> children.put(key, fromNode(child)));
        yield ObjectValue.builder().putAllChildren(children).build();
      }
      case LIST -> ListValue.builder()
        .addAllChildren(node.listChildren().stream().map(ConfigValue::fromNode).toList())
        .build();
      case SCALAR -> ScalarValue.builder()
        .value(node.rawValue())
        .build();
      case NULL -> NullValue.INSTANCE;
    };
  }

  /// Converts an immutable value tree into mutable nodes.
  ///
  /// @param value source value
  /// @return mutable node
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
