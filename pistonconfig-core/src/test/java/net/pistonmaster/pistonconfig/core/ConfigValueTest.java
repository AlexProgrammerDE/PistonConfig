package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

final class ConfigValueTest {
  @Test
  void convertsNodesToImmutableValuesAndBack() {
    var node = ConfigNode.object()
      .set(ConfigPath.parse("server.host"), "localhost")
      .set(ConfigPath.parse("server.port"), 25565)
      .setNode(ConfigPath.of("features"), ConfigNode.list()
        .addListValue("chat")
        .addListNode(ConfigNode.nullValue()));

    var value = ConfigValue.fromNode(node);
    var restored = ConfigValue.toNode(value);

    assertEquals(ConfigValueKind.OBJECT, value.kind());
    assertEquals("localhost", restored.find(ConfigPath.parse("server.host")).flatMap(ConfigNode::asString).orElseThrow());
    assertEquals(25565, restored.find(ConfigPath.parse("server.port")).flatMap(ConfigNode::asInt).orElseThrow());
    assertEquals(ConfigValueKind.NULL, restored.find(ConfigPath.of("features")).orElseThrow().listChildren().get(1).kind());
  }

  @Test
  void immutableValueContainersRejectMutation() {
    var children = new LinkedHashMap<String, ConfigValue>();
    children.put("enabled", ScalarValue.<Boolean>builder().value(true).build());

    var object = ObjectValue.builder().putAllChildren(children).build();
    var list = ListValue.builder()
      .addChildren(ScalarValue.<String>builder().value("value").build())
      .build();

    children.put("added-later", ScalarValue.<String>builder().value("ignored").build());

    assertEquals(1, object.children().size());
    assertThrows(UnsupportedOperationException.class, () -> object.children().put("other", NullValue.INSTANCE));
    assertThrows(UnsupportedOperationException.class, () -> list.children().add(NullValue.INSTANCE));
  }

  @Test
  void convertsSealedValueVariantsToNodes() {
    assertEquals(ConfigValueKind.NULL, ConfigValue.toNode(NullValue.INSTANCE).kind());
    assertEquals("value", ConfigValue.toNode(ScalarValue.<String>builder().value("value").build()).asString().orElseThrow());
    assertInstanceOf(ConfigNode.class, ConfigValue.toNode(ListValue.builder().build()));
  }
}
