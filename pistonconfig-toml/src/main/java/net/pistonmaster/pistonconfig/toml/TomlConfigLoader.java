package net.pistonmaster.pistonconfig.toml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlVersion;
import com.electronwill.nightconfig.toml.TomlWriter;
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

/**
 * TOML reader and writer backed by Night Config.
 */
public final class TomlConfigLoader implements ConfigLoader {
  @Override
  public ConfigDocument load(Reader reader) {
    try {
      var parser = new TomlParser();
      parser.setTomlVersion(TomlVersion.v1_1);
      return ConfigDocument.of(fromConfig(parser.parse(reader)));
    } catch (RuntimeException exception) {
      throw new ConfigException("Could not parse TOML configuration.", exception);
    }
  }

  @Override
  public void save(ConfigDocument document, Writer writer) {
    try {
      var tomlWriter = new TomlWriter();
      tomlWriter.setLenientWithBareKeys(false);
      tomlWriter.write(toConfig(document.root()), writer);
    } catch (RuntimeException exception) {
      throw new UncheckedIOException(new java.io.IOException("Could not write TOML configuration.", exception));
    }
  }

  private static ConfigNode fromConfig(CommentedConfig config) {
    var node = ConfigNode.object();
    for (CommentedConfig.Entry entry : config.entrySet()) {
      var child = fromValue(entry.getRawValue());
      var comment = entry.getComment();
      if (comment != null && !comment.isBlank()) {
        child.setComment(new ConfigComment(comment.lines().map(String::stripLeading).toList(), ""));
      }
      child.decorate(decorations -> decorations.withCollectionStyle(ConfigCollectionStyle.TABLE));
      node.setNode(ConfigPath.of(entry.getKey()), child);
    }
    return node;
  }

  private static ConfigNode fromValue(Object value) {
    if (value instanceof CommentedConfig config) {
      return fromConfig(config);
    }
    if (value instanceof List<?> values) {
      var node = ConfigNode.list();
      values.forEach(item -> node.addListNode(fromValue(item)));
      return node;
    }
    if (value instanceof Map<?, ?> values) {
      var node = ConfigNode.object();
      values.forEach((key, child) -> node.setNode(ConfigPath.of(key.toString()), fromValue(child)));
      return node;
    }
    return ConfigNode.scalar(value);
  }

  private static CommentedConfig toConfig(ConfigNode root) {
    var config = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
    for (Map.Entry<String, ConfigNode> entry : root.objectChildren().entrySet()) {
      writeEntry(config, List.of(entry.getKey()), entry.getValue());
    }
    return config;
  }

  private static void writeEntry(CommentedConfig config, List<String> path, ConfigNode node) {
    config.set(path, toValue(node));
    if (!node.comment().isEmpty()) {
      config.setComment(path, String.join(System.lineSeparator(), node.comment().all().stream()
        .map(net.pistonmaster.pistonconfig.core.ConfigCommentLine::text)
        .toList()));
    }
  }

  private static Object toValue(ConfigNode node) {
    if (node.isObject()) {
      var config = CommentedConfig.of(LinkedHashMap::new, TomlFormat.instance());
      for (Map.Entry<String, ConfigNode> entry : node.objectChildren().entrySet()) {
        writeEntry(config, List.of(entry.getKey()), entry.getValue());
      }
      return config;
    }
    if (node.isList()) {
      var values = new ArrayList<>();
      for (ConfigNode child : node.listChildren()) {
        values.add(toValue(child));
      }
      return values;
    }
    return node.rawValue();
  }

}
