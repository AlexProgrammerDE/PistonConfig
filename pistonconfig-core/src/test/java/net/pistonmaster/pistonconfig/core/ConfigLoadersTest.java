package net.pistonmaster.pistonconfig.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ConfigLoadersTest {
  @TempDir
  private Path tempDir;

  @Test
  void savesParentDirectoriesAndLoadsDocument() {
    var path = tempDir.resolve("nested/config.txt");
    var loader = new PlainTextLoader();
    var document = ConfigDocument.empty().set("text", "hello");

    ConfigLoaders.save(path, loader, document);
    var loaded = ConfigLoaders.load(path, loader);

    assertEquals("hello", loaded.find("text").flatMap(ConfigNode::asString).orElseThrow());
  }

  @Test
  void wrapsFileSystemLoadFailures() {
    var missingPath = tempDir.resolve("missing.txt");

    assertThrows(ConfigException.class, () -> ConfigLoaders.load(missingPath, new PlainTextLoader()));
  }

  @Test
  void wrapsFileSystemSaveFailures() throws IOException {
    var fileParent = Files.createFile(tempDir.resolve("file-parent"));
    var target = fileParent.resolve("config.txt");

    assertThrows(ConfigException.class, () -> ConfigLoaders.save(target, new PlainTextLoader(), ConfigDocument.empty()));
  }

  private static final class PlainTextLoader implements ConfigLoader {
    @Override
    public ConfigDocument load(Reader reader) {
      try {
        var writer = new StringWriter();
        reader.transferTo(writer);
        return ConfigDocument.empty().set("text", writer.toString());
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }

    @Override
    public void save(ConfigDocument document, Writer writer) {
      try {
        writer.write(document.find("text").flatMap(ConfigNode::asString).orElse(""));
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }
  }
}
