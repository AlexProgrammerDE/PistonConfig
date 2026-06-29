package net.pistonmaster.pistonconfig.properties;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import net.pistonmaster.pistonconfig.core.ConfigCollectionStyle;
import net.pistonmaster.pistonconfig.core.ConfigComment;
import net.pistonmaster.pistonconfig.core.ConfigCommentLine;
import net.pistonmaster.pistonconfig.core.ConfigCommentMarker;
import net.pistonmaster.pistonconfig.core.ConfigCommentType;
import net.pistonmaster.pistonconfig.core.ConfigDocument;
import net.pistonmaster.pistonconfig.core.ConfigException;
import net.pistonmaster.pistonconfig.core.ConfigLoader;
import net.pistonmaster.pistonconfig.core.ConfigNode;
import net.pistonmaster.pistonconfig.core.ConfigPath;
import net.pistonmaster.pistonconfig.core.ImmutableConfigNodeDecorations;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfigurationLayout;
import org.apache.commons.configuration2.ex.ConfigurationException;

/// Properties reader and writer backed by Apache Commons Configuration.
public final class PropertiesConfigLoader implements ConfigLoader {
  /// Creates a properties loader.
  public PropertiesConfigLoader() {
  }

  /// Loads a properties document from a reader.
  ///
  /// @param reader source reader
  /// @return loaded document
  @Override
  public ConfigDocument load(Reader reader) {
    var configuration = new PropertiesConfiguration();
    configuration.setIncludesAllowed(true);

    try {
      configuration.read(reader);
    } catch (ConfigurationException exception) {
      throw new ConfigException("Could not parse properties configuration.", exception);
    } catch (IOException exception) {
      throw new ConfigException("Could not read properties configuration.", exception);
    }

    var document = ConfigDocument.empty();
    var layout = configuration.getLayout();
    for (Iterator<String> keys = configuration.getKeys(); keys.hasNext(); ) {
      var key = keys.next();
      var value = configuration.getProperty(key);
      var node = toNode(value);

      var comment = layout.getComment(key);
      if (comment != null && !comment.isBlank()) {
        node.setComment(parseComment(comment));
      }
      var attributes = new LinkedHashMap<String, String>();
      var separator = layout.getSeparator(key);
      if (separator != null) {
        attributes.put(PropertiesMetadataKeys.SEPARATOR, separator);
      }
      attributes.put(PropertiesMetadataKeys.SINGLE_LINE, Boolean.toString(layout.isSingleLine(key)));
      attributes.put(PropertiesMetadataKeys.BLANK_LINES_BEFORE, Integer.toString(layout.getBlankLinesBefore(key)));
      node.decorate(decorations -> ImmutableConfigNodeDecorations.copyOf(decorations)
        .withCollectionStyle(ConfigCollectionStyle.INLINE)
        .withAttributes(attributes));

      document.setNode(ConfigPath.parse(key), node);
    }

    if (layout.getHeaderComment() != null || layout.getFooterComment() != null) {
      document.root().setComment(ConfigComment.builder()
        .addAllLeading(layout.getHeaderComment() == null ? List.of() : parseCommentLines(layout.getHeaderComment()))
        .addAllTrailing(layout.getFooterComment() == null ? List.of() : parseCommentLines(layout.getFooterComment()))
        .build());
    }

    return document;
  }

  /// Saves a document as a properties file.
  ///
  /// @param document document to save
  /// @param writer destination writer
  @Override
  public void save(ConfigDocument document, Writer writer) {
    var configuration = new PropertiesConfiguration();
    var layout = new PropertiesConfigurationLayout();
    layout.setGlobalSeparator("=");
    configuration.setLayout(layout);
    if (!document.root().comment().leading().isEmpty()) {
      layout.setHeaderComment(String.join(System.lineSeparator(), document.root().comment().leadingText()));
    }
    if (!document.root().comment().trailing().isEmpty()) {
      layout.setFooterComment(String.join(System.lineSeparator(), document.root().comment().trailingText()));
    }

    writeNode(configuration, layout, ConfigPath.root(), document.root());

    try {
      configuration.write(writer);
    } catch (ConfigurationException exception) {
      throw new ConfigException("Could not serialize properties configuration.", exception);
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not write properties configuration.", exception);
    }
  }

  private static void writeNode(PropertiesConfiguration configuration, PropertiesConfigurationLayout layout, ConfigPath path, ConfigNode node) {
    if (node.isObject()) {
      node.objectChildren().forEach((key, child) -> writeNode(
        configuration,
        layout,
        path.isRoot() ? ConfigPath.of(key) : path.child(key),
        child
      ));
      return;
    }

    var key = path.toString();
    if (node.isList()) {
      for (ConfigNode child : node.listChildren()) {
        configuration.addProperty(key, scalarValue(child));
      }
      layout.setSingleLine(key, false);
    } else {
      configuration.setProperty(key, scalarValue(node));
      layout.setSingleLine(key, true);
    }

    if (!node.comment().isEmpty()) {
      layout.setComment(key, String.join(System.lineSeparator(), node.comment().leadingText()));
    }
    if (node.decorations().attributes().containsKey(PropertiesMetadataKeys.SEPARATOR)) {
      layout.setSeparator(key, node.decorations().attributes().get(PropertiesMetadataKeys.SEPARATOR));
    }
    if (node.decorations().attributes().containsKey(PropertiesMetadataKeys.BLANK_LINES_BEFORE)) {
      layout.setBlankLinesBefore(key, Integer.parseInt(node.decorations().attributes().get(PropertiesMetadataKeys.BLANK_LINES_BEFORE)));
    }
    if (node.decorations().attributes().containsKey(PropertiesMetadataKeys.SINGLE_LINE)) {
      layout.setSingleLine(key, Boolean.parseBoolean(node.decorations().attributes().get(PropertiesMetadataKeys.SINGLE_LINE)));
    }
  }

  private static ConfigNode toNode(Object value) {
    if (value instanceof List<?> values) {
      var node = ConfigNode.list();
      values.forEach(item -> node.addListValue(item == null ? "" : item));
      return node;
    }

    return ConfigNode.scalar(value == null ? "" : value);
  }

  private static Object scalarValue(ConfigNode node) {
    if (node.rawValue() == null) {
      return "";
    }

    return node.rawValue();
  }

  private static ConfigComment parseComment(String comment) {
    return ConfigComment.builder()
      .addAllLeading(parseCommentLines(comment))
      .build();
  }

  private static List<ConfigCommentLine> parseCommentLines(String comment) {
    return comment.lines()
      .map(PropertiesConfigLoader::parseCommentLine)
      .toList();
  }

  private static ConfigCommentLine parseCommentLine(String rawLine) {
    var line = rawLine.stripLeading();
    if (line.isEmpty()) {
      return ConfigCommentLine.builder()
        .text("")
        .type(ConfigCommentType.BLANK)
        .marker(ConfigCommentMarker.NONE)
        .build();
    }

    var marker = ConfigCommentMarker.UNKNOWN;
    if (line.startsWith("#")) {
      marker = ConfigCommentMarker.HASH;
      line = line.substring(1).stripLeading();
    } else if (line.startsWith("!")) {
      marker = ConfigCommentMarker.EXCLAMATION;
      line = line.substring(1).stripLeading();
    }

    return ConfigCommentLine.builder()
      .text(line)
      .type(ConfigCommentType.BLOCK)
      .marker(marker)
      .build();
  }
}
