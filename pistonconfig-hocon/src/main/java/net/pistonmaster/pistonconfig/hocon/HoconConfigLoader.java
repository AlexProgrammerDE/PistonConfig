package net.pistonmaster.pistonconfig.hocon;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import com.typesafe.config.ConfigValueType;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.pistonmaster.pistonconfig.core.ConfigCollectionStyle;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ConfigSourceLocation;

/**
 * HOCON reader and writer backed by Lightbend Config.
 */
public final class HoconConfigLoader implements ConfigLoader {
  @Override
  public ConfigDocument load(Reader reader) {
    try {
      var options = ConfigParseOptions.defaults()
        .setSyntax(ConfigSyntax.CONF)
        .setAllowMissing(false)
        .setOriginDescription("pistonconfig-hocon");

      return ConfigDocument.of(fromValue(ConfigFactory.parseReader(reader, options).root()));
    } catch (RuntimeException exception) {
      throw new ConfigException("Could not parse HOCON configuration.", exception);
    }
  }

  @Override
  public void save(ConfigDocument document, Writer writer) {
    var renderOptions = ConfigRenderOptions.defaults()
      .setComments(true)
      .setOriginComments(false)
      .setFormatted(true)
      .setJson(false);

    try {
      writer.write(toValue(document.root()).render(renderOptions));
      writer.write(System.lineSeparator());
    } catch (java.io.IOException exception) {
      throw new UncheckedIOException("Could not write HOCON configuration.", exception);
    }
  }

  private static ConfigNode fromValue(ConfigValue value) {
    ConfigNode node = switch (value.valueType()) {
      case OBJECT -> fromObject((ConfigObject) value);
      case LIST -> fromList((ConfigList) value);
      case BOOLEAN, NUMBER, STRING -> ConfigNode.scalar(value.unwrapped());
      case NULL -> ConfigNode.nullValue();
    };

    var comments = value.origin().comments();
    if (comments != null && !comments.isEmpty()) {
      node.setComment(new ConfigComment(comments, ""));
    }
    node.decorate(decorations -> decorations
      .withValueLocation(ConfigSourceLocation.of(value.origin().description(), value.origin().lineNumber(), -1))
      .withCollectionStyle(value.valueType() == ConfigValueType.OBJECT ? ConfigCollectionStyle.BLOCK : decorations.collectionStyle())
      .withAttribute(HoconMetadataKeys.ORIGIN_DESCRIPTION, value.origin().description())
      .withAttribute(HoconMetadataKeys.RENDERED, value.render(ConfigRenderOptions.concise().setComments(true).setJson(false))));
    return node;
  }

  private static ConfigNode fromObject(ConfigObject object) {
    var node = ConfigNode.object();
    for (Map.Entry<String, ConfigValue> entry : object.entrySet()) {
      node.setNode(ConfigPath.of(entry.getKey()), fromValue(entry.getValue()));
    }
    return node;
  }

  private static ConfigNode fromList(ConfigList list) {
    var node = ConfigNode.list();
    for (ConfigValue value : list) {
      node.addListNode(fromValue(value));
    }
    return node;
  }

  private static ConfigValue toValue(ConfigNode node) {
    ConfigValue value;
    if (node.isObject()) {
      var object = ConfigFactory.empty().root();
      for (Map.Entry<String, ConfigNode> entry : node.objectChildren().entrySet()) {
        object = object.withValue(entry.getKey(), toValue(entry.getValue()));
      }
      value = object;
    } else if (node.isList()) {
      var values = new ArrayList<ConfigValue>();
      for (ConfigNode child : node.listChildren()) {
        values.add(toValue(child));
      }
      value = ConfigValueFactory.fromIterable(values, "pistonconfig-hocon");
    } else {
      value = ConfigValueFactory.fromAnyRef(toPlainValue(node), "pistonconfig-hocon");
    }

    if (!node.comment().isEmpty()) {
      value = value.withOrigin(value.origin().withComments(node.comment().all().stream()
        .map(net.pistonmaster.pistonconfig.core.ConfigCommentLine::text)
        .toList()));
    }
    return value;
  }

  private static Object toPlainValue(ConfigNode node) {
    return switch (node.kind()) {
      case NULL -> null;
      case SCALAR -> node.rawValue();
      case OBJECT -> {
        var values = new LinkedHashMap<String, Object>();
        node.objectChildren().forEach((key, child) -> values.put(key, toPlainValue(child)));
        yield values;
      }
      case LIST -> node.listChildren().stream().map(HoconConfigLoader::toPlainValue).toList();
    };
  }

}
